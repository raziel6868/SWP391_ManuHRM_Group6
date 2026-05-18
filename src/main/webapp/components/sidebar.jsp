<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="user" value="${sessionScope.authUser}" />

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
        <a class="${activeMenu == 'home' ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
           href="${homeUrl}">
            <span class="material-symbols-outlined">dashboard</span>
            <span>Tổng quan</span>
        </a>

        <c:url var="profileUrl" value="/profile" />
        <a class="${activeMenu == 'profile' ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
           href="${profileUrl}">
            <span class="material-symbols-outlined">account_circle</span>
            <span>Hồ sơ cá nhân</span>
        </a>

        <hr class="my-3 border-secondary opacity-25">

        <c:forEach var="permission" items="${permissions}">
            <c:choose>
                <c:when test="${permission.code == 'USER_VIEW'}">
                    <c:url var="permissionUrl" value="/user-list" />
                    <c:set var="permissionIcon" value="groups" />
                </c:when>
                <c:when test="${permission.code == 'USER_CREATE'}">
                    <c:url var="permissionUrl" value="/user-create" />
                    <c:set var="permissionIcon" value="person_add" />
                </c:when>
                <c:when test="${permission.code == 'USER_UPDATE'}">
                    <c:url var="permissionUrl" value="/user-list" />
                    <c:set var="permissionIcon" value="manage_accounts" />
                </c:when>
                <c:when test="${permission.code == 'USER_STATUS'}">
                    <c:url var="permissionUrl" value="/user-list" />
                    <c:set var="permissionIcon" value="lock_open" />
                </c:when>
                <c:when test="${permission.code == 'ROLE_VIEW'}">
                    <c:url var="permissionUrl" value="/role-list" />
                    <c:set var="permissionIcon" value="verified_user" />
                </c:when>
                <c:when test="${permission.code == 'ROLE_UPDATE'}">
                    <c:url var="permissionUrl" value="/role-list" />
                    <c:set var="permissionIcon" value="admin_panel_settings" />
                </c:when>
                <c:when test="${permission.code == 'ROLE_PERMISSION'}">
                    <c:url var="permissionUrl" value="/role-permission" />
                    <c:set var="permissionIcon" value="rule_settings" />
                </c:when>
                <c:otherwise>
                    <c:url var="permissionUrl" value="${permission.urlPattern}" />
                    <c:set var="permissionIcon" value="link" />
                </c:otherwise>
            </c:choose>

            <a class="sidebar-nav-item text-decoration-none" href="${permissionUrl}">
                <span class="material-symbols-outlined"><c:out value="${permissionIcon}" /></span>
                <span><c:out value="${permission.name}" /></span>
            </a>
        </c:forEach>

        <c:if test="${empty permissions}">
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
