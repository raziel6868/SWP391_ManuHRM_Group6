<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo hợp đồng | ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
<div class="layout-wrapper">
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <jsp:include page="/components/header.jsp" />

        <main class="page-container">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="h3 text-on-surface fw-bold mb-0">Báo cáo hợp đồng</h2>
                <button onclick="window.print()" class="btn btn-outline-primary">
                    <span class="material-symbols-outlined">print</span> In báo cáo
                </button>
            </div>

            <div class="card-premium shadow-sm mb-4">
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/report-contract" class="row g-3">
                        <div class="col-md-6">
                            <label for="departmentId" class="form-label text-on-surface fw-medium">Phong ban</label>
                            <select id="departmentId" name="departmentId" class="form-select input-premium">
                                <option value="">Tất cả</option>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.id}" ${dept.id == selectedDepartmentId ? 'selected' : ''}>${dept.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6 d-flex align-items-end">
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
                            <p class="body-md text-on-surface-variant mt-2">Không có dữ liệu hợp đồng.</p>
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
                                        <th>Đang hoạt động</th>
                                        <th>Đã hết hạn</th>
                                        <th>Cho gia hạn</th>
                                        <th>Đã chấp dứt</th>
                                        <th>Tổng số</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${rows}">
                                        <tr>
                                            <td>${row.departmentName}</td>
                                            <td>${row.activeContracts}</td>
                                            <td>${row.expiredContracts}</td>
                                            <td>${row.pendingRenewal}</td>
                                            <td>${row.terminatedContracts}</td>
                                            <td>${row.totalContracts}</td>
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
