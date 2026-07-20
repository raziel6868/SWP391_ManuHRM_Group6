<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo nhân sự | ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
<div class="layout-wrapper">
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <jsp:include page="/components/header.jsp" />

        <main class="page-container">
            <div class="d-flex justify-content-between align-items-center gap-3 mb-4">
                <h2 class="h3 text-on-surface fw-bold mb-0">Báo cáo nhân sự</h2>
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
                    <form method="get" action="${pageContext.request.contextPath}/report-headcount" class="row g-3">
                        <div class="col-md-8">
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
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4">
                        <div class="d-flex align-items-center justify-content-between gap-3">
                            <div>
                                <p class="label-sm text-on-surface-variant mb-1">Nhân sự active</p>
                                <h3 class="h2 text-on-surface mb-0">${totalActiveEmployees}</h3>
                            </div>
                            <span class="material-symbols-outlined text-primary" style="font-size: 2rem;">groups</span>
                        </div>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4">
                        <div class="d-flex align-items-center justify-content-between gap-3">
                            <div>
                                <p class="label-sm text-on-surface-variant mb-1">Văn phòng</p>
                                <h3 class="h2 text-on-surface mb-0">${totalOfficeEmployees}</h3>
                            </div>
                            <span class="material-symbols-outlined text-secondary" style="font-size: 2rem;">badge</span>
                        </div>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4">
                        <div class="d-flex align-items-center justify-content-between gap-3">
                            <div>
                                <p class="label-sm text-on-surface-variant mb-1">Công nhân</p>
                                <h3 class="h2 text-on-surface mb-0">${totalWorkerEmployees}</h3>
                            </div>
                            <span class="material-symbols-outlined text-warning" style="font-size: 2rem;">engineering</span>
                        </div>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4">
                        <div class="d-flex align-items-center justify-content-between gap-3">
                            <div>
                                <p class="label-sm text-on-surface-variant mb-1">Phòng ban có nhân sự</p>
                                <h3 class="h2 text-on-surface mb-0">${activeDepartmentCount}</h3>
                            </div>
                            <span class="material-symbols-outlined text-success" style="font-size: 2rem;">apartment</span>
                        </div>
                    </article>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty rows}">
                    <div class="card-premium shadow-sm">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2 mb-0">Không có dữ liệu nhân sự active.</p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row g-4 mb-4">
                        <div class="col-lg-7">
                            <section class="card-premium h-100 p-4">
                                <div class="d-flex align-items-center justify-content-between gap-3 mb-4">
                                    <h3 class="h5 fw-bold mb-0">Nhân sự theo phòng ban</h3>
                                    <span class="badge text-bg-light">Active</span>
                                </div>
                                <div class="d-flex flex-column gap-3">
                                    <c:forEach var="row" items="${rows}">
                                        <div>
                                            <div class="d-flex justify-content-between gap-3 mb-1">
                                                <span class="body-sm fw-semibold text-on-surface">${row.departmentName}</span>
                                                <span class="body-sm text-on-surface-variant">${row.activeEmployees} nhân sự</span>
                                            </div>
                                            <div class="progress" role="progressbar" aria-label="Tỷ lệ nhân sự ${row.departmentName}"
                                                 aria-valuemin="0" aria-valuemax="100" aria-valuenow="${row.companyPercentage}"
                                                 style="height: 12px; background-color: var(--surface-container);">
                                                <div class="progress-bar" style="width: ${row.companyPercentage}%; background: var(--primary-gradient);"></div>
                                            </div>
                                            <div class="body-sm text-on-surface-variant mt-1">
                                                <fmt:formatNumber value="${row.companyPercentage}" pattern="#,##0.00" />% toàn công ty
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </section>
                        </div>
                        <div class="col-lg-5">
                            <section class="card-premium h-100 p-4">
                                <h3 class="h5 fw-bold mb-4">Tỷ lệ văn phòng / công nhân</h3>
                                <div class="progress mb-3" role="progressbar" aria-label="Tỷ lệ văn phòng và công nhân"
                                     aria-valuemin="0" aria-valuemax="100" style="height: 28px;">
                                    <div class="progress-bar" style="width: ${officePercentage}%; background-color: var(--primary);">
                                        <fmt:formatNumber value="${officePercentage}" pattern="#,##0.##" />%
                                    </div>
                                    <div class="progress-bar" style="width: ${workerPercentage}%; background-color: var(--tertiary);">
                                        <fmt:formatNumber value="${workerPercentage}" pattern="#,##0.##" />%
                                    </div>
                                </div>
                                <div class="d-flex flex-column gap-2">
                                    <div class="d-flex align-items-center justify-content-between">
                                        <span class="body-sm"><span class="d-inline-block rounded-circle me-2" style="width: 10px; height: 10px; background-color: var(--primary);"></span>Văn phòng</span>
                                        <strong>${totalOfficeEmployees}</strong>
                                    </div>
                                    <div class="d-flex align-items-center justify-content-between">
                                        <span class="body-sm"><span class="d-inline-block rounded-circle me-2" style="width: 10px; height: 10px; background-color: var(--tertiary);"></span>Công nhân</span>
                                        <strong>${totalWorkerEmployees}</strong>
                                    </div>
                                </div>
                            </section>
                        </div>
                    </div>

                    <c:if test="${timelineDataLimited}">
                        <div class="alert alert-info d-flex align-items-center gap-2" role="alert">
                            <span class="material-symbols-outlined">info</span>
                            <span>Schema hiện tại có ngày tạo tài khoản nhưng chưa có ngày nghỉ việc rõ ràng, nên phần biến động nhân sự mới/nghỉ việc đang bị giới hạn bởi dữ liệu hiện có.</span>
                        </div>
                    </c:if>

                    <div class="card-premium shadow-sm">
                        <div class="table-responsive">
                            <table class="table table-premium mb-0">
                                <thead>
                                    <tr>
                                        <th>Phòng ban</th>
                                        <th>Nhân viên văn phòng</th>
                                        <th>Công nhân</th>
                                        <th>Tổng active</th>
                                        <th>Tỷ lệ toàn công ty</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${rows}">
                                        <tr>
                                            <td>${row.departmentName}</td>
                                            <td>${row.officeEmployees}</td>
                                            <td>${row.workerEmployees}</td>
                                            <td>${row.activeEmployees}</td>
                                            <td><fmt:formatNumber value="${row.companyPercentage}" pattern="#,##0.00" />%</td>
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
