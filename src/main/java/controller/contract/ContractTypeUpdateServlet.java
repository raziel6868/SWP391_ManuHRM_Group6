package controller.contract;

import dal.ContractTypeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ContractType;
import util.ValidationUtil;

import java.io.IOException;

@WebServlet(name = "ContractTypeUpdateServlet", urlPatterns = {"/contract-type-update"})
public class ContractTypeUpdateServlet extends HttpServlet {

	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idStr = request.getParameter("id");
		if (idStr != null && !idStr.isEmpty()) {
			try {
				Long id = Long.parseLong(idStr);
				ContractType contractType = contractTypeDAO.getById(id);
				if (contractType != null) {
					request.setAttribute("contractType", contractType);
				} else {
					request.getSession().setAttribute("errorMsg", "Không tìm thấy loại hợp đồng.");
					response.sendRedirect(request.getContextPath() + "/contract-type-list");
					return;
				}
			} catch (NumberFormatException e) {
				request.getSession().setAttribute("errorMsg", "ID không hợp lệ.");
				response.sendRedirect(request.getContextPath() + "/contract-type-list");
				return;
			}
		} else {
			response.sendRedirect(request.getContextPath() + "/contract-type-list");
			return;
		}
		request.getRequestDispatcher("/views/contract/contracttype-update.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String idStr = request.getParameter("id");
		String code = request.getParameter("code");
		String name = request.getParameter("name");
		String description = request.getParameter("description");

		if (idStr == null || idStr.isEmpty()) {
			response.sendRedirect(request.getContextPath() + "/contract-type-list");
			return;
		}

		Long id;
		try {
			id = Long.parseLong(idStr);
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMsg", "ID không hợp lệ.");
			response.sendRedirect(request.getContextPath() + "/contract-type-list");
			return;
		}

		ContractType existing = contractTypeDAO.getById(id);
		if (existing == null) {
			request.getSession().setAttribute("errorMsg", "Không tìm thấy loại hợp đồng.");
			response.sendRedirect(request.getContextPath() + "/contract-type-list");
			return;
		}

		if (ValidationUtil.isBlank(code)) {
			request.setAttribute("errorMsg", "Mã loại hợp đồng không được để trống.");
			request.setAttribute("contractType", buildFormModel(id, code, name, description, existing.getIsActive()));
			request.getRequestDispatcher("/views/contract/contracttype-update.jsp").forward(request, response);
			return;
		}
		if (ValidationUtil.isBlank(name)) {
			request.setAttribute("errorMsg", "Tên loại hợp đồng không được để trống.");
			request.setAttribute("contractType", buildFormModel(id, code, name, description, existing.getIsActive()));
			request.getRequestDispatcher("/views/contract/contracttype-update.jsp").forward(request, response);
			return;
		}

		String normalizedCode = code.trim().toUpperCase();
		if (!ValidationUtil.matchRegex(normalizedCode, "^[A-Z][A-Z0-9_]*$")) {
			request.setAttribute("errorMsg",
					"Mã phải bắt đầu bằng chữ in hoa, chỉ chứa chữ in hoa, số và dấu gạch dưới.");
			request.setAttribute("contractType", buildFormModel(id, code, name, description, existing.getIsActive()));
			request.getRequestDispatcher("/views/contract/contracttype-update.jsp").forward(request, response);
			return;
		}

		if (contractTypeDAO.existsByCodeExceptId(normalizedCode, id)) {
			request.setAttribute("errorMsg", "Mã '" + normalizedCode + "' đã tồn tại. Vui lòng chọn mã khác.");
			request.setAttribute("contractType", buildFormModel(id, code, name, description, existing.getIsActive()));
			request.getRequestDispatcher("/views/contract/contracttype-update.jsp").forward(request, response);
			return;
		}

		ContractType contractType = new ContractType();
		contractType.setId(id);
		contractType.setCode(normalizedCode);
		contractType.setName(name.trim());
		contractType.setDescription(description != null ? description.trim() : null);

		if (contractTypeDAO.update(contractType)) {
			request.getSession().setAttribute("successMsg", "Cập nhật loại hợp đồng thành công!");
			response.sendRedirect(request.getContextPath() + "/contract-type-list");
		} else {
			request.setAttribute("errorMsg", "Lỗi: Không thể cập nhật. Vui lòng thử lại.");
			request.setAttribute("contractType", buildFormModel(id, code, name, description, existing.getIsActive()));
			request.getRequestDispatcher("/views/contract/contracttype-update.jsp").forward(request, response);
		}
	}

	private ContractType buildFormModel(Long id, String code, String name, String description, Boolean isActive) {
		ContractType ct = new ContractType();
		ct.setId(id);
		ct.setCode(code != null ? code.trim() : "");
		ct.setName(name != null ? name.trim() : "");
		ct.setDescription(description);
		ct.setIsActive(isActive);
		return ct;
	}
}
