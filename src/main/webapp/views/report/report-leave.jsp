<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo nghỉ phép & Quỹ phép | ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
<div class="layout-wrapper">
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <jsp:include page="/components/header.jsp" />

        <main class="page-container">
            <div class="d-flex justify-content-between align-items-start gap-3 mb-4 flex-wrap">
                <div>
                    <div class="body-sm text-on-surface-variant mb-1">ManuHRM &gt; Báo cáo &gt; Báo cáo nghỉ phép &amp; Quỹ phép</div>
                    <h2 class="h3 text-on-surface fw-bold mb-1">Báo cáo nghỉ phép &amp; Quỹ phép</h2>
                    <p class="body-sm text-on-surface-variant mb-0">
                        Kỳ <c:out value="${periodLabel}" /> từ <c:out value="${periodStart}" /> đến <c:out value="${periodEnd}" />.
                        Nghỉ hưởng lương gồm công ty trả lương và BHXH chi trả.
                    </p>
                </div>
                <button onclick="window.print()" class="btn btn-outline-primary d-flex align-items-center gap-2">
                    <span class="material-symbols-outlined">print</span> In báo cáo
                </button>
            </div>

            <c:if test="${not empty errorMsg}">
                <div class="alert alert-warning d-flex align-items-center gap-2" role="alert">
                    <span class="material-symbols-outlined">warning</span>
                    <span><c:out value="${errorMsg}" /></span>
                </div>
            </c:if>

            <c:if test="${stats.missingAnnualLeaveBalanceCount > 0}">
                <div class="alert alert-info d-flex align-items-center gap-2" role="alert">
                    <span class="material-symbols-outlined">info</span>
                    <span>Có ${stats.missingAnnualLeaveBalanceCount} nhân sự chưa có hạn mức phép năm ${selectedYear}; KPI tồn phép TB chỉ tính các nhân sự đã có hạn mức.</span>
                </div>
            </c:if>

            <div class="card-premium shadow-sm mb-4">
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/report-leave" class="row g-3 align-items-end">
                        <div class="col-lg-3">
                            <label class="form-label text-on-surface fw-medium d-block">Kỳ báo cáo</label>
                            <div class="btn-group w-100" role="group" aria-label="Chọn kỳ báo cáo">
                                <input type="radio" class="btn-check" name="periodType" id="periodMonth" value="month"
                                       ${selectedPeriodType eq 'month' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary" for="periodMonth">Tháng</label>

                                <input type="radio" class="btn-check" name="periodType" id="periodQuarter" value="quarter"
                                       ${selectedPeriodType eq 'quarter' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary" for="periodQuarter">Quý</label>
                            </div>
                        </div>

                        <div class="col-md-4 col-lg-2">
                            <label for="year" class="form-label text-on-surface fw-medium">Năm</label>
                            <select id="year" name="year" class="form-select input-premium">
                                <c:forEach var="y" items="${yearOptions}">
                                    <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-md-4 col-lg-2" data-period-control="month">
                            <label for="month" class="form-label text-on-surface fw-medium">Chọn tháng</label>
                            <select id="month" name="month" class="form-select input-premium">
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>Tháng ${m}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-md-4 col-lg-2" data-period-control="quarter">
                            <label for="quarter" class="form-label text-on-surface fw-medium">Chọn quý</label>
                            <select id="quarter" name="quarter" class="form-select input-premium">
                                <c:forEach var="q" begin="1" end="4">
                                    <option value="${q}" ${q == selectedQuarter ? 'selected' : ''}>Quý ${q}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-md-8 col-lg-3">
                            <label for="leaveTypeId" class="form-label text-on-surface fw-medium">Loại phép</label>
                            <select id="leaveTypeId" name="leaveTypeId" class="form-select input-premium">
                                <option value="">Tất cả loại phép</option>
                                <c:forEach var="leaveType" items="${leaveTypes}">
                                    <option value="${leaveType.id}" ${leaveType.id == selectedLeaveTypeId ? 'selected' : ''}>
                                        <c:out value="${leaveType.name}" />
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-md-4 col-lg">
                            <button type="submit" class="btn btn-primary-gradient w-100 d-flex align-items-center justify-content-center gap-2">
                                <span class="material-symbols-outlined">analytics</span> Xem báo cáo
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <section class="row g-3 mb-4">
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-4 border-primary">
                        <p class="label-sm text-on-surface-variant mb-1">Tổng ngày đã nghỉ</p>
                        <h3 class="h2 text-on-surface mb-2">
                            <fmt:formatNumber value="${stats.totalLeaveDays}" pattern="#,##0.##" /> ngày
                        </h3>
                        <p class="body-sm text-on-surface-variant mb-0">Chỉ tính đơn đã duyệt trong kỳ</p>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-4 border-success">
                        <p class="label-sm text-on-surface-variant mb-1">Nghỉ hưởng lương</p>
                        <h3 class="h2 text-on-surface mb-2">
                            <fmt:formatNumber value="${stats.paidLeaveDays}" pattern="#,##0.##" /> ngày
                        </h3>
                        <p class="body-sm text-on-surface-variant mb-0">Công ty trả lương hoặc BHXH chi trả</p>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-4 border-warning">
                        <p class="label-sm text-on-surface-variant mb-1">Nghỉ không lương</p>
                        <h3 class="h2 text-on-surface mb-2">
                            <fmt:formatNumber value="${stats.unpaidLeaveDays}" pattern="#,##0.##" /> ngày
                        </h3>
                        <p class="body-sm text-on-surface-variant mb-0">Loại phép không có nguồn chi trả</p>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-4 border-secondary">
                        <p class="label-sm text-on-surface-variant mb-1">Tồn phép TB năm</p>
                        <h3 class="h2 text-on-surface mb-2">
                            <fmt:formatNumber value="${stats.averageAnnualLeaveRemainingDays}" pattern="#,##0.##" /> ngày/NV
                        </h3>
                        <p class="body-sm text-on-surface-variant mb-0">Theo quỹ phép năm ${selectedYear}</p>
                    </article>
                </div>
            </section>

            <section class="card-premium p-4 mb-4">
                <div class="d-flex align-items-center justify-content-between gap-3 mb-4 flex-wrap">
                    <h3 class="h5 fw-bold mb-0 d-flex align-items-center gap-2">
                        <span class="material-symbols-outlined">monitoring</span>
                        Tỷ trọng các loại nghỉ phép trong toàn công ty
                    </h3>
                    <span class="badge text-bg-light"><c:out value="${periodLabel}" /></span>
                </div>
                <c:choose>
                    <c:when test="${empty leaveTypeUsageRows}">
                        <p class="body-sm text-on-surface-variant mb-0">Không có đơn nghỉ đã duyệt trong kỳ lọc.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="d-flex flex-column gap-3">
                            <c:forEach var="usage" items="${leaveTypeUsageRows}">
                                <div>
                                    <div class="row align-items-center g-2">
                                        <div class="col-md-3">
                                            <span class="body-sm fw-semibold text-on-surface">
                                                <c:out value="${usage.leaveTypeName}" />
                                            </span>
                                            <span class="body-sm text-on-surface-variant">
                                                (<c:out value="${usage.leaveTypeCode}" />)
                                            </span>
                                        </div>
                                        <div class="col-md-7">
                                            <div class="progress" role="progressbar" aria-label="Tỷ trọng ${usage.leaveTypeName}"
                                                 aria-valuemin="0" aria-valuemax="100" aria-valuenow="${usage.percentage}"
                                                 style="height: 14px; background-color: var(--surface-container);">
                                                <div class="progress-bar" style="width: ${usage.percentage}%; background: var(--primary-gradient);"></div>
                                            </div>
                                        </div>
                                        <div class="col-md-2 text-md-end">
                                            <span class="body-sm fw-semibold">
                                                <fmt:formatNumber value="${usage.percentage}" pattern="#,##0.##" />%
                                            </span>
                                            <span class="body-sm text-on-surface-variant">
                                                (<fmt:formatNumber value="${usage.totalDays}" pattern="#,##0.##" /> ngày)
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="card-premium shadow-sm">
                <div class="card-body border-bottom">
                    <h3 class="h5 fw-bold mb-1 d-flex align-items-center gap-2">
                        <span class="material-symbols-outlined">assignment</span>
                        Chi tiết quỹ phép &amp; số ngày đã nghỉ của từng nhân viên
                    </h3>
                    <p class="body-sm text-on-surface-variant mb-0">
                        Quỹ phép năm lấy từ loại phép năm đang active; ngày nghỉ hưởng lương gồm COMPANY và SOCIAL_INSURANCE.
                    </p>
                </div>
                <c:choose>
                    <c:when test="${empty employeeRows}">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2 mb-0">Không có nhân sự active để hiển thị.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-premium mb-0">
                                <thead>
                                    <tr>
                                        <th>Mã NV</th>
                                        <th>Họ và tên</th>
                                        <th>Phòng ban</th>
                                        <th>Quỹ phép năm</th>
                                        <th>Đã sử dụng</th>
                                        <th>Còn tồn lại</th>
                                        <th>Nghỉ hưởng lương</th>
                                        <th>Nghỉ không lương</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${employeeRows}">
                                        <tr>
                                            <td><c:out value="${row.employeeCode}" /></td>
                                            <td><c:out value="${row.fullName}" /></td>
                                            <td><c:out value="${row.departmentName}" /></td>
                                            <td><fmt:formatNumber value="${row.annualLeaveTotalDays}" pattern="#,##0.##" /> ngày</td>
                                            <td><fmt:formatNumber value="${row.annualLeaveUsedDays}" pattern="#,##0.##" /> ngày</td>
                                            <td class="${row.annualLeaveRemainingDays lt 3 ? 'text-warning fw-semibold' : 'text-success fw-semibold'}">
                                                <fmt:formatNumber value="${row.annualLeaveRemainingDays}" pattern="#,##0.##" /> ngày
                                            </td>
                                            <td><fmt:formatNumber value="${row.paidLeaveDays}" pattern="#,##0.##" /> ngày</td>
                                            <td class="${row.unpaidLeaveDays gt 0 ? 'text-error fw-semibold' : ''}">
                                                <fmt:formatNumber value="${row.unpaidLeaveDays}" pattern="#,##0.##" /> ngày
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
<script>
    (function () {
        function updatePeriodControls() {
            var selected = document.querySelector('input[name="periodType"]:checked');
            var periodType = selected ? selected.value : 'month';
            document.querySelectorAll('[data-period-control]').forEach(function (control) {
                control.classList.toggle('d-none', control.getAttribute('data-period-control') !== periodType);
            });
        }

        document.querySelectorAll('input[name="periodType"]').forEach(function (input) {
            input.addEventListener('change', updatePeriodControls);
        });
        updatePeriodControls();
    })();
</script>
</body>
</html>
