<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="user" value="${sessionScope.authUser}" />
<c:set var="pendingCount" value="${sessionScope.pendingTicketCount != null ? sessionScope.pendingTicketCount : 0}" />

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
        <%-- Notification bell: chỉ hiện khi có TICKET_VIEW permission (dynamic RBAC từ DB) --%>
        <c:if test="${not empty sessionScope.permissions}">
            <c:forEach var="p" items="${sessionScope.permissions}">
                <c:if test="${p.code == 'TICKET_VIEW'}">
                    <a href="${pageContext.request.contextPath}/admin/tickets" class="notification-btn" title="Yêu cầu đặt lại mật khẩu">
                        <span class="material-symbols-outlined">notifications</span>
                        <c:if test="${pendingCount > 0}">
                            <span class="badge-notification">${pendingCount > 9 ? '9+' : pendingCount}</span>
                        </c:if>
                    </a>
                </c:if>
            </c:forEach>
        </c:if>
    </div>
</header>
