<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="user" value="${sessionScope.authUser}" />

<header class="top-header">
    <div class="d-flex align-items-center gap-3 flex-grow-1">
        <button id="sidebarToggle" class="btn-icon d-lg-none" type="button" title="Mở menu">
            <span class="material-symbols-outlined">menu</span>
        </button>

        <div>
            <p class="label-sm text-on-surface-variant mb-0">Xin chào</p>
            <h2 class="h5 mb-0 fw-bold text-on-surface"><c:out value="${user.fullName}" /></h2>
        </div>
    </div>

    <div class="d-flex align-items-center gap-2">
        <button class="btn btn-link text-on-surface-variant p-2" type="button" title="Thông báo">
            <span class="material-symbols-outlined">notifications</span>
        </button>

        <div class="vr mx-2 bg-outline-variant"></div>

        <div class="d-none d-md-flex align-items-center gap-2">
            <div class="rounded-circle d-flex align-items-center justify-content-center text-white"
                 style="width: 36px; height: 36px; background: var(--primary-gradient);">
                <c:out value="${fn:substring(user.fullName, 0, 1)}" />
            </div>
            <div>
                <p class="label-sm fw-bold mb-0 text-on-surface"><c:out value="${user.username}" /></p>
                <p class="label-sm text-muted mb-0" style="font-size: 11px;">
                    <c:out value="${user.jobTitle}" />
                </p>
            </div>
        </div>
    </div>
</header>
