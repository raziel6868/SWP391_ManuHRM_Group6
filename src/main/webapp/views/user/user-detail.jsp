<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Thông tin nhân viên</title>
</head>
<body>

<h2>Thông tin nhân viên</h2>
<a href="user-list">← Quay lại danh sách</a>

<hr/>

<table border="1" cellpadding="8">
    <tr>
        <th>Mã nhân viên</th>
        <td>${user.employeeCode}</td>
    </tr>
    <tr>
        <th>Họ tên</th>
        <td>${user.fullName}</td>
    </tr>
    <tr>
        <th>Username</th>
        <td>${user.username}</td>
    </tr>
    <tr>
        <th>Số điện thoại</th>
        <td>${user.phone}</td>
    </tr>
    <tr>
        <th>Ngày sinh</th>
        <td>${user.dob}</td>
    </tr>
    <tr>
        <th>Chức danh</th>
        <td>${user.jobTitle}</td>
    </tr>
    <tr>
        <th>Phòng ban</th>
        <td>${user.departmentName}</td>
    </tr>
    <tr>
        <th>Quản lý trực tiếp</th>
        <td>${empty user.managerName ? 'Không có' : user.managerName}</td>
    </tr>
    <tr>
        <th>Vai trò</th>
        <td>${user.roleDisplayName}</td>
    </tr>
    <tr>
        <th>Loại nhân viên</th>
        <td>${user.employeeType}</td>
    </tr>
    <tr>
        <th>Trạng thái</th>
        <td>${user.isActive ? 'Đang làm việc' : 'Đã nghỉ'}</td>
    </tr>
    <tr>
        <th>Ngày tạo</th>
        <td>${user.createdAt}</td>
    </tr>
    <tr>
        <th>Cập nhật lần cuối</th>
        <td>${user.updatedAt}</td>
    </tr>
</table>

<hr/>

<a href="user-update?id=${user.id}">Chỉnh sửa</a>

<%-- MỚI (req #10): nút Active/Deactive từ trang detail, redirect về lại detail --%>
<form method="post" action="user-status" style="display:inline">
    <input type="hidden" name="id" value="${user.id}" />
    <input type="hidden" name="referer" value="detail" />
    <c:choose>
        <c:when test="${user.isActive}">
            <input type="hidden" name="isActive" value="false" />
            <button type="submit"
                    onclick="return confirm('Khóa tài khoản ${user.fullName}?')">
                Khóa tài khoản
            </button>
        </c:when>
        <c:otherwise>
            <input type="hidden" name="isActive" value="true" />
            <button type="submit"
                    onclick="return confirm('Mở tài khoản ${user.fullName}?')">
                Mở tài khoản
            </button>
        </c:otherwise>
    </c:choose>
</form>

</body>
</html>
