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
 * Renews a contract: the old contract is moved to EXPIRED and a brand new
 * ACTIVE contract is created with renewal_of_id pointing to the old one.
 *
 * The renewal works on contracts in status ACTIVE / EXPIRED / PENDING_RENEWAL.
 * Pre-populates the new contract fields from the previous one so HR only has to
 * adjust the dates and salary. Supports optional PDF upload in the same step.
 */
@WebServlet(name = "ContractRenewServlet", urlPatterns = {"/contract-renew"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5L * 1024 * 1024, maxRequestSize = 6L * 1024 * 1024)
public class ContractRenewServlet extends HttpServlet {

	private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
	private static final String UPLOAD_ROOT = "/assets/uploads/contracts";

	private final ContractDAO contractDAO = new ContractDAO();
	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		Long id = parseIdFromQuery(request, response, session);
		if (id == null) {
			return;
		}

		ContractDetail previous = contractDAO.getDetail(id);
		if (previous == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}
		if (!isRenewable(previous.getStatus())) {
			session.setAttribute("errorMsg", "Chỉ có thể gia hạn hợp đồng đang hiệu lực, hết hạn, hoặc chờ gia hạn.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		List<ContractType> contractTypes = contractTypeDAO.getActiveContractTypes();
		request.setAttribute("previous", previous);
		request.setAttribute("contractTypes", contractTypes);
		// Pre-fill startDate with the day after the previous end_date
		if (previous.getEndDate() != null) {
			request.setAttribute("defaultStartDate", new Date(previous.getEndDate().getTime() + 24L * 60 * 60 * 1000));
		}
		request.getRequestDispatcher("/views/contract/contract-renew.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		Long previousId = parseId(request.getParameter("id"));
		if (previousId == null) {
			session.setAttribute("errorMsg", "Thiếu mã hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}

		ContractDetail previous = contractDAO.getDetail(previousId);
		if (previous == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}
		if (!isRenewable(previous.getStatus())) {
			session.setAttribute("errorMsg", "Chỉ có thể gia hạn hợp đồng đang hiệu lực, hết hạn, hoặc chờ gia hạn.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + previousId);
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
			returnWithError(request, response, previous, "Vui lòng chọn loại hợp đồng.", contractTypeId, startDateStr,
					endDateStr, salaryStr, null, null);
			return;
		}
		if (startDate == null) {
			returnWithError(request, response, previous, "Ngày bắt đầu không hợp lệ.", contractTypeId, startDateStr,
					endDateStr, salaryStr, null, null);
			return;
		}
		if (endDate != null && endDate.before(startDate)) {
			returnWithError(request, response, previous, "Ngày kết thúc phải sau ngày bắt đầu.", contractTypeId,
					startDateStr, endDateStr, salaryStr, null, null);
			return;
		}
		if (salary != null && salary.signum() < 0) {
			returnWithError(request, response, previous, "Mức lương không được âm.", contractTypeId, startDateStr,
					endDateStr, salaryStr, null, null);
			return;
		}

		Part filePart = null;
		try {
			filePart = request.getPart("contractFile");
		} catch (IllegalStateException e) {
			returnWithError(request, response, previous, "File vượt quá 5MB. Vui lòng chọn file nhỏ hơn.",
					contractTypeId, startDateStr, endDateStr, salaryStr, null, null);
			return;
		}

		String fileError = validateFile(filePart);
		if (fileError != null) {
			returnWithError(request, response, previous, fileError, contractTypeId, startDateStr, endDateStr, salaryStr,
					null, null);
			return;
		}

		// Build the new contract: same employee, linked to previous via renewal_of_id
		Contract renewed = new Contract();
		renewed.setUserId(previous.getUserId());
		renewed.setContractTypeId(contractTypeId);
		renewed.setStartDate(startDate);
		renewed.setEndDate(endDate);
		renewed.setSalary(salary);
		renewed.setStatus(Contract.Status.ACTIVE);
		renewed.setRenewalOfId(previous.getId());

		Long newId = contractDAO.insertReturningId(renewed);
		if (newId == null) {
			session.setAttribute("errorMsg", "Lỗi: Không thể tạo hợp đồng gia hạn. Vui lòng thử lại.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + previousId);
			return;
		}

		// Mark the previous contract as expired
		boolean markedExpired = contractDAO.updateStatus(previous.getId(), Contract.Status.EXPIRED);
		if (!markedExpired) {
			session.setAttribute("errorMsg", "Lỗi: Không thể cập nhật trạng thái hợp đồng cũ. Vui lòng thử lại.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + previousId);
			return;
		}

		// Attach PDF if one was supplied
		boolean hasFile = filePart != null && filePart.getSize() > 0;
		if (hasFile) {
			String relativePath = saveFile(previous.getUserId(), newId, filePart);
			if (relativePath != null) {
				if (!contractDAO.updateFilePath(newId, relativePath)) {
					deleteIfExists(absolutePath(relativePath));
				}
			}
		}

		session.setAttribute("successMsg", "Gia hạn hợp đồng thành công!");
		response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + newId);
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
			System.err.println("ContractRenewServlet.saveFile() ERROR: " + e.getMessage());
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
			System.err.println("ContractRenewServlet.deleteIfExists() ERROR: " + e.getMessage());
		}
	}

	private void returnWithError(HttpServletRequest request, HttpServletResponse response, ContractDetail previous,
			String message, Long contractTypeId, String startDateStr, String endDateStr, String salaryStr,
			String attrName, Object attrValue) throws ServletException, IOException {
		request.setAttribute("errorMsg", message);
		request.setAttribute("previous", previous);
		request.setAttribute("contractTypes", contractTypeDAO.getActiveContractTypes());
		request.setAttribute("selectedContractTypeId", contractTypeId);
		request.setAttribute("startDate", startDateStr);
		request.setAttribute("endDate", endDateStr);
		request.setAttribute("salary", salaryStr);
		if (attrValue != null) {
			request.setAttribute(attrName, attrValue);
		}
		request.getRequestDispatcher("/views/contract/contract-renew.jsp").forward(request, response);
	}

	private boolean isRenewable(String status) {
		return Contract.Status.ACTIVE.name().equals(status) || Contract.Status.EXPIRED.name().equals(status)
				|| Contract.Status.PENDING_RENEWAL.name().equals(status);
	}

	private Long parseIdFromQuery(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws IOException {
		String idStr = request.getParameter("id");
		if (ValidationUtil.isBlank(idStr)) {
			session.setAttribute("errorMsg", "Thiếu mã hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return null;
		}
		try {
			return Long.parseLong(idStr.trim());
		} catch (NumberFormatException e) {
			session.setAttribute("errorMsg", "Mã hợp đồng không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return null;
		}
	}

	private Long parseId(String s) {
		if (ValidationUtil.isBlank(s)) {
			return null;
		}
		try {
			return Long.parseLong(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Long parseLong(String s) {
		return parseId(s);
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
