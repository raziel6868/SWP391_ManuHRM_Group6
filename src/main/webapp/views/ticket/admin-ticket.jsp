<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý yêu cầu đặt lại mật khẩu - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <%-- Notifications --%>
                <c:if test="${not empty successMsg}">
                    <div class="alert alert-success d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">check_circle</span>
                        ${successMsg}
                    </div>
                </c:if>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        ${errorMsg}
                    </div>
                </c:if>

                <div class="d-flex justify-content-between align-items-end mb-4">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Yêu cầu đặt lại mật khẩu</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xử lý yêu cầu khôi phục mật khẩu từ nhân viên.</p>
                    </div>
                    <div class="badge" style="background-color: var(--surface-container-high);">
                        <span class="material-symbols-outlined me-1" style="font-size: 1rem;">inbox</span>
                        ${tickets.size()} yêu cầu
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column w-100">
                    <c:choose>
                        <c:when test="${empty tickets}">
                            <div class="text-center py-5">
                                <span class="material-symbols-outlined text-on-surface-variant" style="font-size: 4rem;">inbox</span>
                                <p class="text-on-surface-variant mt-3 mb-0">Không có yêu cầu nào</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-premium mb-0 w-100">
                                    <thead>
                                        <tr>
                                            <th>Mã nhân viên</th>
                                            <th>Họ tên</th>
                                            <th>Ngày gửi</th>
                                            <th>Trạng thái</th>
                                            <th class="text-end">Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="t" items="${tickets}">
                                            <tr>
                                                <td class="fw-medium text-on-surface font-monospace">${t.employeeCode}</td>
                                                <td>${t.fullName}</td>
                                                <td class="text-on-surface-variant">
                                                    <c:choose>
                                                        <c:when test="${not empty t.createdAt}">
                                                            <jsp:useBean id="dt" class="java.util.Date"/>
                                                            <jsp:setProperty name="dt" property="time" value="${t.createdAt.time}"/>
                                                            <fmt:formatDate value="${dt}" pattern="dd/MM/yyyy HH:mm"/>
                                                        </c:when>
                                                        <c:otherwise>-</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">
                                                        <span class="material-symbols-outlined me-1" style="font-size: 0.875rem;">schedule</span>
                                                        Chờ duyệt
                                                    </span>
                                                </td>
                                                <td class="text-end">
                                                    <div class="d-flex justify-content-end gap-1">
                                                        <a href="${pageContext.request.contextPath}/admin/tickets/set-password?id=${t.id}"
                                                           class="btn btn-sm btn-primary-gradient text-white" title="Đặt mật khẩu">
                                                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">key</span>
                                                            Đặt mật khẩu
                                                        </a>
                                                        <%-- Reject button --%>
                                                        <form action="${pageContext.request.contextPath}/admin/tickets" method="POST" class="d-inline"
                                                              onsubmit="return confirm('Từ chối yêu cầu này?');">
                                                            <input type="hidden" name="action" value="reject" />
                                                            <input type="hidden" name="ticketId" value="${t.id}" />
                                                            <button type="submit" class="btn btn-sm btn-icon text-error hover-error" title="Từ chối">
                                                                <span class="material-symbols-outlined" style="font-size: 1.25rem;">close</span>
                                                            </button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
