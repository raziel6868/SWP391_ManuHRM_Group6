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

                <div class="card-premium overflow-hidden mb-4">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/payroll-preview" method="GET" class="row g-3 align-items-end">
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                <select name="year" class="form-select input-premium">
                                    <c:forEach var="y" begin="2020" end="2030">
                                        <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng</label>
                                <select name="month" class="form-select input-premium">
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <button type="submit" class="btn btn-primary w-100">Xem</button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="d-flex flex-wrap gap-2 mb-3">
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
                            <p class="body-md text-on-surface-variant mt-2 mb-0">
                                Không có nhân viên nào có hợp đồng ACTIVE hợp lệ và salary trong kỳ lương này.
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

                        <div class="card-premium overflow-hidden">
                            <div class="table-responsive">
                                <table class="table table-premium mb-0 w-100">
                                    <thead>
                                        <tr>
                                            <th>Mã NV</th>
                                            <th>Nhân viên</th>
                                            <th>Phòng ban</th>
                                            <th class="text-end">Lương cơ bản</th>
                                            <th class="text-center">Ngày làm</th>
                                            <th class="text-center">Nghỉ phép lương</th>
                                            <th class="text-end">Lương ngày công</th>
                                            <th class="text-end">Lương nghỉ phép</th>
                                            <th class="text-center">OT duyệt (h)</th>
                                            <th class="text-end">Tiền OT</th>
                                            <th class="text-end">Phụ cấp hợp lệ</th>
                                            <th class="text-end">Tổng trước khấu trừ</th>
                                            <th class="text-end">Lương đóng BH</th>
                                            <th class="text-end">Phụ cấp tính BH</th>
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
                                                <td class="text-center">${row.paidLeaveDays}</td>
                                                <td class="text-end"><fmt:formatNumber value="${row.proratedBaseSalary}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.paidLeaveSalary}" pattern="#,##0" /></td>
                                                <td class="text-center">${row.approvedOtHours}</td>
                                                <td class="text-end"><fmt:formatNumber value="${row.overtimePay}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.totalAllowances}" pattern="#,##0" /></td>
                                                <td class="text-end fw-bold"><fmt:formatNumber value="${row.grossIncome}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.insuranceSalary}" pattern="#,##0" /></td>
                                                <td class="text-end"><fmt:formatNumber value="${row.insuranceBasedAllowances}" pattern="#,##0" /></td>
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
                                <h3 class="h5 fw-bold mb-1">Công thức tính lương đang áp dụng</h3>
                                <p class="body-sm text-on-surface-variant mb-0">
                                    Bảng dưới đây diễn giải cách hệ thống tính ra lương thực nhận từ các cột của payroll preview.
                                </p>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-premium mb-0 w-100">
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
                                            <td>Nghỉ phép lương, Lương nghỉ phép</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Tiền OT</td>
                                            <td>Giờ OT được duyệt x Đơn giá giờ x Hệ số OT của kỳ lương</td>
                                            <td>OT duyệt (h), Tiền OT</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Phụ cấp hợp lệ</td>
                                            <td>Cộng phụ cấp đang hiệu lực; riêng ATTENDANCE_BONUS chỉ cộng khi không có LATE và không có ABSENT không phép trong kỳ</td>
                                            <td>Phụ cấp, Đi muộn, Vắng</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Tổng trước khấu trừ</td>
                                            <td>Lương ngày công + Lương nghỉ phép + Tiền OT + Phụ cấp hợp lệ</td>
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
