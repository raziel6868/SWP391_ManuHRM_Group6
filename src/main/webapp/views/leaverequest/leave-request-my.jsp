<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Đơn nghỉ của tôi - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <jsp:include page="/components/alert.jsp" />

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Đơn nghỉ của tôi</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Theo dõi các đơn nghỉ đã gửi và trạng thái phê duyệt.
                        </p>
                    </div>
                    <c:if test="${canCreate}">
                        <a href="${pageContext.request.contextPath}/leave-request-create"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                            Tạo đơn nghỉ
                        </a>
                    </c:if>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã đơn</th>
                                    <th>Loại nghỉ</th>
                                    <th>Từ ngày</th>
                                    <th>Đến ngày</th>
                                    <th class="text-end">Số ngày</th>
                                    <th>Trạng thái</th>
                                    <th>Người duyệt cấp 1</th>
                                    <th>Người duyệt cuối</th>
                                    <c:if test="${canCancel}">
                                        <th class="text-end">Thao tác</th>
                                    </c:if>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="leaveRequest" items="${leaveRequests}">
                                    <tr>
                                        <td class="fw-medium text-on-surface">#${leaveRequest.id}</td>
                                        <td>
                                            <span class="badge" style="background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant);">
                                                <c:out value="${leaveRequest.leaveTypeCode}" />
                                            </span>
                                            <span class="ms-2"><c:out value="${leaveRequest.leaveTypeName}" /></span>
                                        </td>
                                        <td>${leaveRequest.startDate}</td>
                                        <td>${leaveRequest.endDate}</td>
                                        <td class="text-end fw-medium">
                                            <fmt:formatNumber value="${leaveRequest.days}" minFractionDigits="0" maxFractionDigits="2" />
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${leaveRequest.status == 'PENDING'}">
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">Chờ duyệt</span>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'APPROVED_LEVEL_1'}">
                                                    <span class="badge" style="background-color: #dbeafe; color: #1d4ed8;">Đã duyệt cấp 1</span>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'APPROVED'}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Đã duyệt</span>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'REJECTED'}">
                                                    <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Từ chối</span>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'CANCELLED'}">
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Đã hủy</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">
                                                        <c:out value="${leaveRequest.status}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><c:out value="${empty leaveRequest.level1ApproverName ? '-' : leaveRequest.level1ApproverName}" /></td>
                                        <td><c:out value="${empty leaveRequest.approverName ? '-' : leaveRequest.approverName}" /></td>
                                        <c:if test="${canCancel}">
                                            <td class="text-end">
                                                <c:if test="${leaveRequest.status == 'PENDING' or leaveRequest.status == 'APPROVED_LEVEL_1'}">
                                                    <form action="${pageContext.request.contextPath}/leave-request-cancel" method="POST" class="d-inline">
                                                        <input type="hidden" name="id" value="${leaveRequest.id}" />
                                                        <button type="submit"
                                                                class="btn btn-sm btn-icon text-danger"
                                                                title="Hủy đơn nghỉ"
                                                                onclick="return confirm('Bạn có chắc muốn hủy đơn nghỉ này?');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">cancel</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty leaveRequests}">
                                    <tr>
                                        <td colspan="${canCancel ? 9 : 8}" class="text-center py-4 text-on-surface-variant">
                                            Bạn chưa có đơn nghỉ nào.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-between flex-wrap gap-3">
                            <div class="body-sm text-on-surface-variant">
                                Tổng số đơn: ${totalRecords}
                            </div>
                            <div class="d-flex gap-1 flex-wrap">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${pageContext.request.contextPath}/leave-request-my?page=${i}"
                                       class="btn btn-sm ${i == currentPage ? 'fw-bold' : 'btn-light border text-on-surface-variant'}"
                                       style="${i == currentPage ? 'background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant); border: 1px solid var(--primary);' : 'background-color: var(--surface-container-lowest); border-color: var(--outline-variant) !important;'}">
                                        ${i}
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
