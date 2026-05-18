<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>${not empty user.id ? 'Cập nhật' : 'Thêm'} Người Dùng</title>
</head>
<body>

<div class="container">
    <h2>${not empty user.id ? 'Cập nhật thông tin nhân viên' : 'Thêm nhân viên mới'}</h2>

    <c:if test="${not empty errorMsg}">
        <div class="error-box"> ${errorMsg}</div>
    </c:if>

    <form action="${not empty user.id ? 'user-edit' : 'user-create'}" method="POST">

        <c:if test="${not empty user.id}">
            <input type="hidden" name="id" value="${user.id}" />
        </c:if>

        <table class="form-table">

            <tr>
                <td class="label-cell">Mã Nhân Viên <span class="required">*</span></td>
                <td class="input-cell">
                    <input type="text" name="employeeCode"
                           value="${not empty user ? user.employeeCode : param.employeeCode}"
                           required maxlength="20"
                    ${not empty user.id ? 'readonly style="background-color: #e9ecef;"' : ''} />
                </td>
            </tr>

            <tr>
                <td class="label-cell">Tên đăng nhập <span class="required">*</span></td>
                <td class="input-cell">
                    <input type="text" name="username"
                           value="${not empty user ? user.username : param.username}"
                           required maxlength="50" pattern="^[a-zA-Z0-9_]+$"
                    ${not empty user.id ? 'readonly style="background-color: #e9ecef;"' : ''} />
                </td>
            </tr>

            <tr>
                <td class="label-cell">Mật khẩu ${not empty user.id ? '' : '<span class="required">*</span>'}</td>
                <td class="input-cell">
                    <input type="password" name="password" ${not empty user.id ? '' : 'required'} minlength="6" />
                    <c:if test="${not empty user.id}">
                        <span class="hint" style="color: blue;">(Để trống nếu không muốn thay đổi mật khẩu)</span>
                    </c:if>
                </td>
            </tr>

            <tr>
                <td class="label-cell">Họ và Tên <span class="required">*</span></td>
                <td class="input-cell">
                    <input type="text" name="fullName" value="${not empty user ? user.fullName : param.fullName}" required />
                </td>
            </tr>

            <tr>
                <td class="label-cell">Phòng ban</td>
                <td class="input-cell">
                    <select name="departmentId">
                        <option value="">-- Không thuộc phòng ban --</option>
                        <c:forEach var="dept" items="${departments}">
                            <option value="${dept.id}"
                                ${(not empty user ? user.departmentId : param.departmentId) == dept.id ? 'selected' : ''}>
                                    ${dept.name}
                            </option>
                        </c:forEach>
                    </select>
                </td>
            </tr>

            <tr>
                <td></td>
                <td class="btn-row">
                    <button type="submit" class="btn-submit">${not empty user.id ? 'Cập nhật' : 'Lưu thông tin'}</button>
                    <a href="user-list" class="btn-cancel">Hủy bỏ</a>
                </td>
            </tr>

        </table>
    </form>
</div>
</body>
</html>