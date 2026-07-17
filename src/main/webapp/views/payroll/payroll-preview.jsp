<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Bảng lương - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <style>
        .main-content {
            min-width: 0;
        }

        .payroll-preview-page {
            max-width: 100%;
            overflow-x: hidden;
        }

        .payroll-page-heading {
            margin-bottom: 1rem;
        }

        .payroll-page-heading h2 {
            font-size: 1.35rem;
            line-height: 1.25;
        }

        .payroll-page-heading p {
            max-width: 980px;
            font-size: 0.925rem;
            line-height: 1.45;
        }

        .payroll-filter-card {
            margin-bottom: 1rem;
        }

        .payroll-filter-body {
            padding: 0.875rem;
        }

        .payroll-filter-form {
            display: grid;
            grid-template-columns: 140px 140px minmax(260px, 1fr) 110px;
            gap: 0.75rem;
            align-items: end;
        }

        .payroll-filter-form .form-label {
            font-size: 0.8rem;
        }

        .payroll-filter-form .input-premium,
        .payroll-filter-form .btn {
            min-height: 40px;
            padding-top: 0.45rem;
            padding-bottom: 0.45rem;
        }

        .payroll-status-row {
            margin-bottom: 0.875rem;
        }

        .payroll-compact-table th,
        .payroll-compact-table td {
            padding: 0.55rem 0.8rem;
            font-size: 0.85rem;
            line-height: 1.3;
        }

        .payroll-scroll-card {
            max-width: 100%;
            overflow: hidden;
        }

        .payroll-table-scroll {
            max-width: 100%;
            overflow-x: auto;
            overflow-y: visible;
            scrollbar-gutter: stable;
        }

        .payroll-detail-table {
            width: max-content;
            min-width: 2450px;
        }

        .payroll-detail-table th,
        .payroll-detail-table td {
            padding: 0.55rem 0.65rem;
            font-size: 0.78rem;
            line-height: 1.25;
            white-space: nowrap;
        }

        .payroll-detail-table th {
            vertical-align: middle;
        }

        .payroll-detail-table th:nth-child(1),
        .payroll-detail-table td:nth-child(1) {
            position: sticky;
            left: 0;
            z-index: 2;
            width: 86px;
            min-width: 86px;
            background-color: #f1f5ff;
            box-shadow: inset 0 -1px 0 var(--outline-variant);
        }

        .payroll-detail-table th:nth-child(2),
        .payroll-detail-table td:nth-child(2) {
            position: sticky;
            left: 86px;
            z-index: 2;
            width: 140px;
            min-width: 140px;
            background-color: #f1f5ff;
            box-shadow: inset -1px 0 0 var(--outline-variant), inset 0 -1px 0 var(--outline-variant);
            white-space: normal;
        }

        .payroll-detail-table thead th:nth-child(1),
        .payroll-detail-table thead th:nth-child(2) {
            z-index: 3;
            background-color: #e6edff;
        }

        .payroll-detail-table thead th:nth-child(1) {
            box-shadow: inset 0 -1px 0 var(--outline-variant);
        }

        .payroll-detail-table thead th:nth-child(2) {
            box-shadow: inset -1px 0 0 var(--outline-variant), inset 0 -1px 0 var(--outline-variant);
        }

        .payroll-detail-table tbody tr:hover td:nth-child(1),
        .payroll-detail-table tbody tr:hover td:nth-child(2) {
            background-color: #eaf1ff;
        }

        .payroll-formula-table {
            min-width: 900px;
        }

        .payroll-formula-table td {
            white-space: normal;
        }

        @media (max-width: 1199.98px) {
            .payroll-filter-form {
                grid-template-columns: 120px 120px minmax(220px, 1fr) 100px;
            }

        }

        @media (max-width: 767.98px) {
            .payroll-filter-form {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container payroll-preview-page">
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

                <div class="d-flex justify-content-between align-items-end flex-wrap gap-3 payroll-page-heading">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Bảng lương</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Xem trước tổng thu nhập, bảo hiểm, giảm trừ gia cảnh, thu nhập chịu thuế và lương thực nhận theo tháng.
                        </p>
                    </div>
                    <c:if test="${hasGeneratedRows and not empty generatedSheetId}">
                        <a href="${pageContext.request.contextPath}/payslip-view?sheetId=${generatedSheetId}&userId=${generatedSalaries[0].userId}"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">receipt_long</span>
                            Xem phiếu lương
                        </a>
                    </c:if>
                </div>

                <div class="card-premium overflow-hidden payroll-filter-card">
                    <div class="payroll-filter-body bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/payroll-preview" method="GET" class="payroll-filter-form">
                            <div>
                                <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                <select name="year" class="form-select input-premium">
                                    <c:forEach var="y" items="${yearOptions}">
                                        <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng</label>
                                <select name="month" class="form-select input-premium">
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">Tất cả phòng ban</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>
                                            <c:out value="${dept.name}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <button type="submit" class="btn btn-primary w-100">Xem</button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="d-flex flex-wrap gap-2 payroll-status-row">
                    <c:if test="${not empty sheet}">
                        <span class="badge"
                              style="background-color: ${sheet.status == 'CLOSED' ? '#d1fae5' : '#fef3c7'}; color: ${sheet.status == 'CLOSED' ? '#065f46' : '#92400e'};">
                            <span class="material-symbols-outlined" style="font-size: 0.875rem;">
                                ${sheet.status == 'CLOSED' ? 'lock' : 'lock_open'}
                            </span>
                            ${sheet.status == 'CLOSED' ? 'Kỳ tháng đã khóa' : 'Kỳ tháng đang mở'}
                        </span>
                    </c:if>

                    <c:choose>
                        <c:when test="${not hasGeneratedRows}">
                            <span class="badge" style="background-color: #e5e7eb; color: #374151;">
                                <span class="material-symbols-outlined" style="font-size: 0.875rem;">pending_actions</span>
                                Chưa tạo bảng lương
                            </span>
                        </c:when>
                        <c:when test="${hasDraftPayroll}">
                            <span class="badge" style="background-color: #fef3c7; color: #92400e;">
                                <span class="material-symbols-outlined" style="font-size: 0.875rem;">edit_document</span>
                                Đang ở trạng thái nháp
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge" style="background-color: #d1fae5; color: #065f46;">
                                <span class="material-symbols-outlined" style="font-size: 0.875rem;">task_alt</span>
                                Bảng lương đã chốt
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <c:choose>
                    <c:when test="${empty previewRows}">
                        <div class="card-premium p-5 text-center">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--outline);">inbox</span>
                            <h3 class="h5 fw-bold mt-3 mb-1">Chưa có dữ liệu</h3>
                            <p class="body-md text-on-surface-variant mb-0">
                                <c:choose>
                                    <c:when test="${not empty previewUnavailableMsg}">
                                        ${previewUnavailableMsg}
                                    </c:when>
                                    <c:otherwise>
                                        Không có dữ liệu bảng lương cho bộ lọc hiện tại.
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-3">
                            <div class="d-flex flex-column gap-2">
                                <c:if test="${empty sheet}">
                                    <div class="text-on-surface-variant small">
                                        Chưa có bảng công tháng cho kỳ này, nên chưa thể tạo bảng lương.
                                    </div>
                                </c:if>
                                <c:if test="${not empty sheet and not isMonthlySheetClosed and not hasFinalOrPaidPayroll}">
                                    <div class="text-on-surface-variant small">
                                        Cần hoàn tất và đóng sổ bảng công trước khi tạo bảng lương.
                                    </div>
                                </c:if>
                                <c:if test="${false}">
                                    <div class="text-on-surface-variant small">
                                        Kỳ tháng này đang khóa, cần mở lại bảng công trước khi tạo bảng lương.
                                    </div>
                                </c:if>
                                <c:if test="${hasFinalOrPaidPayroll}">
                                    <div class="text-on-surface-variant small">
                                        Bảng lương đã được chốt, hệ thống không cho tạo lại dữ liệu của kỳ này.
                                    </div>
                                </c:if>
                                <c:if test="${hasGeneratedRows and hasDraftPayroll}">
                                    <div class="text-on-surface-variant small">
                                        Dữ liệu lương đã được tạo ở trạng thái nháp. Hãy rà soát trước khi chốt bảng lương.
                                    </div>
                                </c:if>
                            </div>

                            <div class="d-flex gap-2 flex-wrap">
                                <form method="POST" action="${pageContext.request.contextPath}/payroll-generate">
                                    <input type="hidden" name="year" value="${selectedYear}">
                                    <input type="hidden" name="month" value="${selectedMonth}">
                                    <input type="hidden" name="departmentId" value="${selectedDepartmentId}">
                                    <button type="submit"
                                            class="btn-primary-gradient px-4 py-2 d-flex align-items-center gap-2 shadow-sm"
                                            ${canGeneratePayroll ? '' : 'disabled'}
                                            title="${canGeneratePayroll ? 'Tạo hoặc cập nhật bảng lương nháp' : 'Không thể tạo lại bảng lương ở kỳ này'}">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">payments</span>
                                        Tạo bảng lương
                                    </button>
                                </form>

                                <c:if test="${hasGeneratedRows}">
                                    <form method="POST" action="${pageContext.request.contextPath}/payroll-close">
                                        <input type="hidden" name="sheetId" value="${generatedSheetId}">
                                        <input type="hidden" name="year" value="${selectedYear}">
                                        <input type="hidden" name="month" value="${selectedMonth}">
                                        <input type="hidden" name="departmentId" value="${selectedDepartmentId}">
                                        <button type="submit"
                                                class="btn btn-success px-4 py-2 d-flex align-items-center gap-2 shadow-sm"
                                                ${canClosePayroll ? '' : 'disabled'}
                                                title="${canClosePayroll ? 'Chốt toàn bộ dòng lương DRAFT sang FINAL' : 'Chỉ chốt khi kỳ này còn dữ liệu nháp'}">
                                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">task_alt</span>
                                            Chốt bảng lương
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>

                        <div class="card-premium payroll-scroll-card">
                            <div class="payroll-table-scroll">
                                <table class="table table-premium payroll-detail-table mb-0">
                                    <thead>
                                        <tr>
                                            <th>Mã NV</th>
                                            <th>Nhân viên</th>
                                            <th>Phòng ban</th>
                                            <th class="text-end">Lương cơ bản</th>
                                            <th class="text-center">Ngày làm</th>
                                            <th class="text-center">Số ngày phép hưởng lương</th>
                                            <th class="text-end">Lương ngày công</th>
                                            <th class="text-end">Lương nghỉ phép</th>
                                            <th class="text-center">Số giờ OT</th>
                                            <th class="text-end">Tiền OT</th>
                                            <th class="text-end">Phụ cấp</th>
                                            <th class="text-end">Tổng trước khấu trừ</th>
                                            <th class="text-end">Phụ cấp tính BH</th>
                                            <th class="text-end">Lương tính bảo hiểm</th>
                                            <th class="text-end">BHXH</th>
                                            <th class="text-end">BHYT</th>
                                            <th class="text-end">BHTN</th>
                                            <th class="text-end">BH NLĐ đóng</th>
                                            <th class="text-end">Giảm trừ bản thân</th>
                                            <th class="text-center">Người phụ thuộc</th>
                                            <th class="text-end">Giảm trừ NPT</th>
                                            <th class="text-end">Phụ cấp không thuế</th>
                                            <th class="text-end">Thu nhập chịu thuế</th>
                                            <th class="text-end">Thuế TNCN</th>
                                            <th class="text-end">Tổng khấu trừ</th>
                                            <th class="text-end">Thực nhận</th>
                                            <th class="text-center">Trạng thái</th>
                                            <th class="text-end">Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="row" items="${previewRows}">
                                            <c:set var="payrollStatus" value="${generatedStatusByUserId[row.userId]}" />
                                            <tr>
                                                <td class="fw-medium text-on-surface">${row.employeeCode}</td>
                                                <td>${row.userFullName}</td>
                                                <td>${row.departmentName}</td>
                                                <td class="text-end"><fmt:formatNumber value="${row.baseSalary}" pattern="#,##0" /></td>
                                                <td class="text-center">${row.actualWorkDays}</td>
                                                <td class="text-center"><fmt:formatNumber value="${row.paidLeaveDays}" pattern="#,##0.##" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.proratedBaseSalary}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.paidLeaveSalary}" pattern="#,##0" /></td>
                                                <td class="text-center"><fmt:formatNumber value="${row.approvedOtHours}" pattern="#,##0.##" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.overtimePay}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.totalAllowances}" pattern="#,##0" /></td>
                                                <td class="text-end fw-bold"><fmt:formatNumber value="${row.grossIncome}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.insuranceBasedAllowances}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.insuranceSalary}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.socialInsurance}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.healthInsurance}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.unemploymentInsurance}" pattern="#,##0" /></td>
                                                <td class="text-end fw-bold"><fmt:formatNumber value="${row.employeeInsurance}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.personalDeduction}" pattern="#,##0" /></td>
                                                <td class="text-center">${row.dependentCount}</td>
                                                <td class="text-end"><fmt:formatNumber value="${row.dependentDeduction}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.nonTaxableAllowances}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.taxableIncome}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.pitTax}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.deductions}" pattern="#,##0" /></td>
                                                <td class="text-end fw-bold"><fmt:formatNumber value="${row.netSalary}" pattern="#,##0" /></td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${payrollStatus == 'DRAFT'}">
                                                            <span class="badge" style="background-color: #fef3c7; color: #92400e;">DRAFT</span>
                                                        </c:when>
                                                        <c:when test="${payrollStatus == 'FINAL'}">
                                                            <span class="badge" style="background-color: #d1fae5; color: #065f46;">FINAL</span>
                                                        </c:when>
                                                        <c:when test="${payrollStatus == 'PAID'}">
                                                            <span class="badge" style="background-color: #dbeafe; color: #1d4ed8;">PAID</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background-color: #e5e7eb; color: #374151;">Chưa tạo</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end">
                                                    <c:if test="${not empty payrollStatus and not empty generatedSheetId}">
                                                        <a href="${pageContext.request.contextPath}/payslip-view?sheetId=${generatedSheetId}&userId=${row.userId}"
                                                           class="btn btn-sm btn-outline-primary">
                                                            Phiếu lương
                                                        </a>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div class="card-premium overflow-hidden mt-4">
                            <div class="p-3 bg-surface border-bottom border-outline-variant">
                                <h3 class="h5 fw-bold mb-1">Tổng hợp lương</h3>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-premium payroll-compact-table mb-0 w-100">
                                    <thead>
                                        <tr>
                                            <th>Tháng</th>
                                            <th class="text-end">Tổng trước khấu trừ</th>
                                            <th class="text-end">Tổng tiền bảo hiểm NLĐ đóng</th>
                                            <th class="text-end">Tổng thuế thu nhập cá nhân</th>
                                            <th class="text-end">Lương thực nhận</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td class="fw-medium">Tháng ${selectedMonth}/${selectedYear}</td>
                                            <td class="text-end"><fmt:formatNumber value="${summaryStats.totalGrossIncome}" pattern="#,##0" /></td>
                                            <td class="text-end"><fmt:formatNumber value="${summaryStats.totalEmployeeInsurance}" pattern="#,##0" /></td>
                                            <td class="text-end"><fmt:formatNumber value="${summaryStats.totalPitTax}" pattern="#,##0" /></td>
                                            <td class="text-end fw-bold"><fmt:formatNumber value="${summaryStats.totalNetSalary}" pattern="#,##0" /></td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Tháng ${previousMonth}/${previousYear}</td>
                                            <c:choose>
                                                <c:when test="${hasPreviousSummary}">
                                                    <td class="text-end"><fmt:formatNumber value="${previousSummaryStats.totalGrossIncome}" pattern="#,##0" /></td>
                                                    <td class="text-end"><fmt:formatNumber value="${previousSummaryStats.totalEmployeeInsurance}" pattern="#,##0" /></td>
                                                    <td class="text-end"><fmt:formatNumber value="${previousSummaryStats.totalPitTax}" pattern="#,##0" /></td>
                                                    <td class="text-end fw-bold"><fmt:formatNumber value="${previousSummaryStats.totalNetSalary}" pattern="#,##0" /></td>
                                                </c:when>
                                                <c:otherwise>
                                                    <td class="text-center text-on-surface-variant" colspan="4">Chưa có dữ liệu</td>
                                                </c:otherwise>
                                            </c:choose>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div class="card-premium overflow-hidden mt-4">
                            <div class="p-3 bg-surface border-bottom border-outline-variant">
                                <h3 class="h5 fw-bold mb-1">Công thức tính lương đang áp dụng</h3>
                                <p class="body-sm text-on-surface-variant mb-0">
                                    Bảng dưới đây diễn giải cách hệ thống tính ra lương thực nhận từ các cột của payroll preview.
                                </p>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-premium payroll-compact-table payroll-formula-table mb-0 w-100">
                                    <thead>
                                        <tr>
                                            <th>Chỉ tiêu</th>
                                            <th>Công thức</th>
                                            <th>Cột đối chiếu</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td class="fw-medium">Lương ngày công</td>
                                            <td>Ngày làm thực tế / Ngày công chuẩn x Lương cơ bản</td>
                                            <td>Lương cơ bản, Ngày làm, Lương ngày công</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Lương nghỉ phép</td>
                                            <td>Ngày nghỉ phép hưởng lương / Ngày công chuẩn x Lương cơ bản</td>
                                            <td>Số ngày phép hưởng lương, Lương nghỉ phép</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Tiền OT</td>
                                            <td>Giờ OT được duyệt x Đơn giá giờ x Hệ số OT của kỳ lương</td>
                                            <td>Số giờ OT, Tiền OT</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Phụ cấp</td>
                                            <td>Cộng phụ cấp đang hiệu lực; riêng ATTENDANCE_BONUS chỉ cộng khi không có LATE và không có ABSENT không phép trong kỳ</td>
                                            <td>Phụ cấp, Đi muộn, Vắng</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Tổng trước khấu trừ</td>
                                            <td>Lương ngày công + Lương nghỉ phép + Tiền OT + Phụ cấp</td>
                                            <td>Tổng trước khấu trừ</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">BH người lao động đóng</td>
                                            <td>BHXH + BHYT + BHTN</td>
                                            <td>BHXH, BHYT, BHTN, BH NLĐ đóng</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Giảm trừ người phụ thuộc</td>
                                            <td>Số người phụ thuộc x Mức giảm trừ mỗi người phụ thuộc</td>
                                            <td>Người phụ thuộc, Giảm trừ NPT</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Thu nhập chịu thuế</td>
                                            <td>Max(0, Tổng trước khấu trừ - BH NLĐ đóng - Giảm trừ bản thân - Giảm trừ NPT - Phụ cấp không thuế - Tiền OT miễn thuế)</td>
                                            <td>Thu nhập chịu thuế, Phụ cấp không thuế, Tiền OT</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Tổng khấu trừ</td>
                                            <td>BH người lao động đóng + Thuế TNCN</td>
                                            <td>Tổng khấu trừ</td>
                                        </tr>
                                        <tr style="background-color: var(--primary-container);">
                                            <td class="fw-bold">Lương thực nhận</td>
                                            <td class="fw-bold">Tổng trước khấu trừ - Tổng khấu trừ</td>
                                            <td class="fw-bold">Thực nhận</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
