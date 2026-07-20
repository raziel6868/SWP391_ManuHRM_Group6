<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo nghỉ phép | ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
<div class="layout-wrapper">
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <jsp:include page="/components/header.jsp" />

        <main class="page-container">
            <div class="d-flex justify-content-between align-items-center gap-3 mb-4">
                <h2 class="h3 text-on-surface fw-bold mb-0">Báo cáo nghỉ phép</h2>
                <button onclick="window.print()" class="btn btn-outline-primary d-flex align-items-center gap-2">
                    <span class="material-symbols-outlined">print</span> In báo cáo
                </button>
            </div>

            <c:if test="${not empty errorMsg}">
                <div class="alert alert-warning d-flex align-items-center gap-2" role="alert">
                    <span class="material-symbols-outlined">warning</span>
                    <span>${errorMsg}</span>
                </div>
            </c:if>

            <div class="card-premium shadow-sm mb-4">
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/report-leave" class="row g-3">
                        <div class="col-md-4">
                            <label for="year" class="form-label text-on-surface fw-medium">Năm</label>
                            <select id="year" name="year" class="form-select input-premium">
                                <c:forEach var="y" items="${yearOptions}">
                                    <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label for="departmentId" class="form-label text-on-surface fw-medium">Phòng ban</label>
                            <select id="departmentId" name="departmentId" class="form-select input-premium">
                                <option value="">Tất cả</option>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.id}" ${dept.id == selectedDepartmentId ? 'selected' : ''}>${dept.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary-gradient w-100 d-flex align-items-center justify-content-center gap-2">
                                <span class="material-symbols-outlined">search</span> Xem báo cáo
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-sm-6 col-xl">
                    <article class="card-premium h-100 p-4">
                        <p class="label-sm text-on-surface-variant mb-1">Tổng đơn nghỉ</p>
                        <h3 class="h2 text-on-surface mb-0">${summaryTotalRequests}</h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl">
                    <article class="card-premium h-100 p-4">
                        <p class="label-sm text-on-surface-variant mb-1">Đã duyệt</p>
                        <h3 class="h2 text-on-surface mb-0">${summaryApprovedRequests}</h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl">
                    <article class="card-premium h-100 p-4">
                        <p class="label-sm text-on-surface-variant mb-1">Đang chờ</p>
                        <h3 class="h2 text-on-surface mb-0">${summaryPendingRequests}</h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl">
                    <article class="card-premium h-100 p-4">
                        <p class="label-sm text-on-surface-variant mb-1">Đã từ chối</p>
                        <h3 class="h2 text-on-surface mb-0">${summaryRejectedRequests}</h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl">
                    <article class="card-premium h-100 p-4">
                        <p class="label-sm text-on-surface-variant mb-1">Ngày nghỉ đã duyệt</p>
                        <h3 class="h2 text-on-surface mb-0">
                            <fmt:formatNumber value="${summaryApprovedDays}" pattern="#,##0.##" />
                        </h3>
                    </article>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty rows}">
                    <div class="card-premium shadow-sm">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2 mb-0">Không có dữ liệu nghỉ phép.</p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <section class="card-premium p-4 mb-4">
                        <div class="d-flex align-items-center justify-content-between gap-3 mb-4">
                            <h3 class="h5 fw-bold mb-0">Phân bổ trạng thái đơn nghỉ</h3>
                            <span class="badge text-bg-light">${selectedYear}</span>
                        </div>
                        <div class="progress mb-3" role="progressbar" aria-label="Phân bổ trạng thái đơn nghỉ"
                             aria-valuemin="0" aria-valuemax="100" style="height: 28px;">
                            <div class="progress-bar" style="width: ${summaryApprovedPercentage}%; background-color: var(--primary);">
                                <fmt:formatNumber value="${summaryApprovedPercentage}" pattern="#,##0.##" />%
                            </div>
                            <div class="progress-bar" style="width: ${summaryPendingPercentage}%; background-color: var(--secondary);">
                                <fmt:formatNumber value="${summaryPendingPercentage}" pattern="#,##0.##" />%
                            </div>
                            <div class="progress-bar" style="width: ${summaryRejectedPercentage}%; background-color: var(--error);">
                                <fmt:formatNumber value="${summaryRejectedPercentage}" pattern="#,##0.##" />%
                            </div>
                            <c:if test="${hasCancelledRequests}">
                                <div class="progress-bar bg-outline-variant text-on-surface" style="width: ${summaryCancelledPercentage}%;">
                                    <fmt:formatNumber value="${summaryCancelledPercentage}" pattern="#,##0.##" />%
                                </div>
                            </c:if>
                        </div>
                        <div class="row g-2">
                            <div class="col-sm-6 col-lg-3">
                                <span class="body-sm"><span class="d-inline-block rounded-circle me-2" style="width: 10px; height: 10px; background-color: var(--primary);"></span>Đã duyệt: ${summaryApprovedRequests}</span>
                            </div>
                            <div class="col-sm-6 col-lg-3">
                                <span class="body-sm"><span class="d-inline-block rounded-circle me-2" style="width: 10px; height: 10px; background-color: var(--secondary);"></span>Đang chờ: ${summaryPendingRequests}</span>
                            </div>
                            <div class="col-sm-6 col-lg-3">
                                <span class="body-sm"><span class="d-inline-block rounded-circle me-2" style="width: 10px; height: 10px; background-color: var(--error);"></span>Đã từ chối: ${summaryRejectedRequests}</span>
                            </div>
                            <c:if test="${hasCancelledRequests}">
                                <div class="col-sm-6 col-lg-3">
                                    <span class="body-sm"><span class="d-inline-block rounded-circle me-2 bg-outline-variant" style="width: 10px; height: 10px;"></span>Đã hủy: ${summaryCancelledRequests}</span>
                                </div>
                            </c:if>
                        </div>
                    </section>

                    <section class="card-premium p-4 mb-4">
                        <div class="d-flex align-items-center justify-content-between gap-3 mb-4">
                            <h3 class="h5 fw-bold mb-0">Ngày nghỉ đã duyệt theo phòng ban</h3>
                            <span class="body-sm text-on-surface-variant">Chỉ tính đơn APPROVED</span>
                        </div>
                        <div class="d-flex flex-column gap-3">
                            <c:forEach var="row" items="${rows}">
                                <div>
                                    <div class="d-flex justify-content-between gap-3 mb-1">
                                        <span class="body-sm fw-semibold text-on-surface">${row.departmentName}</span>
                                        <span class="body-sm text-on-surface-variant">
                                            <fmt:formatNumber value="${row.approvedDays}" pattern="#,##0.##" /> ngày
                                        </span>
                                    </div>
                                    <div class="progress" role="progressbar" aria-label="Ngày nghỉ đã duyệt ${row.departmentName}"
                                         aria-valuemin="0" aria-valuemax="100" aria-valuenow="${row.approvedDaysBarWidth}"
                                         style="height: 12px; background-color: var(--surface-container);">
                                        <div class="progress-bar" style="width: ${row.approvedDaysBarWidth}%; background: var(--primary-gradient);"></div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </section>

                    <div class="card-premium shadow-sm">
                        <div class="table-responsive">
                            <table class="table table-premium mb-0">
                                <thead>
                                    <tr>
                                        <th>Phòng ban</th>
                                        <th>Tổng yêu cầu</th>
                                        <th>Đã duyệt</th>
                                        <th>Đã từ chối</th>
                                        <th>Đang chờ</th>
                                        <c:if test="${hasCancelledRequests}">
                                            <th>Đã hủy</th>
                                        </c:if>
                                        <th>Ngày nghỉ đã duyệt</th>
                                        <th>TB ngày/đơn duyệt</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${rows}">
                                        <tr>
                                            <td>${row.departmentName}</td>
                                            <td>${row.totalRequests}</td>
                                            <td>${row.approvedRequests}</td>
                                            <td>${row.rejectedRequests}</td>
                                            <td>${row.pendingRequests}</td>
                                            <c:if test="${hasCancelledRequests}">
                                                <td>${row.cancelledRequests}</td>
                                            </c:if>
                                            <td><fmt:formatNumber value="${row.approvedDays}" pattern="#,##0.##" /></td>
                                            <td><fmt:formatNumber value="${row.averageApprovedDays}" pattern="#,##0.00" /></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>
        </main>
    </div>
</div>

<jsp:include page="/components/foot.jsp" />
</body>
</html>
