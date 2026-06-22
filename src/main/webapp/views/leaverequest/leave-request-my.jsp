<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Yêu cầu Nghỉ phép của tôi - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
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

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Yêu cầu Nghỉ phép của tôi</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem và quản lý yêu cầu nghỉ phép của bạn.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/leave-request-create"
                       class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                        Tạo yêu cầu mới
                    </a>
                </div>

                <c:if test="${not empty balances}">
                    <div class="card-premium p-3 mb-4">
                        <h3 class="h5 text-on-surface fw-bold mb-3">Số ngày nghỉ phép năm ${currentYear}</h3>
                        <div class="row g-3">
                            <c:forEach var="balance" items="${balances}">
                                <div class="col-md-4 col-lg-3">
                                    <div class="p-3 rounded-3 border" style="background-color: var(--surface-container-lowest); border-color: var(--outline-variant);">
                                        <p class="body-sm text-on-surface-variant mb-1">${balance.leaveTypeName}</p>
                                        <div class="d-flex align-items-center gap-2">
                                            <span class="h4 mb-0 fw-bold text-on-surface">${balance.remainingDays}</span>
                                            <span class="body-sm text-on-surface-variant">/ ${balance.totalDays} ngày</span>
                                        </div>
                                        <div class="mt-2 rounded" style="height: 4px; background-color: var(--outline-variant);">
                                            <div style="height: 100%; width: ${balance.totalDays > 0 ? (balance.usedDays.divide(balance.totalDays, 2, java.math.RoundingMode.HALF_UP).doubleValue() * 100) : 0}%; background-color: var(--primary); border-radius: 2px;"></div>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Loại nghỉ</th>
                                    <th>Từ ngày</th>
                                    <th>Đến ngày</th>
                                    <th>Số ngày</th>
                                    <th>Lý do</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="req" items="${requests}">
                                    <tr>
                                        <td>${req.leaveTypeName}</td>
                                        <td>${req.startDate}</td>
                                        <td>${req.endDate}</td>
                                        <td>${req.days.toBigInteger()}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty req.reason}">
                                                    <span title="${req.reason}">${req.reason.length() > 30 ? req.reason.substring(0, 30).concat('...') : req.reason}</span>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.status == 'PENDING'}">
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">Chờ duyệt</span>
                                                </c:when>
                                                <c:when test="${req.status == 'APPROVED_LEVEL_1'}">
                                                    <span class="badge" style="background-color: #dbeafe; color: #1e40af;">Đã duyệt Cấp 1</span>
                                                </c:when>
                                                <c:when test="${req.status == 'APPROVED'}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Đã duyệt</span>
                                                </c:when>
                                                <c:when test="${req.status == 'REJECTED'}">
                                                    <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Từ chối</span>
                                                </c:when>
                                                <c:when test="${req.status == 'CANCELLED'}">
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Đã hủy</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <c:if test="${req.status == 'PENDING' || req.status == 'APPROVED_LEVEL_1'}">
                                                <form action="${pageContext.request.contextPath}/leave-request-cancel" method="POST" class="d-inline m-0">
                                                    <input type="hidden" name="id" value="${req.id}" />
                                                    <button type="submit" class="btn btn-sm btn-icon text-danger hover-danger"
                                                            title="Hủy yêu cầu"
                                                            onclick="return confirm('Bạn có chắc muốn hủy yêu cầu này?');">
                                                        <span class="material-symbols-outlined" style="font-size: 1.25rem;">cancel</span>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty requests}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-on-surface-variant">
                                            Bạn chưa có yêu cầu nghỉ phép nào.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
