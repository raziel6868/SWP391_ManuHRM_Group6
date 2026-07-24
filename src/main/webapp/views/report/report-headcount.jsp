<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo tình hình nhân sự &amp; Biến động | ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
<div class="layout-wrapper">
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <jsp:include page="/components/header.jsp" />

        <main class="page-container">
            <div class="d-flex justify-content-between align-items-start gap-3 mb-4">
                <div>
                    <h2 class="h3 text-on-surface fw-bold mb-1">Báo cáo tình hình nhân sự &amp; Biến động</h2>
                    <p class="body-sm text-on-surface-variant mb-0">
                        Kỳ <c:out value="${periodLabel}" /> từ <c:out value="${periodStart}" /> đến <c:out value="${periodEnd}" />.
                    </p>
                </div>
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
                        <div class="col-lg-3 col-md-6">
                            <label class="form-label text-on-surface fw-medium d-block">Kỳ báo cáo</label>
                            <div class="btn-group w-100" role="group" aria-label="Kỳ báo cáo">
                                <input type="radio" class="btn-check" name="periodType" id="periodMonth" value="month"
                                       ${selectedPeriodType eq 'month' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary" for="periodMonth">Tháng</label>
                                <input type="radio" class="btn-check" name="periodType" id="periodQuarter" value="quarter"
                                       ${selectedPeriodType eq 'quarter' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary" for="periodQuarter">Quý</label>
                            </div>
                        </div>
                        <div class="col-lg-2 col-md-6">
                            <label for="year" class="form-label text-on-surface fw-medium">Năm</label>
                            <select id="year" name="year" class="form-select input-premium">
                                <c:forEach var="yearOption" items="${yearOptions}">
                                    <option value="${yearOption}" ${yearOption == selectedYear ? 'selected' : ''}>${yearOption}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-lg-2 col-md-6">
                            <label for="month" class="form-label text-on-surface fw-medium">Tháng</label>
                            <select id="month" name="month" class="form-select input-premium">
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>Tháng ${m}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-lg-2 col-md-6">
                            <label for="quarter" class="form-label text-on-surface fw-medium">Quý</label>
                            <select id="quarter" name="quarter" class="form-select input-premium">
                                <c:forEach var="q" begin="1" end="4">
                                    <option value="${q}" ${q == selectedQuarter ? 'selected' : ''}>Quý ${q}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-lg-3 col-md-6">
                            <label for="departmentId" class="form-label text-on-surface fw-medium">Phòng ban</label>
                            <select id="departmentId" name="departmentId" class="form-select input-premium">
                                <option value="">Tất cả phòng ban</option>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.id}" ${dept.id == selectedDepartmentId ? 'selected' : ''}>${dept.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-lg-3 col-md-6">
                            <label for="employeeType" class="form-label text-on-surface fw-medium">Phân loại</label>
                            <select id="employeeType" name="employeeType" class="form-select input-premium">
                                <option value="" ${empty selectedEmployeeType ? 'selected' : ''}>Tất cả phân loại</option>
                                <option value="OFFICE" ${selectedEmployeeType eq 'OFFICE' ? 'selected' : ''}>Văn phòng</option>
                                <option value="WORKER" ${selectedEmployeeType eq 'WORKER' ? 'selected' : ''}>Công nhân</option>
                            </select>
                        </div>
                        <div class="col-lg-5 col-md-6">
                            <label for="movementStatus" class="form-label text-on-surface fw-medium">Trạng thái bảng chi tiết</label>
                            <select id="movementStatus" name="movementStatus" class="form-select input-premium">
                                <option value="" ${empty selectedMovementStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                                <option value="NEW" ${selectedMovementStatus eq 'NEW' ? 'selected' : ''}>Nhân sự mới</option>
                                <option value="PROBATION" ${selectedMovementStatus eq 'PROBATION' ? 'selected' : ''}>Đang thử việc</option>
                                <option value="OFFICIAL" ${selectedMovementStatus eq 'OFFICIAL' ? 'selected' : ''}>Chính thức</option>
                                <option value="SEASONAL" ${selectedMovementStatus eq 'SEASONAL' ? 'selected' : ''}>Thời vụ</option>
                                <option value="TERMINATED" ${selectedMovementStatus eq 'TERMINATED' ? 'selected' : ''}>Đã thôi việc</option>
                                <option value="NO_CONTRACT" ${selectedMovementStatus eq 'NO_CONTRACT' ? 'selected' : ''}>Chưa có hợp đồng hiệu lực</option>
                            </select>
                        </div>
                        <div class="col-lg-4 col-md-6 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary-gradient w-100 d-flex align-items-center justify-content-center gap-2">
                                <span class="material-symbols-outlined">search</span> Xem báo cáo
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="row g-3 mb-4">
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-primary border-4">
                        <p class="label-sm text-on-surface-variant mb-1">Nhân sự hiện tại cuối kỳ</p>
                        <h3 class="h2 text-on-surface mb-0">${movementStats.currentEmployees}</h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-danger border-4">
                        <p class="label-sm text-on-surface-variant mb-1">Tỷ lệ nghỉ việc</p>
                        <h3 class="h2 text-on-surface mb-0">
                            <fmt:formatNumber value="${movementStats.turnoverRate}" pattern="#,##0.##" />%
                        </h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-success border-4">
                        <p class="label-sm text-on-surface-variant mb-1">Nhân sự mới</p>
                        <h3 class="h2 text-on-surface mb-0">+${movementStats.newEmployees}</h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-warning border-4">
                        <p class="label-sm text-on-surface-variant mb-1">Đã thôi việc</p>
                        <h3 class="h2 text-on-surface mb-0">${movementStats.terminatedEmployees}</h3>
                    </article>
                </div>
            </div>

            <c:if test="${missingContractRows > 0}">
                <div class="alert alert-info d-flex align-items-center gap-2" role="alert">
                    <span class="material-symbols-outlined">info</span>
                    <span>Có ${missingContractRows} nhân sự active chưa có hợp đồng hiệu lực trong danh sách đang lọc.</span>
                </div>
            </c:if>

            <section class="mb-4">
                <div class="d-flex justify-content-between align-items-center gap-3 mb-3">
                    <h3 class="h5 fw-bold mb-0">Cơ cấu nhân sự hiện tại</h3>
                    <span class="badge text-bg-light">Theo users active</span>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-sm-6 col-xl-3">
                        <article class="card-premium h-100 p-4">
                            <p class="label-sm text-on-surface-variant mb-1">Nhân sự active</p>
                            <h4 class="h2 text-on-surface mb-0">${totalActiveEmployees}</h4>
                        </article>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <article class="card-premium h-100 p-4">
                            <p class="label-sm text-on-surface-variant mb-1">Văn phòng</p>
                            <h4 class="h2 text-on-surface mb-0">${totalOfficeEmployees}</h4>
                        </article>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <article class="card-premium h-100 p-4">
                            <p class="label-sm text-on-surface-variant mb-1">Công nhân</p>
                            <h4 class="h2 text-on-surface mb-0">${totalWorkerEmployees}</h4>
                        </article>
                    </div>
                    <div class="col-sm-6 col-xl-3">
                        <article class="card-premium h-100 p-4">
                            <p class="label-sm text-on-surface-variant mb-1">Phòng ban có nhân sự</p>
                            <h4 class="h2 text-on-surface mb-0">${activeDepartmentCount}</h4>
                        </article>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${empty rows}">
                        <div class="card-premium shadow-sm">
                            <div class="card-body text-center py-5">
                                <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                                <p class="body-md text-on-surface-variant mt-2 mb-0">Không có dữ liệu cơ cấu nhân sự.</p>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="row g-4">
                            <div class="col-lg-7">
                                <section class="card-premium h-100 p-4">
                                    <h4 class="h6 fw-bold mb-4">Nhân sự theo phòng ban</h4>
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
                                    <h4 class="h6 fw-bold mb-4">Tỷ lệ văn phòng / công nhân</h4>
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
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="card-premium shadow-sm">
                <div class="card-body border-bottom">
                    <div class="d-flex justify-content-between align-items-center gap-3">
                        <div>
                            <h3 class="h5 fw-bold mb-1">Danh sách chi tiết tình trạng biến động nhân sự trong kỳ</h3>
                            <p class="body-sm text-on-surface-variant mb-0">Bộ lọc trạng thái áp dụng cho bảng này; KPI phía trên luôn tính toàn kỳ.</p>
                        </div>
                        <span class="badge text-bg-light">${periodLabel}</span>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${empty movementRows}">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2 mb-0">Không có dữ liệu biến động nhân sự theo bộ lọc.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-premium mb-0">
                                <thead>
                                    <tr>
                                        <th>Mã NV</th>
                                        <th>Họ và Tên</th>
                                        <th>Phòng ban</th>
                                        <th>Phân loại</th>
                                        <th>Ngày vào làm</th>
                                        <th>Ngày nghỉ việc</th>
                                        <th>Lý do nghỉ việc</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${movementRows}">
                                        <tr>
                                            <td class="fw-semibold">${row.employeeCode}</td>
                                            <td>${row.fullName}</td>
                                            <td>${row.departmentName}</td>
                                            <td>${row.employeeTypeLabel}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty row.hireDate}">
                                                        <fmt:formatDate value="${row.hireDate}" pattern="dd/MM/yyyy" />
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty row.terminatedAt}">
                                                        <fmt:formatDate value="${row.terminatedAt}" pattern="dd/MM/yyyy" />
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty row.terminateReason}">
                                                        <c:out value="${row.terminateReason}" />
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <span class="badge ${row.movementBadgeClass}">${row.statusLabel}</span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </main>
    </div>
</div>

<jsp:include page="/components/foot.jsp" />
</body>
</html>
