package controller.contract;

import dal.ContractDAO;
import dal.ContractTypeDAO;
import dto.ContractDetail;
import model.Contract;
import model.ContractType;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

/**
 * Updates the fields of an existing ACTIVE contract: contract type, start date,
 * end date, salary, and optionally replaces the contract PDF. Only contracts in
 * ACTIVE status can be edited. Expired / terminated contracts are immutable —
 * use renew or terminate instead.
 */
@WebServlet(name = "ContractUpdateServlet", urlPatterns = {"/contract-update"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5L * 1024 * 1024, maxRequestSize = 6L * 1024 * 1024)
public class ContractUpdateServlet extends HttpServlet {

	private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
	private static final String UPLOAD_ROOT = "/assets/uploads/contracts";

	private final ContractDAO contractDAO = new ContractDAO();
	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		Long id = parseId(request.getParameter("id"), session);
		if (id == null) {
			return;
		}

		ContractDetail contract = contractDAO.getDetail(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}
		if (!Contract.Status.ACTIVE.name().equals(contract.getStatus())) {
			session.setAttribute("errorMsg",
					"Chỉ có thể chỉnh sửa hợp đồng đang hiệu lực. Hợp đồng hiện tại có trạng thái: "
							+ contract.getStatus() + ".");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		List<ContractType> contractTypes = contractTypeDAO.getActiveContractTypes();
		request.setAttribute("contract", contract);
		request.setAttribute("contractTypes", contractTypes);
		request.getRequestDispatcher("/views/contract/contract-update.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		Long id = parseId(request.getParameter("id"), session);
		if (id == null) {
			return;
		}

		ContractDetail contract = contractDAO.getDetail(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}
		if (!Contract.Status.ACTIVE.name().equals(contract.getStatus())) {
			session.setAttribute("errorMsg",
					"Chỉ có thể chỉnh sửa hợp đồng đang hiệu lực. Hợp đồng hiện tại có trạng thái: "
							+ contract.getStatus() + ".");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		Long contractTypeId = parseLong(request.getParameter("contractTypeId"));
		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		String salaryStr = request.getParameter("salary");

		Date startDate = parseDate(startDateStr);
		Date endDate = parseDate(endDateStr);
		BigDecimal salary = parseBigDecimal(salaryStr);

		if (contractTypeId == null) {
			returnWithError(request, response, contract, "Vui lòng chọn loại hợp đồng.", contractTypeId, startDateStr,
					endDateStr, salaryStr);
			return;
		}
		if (startDate == null) {
			returnWithError(request, response, contract, "Ngày bắt đầu không hợp lệ.", contractTypeId, startDateStr,
					endDateStr, salaryStr);
			return;
		}
		if (endDate != null && endDate.before(startDate)) {
			returnWithError(request, response, contract, "Ngày kết thúc phải sau ngày bắt đầu.", contractTypeId,
					startDateStr, endDateStr, salaryStr);
			return;
		}
		if (salary != null && salary.signum() < 0) {
			returnWithError(request, response, contract, "Mức lương không được âm.", contractTypeId, startDateStr,
					endDateStr, salaryStr);
			return;
		}

		Part filePart = null;
		try {
			filePart = request.getPart("contractFile");
		} catch (IllegalStateException e) {
			returnWithError(request, response, contract, "File vượt quá 5MB. Vui lòng chọn file nhỏ hơn.",
					contractTypeId, startDateStr, endDateStr, salaryStr);
			return;
		}

		String fileError = validateFile(filePart);
		if (fileError != null) {
			returnWithError(request, response, contract, fileError, contractTypeId, startDateStr, endDateStr,
					salaryStr);
			return;
		}

		Contract updated = new Contract();
		updated.setId(id);
		updated.setContractTypeId(contractTypeId);
		updated.setStartDate(startDate);
		updated.setEndDate(endDate);
		updated.setSalary(salary);

		if (!contractDAO.update(updated)) {
			returnWithError(request, response, contract, "Lỗi: Không thể cập nhật hợp đồng. Vui lòng thử lại.",
					contractTypeId, startDateStr, endDateStr, salaryStr);
			return;
		}

		// Replace PDF if a new one was uploaded
		boolean hasFile = filePart != null && filePart.getSize() > 0;
		if (hasFile) {
			String newRelativePath = saveFile(contract.getUserId(), id, filePart);
			if (newRelativePath != null) {
				String oldPath = contract.getFilePath();
				if (!contractDAO.updateFilePath(id, newRelativePath)) {
					deleteIfExists(absolutePath(newRelativePath));
				} else if (oldPath != null) {
					deleteIfExists(absolutePath(oldPath));
				}
			}
		}

		session.setAttribute("successMsg", "Cập nhật hợp đồng thành công!");
		response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
	}

	private String validateFile(Part filePart) {
		if (filePart == null || filePart.getSize() == 0) {
			return null; // optional
		}
		if (filePart.getSize() > MAX_FILE_SIZE) {
			return "File vượt quá 5MB. Vui lòng chọn file nhỏ hơn.";
		}
		String contentType = filePart.getContentType();
		if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
			return "Chỉ chấp nhận file PDF (application/pdf).";
		}
		String name = getFileName(filePart);
		if (!name.toLowerCase().endsWith(".pdf")) {
			return "Tên file phải có đuôi .pdf.";
		}
		return null;
	}

	private String saveFile(Long userId, Long contractId, Part filePart) {
		try {
			String userDir = UPLOAD_ROOT + "/" + userId;
			Path userDirAbs = absolutePath(userDir);
			Files.createDirectories(userDirAbs);

			String storedName = contractId + "_" + UUID.randomUUID() + ".pdf";
			Path target = userDirAbs.resolve(storedName);

			try (InputStream in = filePart.getInputStream()) {
				Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
			}
			return userDir + "/" + storedName;
		} catch (IOException e) {
			System.err.println("ContractUpdateServlet.saveFile() ERROR: " + e.getMessage());
			return null;
		}
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
			System.err.println("ContractUpdateServlet.deleteIfExists() ERROR: " + e.getMessage());
		}
	}

	private void returnWithError(HttpServletRequest request, HttpServletResponse response, ContractDetail contract,
			String message, Long contractTypeId, String startDateStr, String endDateStr, String salaryStr)
			throws ServletException, IOException {
		request.setAttribute("errorMsg", message);
		request.setAttribute("contract", contract);
		request.setAttribute("contractTypes", contractTypeDAO.getActiveContractTypes());
		request.setAttribute("selectedContractTypeId", contractTypeId);
		request.setAttribute("startDate", startDateStr);
		request.setAttribute("endDate", endDateStr);
		request.setAttribute("salary", salaryStr);
		request.getRequestDispatcher("/views/contract/contract-update.jsp").forward(request, response);
	}

	private Long parseId(String s, HttpSession session) throws IOException {
		if (ValidationUtil.isBlank(s)) {
			session.setAttribute("errorMsg", "Thiếu mã hợp đồng.");
			return null;
		}
		try {
			return Long.parseLong(s.trim());
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Mã hợp đồng không hợp lệ.");
			return null;
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

	private Date parseDate(String s) {
		if (ValidationUtil.isBlank(s)) {
			return null;
		}
		try {
			return Date.valueOf(s.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private BigDecimal parseBigDecimal(String s) {
		if (ValidationUtil.isBlank(s)) {
			return null;
		}
		try {
			return new BigDecimal(s.trim().replace(",", ""));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String getFileName(Part part) {
		String name = part.getSubmittedFileName();
		if (name == null) {
			return "";
		}
		int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
		return slash >= 0 ? name.substring(slash + 1) : name;
	}
}
