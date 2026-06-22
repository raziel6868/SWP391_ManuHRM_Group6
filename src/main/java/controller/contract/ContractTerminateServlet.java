package controller.contract;

import dal.ContractDAO;
import dto.ContractDetail;
import model.Contract;
import model.User;
import util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Soft-terminates a contract: sets status to TERMINATED and records the actor
 * (current auth user), the effective termination date, and the reason.
 * Idempotent - calling twice on the same contract is a no-op.
 *
 * Allowed source states: ACTIVE, PENDING_RENEWAL. TERMINATED and EXPIRED
 * contracts cannot be re-terminated.
 */
@WebServlet(name = "ContractTerminateServlet", urlPatterns = {"/contract-terminate"})
public class ContractTerminateServlet extends HttpServlet {

	private static final int REASON_MIN_LENGTH = 5;
	private static final int REASON_MAX_LENGTH = 500;
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final ContractDAO contractDAO = new ContractDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		Long id = parseIdFromQuery(request, response, session);
		if (id == null) {
			return;
		}

		ContractDetail contract = contractDAO.getDetail(id);
		if (contract == null) {
			session.setAttribute("errorMsg", "Không tìm thấy hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-list");
			return;
		}
		if (isNotTerminatable(contract.getStatus())) {
			session.setAttribute("errorMsg",
					"Hợp đồng ở trạng thái '" + contract.getStatus() + "' không thể chấm dứt.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		request.setAttribute("contract", contract);
		request.getRequestDispatcher("/views/contract/contract-terminate.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		Long id = parseId(request.getParameter("id"));
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
		if (isNotTerminatable(contract.getStatus())) {
			session.setAttribute("errorMsg",
					"Hợp đồng ở trạng thái '" + contract.getStatus() + "' không thể chấm dứt.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
			return;
		}

		String effectiveDateStr = request.getParameter("effectiveDate");
		Date effectiveDate = parseAndValidateEffectiveDate(effectiveDateStr, contract.getStartDate(), request, response,
				contract);
		if (effectiveDate == null) {
			return;
		}

		String reason = request.getParameter("reason");
		if (ValidationUtil.isBlank(reason)) {
			returnWithError(request, response, contract, "Vui lòng nhập lý do chấm dứt.", effectiveDateStr, null);
			return;
		}
		String trimmed = reason.trim();
		if (trimmed.length() < REASON_MIN_LENGTH) {
			returnWithError(request, response, contract,
					"Lý do quá ngắn. Vui lòng mô tả tối thiểu " + REASON_MIN_LENGTH + " ký tự.", effectiveDateStr,
					null);
			return;
		}
		if (trimmed.length() > REASON_MAX_LENGTH) {
			returnWithError(request, response, contract, "Lý do quá dài. Tối đa " + REASON_MAX_LENGTH + " ký tự.",
					effectiveDateStr, null);
			return;
		}

		User actor = (User) session.getAttribute("authUser");
		Long terminatedBy = actor != null ? actor.getId() : null;

		if (contractDAO.terminate(id, effectiveDate, terminatedBy, trimmed)) {
			session.setAttribute("successMsg", "Đã chấm dứt hợp đồng thành công.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
		} else {
			session.setAttribute("errorMsg",
					"Không thể chấm dứt hợp đồng. Có thể hợp đồng đã thay đổi trạng thái bởi người khác.");
			response.sendRedirect(request.getContextPath() + "/contract-detail?id=" + id);
		}
	}

	private boolean isNotTerminatable(String status) {
		return Contract.Status.TERMINATED.name().equals(status) || Contract.Status.EXPIRED.name().equals(status);
	}

	private Date parseAndValidateEffectiveDate(String dateStr, Date contractStartDate, HttpServletRequest request,
			HttpServletResponse response, ContractDetail contract) throws ServletException, IOException {
		if (ValidationUtil.isBlank(dateStr)) {
			returnWithError(request, response, contract, "Vui lòng chọn ngày chấm dứt hợp đồng.", null, null);
			return null;
		}
		LocalDate effective;
		try {
			effective = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
		} catch (DateTimeParseException e) {
			returnWithError(request, response, contract, "Ngày chấm dứt không hợp lệ. Định dạng: yyyy-MM-dd.", dateStr,
					null);
			return null;
		}
		LocalDate startDate = contractStartDate.toLocalDate();
		if (effective.isBefore(startDate)) {
			returnWithError(request, response, contract,
					"Ngày chấm dứt không được trước ngày bắt đầu hợp đồng (" + startDate + ").", dateStr, null);
			return null;
		}
		return Date.valueOf(effective);
	}

	private void returnWithError(HttpServletRequest request, HttpServletResponse response, ContractDetail contract,
			String message, String effectiveDate, String reason) throws ServletException, IOException {
		request.setAttribute("errorMsg", message);
		request.setAttribute("contract", contract);
		request.setAttribute("effectiveDate", effectiveDate);
		request.setAttribute("reason", reason);
		request.getRequestDispatcher("/views/contract/contract-terminate.jsp").forward(request, response);
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
}
