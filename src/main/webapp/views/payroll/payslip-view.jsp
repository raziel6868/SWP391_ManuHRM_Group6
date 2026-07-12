<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Phiếu lương - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <style>
        @media print {
            .sidebar, .main-header, .page-container > .d-flex:first-child,
            .btn, .no-print { display: none !important; }
            .layout-wrapper { padding: 0 !important; }
            .main-content { margin: 0 !important; padding: 20mm !important; }
            .card-premium { box-shadow: none !important; border: 1px solid #ccc !important; }
        }
    </style>
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

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3 no-print">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Phiếu lương</h2>
                        <p class="body-md text-on-surface-variant mb-0">Chi tiết thu nhập, bảo hiểm, giảm trừ gia cảnh, thuế TNCN và lương thực nhận.</p>
                    </div>
                    <div class="d-flex gap-2">
                        <c:if test="${not empty sheet}">
                            <a href="${pageContext.request.contextPath}/payroll-preview?year=${sheet.year}&month=${sheet.month}"
                               class="btn btn-light border px-3 py-2">
                                <span class="material-symbols-outlined">arrow_back</span>
                                Quay lại
                            </a>
                        </c:if>
                        <button onclick="window.print()" class="btn-primary-gradient px-3 py-2 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">print</span>
                            In phiếu lương
                        </button>
                    </div>
                </div>

                <c:if test="${not empty payslipPeriods}">
                    <div class="card-premium overflow-hidden mb-4 no-print" style="max-width: 760px;">
                        <div class="p-3 bg-surface border-bottom border-outline-variant">
                            <h3 class="h5 fw-bold mb-1">Danh s&aacute;ch k&#7923; l&#432;&#417;ng</h3>
                            <p class="body-sm text-on-surface-variant mb-0">
                                Ch&#7885;n th&aacute;ng l&#432;&#417;ng c&#7847;n xem; danh s&aacute;ch s&#7855;p x&#7871;p t&#7915; m&#7899;i &#273;&#7871;n c&#361;.
                            </p>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-premium mb-0 w-100">
                                <thead>
                                    <tr>
                                        <th>K&#7923; l&#432;&#417;ng</th>
                                        <th>Tr&#7841;ng th&aacute;i</th>
                                        <th class="text-end">L&#432;&#417;ng th&#7921;c nh&#7853;n</th>
                                        <th>Ng&agrave;y c&#7853;p nh&#7853;t</th>
                                        <th class="text-end">Thao t&aacute;c</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="item" items="${payslipPeriods}">
                                        <c:url var="periodUrl" value="/payslip-view">
                                            <c:param name="sheetId" value="${item.monthlySheetId}" />
                                            <c:param name="userId" value="${item.userId}" />
                                        </c:url>
                                        <tr style="${selectedSheetId == item.monthlySheetId ? 'background-color: var(--primary-container);' : ''}">
                                            <td class="fw-medium">Th&aacute;ng ${item.month}/${item.year}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${item.status == 'PAID'}">
                                                        <span class="badge-premium badge-success">&#272;&atilde; thanh to&aacute;n</span>
                                                    </c:when>
                                                    <c:when test="${item.status == 'FINAL'}">
                                                        <span class="badge-premium badge-primary">&#272;&atilde; ch&#7889;t</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge-premium badge-warning">Nh&aacute;p</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-end fw-medium">
                                                <fmt:formatNumber value="${item.netSalary}" pattern="#,##0" /> VND
                                            </td>
                                            <td>
                                                <fmt:formatDate value="${item.generatedAt}" pattern="dd/MM/yyyy" />
                                            </td>
                                            <td class="text-end">
                                                <a href="${periodUrl}" class="btn btn-light border px-3 py-2">
                                                    <span class="material-symbols-outlined" style="font-size: 1rem;">visibility</span>
                                                    Xem
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${noPayslip}">
                        <div class="card-premium p-5 text-center">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--outline);">receipt_long</span>
                            <p class="body-md text-on-surface-variant mt-2">${noPayslipMessage}</p>
                        </div>
                    </c:when>
                    <c:when test="${not empty salary}">
                        <div class="card-premium" style="max-width: 760px;">
                            <div style="background: linear-gradient(135deg, var(--primary) 0%, var(--primary-fixed-dim) 100%); padding: 1.25rem 1.5rem; border-radius: 12px 12px 0 0;">
                                <h4 class="mb-0 fw-bold text-white">PHIẾU LƯƠNG THÁNG ${salary.month}/${salary.year}</h4>
                                <p class="mb-0 text-white-50" style="font-size: 0.875rem;">ManuHRM - Manufacturing HR Management</p>
                            </div>
                            <div class="p-4">
                                <div class="row mb-4">
                                    <div class="col-md-6">
                                        <p class="mb-2 text-on-surface-variant" style="font-size: 0.8125rem;">Mã nhân viên</p>
                                        <p class="mb-3 fw-bold text-on-surface">${salary.employeeCode}</p>
                                        <p class="mb-2 text-on-surface-variant" style="font-size: 0.8125rem;">Tên nhân viên</p>
                                        <p class="mb-3 fw-bold text-on-surface">${salary.userFullName}</p>
                                        <p class="mb-2 text-on-surface-variant" style="font-size: 0.8125rem;">Phòng ban</p>
                                        <p class="mb-0 fw-bold text-on-surface">${salary.departmentName}</p>
                                    </div>
                                    <div class="col-md-6 text-md-end">
                                        <p class="mb-2 text-on-surface-variant" style="font-size: 0.8125rem;">Ngày in</p>
                                        <p class="mb-3 fw-bold text-on-surface"><fmt:formatDate value="${printDate}" pattern="dd/MM/yyyy" /></p>
                                        <p class="mb-2 text-on-surface-variant" style="font-size: 0.8125rem;">Kỳ lương</p>
                                        <p class="mb-0 fw-bold text-on-surface">Tháng ${salary.month}/${salary.year}</p>
                                    </div>
                                </div>

                                <hr class="my-3" style="border-color: var(--outline-variant);">

                                <table class="table table-premium mb-0">
                                    <tbody>
                                        <tr>
                                            <td class="text-on-surface-variant">Lương cơ bản</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.baseSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Ngày công chuẩn</td>
                                            <td class="text-end fw-medium text-on-surface">${salary.standardWorkDays} ngày</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Số ngày làm việc</td>
                                            <td class="text-end fw-medium text-on-surface">${salary.actualWorkDays} ngày</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Nghỉ phép có lương</td>
                                            <td class="text-end fw-medium text-on-surface">${salary.paidLeaveDays} ngày</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Lương ngày công thực tế</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.proratedBaseSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Tiền nghỉ phép có lương</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.paidLeaveSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Tiền làm thêm giờ</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.overtimePay}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Phụ cấp</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.totalAllowances}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-bold text-on-surface">Tổng thu nhập trước khấu trừ</td>
                                            <td class="text-end fw-bold text-on-surface"><fmt:formatNumber value="${salary.grossIncome}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Lương làm căn cứ bảo hiểm</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.insuranceSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Phụ cấp tính bảo hiểm</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.insuranceBasedAllowances}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">BHXH</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.socialInsurance}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">BHYT</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.healthInsurance}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">BHTN</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.unemploymentInsurance}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-bold text-on-surface">Tổng bảo hiểm người lao động đóng</td>
                                            <td class="text-end fw-bold text-on-surface"><fmt:formatNumber value="${salary.employeeInsurance}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Giảm trừ bản thân</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.personalDeduction}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Số người phụ thuộc</td>
                                            <td class="text-end fw-medium text-on-surface">${salary.dependentCount}</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Giảm trừ người phụ thuộc</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.dependentDeduction}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Phụ cấp không tính thuế</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.nonTaxableAllowances}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Tiền OT miễn thuế</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.overtimePay}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Thu nhập chịu thuế</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.taxableIncome}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Thuế TNCN</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.pitTax}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-bold text-on-surface">Tổng khấu trừ</td>
                                            <td class="text-end fw-bold text-on-surface"><fmt:formatNumber value="${salary.deductions}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr style="background-color: var(--primary-container);">
                                            <td class="fw-bold text-on-surface" style="border-bottom: none; padding: 0.875rem 1rem;">Lương thực nhận</td>
                                            <td class="text-end fw-bold text-on-surface" style="border-bottom: none; padding: 0.875rem 1rem; font-size: 1.125rem;"><fmt:formatNumber value="${salary.netSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <div class="card-premium overflow-hidden mt-4" style="max-width: 760px;">
                            <div class="p-3 bg-surface border-bottom border-outline-variant">
                                <h3 class="h5 fw-bold mb-1">Diễn giải công thức lương thực nhận</h3>
                                <p class="body-sm text-on-surface-variant mb-0">
                                    Các bước dưới đây cho biết số thực nhận của phiếu lương được hình thành từ những khoản nào.
                                </p>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-premium mb-0 w-100">
                                    <thead>
                                        <tr>
                                            <th>Chỉ tiêu</th>
                                            <th>Công thức áp dụng</th>
                                            <th class="text-end">Giá trị kỳ này</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td class="fw-medium">Lương ngày công</td>
                                            <td>${salary.actualWorkDays} / ${salary.standardWorkDays} x <fmt:formatNumber value="${salary.baseSalary}" pattern="#,##0" /></td>
                                            <td class="text-end"><fmt:formatNumber value="${salary.proratedBaseSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Lương nghỉ phép</td>
                                            <td>${salary.paidLeaveDays} / ${salary.standardWorkDays} x <fmt:formatNumber value="${salary.baseSalary}" pattern="#,##0" /></td>
                                            <td class="text-end"><fmt:formatNumber value="${salary.paidLeaveSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Tiền OT</td>
                                            <td>${salary.approvedOtHours} giờ x Đơn giá giờ x Hệ số OT của kỳ lương</td>
                                            <td class="text-end"><fmt:formatNumber value="${salary.overtimePay}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Tổng trước khấu trừ</td>
                                            <td><fmt:formatNumber value="${salary.proratedBaseSalary}" pattern="#,##0" /> + <fmt:formatNumber value="${salary.paidLeaveSalary}" pattern="#,##0" /> + <fmt:formatNumber value="${salary.overtimePay}" pattern="#,##0" /> + <fmt:formatNumber value="${salary.totalAllowances}" pattern="#,##0" /></td>
                                            <td class="text-end fw-bold"><fmt:formatNumber value="${salary.grossIncome}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">BH người lao động đóng</td>
                                            <td><fmt:formatNumber value="${salary.socialInsurance}" pattern="#,##0" /> + <fmt:formatNumber value="${salary.healthInsurance}" pattern="#,##0" /> + <fmt:formatNumber value="${salary.unemploymentInsurance}" pattern="#,##0" /></td>
                                            <td class="text-end"><fmt:formatNumber value="${salary.employeeInsurance}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Giảm trừ người phụ thuộc</td>
                                            <td>${salary.dependentCount} người</td>
                                            <td class="text-end"><fmt:formatNumber value="${salary.dependentDeduction}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Thu nhập chịu thuế</td>
                                            <td>Max(0, <fmt:formatNumber value="${salary.grossIncome}" pattern="#,##0" /> - <fmt:formatNumber value="${salary.employeeInsurance}" pattern="#,##0" /> - <fmt:formatNumber value="${salary.personalDeduction}" pattern="#,##0" /> - <fmt:formatNumber value="${salary.dependentDeduction}" pattern="#,##0" /> - <fmt:formatNumber value="${salary.nonTaxableAllowances}" pattern="#,##0" /> - <fmt:formatNumber value="${salary.overtimePay}" pattern="#,##0" />)</td>
                                            <td class="text-end"><fmt:formatNumber value="${salary.taxableIncome}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-medium">Tổng khấu trừ</td>
                                            <td><fmt:formatNumber value="${salary.employeeInsurance}" pattern="#,##0" /> + <fmt:formatNumber value="${salary.pitTax}" pattern="#,##0" /></td>
                                            <td class="text-end fw-bold"><fmt:formatNumber value="${salary.deductions}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr style="background-color: var(--primary-container);">
                                            <td class="fw-bold">Lương thực nhận</td>
                                            <td class="fw-bold"><fmt:formatNumber value="${salary.grossIncome}" pattern="#,##0" /> - <fmt:formatNumber value="${salary.deductions}" pattern="#,##0" /></td>
                                            <td class="text-end fw-bold" style="font-size: 1.05rem;"><fmt:formatNumber value="${salary.netSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:when>
                </c:choose>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
