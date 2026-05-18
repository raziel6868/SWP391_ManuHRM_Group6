<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="user" value="${sessionScope.authUser}" />

<aside class="sidebar">
    <div>
        <div class="brand">
            <span class="brand-mark">M</span>
            <span>ManuHRM</span>
        </div>

        <nav class="nav-list" aria-label="Điều hướng chính">
            <c:url var="homeUrl" value="/home" />
            <a class="${activeMenu == 'home' ? 'nav-link active' : 'nav-link'}" href="${homeUrl}">
                Tổng quan
            </a>

            <a class="${activeMenu == 'profile' ? 'nav-link active' : 'nav-link'}" href="#">
                Hồ sơ cá nhân
            </a>

            <c:forEach var="permission" items="${permissions}">
                <c:url var="permissionUrl" value="${permission.urlPattern}" />
                <a class="nav-link" href="${permissionUrl}">
                    <c:out value="${permission.name}" />
                </a>
            </c:forEach>

            <c:if test="${empty permissions}">
                <div class="nav-empty">Role hiện tại chưa có quyền quản trị.</div>
            </c:if>
        </nav>
    </div>

    <div class="sidebar-footer">
        <c:choose>
            <c:when test="${not empty user.departmentName}">
                <c:out value="${user.departmentName}" />
            </c:when>
            <c:otherwise>Chưa cập nhật phòng ban</c:otherwise>
        </c:choose>
        <br>
        <c:out value="${user.employeeCode}" /> · <c:out value="${user.employeeType}" />
    </div>
</aside>
