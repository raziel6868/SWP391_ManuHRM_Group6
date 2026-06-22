<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý hợp đồng lao động - ManuHRM</title>
    <link href="${ctx}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <jsp:include page="/components/alert.jsp" />

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-2">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý hợp đồng lao động</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Theo dõi hợp đồng lao động của nhân viên: tạo mới, gia hạn, tải PDF và chấm dứt.
                        </p>
                    </div>
                    <c:if test="${hasContractCreatePerm}">
                        <a href="${ctx}/contract-create"
                            class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                            Tạo hợp đồng
                        </a>
                    </c:if>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-12 col-md-3">
                        <div class="card-premium p-3 d-flex flex-column gap-1">
                            <span class="label-md text-on-surface-variant">Đang hiệu lực</span>
                            <span class="h3 fw-bold text-primary mb-0">${activeCount}</span>
                        </div>
                    </div>
                    <div class="col-12 col-md-3">
                        <div class="card-premium p-3 d-flex flex-column gap-1">
                            <span class="label-md text-on-surface-variant">Chờ gia hạn</span>
                            <span class="h3 fw-bold mb-0" style="color: #b45309;">${pendingRenewalCount}</span>
                        </div>
                    </div>
                    <div class="col-12 col-md-3">
                        <div class="card-premium p-3 d-flex flex-column gap-1">
                            <span class="label-md text-on-surface-variant">Sắp hết hạn (30 ngày)</span>
                            <span class="h3 fw-bold mb-0" style="color: #b91c1c;">${expiringSoonCount}</span>
                        </div>
                    </div>
                    <div class="col-12 col-md-3">
                        <div class="card-premium p-3 d-flex flex-column gap-1">
                            <span class="label-md text-on-surface-variant">Đã chấm dứt</span>
                            <span class="h3 fw-bold text-on-surface-variant mb-0">${terminatedCount}</span>
                        </div>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant d-flex flex-wrap gap-2 align-items-center">
                        <form action="${ctx}/contract-list" method="GET"
                            class="d-flex position-relative" style="width: 320px;">
                            <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                style="left: 12px; font-size: 1.25rem;">search</span>
                            <input type="text" name="keyword" value="${keyword}"
                                class="input-premium w-100 py-1"
                                placeholder="Tìm theo mã NV, họ tên, loại HĐ..."
                                style="padding-left: 2.5rem;" />
                        </form>

                        <form action="${ctx}/contract-list" method="GET" class="d-flex align-items-center gap-2">
                            <c:if test="${not empty keyword}">
                                <input type="hidden" name="keyword" value="${keyword}" />
                            </c:if>
                            <label class="label-sm text-on-surface-variant mb-0">Trạng thái:</label>
                            <select name="status" class="form-select form-select-sm" style="width: 160px;" onchange="this.form.submit()">
                                <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả</option>
                                <c:forEach var="s" items="${statuses}">
                                    <option value="${s.name()}" ${selectedStatus == s.name() ? 'selected' : ''}>
                                        <c:choose>
                                            <c:when test="${s.name() == 'ACTIVE'}">Đang hiệu lực</c:when>
                                            <c:when test="${s.name() == 'EXPIRED'}">Hết hạn</c:when>
                                            <c:when test="${s.name() == 'PENDING_RENEWAL'}">Chờ gia hạn</c:when>
                                            <c:when test="${s.name() == 'TERMINATED'}">Đã chấm dứt</c:when>
                                        </c:choose>
                                    </option>
                                </c:forEach>
                            </select>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Nhân viên</th>
                                    <th>Loại HĐ</th>
                                    <th>Ngày bắt đầu</th>
                                    <th>Ngày kết thúc</th>
                                    <th>Mức lương</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${contracts}">
                                    <tr>
                                        <td>
                                            <div class="fw-medium text-on-surface">${c.fullName}</div>
                                            <div class="label-sm text-on-surface-variant">
                                                ${c.employeeCode} - ${c.departmentName}
                                            </div>
                                        </td>
                                        <td>
                                            <span class="label-md">${c.contractTypeName}</span>
                                            <div class="label-sm text-on-surface-variant">${c.contractTypeCode}</div>
                                        </td>
                                        <td>${c.startDate}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty c.endDate}">
                                                    <span class="text-on-surface-variant">Không xác định</span>
                                                </c:when>
                                                <c:otherwise>
                                                    ${c.endDate}
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty c.salary}">
                                                    <span class="text-on-surface-variant">-</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="salaryFmt" value="${c.salary}" />
                                                    ${salaryFmt}
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${c.status == 'ACTIVE'}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Đang hiệu lực</span>
                                                </c:when>
                                                <c:when test="${c.status == 'EXPIRED'}">
                                                    <span class="badge" style="background-color: #e5e7eb; color: #374151;">Hết hạn</span>
                                                </c:when>
                                                <c:when test="${c.status == 'PENDING_RENEWAL'}">
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">Chờ gia hạn</span>
                                                </c:when>
                                                <c:when test="${c.status == 'TERMINATED'}">
                                                    <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Đã chấm dứt</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-flex justify-content-end gap-1">
                                                <c:if test="${hasContractDetailPerm}">
                                                    <a href="${ctx}/contract-detail?id=${c.id}"
                                                        class="btn btn-sm btn-icon text-on-surface-variant hover-primary" title="Xem chi tiết">
                                                        <span class="material-symbols-outlined" style="font-size: 1.25rem;">visibility</span>
                                                    </a>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty contracts}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-on-surface-variant">
                                            Không tìm thấy hợp đồng nào phù hợp.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
                            <div class="d-flex gap-1">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${ctx}/contract-list?page=${i}&keyword=${keyword}&status=${selectedStatus}"
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
