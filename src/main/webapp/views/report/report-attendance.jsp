<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo chấm công | ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
<div class="layout-wrapper">
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <jsp:include page="/components/header.jsp" />

        <main class="page-container">
            <div class="d-flex justify-content-between align-items-center gap-3 mb-4">
                <h2 class="h3 text-on-surface fw-bold mb-0">Báo cáo chấm công</h2>
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

            <c:if test="${yearlyExpectedDaysNote}">
                <div class="alert alert-info d-flex align-items-center gap-2" role="alert">
                    <span class="material-symbols-outlined">info</span>
                    <span>Đang xem theo năm: ngày công kỳ vọng được tính bằng số nhân viên x 26 x 12.</span>
                </div>
            </c:if>

            <c:if test="${not empty rows and not hasAttendanceRecords}">
                <div class="alert alert-info d-flex align-items-center gap-2" role="alert">
                    <span class="material-symbols-outlined">info</span>
                    <span>Kỳ này chưa có bản ghi chấm công; báo cáo vẫn hiển thị nhân sự active với ngày công thực tế bằng 0.</span>
                </div>
            </c:if>

            <div class="card-premium shadow-sm mb-4">
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/report-attendance" class="row g-3">
                        <div class="col-md-3">
                            <label for="year" class="form-label text-on-surface fw-medium">Năm</label>
                            <select id="year" name="year" class="form-select input-premium">
                                <c:forEach var="y" items="${yearOptions}">
                                    <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label for="month" class="form-label text-on-surface fw-medium">Tháng</label>
                            <select id="month" name="month" class="form-select input-premium">
                                <option value="">Tất cả</option>
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>${m}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label for="departmentId" class="form-label text-on-surface fw-medium">Phòng ban</label>
                            <select id="departmentId" name="departmentId" class="form-select input-premium">
                                <option value="">Tất cả</option>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.id}" ${dept.id == selectedDepartmentId ? 'selected' : ''}>${dept.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
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
                        <p class="label-sm text-on-surface-variant mb-1">Tỷ lệ điểm danh</p>
                        <h3 class="h2 text-on-surface mb-3">
                            <fmt:formatNumber value="${summaryAttendanceRate}" pattern="#,##0.00" />%
                        </h3>
                        <div class="progress" role="progressbar" aria-label="Tỷ lệ điểm danh tổng"
                             aria-valuemin="0" aria-valuemax="100" aria-valuenow="${summaryAttendanceRateBarWidth}"
                             style="height: 8px; background-color: var(--surface-container);">
                            <div class="progress-bar" style="width: ${summaryAttendanceRateBarWidth}%; background: var(--primary-gradient);"></div>
                        </div>
                    </article>
                </div>
                <div class="col-sm-6 col-xl">
                    <article class="card-premium h-100 p-4">
                        <p class="label-sm text-on-surface-variant mb-1">Ngày công kỳ vọng</p>
                        <h3 class="h2 text-on-surface mb-0">${summaryExpectedWorkDays}</h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl">
                    <article class="card-premium h-100 p-4">
                        <p class="label-sm text-on-surface-variant mb-1">Ngày công thực tế</p>
                        <h3 class="h2 text-on-surface mb-0">${summaryActualWorkDays}</h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl">
                    <article class="card-premium h-100 p-4">
                        <p class="label-sm text-on-surface-variant mb-1">Ngày vắng</p>
                        <h3 class="h2 text-on-surface mb-0">${summaryAbsentDays}</h3>
                    </article>
                </div>
                <div class="col-sm-6 col-xl">
                    <article class="card-premium h-100 p-4">
                        <p class="label-sm text-on-surface-variant mb-1">Số lần đi muộn</p>
                        <h3 class="h2 text-on-surface mb-0">${summaryLateCount}</h3>
                    </article>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty rows}">
                    <div class="card-premium shadow-sm">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2 mb-0">Không có dữ liệu chấm công.</p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <section class="card-premium p-4 mb-4">
                        <div class="d-flex align-items-center justify-content-between gap-3 mb-4">
                            <h3 class="h5 fw-bold mb-0">Tỷ lệ điểm danh theo phòng ban</h3>
                            <span class="badge text-bg-light">${selectedMonth == null ? 'Theo năm' : 'Theo tháng'}</span>
                        </div>
                        <div class="d-flex flex-column gap-3">
                            <c:forEach var="row" items="${rows}">
                                <div>
                                    <div class="d-flex justify-content-between gap-3 mb-1">
                                        <span class="body-sm fw-semibold text-on-surface">${row.departmentName}</span>
                                        <span class="body-sm ${row.attendanceRate lt 80 ? 'text-error fw-semibold' : 'text-on-surface-variant'}">
                                            <fmt:formatNumber value="${row.attendanceRate}" pattern="#,##0.00" />%
                                        </span>
                                    </div>
                                    <div class="progress" role="progressbar" aria-label="Tỷ lệ điểm danh ${row.departmentName}"
                                         aria-valuemin="0" aria-valuemax="100" aria-valuenow="${row.attendanceRateBarWidth}"
                                         style="height: 12px; background-color: var(--surface-container);">
                                        <div class="progress-bar" style="width: ${row.attendanceRateBarWidth}%; background: ${row.attendanceRate lt 80 ? 'var(--error)' : 'var(--primary-gradient)'};"></div>
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
                                        <th>Số nhân viên</th>
                                        <th>Ngày công kỳ vọng</th>
                                        <th>Ngày công thực tế</th>
                                        <th>Ngày vắng</th>
                                        <th>Số lần đi muộn</th>
                                        <th>Tỷ lệ điểm danh</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${rows}">
                                        <tr class="${row.attendanceRate lt 80 ? 'table-warning' : ''}">
                                            <td>${row.departmentName}</td>
                                            <td>${row.totalEmployees}</td>
                                            <td>${row.expectedWorkDays}</td>
                                            <td>${row.actualWorkDays}</td>
                                            <td>${row.absentDays}</td>
                                            <td>${row.lateCount}</td>
                                            <td>
                                                <span class="${row.attendanceRate lt 80 ? 'text-error fw-semibold' : ''}">
                                                    <fmt:formatNumber value="${row.attendanceRate}" pattern="#,##0.00" />%
                                                </span>
                                            </td>
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
