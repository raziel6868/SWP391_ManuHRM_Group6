<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo chấm công & OT | ManuHRM</title>
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
                    <div class="body-sm text-on-surface-variant mb-1">ManuHRM &gt; Báo cáo &gt; Báo cáo chấm công &amp; OT</div>
                    <h2 class="h3 text-on-surface fw-bold mb-1">Báo cáo chấm công &amp; OT</h2>
                    <p class="body-sm text-on-surface-variant mb-0">
                        Kỳ <c:out value="${periodLabel}" /> từ <c:out value="${periodStart}" /> đến <c:out value="${periodEnd}" />,
                        công kỳ vọng tính theo ngày làm việc thực tế, trừ thứ 7 và chủ nhật.
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

            <div class="card-premium shadow-sm mb-4">
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/report-attendance" class="row g-3 align-items-end">
                        <div class="col-lg-4">
                            <label class="form-label text-on-surface fw-medium d-block">Kỳ báo cáo</label>
                            <div class="btn-group w-100" role="group" aria-label="Chọn kỳ báo cáo">
                                <input type="radio" class="btn-check" name="periodType" id="periodMonth" value="month"
                                       ${selectedPeriodType eq 'month' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary" for="periodMonth">Tháng</label>

                                <input type="radio" class="btn-check" name="periodType" id="periodQuarter" value="quarter"
                                       ${selectedPeriodType eq 'quarter' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary" for="periodQuarter">Quý</label>

                                <input type="radio" class="btn-check" name="periodType" id="periodYear" value="year"
                                       ${selectedPeriodType eq 'year' ? 'checked' : ''}>
                                <label class="btn btn-outline-primary" for="periodYear">Năm</label>
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
                            <label for="departmentId" class="form-label text-on-surface fw-medium">Phòng ban</label>
                            <select id="departmentId" name="departmentId" class="form-select input-premium">
                                <option value="">Tất cả phòng ban</option>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.id}" ${dept.id == selectedDepartmentId ? 'selected' : ''}>
                                        <c:out value="${dept.name}" />
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
                        <p class="label-sm text-on-surface-variant mb-1">Tỷ lệ đi làm</p>
                        <h3 class="h2 text-success mb-2"><fmt:formatNumber value="${stats.attendanceRate}" pattern="#,##0.00" />%</h3>
                        <p class="body-sm text-on-surface-variant mb-0">${stats.totalActualWorkDays}/${stats.totalExpectedWorkDays} công</p>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-4 border-warning">
                        <p class="label-sm text-on-surface-variant mb-1">Tổng giờ OT</p>
                        <h3 class="h2 text-on-surface mb-2"><fmt:formatNumber value="${stats.totalOtHours}" pattern="#,##0.##" /> h</h3>
                        <p class="body-sm text-on-surface-variant mb-0">Chỉ tính OT đã duyệt</p>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-4 border-danger">
                        <p class="label-sm text-on-surface-variant mb-1">Số ca đi muộn</p>
                        <h3 class="h2 text-on-surface mb-2">${stats.totalLateCount} lần</h3>
                        <p class="body-sm text-on-surface-variant mb-0">Theo trạng thái LATE</p>
                    </article>
                </div>
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4 border-top border-4 border-secondary">
                        <p class="label-sm text-on-surface-variant mb-1">Vắng không phép</p>
                        <h3 class="h2 text-on-surface mb-2"><fmt:formatNumber value="${stats.totalUnauthorizedAbsenceDays}" pattern="#,##0.##" /> ca</h3>
                        <p class="body-sm text-on-surface-variant mb-0">Công kỳ vọng - công thực tế - nghỉ có phép</p>
                    </article>
                </div>
            </section>

            <section class="row g-4 mb-4">
                <div class="col-lg-6">
                    <article class="card-premium h-100">
                        <div class="card-body border-bottom">
                            <h3 class="h5 fw-bold text-success mb-0 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined">workspace_premium</span>
                                Top nhân sự chăm chỉ
                            </h3>
                        </div>
                        <div class="card-body">
                            <c:choose>
                                <c:when test="${empty topDiligentRows}">
                                    <p class="body-sm text-on-surface-variant mb-0">Chưa có nhân sự đạt đủ công và không đi muộn trong kỳ này.</p>
                                </c:when>
                                <c:otherwise>
                                    <div class="d-flex flex-column gap-3">
                                        <c:forEach var="row" items="${topDiligentRows}" varStatus="status">
                                            <div class="d-flex align-items-center justify-content-between gap-3 border-bottom pb-3">
                                                <div class="d-flex align-items-center gap-3">
                                                    <span class="badge-premium badge-success">${status.count}</span>
                                                    <div>
                                                        <div class="fw-semibold"><c:out value="${row.fullName}" /></div>
                                                        <div class="body-sm text-on-surface-variant"><c:out value="${row.departmentName}" /></div>
                                                    </div>
                                                </div>
                                                <strong class="text-success">${row.actualWorkDays}/${row.expectedWorkDays} công</strong>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </article>
                </div>

                <div class="col-lg-6">
                    <article class="card-premium h-100">
                        <div class="card-body border-bottom">
                            <h3 class="h5 fw-bold text-error mb-0 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined">warning</span>
                                Cảnh báo vi phạm kỷ luật
                            </h3>
                        </div>
                        <div class="card-body">
                            <c:choose>
                                <c:when test="${empty warningRows}">
                                    <p class="body-sm text-on-surface-variant mb-0">Không có đi muộn hoặc vắng không phép trong kỳ này.</p>
                                </c:when>
                                <c:otherwise>
                                    <div class="d-flex flex-column gap-3">
                                        <c:forEach var="row" items="${warningRows}" varStatus="status">
                                            <div class="d-flex align-items-center justify-content-between gap-3 border-bottom pb-3">
                                                <div class="d-flex align-items-center gap-3">
                                                    <span class="badge-premium badge-error">${status.count}</span>
                                                    <div>
                                                        <div class="fw-semibold"><c:out value="${row.fullName}" /></div>
                                                        <div class="body-sm text-on-surface-variant"><c:out value="${row.departmentName}" /></div>
                                                    </div>
                                                </div>
                                                <div class="text-end body-sm">
                                                    <div class="text-error fw-semibold">${row.lateCount} lần muộn</div>
                                                    <div class="text-on-surface-variant">
                                                        <fmt:formatNumber value="${row.unauthorizedAbsenceDays}" pattern="#,##0.##" /> ca vắng
                                                    </div>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </article>
                </div>
            </section>

            <section class="card-premium shadow-sm">
                <div class="card-body border-bottom">
                    <h3 class="h5 fw-bold mb-1 d-flex align-items-center gap-2">
                        <span class="material-symbols-outlined">assignment</span>
                        Danh sách chi tiết chấm công &amp; OT của nhân viên
                    </h3>
                    <p class="body-sm text-on-surface-variant mb-0">
                        Công kỳ vọng từng nhân viên được tính theo phần kỳ giao với hợp đồng hiệu lực.
                    </p>
                </div>
                <c:choose>
                    <c:when test="${empty employeeRows}">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2 mb-0">Không có nhân sự phù hợp với bộ lọc.</p>
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
                                        <th>Công kỳ vọng</th>
                                        <th>Công thực tế</th>
                                        <th>Nghỉ có phép</th>
                                        <th>Tổng giờ OT</th>
                                        <th>Đi muộn</th>
                                        <th>Vắng KP</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${employeeRows}">
                                        <tr>
                                            <td><c:out value="${row.employeeCode}" /></td>
                                            <td><c:out value="${row.fullName}" /></td>
                                            <td><c:out value="${row.departmentName}" /></td>
                                            <td>${row.expectedWorkDays} ca</td>
                                            <td>${row.actualWorkDays} ca</td>
                                            <td><fmt:formatNumber value="${row.approvedLeaveDays}" pattern="#,##0.##" /> ngày</td>
                                            <td><fmt:formatNumber value="${row.totalOtHours}" pattern="#,##0.##" /> h</td>
                                            <td class="${row.lateCount gt 0 ? 'text-error fw-semibold' : ''}">${row.lateCount} lần</td>
                                            <td class="${row.unauthorizedAbsenceDays gt 0 ? 'text-error fw-semibold' : ''}">
                                                <fmt:formatNumber value="${row.unauthorizedAbsenceDays}" pattern="#,##0.##" /> ca
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
