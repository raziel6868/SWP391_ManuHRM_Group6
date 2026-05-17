<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>User List</title>
</head>
<body>

<h2>Danh sách nhân viên (${totalRecords} người)</h2>

<form method="get" action="user-list">
    <input type="text" name="keyword" value="${keyword}" placeholder="Tìm mã NV, tên, username" />

    <select name="departmentId">
        <option value="">-- Phòng ban --</option>
        <c:forEach var="dept" items="${departments}">
            <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>
                ${dept.name}
            </option>
        </c:forEach>
    </select>

    <select name="roleId">
        <option value="">-- Vai trò --</option>
        <c:forEach var="role" items="${roles}">
            <option value="${role.id}" ${selectedRoleId == role.id ? 'selected' : ''}>
                ${role.displayName}
            </option>
        </c:forEach>
    </select>

    <select name="isActive">
        <option value="">-- Trạng thái --</option>
        <option value="1" ${selectedStatus == '1' ? 'selected' : ''}>Đang làm</option>
        <option value="0" ${selectedStatus == '0' ? 'selected' : ''}>Đã nghỉ</option>
    </select>

    <button type="submit">Tìm kiếm</button>
    <a href="user-list">Reset</a>
</form>

<hr/>

<c:choose>
    <c:when test="${empty users}">
        <p>Không tìm thấy nhân viên nào.</p>
    </c:when>
    <c:otherwise>
        <table border="1" cellpadding="6">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Mã NV</th>
                    <th>Username</th>
                    <th>Họ tên</th>
                    <th>SĐT</th>
                    <th>Chức danh</th>
                    <th>Phòng ban</th>
                    <th>Vai trò</th>
                    <th>Loại</th>
                    <th>Trạng thái</th>
                    <th>Hành động</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="u" items="${users}">
                    <tr>
                        <td>${u.id}</td>
                        <td>${u.employeeCode}</td>
                        <td>${u.username}</td>
                        <td>${u.fullName}</td>
                        <td>${u.phone}</td>
                        <td>${u.jobTitle}</td>
                        <td>${u.departmentName}</td>
                        <td>${u.roleDisplayName}</td>
                        <td>${u.employeeType}</td>
                        <td>${u.isActive ? 'Active' : 'Inactive'}</td>
                        <td>
                            <a href="user-detail?id=${u.id}">Xem</a> |
                            <a href="user-update?id=${u.id}">Sửa</a> |

                            <%-- MỚI (req #10): nút Active/Deactive --%>
                            <form method="post" action="user-status" style="display:inline">
                                <input type="hidden" name="id" value="${u.id}" />
                                <input type="hidden" name="referer" value="list" />
                                <c:choose>
                                    <c:when test="${u.isActive}">
                                        <input type="hidden" name="isActive" value="false" />
                                        <button type="submit"
                                                onclick="return confirm('Khóa tài khoản ${u.fullName}?')">
                                            Khóa
                                        </button>
                                    </c:when>
                                    <c:otherwise>
                                        <input type="hidden" name="isActive" value="true" />
                                        <button type="submit"
                                                onclick="return confirm('Mở tài khoản ${u.fullName}?')">
                                            Mở
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

<hr/>

<p>Trang ${currentPage} / ${totalPages}</p>
<c:if test="${currentPage > 1}">
    <a href="user-list?page=${currentPage - 1}&keyword=${keyword}&departmentId=${selectedDepartmentId}&roleId=${selectedRoleId}&isActive=${selectedStatus}">« Trước</a>
</c:if>
<c:forEach begin="1" end="${totalPages}" var="i">
    <c:choose>
        <c:when test="${i == currentPage}">[${i}]</c:when>
        <c:otherwise>
            <a href="user-list?page=${i}&keyword=${keyword}&departmentId=${selectedDepartmentId}&roleId=${selectedRoleId}&isActive=${selectedStatus}">${i}</a>
        </c:otherwise>
    </c:choose>
</c:forEach>
<c:if test="${currentPage < totalPages}">
    <a href="user-list?page=${currentPage + 1}&keyword=${keyword}&departmentId=${selectedDepartmentId}&roleId=${selectedRoleId}&isActive=${selectedStatus}">Tiếp »</a>
</c:if>

<hr/>


</body>
</html>
