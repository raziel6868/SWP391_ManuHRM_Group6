package controller.contract;

import dal.ContractDAO;
import dal.ContractTypeDAO;
import dal.UserDAO;
import model.Contract;
import model.ContractType;
import model.User;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * Form for creating a new employee contract. Validates required fields,
 * enforces business rules (user active, no overlapping ACTIVE contract,
 * start_date <= end_date when both provided), and persists with status ACTIVE
 * by default. Supports optional PDF upload in the same step.
 */
@WebServlet(name = "ContractCreateServlet", urlPatterns = {"/contract-create"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5L * 1024 * 1024, maxRequestSize = 6L * 1024 * 1024)
public class ContractCreateServlet extends HttpServlet {

	private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
	private static final String UPLOAD_ROOT = "/assets/uploads/contracts";

	private final ContractDAO contractDAO = new ContractDAO();
	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();
	private final UserDAO userDAO = new UserDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<User> users = userDAO.getActiveUsersForDropdown();
		List<ContractType> contractTypes = contractTypeDAO.getActiveContractTypes();

		request.setAttribute("users", users);
		request.setAttribute("contractTypes", contractTypes);
		request.getRequestDispatcher("/views/contract/contract-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		Long userId = parseLong(request.getParameter("userId"));
		Long contractTypeId = parseLong(request.getParameter("contractTypeId"));
		String startDateStr = request.getParameter("startDate");
		String endDateStr = request.getParameter("endDate");
		String salaryStr = request.getParameter("salary");

		Date startDate = parseDate(startDateStr);
		Date endDate = parseDate(endDateStr);
		BigDecimal salary = parseBigDecimal(salaryStr);

		if (userId == null) {
			returnWithError(request, response, "Vui lòng chọn nhân viên.", userId, contractTypeId, startDateStr,
					endDateStr, salaryStr, null, null);
			return;
		}
		if (contractTypeId == null) {
			returnWithError(request, response, "Vui lòng chọn loại hợp đồng.", userId, contractTypeId, startDateStr,
					endDateStr, salaryStr, null, null);
			return;
		}
		if (startDate == null) {
			returnWithError(request, response, "Ngày bắt đầu không hợp lệ.", userId, contractTypeId, startDateStr,
					endDateStr, salaryStr, null, null);
			return;
		}
		if (endDate != null && endDate.before(startDate)) {
			returnWithError(request, response, "Ngày kết thúc phải sau ngày bắt đầu.", userId, contractTypeId,
					startDateStr, endDateStr, salaryStr, null, null);
			return;
		}
		if (salary != null && salary.signum() < 0) {
			returnWithError(request, response, "Mức lương không được âm.", userId, contractTypeId, startDateStr,
					endDateStr, salaryStr, null, null);
			return;
		}

		Contract existing = contractDAO.getActiveByUser(userId);
		if (existing != null) {
			returnWithError(request, response,
					"Nhân viên này đã có hợp đồng đang hiệu lực. Hãy chấm dứt hoặc gia hạn thay vì tạo mới.", userId,
					contractTypeId, startDateStr, endDateStr, salaryStr, null, null);
			return;
		}

		Part filePart = null;
		try {
			filePart = request.getPart("contractFile");
		} catch (IllegalStateException e) {
			returnWithError(request, response, "File vượt quá 5MB. Vui lòng chọn file nhỏ hơn.", userId, contractTypeId,
					startDateStr, endDateStr, salaryStr, null, null);
			return;
		}

		String fileError = validateFile(filePart);
		if (fileError != null) {
			returnWithError(request, response, fileError, userId, contractTypeId, startDateStr, endDateStr, salaryStr,
					null, null);
			return;
		}

		Contract c = new Contract();
		c.setUserId(userId);
		c.setContractTypeId(contractTypeId);
		c.setStartDate(startDate);
		c.setEndDate(endDate);
		c.setSalary(salary);
		c.setStatus(Contract.Status.ACTIVE);

		Long newId = contractDAO.insertReturningId(c);
		if (newId == null) {
			returnWithError(request, response, "Lỗi: Không thể tạo hợp đồng. Vui lòng thử lại.", userId, contractTypeId,
					startDateStr, endDateStr, salaryStr, null, null);
			return;
		}

		// Attach PDF if one was supplied
		boolean hasFile = filePart != null && filePart.getSize() > 0;
		if (hasFile) {
			String relativePath = saveFile(userId, newId, filePart);
			if (relativePath != null) {
				if (!contractDAO.updateFilePath(newId, relativePath)) {
					deleteIfExists(absolutePath(relativePath));
				}
			}
		}

		request.getSession().setAttribute("successMsg", "Tạo hợp đồng mới thành công!");
		response.sendRedirect(request.getContextPath() + "/contract-list");
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
			System.err.println("ContractCreateServlet.saveFile() ERROR: " + e.getMessage());
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
			System.err.println("ContractCreateServlet.deleteIfExists() ERROR: " + e.getMessage());
		}
	}

	private void returnWithError(HttpServletRequest request, HttpServletResponse response, String message, Long userId,
			Long contractTypeId, String startDateStr, String endDateStr, String salaryStr, String attrName,
			Object attrValue) throws ServletException, IOException {
		request.setAttribute("errorMsg", message);
		request.setAttribute("selectedUserId", userId);
		request.setAttribute("selectedContractTypeId", contractTypeId);
		request.setAttribute("startDate", startDateStr);
		request.setAttribute("endDate", endDateStr);
		request.setAttribute("salary", salaryStr);
		request.setAttribute("users", userDAO.getActiveUsersForDropdown());
		request.setAttribute("contractTypes", contractTypeDAO.getActiveContractTypes());
		request.getRequestDispatcher("/views/contract/contract-create.jsp").forward(request, response);
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
