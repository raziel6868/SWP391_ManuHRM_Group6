<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="user" value="${sessionScope.authUser}" />
<c:set var="currentPath" value="${pageContext.request.servletPath}" />
<c:set var="hasAdminPermission" value="${false}" />
<c:forEach var="permission" items="${sessionScope.permissions}">
    <c:if test="${permission.code == 'USER_VIEW' or permission.code == 'ROLE_VIEW' or permission.code == 'CONTRACT_TYPE_VIEW' or permission.code == 'LEAVE_TYPE_VIEW' or permission.code == 'SHIFT_VIEW' or permission.code == 'JOB_TITLE_VIEW' or permission.code == 'DEPARTMENT_VIEW'}">
        <c:set var="hasAdminPermission" value="${true}" />
    </c:if>
</c:forEach>

<aside class="sidebar">
    <div class="sidebar-header">
        <div class="d-flex align-items-center justify-content-center shadow-sm"
             style="width: 40px; height: 40px; border-radius: 8px; background: var(--primary-gradient);">
            <span class="material-symbols-outlined text-white" style="font-variation-settings: 'FILL' 1;">factory</span>
        </div>
        <div>
            <h1 class="h3 mb-0 text-primary fw-bolder" style="font-size: 20px;">ManuHRM</h1>
            <p class="label-sm text-muted mb-0 text-uppercase" style="font-size: 10px; letter-spacing: 0.05em;">
                Manufacturing Ops
            </p>
        </div>
    </div>

    <nav class="sidebar-menu">
        <c:url var="homeUrl" value="/home" />
        <a class="${currentPath == '/home' ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
           href="${homeUrl}">
            <span class="material-symbols-outlined">dashboard</span>
            <span>Bảng điều khiển</span>
        </a>

        <c:forEach var="permission" items="${sessionScope.permissions}">
            <c:choose>
                <c:when test="${permission.code == 'USER_VIEW'}">
                    <c:url var="menuUrl" value="${permission.urlPattern}" />
                    <a class="${currentPath == permission.urlPattern ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
                       href="${menuUrl}">
                        <span class="material-symbols-outlined">groups</span>
                        <span>Quản lý Nhân sự</span>
                    </a>
                </c:when>
                <c:when test="${permission.code == 'ROLE_VIEW'}">
                    <c:url var="menuUrl" value="${permission.urlPattern}" />
                    <a class="${currentPath == permission.urlPattern ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
                       href="${menuUrl}">
                        <span class="material-symbols-outlined">verified_user</span>
                        <span>Quản lý Vai trò</span>
                    </a>
                </c:when>
                <c:when test="${permission.code == 'CONTRACT_TYPE_VIEW'}">
                    <c:url var="menuUrl" value="${permission.urlPattern}" />
                    <a class="${currentPath == permission.urlPattern ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
                       href="${menuUrl}">
                        <span class="material-symbols-outlined">description</span>
                        <span>Quản lý Loại hợp đồng</span>
                    </a>
                </c:when>
                <c:when test="${permission.code == 'LEAVE_TYPE_VIEW'}">
                    <c:url var="menuUrl" value="${permission.urlPattern}" />
                    <a class="${currentPath == permission.urlPattern ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
                       href="${menuUrl}">
                        <span class="material-symbols-outlined">event_busy</span>
                        <span>Quản lý Loại nghỉ</span>
                    </a>
                </c:when>
                <c:when test="${permission.code == 'SHIFT_VIEW'}">
                    <c:url var="menuUrl" value="${permission.urlPattern}" />
                    <a class="${currentPath == permission.urlPattern ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
                       href="${menuUrl}">
                        <span class="material-symbols-outlined">schedule</span>
                        <span>Quản lý Ca làm việc</span>
                    </a>
                </c:when>
                <c:when test="${permission.code == 'JOB_TITLE_VIEW'}">
                    <c:url var="menuUrl" value="${permission.urlPattern}" />
                    <a class="${currentPath == permission.urlPattern ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
                       href="${menuUrl}">
                        <span class="material-symbols-outlined">badge</span>
                        <span>Quản lý Chức danh</span>
                    </a>
                </c:when>
                <c:when test="${permission.code == 'DEPARTMENT_VIEW'}">
                    <c:url var="menuUrl" value="${permission.urlPattern}" />
                    <a class="${currentPath == permission.urlPattern ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
                       href="${menuUrl}">
                        <span class="material-symbols-outlined">account_tree</span>
                        <span>Quản lý Phòng ban</span>
                    </a>
                </c:when>
                <c:when test="${permission.code == 'HOLIDAY_VIEW'}">
                    <c:url var="menuUrl" value="${permission.urlPattern}" />
                    <a class="${currentPath == permission.urlPattern ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
                       href="${menuUrl}">
                        <span class="material-symbols-outlined">celebration</span>
                        <span>Quản lý Ngày lễ</span>
                    </a>
                </c:when>
            </c:choose>
        </c:forEach>

        <hr class="my-3 border-secondary opacity-25">

        <c:url var="profileUrl" value="/profile" />
        <a class="${currentPath == '/profile' ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
           href="${profileUrl}">
            <span class="material-symbols-outlined">account_circle</span>
            <span>Hồ sơ của tôi</span>
        </a>

        <hr class="my-3 border-secondary opacity-25">

        <c:forEach var="permission" items="${sessionScope.permissions}">
            <c:choose>
                <c:when test="${permission.code == 'SHIFT_ASSIGNMENT_VIEW'}">
                    <c:url var="shiftAssignUrl" value="${permission.urlPattern}" />
                    <a class="${currentPath == permission.urlPattern ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
                       href="${shiftAssignUrl}">
                        <span class="material-symbols-outlined">assignment</span>
                        <span>Phân công ca</span>
                    </a>
                </c:when>
            </c:choose>
        </c:forEach>

        <c:url var="myShiftUrl" value="/my-shift" />
        <a class="${currentPath == '/my-shift' ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
           href="${myShiftUrl}">
            <span class="material-symbols-outlined">calendar_month</span>
            <span>Ca làm việc của tôi</span>
        </a>

        <c:url var="shiftCalendarUrl" value="/shift-calendar" />
        <a class="${currentPath == '/shift-calendar' ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
           href="${shiftCalendarUrl}">
            <span class="material-symbols-outlined">calendar_view_month</span>
            <span>Lịch phân ca</span>
        </a>

        <c:url var="shiftConflictUrl" value="/shift-conflict" />
        <a class="${currentPath == '/shift-conflict' ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
           href="${shiftConflictUrl}">
            <span class="material-symbols-outlined">assignment_turned_in</span>
            <span>Kiểm tra xung đột ca</span>
        </a>

        <c:if test="${not hasAdminPermission}">
            <div class="text-on-surface-variant body-sm px-3 py-2">
                Role hiện tại chưa có quyền quản trị.
            </div>
        </c:if>
    </nav>

    <div class="sidebar-footer">
        <form method="post" action="${pageContext.request.contextPath}/logout" class="mb-3">
            <button type="submit" class="sidebar-nav-item text-danger text-decoration-none border-0 bg-transparent w-100">
                <span class="material-symbols-outlined">logout</span>
                <span>Đăng xuất</span>
            </button>
        </form>

        <div class="d-flex align-items-center gap-3 px-2">
            <div class="rounded-circle d-flex align-items-center justify-content-center text-white fw-bold"
                 style="width: 40px; height: 40px; background: var(--primary-gradient); border: 2px solid var(--primary-fixed-dim);">
                <c:out value="${fn:substring(user.fullName, 0, 1)}" />
            </div>
            <div>
                <p class="label-sm fw-bold mb-0 text-on-surface"><c:out value="${user.fullName}" /></p>
                <p class="label-sm text-muted mb-0" style="font-size: 11px;">
                    <c:out value="${user.roleDisplayName}" />
                </p>
            </div>
        </div>
    </div>
</aside>
