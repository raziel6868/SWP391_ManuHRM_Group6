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
                    <form method="get" action="${pageContext.request.contextPath}/report-payroll" class="row g-3">
                        <div class="col-md-4">
                            <label for="year" class="form-label text-on-surface fw-medium">Năm</label>
                            <select id="year" name="year" class="form-select input-premium">
                                <c:forEach var="y" begin="2020" end="2030">
                                    <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label for="month" class="form-label text-on-surface fw-medium">Tháng</label>
                            <select id="month" name="month" class="form-select input-premium">
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>${m}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-4 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary-gradient w-100">
                                <span class="material-symbols-outlined">search</span> Xem báo cáo
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <section class="row g-3 mb-4">
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Tổng gross income</p>
                        <p class="h3 fw-bold mb-0"><fmt:formatNumber value="${totals.grossIncome}" pattern="#,##0" /> VND</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Tổng net salary</p>
                        <p class="h3 fw-bold mb-0"><fmt:formatNumber value="${totals.netSalary}" pattern="#,##0" /> VND</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Tổng deductions</p>
                        <p class="h3 fw-bold mb-0"><fmt:formatNumber value="${totals.deductions}" pattern="#,##0" /> VND</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Nhân viên trong kỳ</p>
                        <p class="h3 fw-bold mb-0">${totals.employeeCount}</p>
                    </div>
                </div>
            </section>

            <c:choose>
                <c:when test="${empty rows}">
                    <div class="card-premium shadow-sm">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2">Không có dữ liệu lương. Hãy kiểm tra lương cơ bản và cấu hình payroll cho kỳ đã chọn.</p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <section class="row g-4 mb-4">
                        <div class="col-lg-7">
                            <div class="card-premium p-4 h-100">
                                <h3 class="h5 fw-bold mb-3">Chi phí lương theo phòng ban</h3>
                                <div class="d-flex flex-column gap-3">
                                    <c:forEach var="row" items="${rows}">
                                        <div>
                                            <div class="d-flex justify-content-between mb-1">
                                                <span><c:out value="${row.departmentName}" /></span>
                                                <strong><fmt:formatNumber value="${row.grossIncome}" pattern="#,##0" /> VND</strong>
                                            </div>
                                            <div class="progress">
                                                <div class="progress-bar" style="width: ${row.grossIncome * 100 / maxGrossIncome}%;"></div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-5">
                            <div class="card-premium p-4 h-100">
                                <h3 class="h5 fw-bold mb-3">Cơ cấu chi phí</h3>
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
                                        <div class="d-flex justify-content-between mb-1"><span>Tiền OT</span><strong><fmt:formatNumber value="${totals.totalOtCost}" pattern="#,##0" /></strong></div>
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

                    <div class="card-premium shadow-sm mb-4">
                        <div class="table-responsive">
                            <table class="table table-premium mb-0">
                                <thead>
                                    <tr>
                                        <th>Phòng ban</th>
                                        <th>Số nhân viên</th>
                                        <th>Gross income</th>
                                        <th>Phụ cấp</th>
                                        <th>Tiền OT</th>
                                        <th>Bảo hiểm</th>
                                        <th>Thuế PIT</th>
                                        <th>Net salary</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${rows}">
                                        <tr>
                                            <td><c:out value="${row.departmentName}" /></td>
                                            <td>${row.employeeCount}</td>
                                            <td><fmt:formatNumber value="${row.grossIncome}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${row.totalAllowances}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${row.totalOtCost}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${row.employeeInsurance}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${row.pitTax}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${row.netSalary}" pattern="#,##0" /> VND</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:otherwise>
            </c:choose>

            <div class="card-premium shadow-sm">
                <div class="card-body border-bottom">
                    <h3 class="h5 fw-bold mb-1">Chi tiết lương theo nhân viên</h3>
                    <p class="body-sm text-on-surface-variant mb-0">Các dòng cảnh báo giúp HR kiểm tra dữ liệu trước khi chốt bảng lương.</p>
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
                                        <th>Giờ OT</th>
                                        <th>Gross income</th>
                                        <th>Deductions</th>
                                        <th>Net salary</th>
                                        <th>Cảnh báo</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="employee" items="${employeeRows}">
                                        <tr>
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
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>

<jsp:include page="/components/foot.jsp" />
</body>
</html>
