package controller.contract;

import dal.ContractDAO;
import dto.ContractDetail;
import model.Contract;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Uploads the contract PDF for an existing contract. Validates type, size,
 * saves under assets/uploads/contracts/{userId}/{contractId}_{uuid}.pdf and
 * writes the relative path to the contracts.file_path column.
 *
 * Only ACTIVE contracts accept a fresh PDF (terminated/expired contracts are
 * historical and should keep the file path that was recorded at the time of the
 * event).
 */
@WebServlet(name = "ContractUploadServlet", urlPatterns = {"/contract-upload"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5L * 1024 * 1024, maxRequestSize = 6L * 1024 * 1024)
public class ContractUploadServlet extends HttpServlet {

	private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
	static final String UPLOAD_ROOT = "/assets/uploads/contracts";
	private static final String[] ALLOWED_MIME = {"application/pdf"};

	private final ContractDAO contractDAO = new ContractDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		String idStr = request.getParameter("id");
		if (ValidationUtil.isBlank(idStr)) {
			session.setAttribute("errorMsg", "Thiếu mã hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		Long id;
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Mã hợp đồng không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		ContractDetail contract = contractDAO.getDetail(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		request.setAttribute("contract", contract);
		request.getRequestDispatcher("/views/contract/contract-upload.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		Long id = parseLong(request.getParameter("id"));
		if (id == null) {
			session.setAttribute("errorMsg", "Thiếu mã hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		ContractDetail contract = contractDAO.getDetail(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}
		if (!Contract.Status.ACTIVE.name().equals(contract.getStatus())) {
			session.setAttribute("errorMsg", "Chỉ có thể tải lên PDF cho hợp đồng đang hiệu lực.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		Part filePart;
		try {
			filePart = request.getPart("contractFile");
		} catch (IllegalStateException e) {
			// Triggered by maxFileSize / maxRequestSize breach
			returnWithError(request, response, contract, "File vượt quá 5MB. Vui lòng chọn file nhỏ hơn.");
			return;
		}
		if (filePart == null || filePart.getSize() == 0) {
			returnWithError(request, response, contract, "Vui lòng chọn file PDF.");
			return;
		}
		if (filePart.getSize() > MAX_FILE_SIZE) {
			returnWithError(request, response, contract, "File vượt quá 5MB. Vui lòng chọn file nhỏ hơn.");
			return;
		}
		String contentType = filePart.getContentType();
		if (contentType == null || !isAllowedMime(contentType)) {
			returnWithError(request, response, contract, "Chỉ chấp nhận file PDF (application/pdf).");
			return;
		}

		String originalName = getFileName(filePart);
		if (!originalName.toLowerCase().endsWith(".pdf")) {
			returnWithError(request, response, contract, "Tên file phải có đuôi .pdf");
			return;
		}

		String relativePath = saveFile(contract, filePart);
		if (relativePath == null) {
			returnWithError(request, response, contract, "Lỗi khi lưu file. Vui lòng thử lại.");
			return;
		}

		if (contractDAO.updateFilePath(contract.getId(), relativePath)) {
			session.setAttribute("successMsg", "Tải lên PDF thành công!");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
		} else {
			// Roll back: remove the file we just wrote so DB and disk stay in sync
			deleteIfExists(absolutePath(relativePath));
			session.setAttribute("errorMsg", "Lỗi: Không thể cập nhật hợp đồng. Vui lòng thử lại.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
		}
	}

	private void returnWithError(HttpServletRequest request, HttpServletResponse response, ContractDetail contract,
			String message) throws ServletException, IOException {
		request.setAttribute("errorMsg", message);
		request.setAttribute("contract", contract);
		request.getRequestDispatcher("/views/contract/contract-upload.jsp").forward(request, response);
	}

	private boolean isAllowedMime(String contentType) {
		for (String allowed : ALLOWED_MIME) {
			if (allowed.equalsIgnoreCase(contentType)) {
				return true;
			}
		}
		return false;
	}

	private String getFileName(Part part) {
		String name = part.getSubmittedFileName();
		if (name == null) {
			return "";
		}
		int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		return slash >= 0 ? name.substring(slash + 1) : name;
	}

	private String saveFile(ContractDetail contract, Part filePart) throws IOException {
		String userDir = UPLOAD_ROOT + File.separator + contract.getUserId();
		Path userDirAbs = absolutePath(userDir);
		Files.createDirectories(userDirAbs);

		String storedName = contract.getId() + "_" + UUID.randomUUID() + ".pdf";
		Path target = userDirAbs.resolve(storedName);

		try (InputStream in = filePart.getInputStream()) {
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		}
		// Store web-relative path with forward slashes
		return userDir.replace(File.separatorChar, '/') + "/" + storedName;
	}

	private Path absolutePath(String webRelativePath) {
		String realRoot = getServletContext().getRealPath("");
		if (realRoot == null) {
			throw new IllegalStateException("ServletContext.getRealPath returned null");
		}
		return Paths.get(realRoot, webRelativePath);
	}

	private void deleteIfExists(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			System.err.println("ContractUploadServlet.deleteIfExists() ERROR: " + e.getMessage());
		}
	}

	private Long parseLong(String s) {
		if (ValidationUtil.isBlank(s)) {
			return null;
		}
		try {
			return Long.parseLong(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/* ==================== Static helpers for reuse ==================== */

	/**
	 * Saves an uploaded PDF and returns the web-relative path, or null on failure.
	 * The file is stored under
	 * {@code /assets/uploads/contracts/{userId}/{contractId}_{uuid}.pdf}.
	 *
	 * @param ctx
	 *            the ServletContext (used to resolve the real path)
	 * @param userId
	 *            the employee user ID, used to create the per-user subdirectory
	 * @param contractId
	 *            the newly-created contract ID, used in the file name
	 * @param filePart
	 *            the uploaded multipart file part
	 * @return the relative path (e.g. "/assets/uploads/contracts/5/123_abc.pdf"),
	 *         or null
	 */
	public static String saveUploadedFile(jakarta.servlet.ServletContext ctx, Long userId, Long contractId,
			Part filePart) throws IOException {
		if (userId == null || contractId == null || filePart == null) {
			return null;
		}
		String userDir = UPLOAD_ROOT + "/" + userId;
		Path userDirAbs = absolutePathStatic(ctx, userDir);
		Files.createDirectories(userDirAbs);

		String storedName = contractId + "_" + UUID.randomUUID() + ".pdf";
		Path target = userDirAbs.resolve(storedName);

		try (InputStream in = filePart.getInputStream()) {
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		}
		return userDir + "/" + storedName;
	}

	private static Path absolutePathStatic(jakarta.servlet.ServletContext ctx, String webRelativePath) {
		String realRoot = ctx.getRealPath("");
		if (realRoot == null) {
			throw new IllegalStateException("ServletContext.getRealPath returned null");
		}
		return Paths.get(realRoot, webRelativePath);
	}

	/**
	 * Deletes a file at the given web-relative path, silently ignoring errors.
	 */
	public static void deleteFileIfExists(jakarta.servlet.ServletContext ctx, String webRelativePath) {
		if (webRelativePath == null) {
			return;
		}
		try {
			Files.deleteIfExists(absolutePathStatic(ctx, webRelativePath));
		} catch (IOException e) {
			System.err.println("ContractUploadServlet.deleteFileIfExists() ERROR: " + e.getMessage());
		}
	}
}
