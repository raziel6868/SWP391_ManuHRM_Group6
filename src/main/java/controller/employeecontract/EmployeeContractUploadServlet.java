package controller.employeecontract;

import dal.EmployeeContractDAO;
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

@WebServlet(name = "EmployeeContractUploadServlet", urlPatterns = {"/contract-upload"})
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class EmployeeContractUploadServlet extends HttpServlet {

	private static final String UPLOAD_DIR = "assets/uploads/contracts";
	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

	private final EmployeeContractDAO contractDAO = new EmployeeContractDAO();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		String idParam = request.getParameter("id");
		Long id = parseId(idParam);

		if (id == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		var contract = contractDAO.getById(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		Part filePart = request.getPart("file");
		if (filePart == null || filePart.getInputStream() == null || filePart.getSize() == 0) {
			session.setAttribute("errorMsg", "Vui lòng chọn file để tải lên.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		String contentType = filePart.getContentType();
		if (!"application/pdf".equals(contentType)) {
			session.setAttribute("errorMsg", "Chỉ chấp nhận file PDF.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		if (filePart.getSize() > MAX_FILE_SIZE) {
			session.setAttribute("errorMsg", "File quá lớn. Kích thước tối đa là 5MB.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		String originalFilename = getSubmittedFileName(filePart);
		if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
			session.setAttribute("errorMsg", "File phải có định dạng PDF.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
		Path uploadDir = Paths.get(uploadPath);
		if (!Files.exists(uploadDir)) {
			Files.createDirectories(uploadDir);
		}

		String storedFilename = UUID.randomUUID().toString() + ".pdf";
		Path targetPath = uploadDir.resolve(storedFilename).normalize();

		if (!targetPath.startsWith(uploadDir.normalize())) {
			session.setAttribute("errorMsg", "Đường dẫn file không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		String filePath = UPLOAD_DIR + "/" + storedFilename;

		try (InputStream input = filePart.getInputStream()) {
			Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING);
		}

		boolean success = contractDAO.updateFilePath(id, filePath);
		if (success) {
			session.setAttribute("successMsg", "Tải lên hợp đồng thành công.");
		} else {
			session.setAttribute("errorMsg", "Không thể cập nhật đường dẫn file. Vui lòng thử lại.");
		}

		response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
	}

	private String getSubmittedFileName(Part part) {
		String contentDisposition = part.getHeader("content-disposition");
		if (contentDisposition != null) {
			for (String token : contentDisposition.split(";")) {
				token = token.trim();
				if (token.startsWith("filename=")) {
					String filename = token.substring(9);
					if (filename.startsWith("\"") && filename.endsWith("\"")) {
						filename = filename.substring(1, filename.length() - 1);
					}
					return filename;
				}
			}
		}
		return null;
	}

	private Long parseId(String idParam) {
		if (idParam == null || idParam.isBlank()) {
			return null;
		}
		try {
			return Long.parseLong(idParam);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
