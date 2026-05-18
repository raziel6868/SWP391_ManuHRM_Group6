<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Danh sách Vai trò (Roles)</title>
</head>
<body>

<h2>QUẢN LÝ VAI TRÒ (GIẢ LẬP)</h2>

<c:if test="${param.msg == 'success'}">
    <p>Cập nhật thông tin vai trò thành công!</p>
</c:if>

<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Mã vai trò (System Name)</th>
        <th>Tên hiển thị</th>
        <th>Mô tả</th>
        <th>Hành động</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>1</td>
        <td>ADMIN</td>
        <td>Quản trị hệ thống</td>
        <td>Toàn quyền quản lý cấu hình và nhân sự toàn công ty.</td>
        <td>
            <a href="${pageContext.request.contextPath}/role-update?id=1">Cập nhật</a>
        </td>
    </tr>
    <tr>
        <td>2</td>
        <td>HR_MANAGER</td>
        <td>Quản lý Nhân sự</td>
        <td>Quản lý thông tin nhân viên, phòng ban và phân quyền cơ bản.</td>
        <td>
            <a href="${pageContext.request.contextPath}/role-update?id=2">Cập nhật</a>
        </td>
    </tr>
    <tr>
        <td>3</td>
        <td>FACTORY_WORKER</td>
        <td>Công nhân nhà máy</td>
        <td>Vai trò mặc định dành cho đối tượng lao động trực tiếp tại xưởng sản xuất.</td>
        <td>
            <a href="${pageContext.request.contextPath}/role-update?id=3">Cập nhật</a>
        </td>
    </tr>
    </tbody>
</table>

</body>
</html>