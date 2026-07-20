<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo cáo tăng ca | ManuHRM</title>
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
                    <h2 class="h3 text-on-surface fw-bold mb-1">Báo cáo tăng ca</h2>
                    <p class="body-sm text-on-surface-variant mb-0">Chi phí OT đang ước tính theo ${defaultWorkDays} ngày công, ${defaultHoursPerDay} giờ/ngày, hệ số ${defaultOtRate}.</p>
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
                    <form method="get" action="${pageContext.request.contextPath}/report-overtime" class="row g-3">
                        <div class="col-md-3">
                            <label for="year" class="form-label text-on-surface fw-medium">Năm</label>
                            <select id="year" name="year" class="form-select input-premium">
                                <c:forEach var="y" items="${yearOptions}">
                                    <option value="${y}" ${y == selectedYear ? 'selected' : ''}>${y}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label for="month" class="form-label text-on-surface fw-medium">Tháng</label>
                            <select id="month" name="month" class="form-select input-premium">
                                <option value="">Tất cả</option>
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>${m}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label for="departmentId" class="form-label text-on-surface fw-medium">Phòng ban</label>
                            <select id="departmentId" name="departmentId" class="form-select input-premium">
                                <option value="">Tất cả</option>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.id}" ${dept.id == selectedDepartmentId ? 'selected' : ''}>${dept.name}</option>
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

            <section class="row g-3 mb-4">
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Tổng yêu cầu OT</p>
                        <p class="h3 fw-bold mb-0">${totals.totalRequests}</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Giờ OT đã duyệt</p>
                        <p class="h3 fw-bold mb-0"><fmt:formatNumber value="${totals.totalOtHours}" pattern="#,##0.00" /></p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Yêu cầu đang chờ</p>
                        <p class="h3 fw-bold mb-0">${totals.pendingRequests}</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-premium p-4 h-100">
                        <p class="label-md text-on-surface-variant mb-1">Chi phí OT ước tính</p>
                        <p class="h3 fw-bold mb-0"><fmt:formatNumber value="${totals.totalOtCost}" pattern="#,##0" /> VND</p>
                    </div>
                </div>
            </section>

            <c:choose>
                <c:when test="${empty rows}">
                    <div class="card-premium shadow-sm">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                            <p class="body-md text-on-surface-variant mt-2">Không có dữ liệu tăng ca.</p>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <section class="row g-4 mb-4">
                        <div class="col-lg-7">
                            <div class="card-premium p-4 h-100">
                                <h3 class="h5 fw-bold mb-3">Giờ OT theo phòng ban</h3>
                                <div class="d-flex flex-column gap-3">
                                    <c:forEach var="row" items="${rows}">
                                        <div>
                                            <div class="d-flex justify-content-between mb-1">
                                                <span><c:out value="${empty row.departmentName ? 'Chưa có phòng ban' : row.departmentName}" /></span>
                                                <strong><fmt:formatNumber value="${row.totalOtHours}" pattern="#,##0.00" /> giờ</strong>
                                            </div>
                                            <div class="progress">
                                                <div class="progress-bar" style="width: ${row.totalOtHours * 100 / maxOtHours}%;"></div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                        <div class="col-lg-5">
                            <div class="card-premium p-4 h-100">
                                <h3 class="h5 fw-bold mb-3">Phân bổ trạng thái yêu cầu</h3>
                                <div class="d-flex flex-column gap-3">
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Đã duyệt</span><strong>${totals.approvedRequests}</strong></div>
                                        <div class="progress"><div class="progress-bar" style="width: ${totals.totalRequests == 0 ? 0 : totals.approvedRequests * 100 / totals.totalRequests}%;"></div></div>
                                    </div>
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Đang chờ</span><strong>${totals.pendingRequests}</strong></div>
                                        <div class="progress"><div class="progress-bar bg-warning" style="width: ${totals.totalRequests == 0 ? 0 : totals.pendingRequests * 100 / totals.totalRequests}%;"></div></div>
                                    </div>
                                    <div>
                                        <div class="d-flex justify-content-between mb-1"><span>Đã từ chối</span><strong>${totals.rejectedRequests}</strong></div>
                                        <div class="progress"><div class="progress-bar bg-danger" style="width: ${totals.totalRequests == 0 ? 0 : totals.rejectedRequests * 100 / totals.totalRequests}%;"></div></div>
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
                                        <th>Tổng yêu cầu</th>
                                        <th>Đã duyệt</th>
                                        <th>Đã từ chối</th>
                                        <th>Đang chờ</th>
                                        <th>Giờ OT đã duyệt</th>
                                        <th>Chi phí OT ước tính</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="row" items="${rows}">
                                        <tr>
                                            <td><c:out value="${empty row.departmentName ? 'Chưa có phòng ban' : row.departmentName}" /></td>
                                            <td>${row.totalRequests}</td>
                                            <td>${row.approvedRequests}</td>
                                            <td>${row.rejectedRequests}</td>
                                            <td>${row.pendingRequests}</td>
                                            <td><fmt:formatNumber value="${row.totalOtHours}" pattern="#,##0.00" /></td>
                                            <td><fmt:formatNumber value="${row.totalOtCost}" pattern="#,##0" /> VND</td>
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
                    <h3 class="h5 fw-bold mb-1">Top nhân viên tăng ca</h3>
                    <p class="body-sm text-on-surface-variant mb-0">Chỉ tính yêu cầu đã duyệt và có giờ OT được duyệt.</p>
                </div>
                <c:choose>
                    <c:when test="${empty topEmployees}">
                        <div class="card-body text-center py-5">
                            <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">timer_off</span>
                            <p class="body-md text-on-surface-variant mt-2">Không có nhân viên OT đã duyệt trong kỳ lọc.</p>
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
                                        <th>Giờ OT đã duyệt</th>
                                        <th>Chi phí OT ước tính</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="employee" items="${topEmployees}">
                                        <tr>
                                            <td><c:out value="${employee.employeeCode}" /></td>
                                            <td><c:out value="${employee.fullName}" /></td>
                                            <td><c:out value="${employee.departmentName}" /></td>
                                            <td><fmt:formatNumber value="${employee.totalApprovedHours}" pattern="#,##0.00" /></td>
                                            <td><fmt:formatNumber value="${employee.estimatedOtCost}" pattern="#,##0" /> VND</td>
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
