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
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="h3 text-on-surface fw-bold mb-0">Báo cáo lương</h2>
                <button onclick="window.print()" class="btn btn-outline-primary">
                    <span class="material-symbols-outlined">print</span> In báo cáo
                </button>
            </div>

            <div class="card-premium shadow-sm mb-4">
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/report-payroll" class="row g-3">
                        <div class="col-md-3">
                            <label for="year" class="form-label text-on-surface fw-medium">Nam</label>
                            <select id="year" name="year" class="form-select input-premium">
                                <c:forEach var="y" begin="2020" end="2030">
                                    <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label for="month" class="form-label text-on-surface fw-medium">Thang</label>
                            <select id="month" name="month" class="form-select input-premium">
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>${m}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary-gradient w-100">
                                <span class="material-symbols-outlined">search</span> Xem báo cáo
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty rows}">
                    <div class="card-premium shadow-sm">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2">Không có dữ liệu lương.</p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="card-premium shadow-sm">
                        <div class="table-responsive">
                            <table class="table table-premium mb-0">
                                <thead>
                                    <tr>
                                        <th>Phòng ban</th>
                                        <th>Số nhân viên</th>
                                        <th>Tổng lương cơ bản</th>
                                        <th>Lương trung bình</th>
                                        <th>Chi phí tăng ca</th>
                                        <th>Tổng chi phí</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${rows}">
                                        <tr>
                                            <td>${row.departmentName}</td>
                                            <td>${row.employeeCount}</td>
                                            <td><fmt:formatNumber value="${row.totalSalary}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${row.averageSalary}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${row.totalOtCost}" pattern="#,##0" /> VND</td>
                                            <td><fmt:formatNumber value="${row.totalCost}" pattern="#,##0" /> VND</td>
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

<script src="${pageContext.request.contextPath}/assets/js/bootstrap.bundle.min.js"></script>
</body>
</html>
