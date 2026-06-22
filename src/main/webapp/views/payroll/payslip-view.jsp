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
                        <p class="body-md text-on-surface-variant mb-0">Chi tiết phiếu lương nhân viên.</p>
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

                <c:choose>
                    <c:when test="${noPayslip}">
                        <div class="card-premium p-5 text-center">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--outline);">receipt_long</span>
                            <p class="body-md text-on-surface-variant mt-2">${noPayslipMessage}</p>
                        </div>
                    </c:when>
                    <c:when test="${not empty salary}">
                        <div class="card-premium" style="max-width: 680px;">
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
                                        <p class="mb-3 fw-bold text-on-surface"><fmt:formatDate value="<%=new java.util.Date()%>" pattern="dd/MM/yyyy" /></p>
                                        <p class="mb-2 text-on-surface-variant" style="font-size: 0.8125rem;">Kỳ lương</p>
                                        <p class="mb-0 fw-bold text-on-surface">Tháng ${salary.month}/${salary.year}</p>
                                    </div>
                                </div>

                                <hr class="my-3" style="border-color: var(--outline-variant);">

                                <table class="table table-premium mb-0">
                                    <tbody>
                                        <tr>
                                            <td class="text-on-surface-variant">Số ngày làm việc</td>
                                            <td class="text-end fw-medium text-on-surface">${salary.actualWorkDays} ngày</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Số giờ tăng ca</td>
                                            <td class="text-end fw-medium text-on-surface">${salary.otHours} giờ</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Lương gross</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.grossSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr>
                                            <td class="text-on-surface-variant">Khấu trừ</td>
                                            <td class="text-end fw-medium text-on-surface"><fmt:formatNumber value="${salary.deductions}" pattern="#,##0" /> VND</td>
                                        </tr>
                                        <tr style="background-color: var(--primary-container);">
                                            <td class="fw-bold text-on-surface" style="border-bottom: none; padding: 0.875rem 1rem;">Thực lãnh</td>
                                            <td class="text-end fw-bold text-on-surface" style="border-bottom: none; padding: 0.875rem 1rem; font-size: 1.125rem;"><fmt:formatNumber value="${salary.netSalary}" pattern="#,##0" /> VND</td>
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
