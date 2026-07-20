<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
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
            <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
                <div>
                    <h2 class="h3 text-on-surface fw-bold mb-1">Báo cáo hợp đồng</h2>
                    <p class="body-sm text-on-surface-variant mb-0">Theo dõi trạng thái hợp đồng và các hợp đồng sắp hết hạn trong ${expiringDays} ngày.</p>
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
                    <form method="get" action="${pageContext.request.contextPath}/report-contract" class="row g-3">
                        <div class="col-md-8">
                            <label for="departmentId" class="form-label text-on-surface fw-medium">Phòng ban</label>
                            <select id="departmentId" name="departmentId" class="form-select input-premium">
                                <option value="">Tất cả</option>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.id}" ${dept.id == selectedDepartmentId ? 'selected' : ''}>${dept.name}</option>
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
                        <p class="label-md text-on-surface-variant mb-1">Đang hoạt động</p>
                        <p class="h3 fw-bold mb-0">${totals.activeContracts}</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Đã hết hạn</p>
                        <p class="h3 fw-bold mb-0">${totals.expiredContracts}</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Sắp hết hạn ${expiringDays} ngày</p>
                        <p class="h3 fw-bold mb-0">${totals.expiringSoonContracts}</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Chờ gia hạn</p>
                        <p class="h3 fw-bold mb-0">${totals.pendingRenewal}</p>
                    </div>
                </div>
            </section>

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
                    <section class="row g-4 mb-4">
                        <div class="col-lg-5">
                            <div class="card-premium p-4 h-100">
                                <h3 class="h5 fw-bold mb-3">Phân bổ theo trạng thái</h3>
                                <div class="d-flex flex-column gap-3">
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Đang hoạt động</span><strong>${totals.activeContracts}</strong></div>
                                        <div class="progress"><div class="progress-bar" style="width: ${totals.totalContracts == 0 ? 0 : totals.activeContracts * 100 / totals.totalContracts}%;"></div></div>
                                    </div>
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Sắp hết hạn</span><strong>${totals.expiringSoonContracts}</strong></div>
                                        <div class="progress"><div class="progress-bar bg-warning" style="width: ${totals.totalContracts == 0 ? 0 : totals.expiringSoonContracts * 100 / totals.totalContracts}%;"></div></div>
                                    </div>
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Chờ gia hạn</span><strong>${totals.pendingRenewal}</strong></div>
                                        <div class="progress"><div class="progress-bar bg-info" style="width: ${totals.totalContracts == 0 ? 0 : totals.pendingRenewal * 100 / totals.totalContracts}%;"></div></div>
                                    </div>
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Đã hết hạn / chấm dứt</span><strong>${totals.expiredContracts + totals.terminatedContracts}</strong></div>
                                        <div class="progress"><div class="progress-bar bg-danger" style="width: ${totals.totalContracts == 0 ? 0 : (totals.expiredContracts + totals.terminatedContracts) * 100 / totals.totalContracts}%;"></div></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-7">
                            <div class="card-premium p-4 h-100">
                                <h3 class="h5 fw-bold mb-3">Số lượng hợp đồng theo phòng ban</h3>
                                <div class="d-flex flex-column gap-3">
                                    <c:forEach var="row" items="${rows}">
                                        <div>
                                            <div class="d-flex justify-content-between mb-1">
                                                <span><c:out value="${empty row.departmentName ? 'Chưa có phòng ban' : row.departmentName}" /></span>
                                                <strong>${row.totalContracts}</strong>
                                            </div>
                                            <div class="progress">
                                                <div class="progress-bar" style="width: ${row.totalContracts * 100 / maxContracts}%;"></div>
                                            </div>
                                        </div>
                                    </c:forEach>
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
                                        <th>Đang hoạt động</th>
                                        <th>Đã hết hạn</th>
                                        <th>Sắp hết hạn</th>
                                        <th>Chờ gia hạn</th>
                                        <th>Đã chấm dứt</th>
                                        <th>Tổng số</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${rows}">
                                        <tr>
                                            <td><c:out value="${empty row.departmentName ? 'Chưa có phòng ban' : row.departmentName}" /></td>
                                            <td>${row.activeContracts}</td>
                                            <td>${row.expiredContracts}</td>
                                            <td>${row.expiringSoonContracts}</td>
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

            <div class="card-premium shadow-sm">
                <div class="card-body border-bottom">
                    <h3 class="h5 fw-bold mb-1">Hợp đồng sắp hết hạn</h3>
                    <p class="body-sm text-on-surface-variant mb-0">Danh sách hợp đồng còn từ 0 đến ${expiringDays} ngày.</p>
                </div>
                <c:choose>
                    <c:when test="${empty expiringContracts}">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">event_available</span>
                            <p class="body-md text-on-surface-variant mt-2">Không có hợp đồng sắp hết hạn trong kỳ lọc.</p>
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
                                        <th>Loại hợp đồng</th>
                                        <th>Ngày bắt đầu</th>
                                        <th>Ngày kết thúc</th>
                                        <th>Còn lại</th>
                                        <th>Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="contract" items="${expiringContracts}">
                                        <tr>
                                            <td><c:out value="${contract.employeeCode}" /></td>
                                            <td><c:out value="${contract.fullName}" /></td>
                                            <td><c:out value="${empty contract.departmentName ? 'Chưa có phòng ban' : contract.departmentName}" /></td>
                                            <td><c:out value="${contract.contractTypeName}" /></td>
                                            <td><fmt:formatDate value="${contract.startDate}" pattern="dd/MM/yyyy" /></td>
                                            <td><fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy" /></td>
                                            <td>${contract.daysRemaining} ngày</td>
                                            <td><c:out value="${contract.status}" /></td>
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
