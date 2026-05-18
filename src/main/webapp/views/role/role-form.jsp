<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Cập nhật thông tin Vai trò</title>
</head>
<body>

<h2>Cập Nhật Thông Tin Vai Trò</h2>

<c:if test="${param.msg == 'failed'}">
    <p>Đã có lỗi xảy ra. Vui lòng kiểm tra lại dữ liệu!</p>
</c:if>

<form action="${pageContext.request.contextPath}/role-update" method="POST">
    <input type="hidden" name="id" value="${role.id}" />

    <table>
        <tr>
            <td>
                <label>Mã hệ thống (Không cho phép sửa):</label>
            </td>
            <td>
                <input type="text" value="${role.name}" disabled />
            </td>
        </tr>
        <tr>
            <td>
                <label>Tên hiển thị (*):</label>
            </td>
            <td>
                <input type="text" name="displayName" value="${role.displayName}" required />
            </td>
        </tr>
        <tr>
            <td>
                <label>Mô tả chi tiết:</label>
            </td>
            <td>
                <textarea name="description" rows="4">${role.description}</textarea>
            </td>
        </tr>
        <tr>
            <td></td>
            <td>
                <a href="${pageContext.request.contextPath}/role-list.jsp">Hủy bỏ</a>
                <button type="submit">Lưu thay đổi</button>
            </td>
        </tr>
    </table>
</form>

</body>
</html>