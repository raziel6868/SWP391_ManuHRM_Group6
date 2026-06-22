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
                        <p class="body-md text-on-surface-variant mb-0">Xem trước và tạo bảng lương theo tháng.</p>
                    </div>
                    <c:if test="${not empty generatedSalaries && not empty generatedSheetId}">
                        <a href="${pageContext.request.contextPath}/payslip-view?sheetId=${generatedSheetId}&userId=${previewRows[0].userId}"
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

                <c:if test="${not empty sheet}">
                    <div class="mb-3">
                        <span class="badge ${sheet.status == 'CLOSED' ? '' : ''}"
                              style="background-color: ${sheet.status == 'CLOSED' ? '#d1fae5' : '#fef3c7'}; color: ${sheet.status == 'CLOSED' ? '#065f46' : '#92400e'};">
                            <span class="material-symbols-outlined" style="font-size: 0.875rem;">${sheet.status == 'CLOSED' ? 'lock' : 'lock_open'}</span>
                            ${sheet.status == 'CLOSED' ? 'Đã đóng' : 'Đang mở'}
                        </span>
                    </div>
                </c:if>

                <c:choose>
                    <c:when test="${empty previewRows}">
                        <div class="card-premium p-5 text-center">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--outline);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2 mb-0">
                                Không có nhân viên nào có lương cơ sở được thiết lập cho tháng này.
                            </p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="d-flex justify-content-end mb-3">
                            <form method="POST" action="${pageContext.request.contextPath}/payroll-generate">
                                <input type="hidden" name="year" value="${selectedYear}">
                                <input type="hidden" name="month" value="${selectedMonth}">
                                <button type="submit" class="btn-primary-gradient px-4 py-2 d-flex align-items-center gap-2 shadow-sm">
                                    <span class="material-symbols-outlined" style="font-size: 1.125rem;">payments</span>
                                    Tạo bảng lương
                                </button>
                            </form>
                        </div>

                        <div class="card-premium overflow-hidden">
                            <div class="table-responsive">
                                <table class="table table-premium mb-0 w-100">
                                    <thead>
                                        <tr>
                                            <th>Mã NV</th>
                                            <th>Nhân viên</th>
                                            <th>Phòng ban</th>
                                            <th class="text-end">Lương cơ sở</th>
                                            <th class="text-center">Ngày làm</th>
                                            <th class="text-center">Ngày nghỉ</th>
                                            <th class="text-center">Tăng ca (h)</th>
                                            <th class="text-end">Thực lãnh</th>
                                            <th class="text-end">Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="row" items="${previewRows}">
                                            <tr>
                                                <td class="fw-medium text-on-surface">${row.employeeCode}</td>
                                                <td>${row.userFullName}</td>
                                                <td>${row.departmentName}</td>
                                                <td class="text-end"><fmt:formatNumber value="${row.baseSalary}" pattern="#,##0" /></td>
                                                <td class="text-center">${row.actualWorkDays}</td>
                                                <td class="text-center">${row.absentDays}</td>
                                                <td class="text-center">${row.otHours}</td>
                                                <td class="text-end fw-bold"><fmt:formatNumber value="${row.netSalary}" pattern="#,##0" /></td>
                                                <td class="text-end">
                                                    <c:if test="${not empty generatedSalaries && not empty generatedSheetId}">
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
                    </c:otherwise>
                </c:choose>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
