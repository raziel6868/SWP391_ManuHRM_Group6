<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo lương | ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <style>
        .payroll-kpi-note {
            font-size: 0.82rem;
            margin-top: 0.65rem;
        }

        .payroll-period-options {
            display: grid;
            gap: 0.75rem;
            grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
        }

        .payroll-period-option {
            border: 1px solid var(--outline-variant);
            border-radius: 8px;
            cursor: pointer;
            display: flex;
            gap: 0.75rem;
            padding: 0.85rem 1rem;
            transition: border-color 0.2s ease, box-shadow 0.2s ease;
        }

        .payroll-period-option:has(input:checked) {
            border-color: var(--primary);
            box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
        }

        .payroll-kpi-card {
            overflow: hidden;
            position: relative;
        }

        .payroll-kpi-card::after {
            background: linear-gradient(135deg, rgba(37, 99, 235, 0.08), transparent 65%);
            content: "";
            height: 110px;
            position: absolute;
            right: -36px;
            top: -42px;
            transform: rotate(16deg);
            width: 150px;
        }

        .payroll-kpi-icon {
            align-items: center;
            border-radius: 999px;
            display: flex;
            height: 48px;
            justify-content: center;
            position: absolute;
            right: 1.25rem;
            top: 1.25rem;
            width: 48px;
            z-index: 1;
        }

        .payroll-kpi-blue {
            background: #dbeafe;
            color: #1d4ed8;
        }

        .payroll-kpi-green {
            background: #dcfce7;
            color: #15803d;
        }

        .payroll-kpi-red {
            background: #fee2e2;
            color: #b91c1c;
        }

        .payroll-kpi-amber {
            background: #fef3c7;
            color: #b45309;
        }

        .payroll-column-chart {
            align-items: end;
            display: flex;
            gap: 1rem;
            min-height: 250px;
            overflow-x: auto;
            padding-top: 1rem;
        }

        .payroll-column {
            align-items: center;
            display: flex;
            flex: 1 0 64px;
            flex-direction: column;
            gap: 0.65rem;
            justify-content: end;
            min-width: 64px;
        }

        .payroll-bars {
            align-items: end;
            border-bottom: 1px solid var(--outline-variant);
            display: flex;
            gap: 0.35rem;
            height: 180px;
            justify-content: center;
            width: 100%;
        }

        .payroll-bar {
            border-radius: 8px 8px 2px 2px;
            min-height: 4px;
            width: 20px;
        }

        .payroll-bar-gross {
            background: linear-gradient(180deg, #2563eb 0%, #93c5fd 100%);
        }

        .payroll-bar-net {
            background: linear-gradient(180deg, #16a34a 0%, #bbf7d0 100%);
        }

        .department-spend-grid {
            display: grid;
            gap: 1rem;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
        }

        .department-spend-item {
            background: var(--surface-container-low);
            border: 1px solid var(--outline-variant);
            border-radius: 8px;
            min-width: 0;
            padding: 1rem;
        }

        .department-spend-name {
            color: var(--on-surface-variant);
            font-size: 0.82rem;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .department-segment-track {
            background: #dbeafe;
            border-radius: 8px;
            height: 42px;
            overflow: hidden;
        }

        .department-segment-fill {
            border-radius: 8px;
            height: 100%;
            min-width: 6px;
        }

        .segment-0 {
            background: #2563eb;
        }

        .segment-1 {
            background: #60a5fa;
        }

        .segment-2 {
            background: #93c5fd;
        }

        .segment-3 {
            background: #bfdbfe;
        }
    </style>
</head>
<body class="bg-background text-on-surface">
<div class="layout-wrapper">
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <jsp:include page="/components/header.jsp" />

        <main class="page-container">
            <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
                <div>
                    <h2 class="h3 text-on-surface fw-bold mb-1">Báo cáo lương</h2>
                    <p class="body-sm text-on-surface-variant mb-0"><c:out value="${payrollSourceMessage}" /></p>
                </div>
                <button onclick="window.print()" class="btn btn-outline-primary">
                    <span class="material-symbols-outlined">print</span> In báo cáo
                </button>
            </div>

            <c:if test="${not empty validationErrors}">
                <div class="alert alert-warning mb-4">
                    <c:forEach var="error" items="${validationErrors}">
                        <div><c:out value="${error}" /></div>
                    </c:forEach>
                </div>
            </c:if>

            <div class="card-premium shadow-sm mb-4">
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/report-payroll" class="row g-3" data-payroll-filter>
                        <div class="col-12">
                            <label class="form-label text-on-surface fw-medium">Kiểu báo cáo</label>
                            <div class="payroll-period-options">
                                <label class="payroll-period-option">
                                    <input type="radio" name="periodType" value="month" ${selectedPeriodType == 'month' ? 'checked' : ''}>
                                    <span>
                                        <strong>Theo tháng</strong>
                                        <span class="d-block body-sm text-on-surface-variant">Chọn năm và tháng cần xem</span>
                                    </span>
                                </label>
                                <label class="payroll-period-option">
                                    <input type="radio" name="periodType" value="quarter" ${selectedPeriodType == 'quarter' ? 'checked' : ''}>
                                    <span>
                                        <strong>Theo quý</strong>
                                        <span class="d-block body-sm text-on-surface-variant">Chọn năm và quý cần xem</span>
                                    </span>
                                </label>
                            </div>
                        </div>

                        <div class="col-md-4 payroll-filter-control" data-period-control="month quarter">
                            <label for="year" class="form-label text-on-surface fw-medium">Năm</label>
                            <select id="year" name="year" class="form-select input-premium">
                                <c:forEach var="y" items="${yearOptions}">
                                    <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4 payroll-filter-control ${selectedPeriodType == 'quarter' ? '' : 'd-none'}" data-period-control="quarter">
                            <label for="quarter" class="form-label text-on-surface fw-medium">Quý</label>
                            <select id="quarter" name="quarter" class="form-select input-premium" ${selectedPeriodType == 'quarter' ? '' : 'disabled'}>
                                <c:forEach var="q" begin="1" end="4">
                                    <option value="${q}" ${q == selectedQuarter ? 'selected' : ''}
                                            ${selectedYear == currentYear and ((q - 1) * 3 + 1) > currentMonth ? 'disabled' : ''}>Quý ${q}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4 payroll-filter-control ${selectedPeriodType == 'month' ? '' : 'd-none'}" data-period-control="month">
                            <label for="month" class="form-label text-on-surface fw-medium">Tháng</label>
                            <select id="month" name="month" class="form-select input-premium" ${selectedPeriodType == 'month' ? '' : 'disabled'}>
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" ${m == selectedMonth ? 'selected' : ''}
                                            ${selectedYear == currentYear and m > currentMonth ? 'disabled' : ''}>Tháng ${m}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary-gradient w-100">
                                <span class="material-symbols-outlined">search</span> Xem báo cáo
                            </button>
                        </div>
                        <div class="col-12">
                            <p class="body-sm text-on-surface-variant mb-0">
                                Kỳ đang xem: <strong><c:out value="${periodLabel}" /></strong>.
                            </p>
                        </div>
                    </form>
                </div>
            </div>

            <section class="row g-3 mb-4">
                <div class="col-lg-3 col-md-6">
                    <div class="card-premium p-4 h-100 payroll-kpi-card pe-5">
                        <span class="material-symbols-outlined payroll-kpi-icon payroll-kpi-blue">payments</span>
                        <p class="label-md text-on-surface-variant mb-1">Tổng thu nhập</p>
                        <p class="h3 fw-bold mb-0"><fmt:formatNumber value="${totals.grossIncome}" pattern="#,##0" /> VND</p>
                        <p class="payroll-kpi-note ${grossIncomeChangeClass} mb-0">${grossIncomeChangeLabel} so với ${previousPeriodLabel}</p>
                    </div>
                </div>
                <div class="col-lg-3 col-md-6">
                    <div class="card-premium p-4 h-100 payroll-kpi-card pe-5">
                        <span class="material-symbols-outlined payroll-kpi-icon payroll-kpi-green">account_balance_wallet</span>
                        <p class="label-md text-on-surface-variant mb-1">Lương thực nhận</p>
                        <p class="h3 fw-bold mb-0"><fmt:formatNumber value="${totals.netSalary}" pattern="#,##0" /> VND</p>
                        <p class="payroll-kpi-note ${netSalaryChangeClass} mb-0">${netSalaryChangeLabel} so với ${previousPeriodLabel}</p>
                    </div>
                </div>
                <div class="col-lg-3 col-md-6">
                    <div class="card-premium p-4 h-100 payroll-kpi-card pe-5">
                        <span class="material-symbols-outlined payroll-kpi-icon payroll-kpi-red">receipt_long</span>
                        <p class="label-md text-on-surface-variant mb-1">Khấu trừ</p>
                        <p class="h3 fw-bold mb-0"><fmt:formatNumber value="${totals.deductions}" pattern="#,##0" /> VND</p>
                        <p class="payroll-kpi-note ${deductionsChangeClass} mb-0">${deductionsChangeLabel} so với ${previousPeriodLabel}</p>
                    </div>
                </div>
                <div class="col-lg-3 col-md-6">
                    <div class="card-premium p-4 h-100 payroll-kpi-card pe-5">
                        <span class="material-symbols-outlined payroll-kpi-icon payroll-kpi-amber">schedule</span>
                        <p class="label-md text-on-surface-variant mb-1">Tiền tăng ca</p>
                        <p class="h3 fw-bold mb-0"><fmt:formatNumber value="${totals.totalOtCost}" pattern="#,##0" /> VND</p>
                        <p class="payroll-kpi-note ${overtimeCostChangeClass} mb-0">${overtimeCostChangeLabel} so với ${previousPeriodLabel}</p>
                    </div>
                </div>
            </section>

            <c:choose>
                <c:when test="${empty rows}">
                    <div class="card-premium shadow-sm">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2">Không có dữ liệu lương trong kỳ đã chọn. Hãy kiểm tra lương cơ bản, chấm công và cấu hình bảng lương.</p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <section class="row g-4 mb-4">
                        <div class="col-lg-8">
                            <div class="card-premium p-4 h-100">
                                <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                                    <div>
                                        <h3 class="h5 fw-bold mb-1">Biểu đồ tăng trưởng lương</h3>
                                        <p class="body-sm text-on-surface-variant mb-0">Theo dõi tổng thu nhập và lương thực nhận theo thời gian.</p>
                                    </div>
                                    <span class="badge bg-light text-on-surface border"><c:out value="${trendPeriodLabel}" /></span>
                                </div>
                                <div class="payroll-column-chart">
                                    <c:forEach var="trend" items="${trendRows}">
                                        <div class="payroll-column">
                                            <div class="payroll-bars">
                                                <div class="payroll-bar payroll-bar-gross" title="Tổng thu nhập"
                                                     style="height: ${trend.grossIncome * 100 / maxTrendGrossIncome}%;"></div>
                                                <div class="payroll-bar payroll-bar-net" title="Lương thực nhận"
                                                     style="height: ${trend.netSalary * 100 / maxTrendGrossIncome}%;"></div>
                                            </div>
                                            <div class="text-center">
                                                <div class="label-sm fw-medium"><c:out value="${trend.label}" /></div>
                                                <div class="body-sm text-on-surface-variant"><fmt:formatNumber value="${trend.grossIncome}" pattern="#,##0" /></div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                                <div class="d-flex gap-3 mt-3 body-sm text-on-surface-variant flex-wrap">
                                    <span><span class="badge bg-primary">&nbsp;</span> Tổng thu nhập</span>
                                    <span><span class="badge bg-success">&nbsp;</span> Lương thực nhận</span>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-4">
                            <div class="card-premium p-4 h-100">
                                <h3 class="h5 fw-bold mb-3">Cơ cấu chi phí lương</h3>
                                <div class="d-flex flex-column gap-3">
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Lương cơ bản</span><strong><fmt:formatNumber value="${totals.totalSalary}" pattern="#,##0" /></strong></div>
                                        <div class="progress"><div class="progress-bar" style="width: ${totals.grossIncome == 0 ? 0 : totals.totalSalary * 100 / totals.grossIncome}%;"></div></div>
                                    </div>
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Phụ cấp</span><strong><fmt:formatNumber value="${totals.totalAllowances}" pattern="#,##0" /></strong></div>
                                        <div class="progress"><div class="progress-bar bg-info" style="width: ${totals.grossIncome == 0 ? 0 : totals.totalAllowances * 100 / totals.grossIncome}%;"></div></div>
                                    </div>
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Tiền tăng ca</span><strong><fmt:formatNumber value="${totals.totalOtCost}" pattern="#,##0" /></strong></div>
                                        <div class="progress"><div class="progress-bar bg-warning" style="width: ${totals.grossIncome == 0 ? 0 : totals.totalOtCost * 100 / totals.grossIncome}%;"></div></div>
                                    </div>
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Khấu trừ</span><strong><fmt:formatNumber value="${totals.deductions}" pattern="#,##0" /></strong></div>
                                        <div class="progress"><div class="progress-bar bg-danger" style="width: ${totals.grossIncome == 0 ? 0 : totals.deductions * 100 / totals.grossIncome}%;"></div></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </section>

                    <section class="card-premium p-4 shadow-sm mb-4">
                        <div class="d-flex justify-content-between align-items-start gap-3 mb-4 flex-wrap">
                            <div>
                                <h3 class="h5 fw-bold mb-1">Chi phí lương theo phòng ban</h3>
                                <p class="h3 fw-bold mb-1"><fmt:formatNumber value="${totals.grossIncome}" pattern="#,##0" /> VND</p>
                                <p class="${grossIncomeChangeClass} body-sm mb-0">${grossIncomeChangeLabel} so với ${previousPeriodLabel}</p>
                            </div>
                            <span class="badge bg-light text-on-surface border"><c:out value="${periodLabel}" /></span>
                        </div>

                        <div class="department-spend-grid mb-3">
                            <c:forEach var="row" items="${rows}" varStatus="departmentStatus">
                                <div class="department-spend-item">
                                    <p class="fw-bold mb-1"><fmt:formatNumber value="${row.grossIncome}" pattern="#,##0" /> VND</p>
                                    <p class="department-spend-name mb-0"><c:out value="${row.departmentName}" /></p>
                                    <div class="department-segment-track mt-3">
                                        <div class="department-segment-fill segment-${departmentStatus.index mod 4}"
                                             style="width: ${totals.grossIncome == 0 ? 0 : row.grossIncome * 100 / totals.grossIncome}%;"></div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </section>
                </c:otherwise>
            </c:choose>

            <div class="card-premium shadow-sm">
                <div class="card-body border-bottom">
                    <h3 class="h5 fw-bold mb-1">Chi tiết lương theo nhân viên</h3>
                    <p class="body-sm text-on-surface-variant mb-0">Các dòng cảnh báo giúp HR kiểm tra dữ liệu trước khi chốt bảng lương.</p>
                </div>
                <div class="card-body border-bottom">
                    <div class="position-relative" style="max-width: 420px;">
                        <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                              style="left: 12px; font-size: 1.25rem;">search</span>
                        <input type="search" class="form-control input-premium ps-5" data-payroll-employee-search
                               placeholder="Tìm mã, tên hoặc phòng ban">
                    </div>
                </div>
                <c:choose>
                    <c:when test="${empty employeeRows}">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">payments</span>
                            <p class="body-md text-on-surface-variant mt-2">Không có dòng lương chi tiết trong kỳ đã chọn.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-premium mb-0">
                                <thead>
                                    <tr>
                                        <th>Mã NV</th>
                                        <th>Họ tên</th>
                                        <th>Phòng ban</th>
                                        <th>Ngày công</th>
                                        <th>Nghỉ có lương</th>
                                        <th>Giờ tăng ca</th>
                                        <th>Tổng thu nhập</th>
                                        <th>Khấu trừ</th>
                                        <th>Lương thực nhận</th>
                                        <th>Cảnh báo</th>
                                    </tr>
                                </thead>
                                <tbody data-payroll-employee-table>
                                    <c:forEach var="employee" items="${employeeRows}">
                                        <tr data-search-text="${employee.employeeCode} ${employee.fullName} ${employee.departmentName} ${employee.warningStatus}">
                                            <td><c:out value="${employee.employeeCode}" /></td>
                                            <td><c:out value="${employee.fullName}" /></td>
                                            <td><c:out value="${employee.departmentName}" /></td>
                                            <td><fmt:formatNumber value="${employee.actualWorkDays}" pattern="#,##0.##" /></td>
                                            <td><fmt:formatNumber value="${employee.paidLeaveDays}" pattern="#,##0.##" /></td>
                                            <td><fmt:formatNumber value="${employee.approvedOtHours}" pattern="#,##0.##" /></td>
                                            <td><fmt:formatNumber value="${employee.grossIncome}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${employee.deductions}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${employee.netSalary}" pattern="#,##0" /> VND</td>
                                            <td><c:out value="${employee.warningStatus}" /></td>
                                        </tr>
                                    </c:forEach>
                                    <tr class="d-none" data-payroll-empty-row>
                                        <td colspan="10" class="text-center text-on-surface-variant py-4">Không tìm thấy nhân viên phù hợp.</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>

<script>
    (function () {
        const form = document.querySelector('[data-payroll-filter]');
        if (!form) {
            return;
        }
        const controls = form.querySelectorAll('[data-period-control]');
        const radios = form.querySelectorAll('input[name="periodType"]');

        function selectedPeriod() {
            const checked = form.querySelector('input[name="periodType"]:checked');
            return checked ? checked.value : 'month';
        }

        function syncControls() {
            const period = selectedPeriod();
            controls.forEach((control) => {
                const periods = control.dataset.periodControl.split(' ');
                const shouldShow = periods.includes(period);
                control.classList.toggle('d-none', !shouldShow);
                control.querySelectorAll('select, input').forEach((field) => {
                    field.disabled = !shouldShow;
                });
            });
        }

        radios.forEach((radio) => radio.addEventListener('change', syncControls));
        syncControls();

        function normalizeSearchText(value) {
            return (value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
        }

        const employeeSearch = document.querySelector('[data-payroll-employee-search]');
        const employeeTable = document.querySelector('[data-payroll-employee-table]');
        const emptyRow = document.querySelector('[data-payroll-empty-row]');
        if (employeeSearch && employeeTable) {
            const rows = Array.from(employeeTable.querySelectorAll('tr[data-search-text]'));
            employeeSearch.addEventListener('input', () => {
                const keyword = normalizeSearchText(employeeSearch.value.trim());
                let visibleCount = 0;
                rows.forEach((row) => {
                    const matched = normalizeSearchText(row.dataset.searchText).includes(keyword);
                    row.classList.toggle('d-none', !matched);
                    if (matched) {
                        visibleCount += 1;
                    }
                });
                if (emptyRow) {
                    emptyRow.classList.toggle('d-none', visibleCount > 0);
                }
            });
        }
    })();
</script>
<jsp:include page="/components/foot.jsp" />
</body>
</html>
