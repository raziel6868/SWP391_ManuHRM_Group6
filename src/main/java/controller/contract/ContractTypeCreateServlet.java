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

@WebServlet(name = "ContractTypeCreateServlet", urlPatterns = {"/contract-type-create"})
public class ContractTypeCreateServlet extends HttpServlet {

	private final ContractTypeDAO contractTypeDAO = new ContractTypeDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/contract/contracttype-create.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String code = request.getParameter("code");
		String name = request.getParameter("name");
		String description = request.getParameter("description");

		if (ValidationUtil.isBlank(code)) {
			request.setAttribute("errorMsg", "Mã loại hợp đồng không được để trống.");
			forwardCreate(request, response, code, name, description);
			return;
		}
		if (ValidationUtil.isBlank(name)) {
			request.setAttribute("errorMsg", "Tên loại hợp đồng không được để trống.");
			forwardCreate(request, response, code, name, description);
			return;
		}

		String normalizedCode = code.trim().toUpperCase();
		if (!ValidationUtil.matchRegex(normalizedCode, "^[A-Z][A-Z0-9_]*$")) {
			request.setAttribute("errorMsg",
					"Mã phải bắt đầu bằng chữ in hoa, chỉ chứa chữ in hoa, số và dấu gạch dưới.");
			forwardCreate(request, response, code, name, description);
			return;
		}

		if (contractTypeDAO.existsByCode(normalizedCode)) {
			request.setAttribute("errorMsg", "Mã '" + normalizedCode + "' đã tồn tại. Vui lòng chọn mã khác.");
			forwardCreate(request, response, code, name, description);
			return;
		}

		ContractType contractType = new ContractType();
		contractType.setCode(normalizedCode);
		contractType.setName(name.trim());
		contractType.setDescription(description != null ? description.trim() : null);
		contractType.setIsActive(true);

		if (contractTypeDAO.insert(contractType)) {
			request.getSession().setAttribute("successMsg",
					"Thêm loại hợp đồng '" + contractType.getName() + "' thành công!");
			response.sendRedirect(request.getContextPath() + "/contract-type-list");
		} else {
			request.setAttribute("errorMsg", "Lỗi: Không thể thêm loại hợp đồng. Vui lòng thử lại.");
			forwardCreate(request, response, code, name, description);
		}
	}

	private void forwardCreate(HttpServletRequest request, HttpServletResponse response, String code, String name,
			String description) throws ServletException, IOException {
		request.setAttribute("code", code);
		request.setAttribute("name", name);
		request.setAttribute("description", description);
		request.getRequestDispatcher("/views/contract/contracttype-create.jsp").forward(request, response);
	}
}
