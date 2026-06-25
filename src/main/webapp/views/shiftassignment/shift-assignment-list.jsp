<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Danh sách phân ca - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Phân công ca</h2>
                        <p class="body-md text-on-surface-variant mb-0">Quản lý phân công ca làm việc.</p>
                    </div>
                    <div class="d-flex gap-2">
<!--                        <a href="${pageContext.request.contextPath}/shift-conflict"
                           class="btn btn-outline-primary px-3 py-2 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">assignment</span>
                            Kiểm tra xung đột
                        </a>-->
                        <a href="${pageContext.request.contextPath}/shift-assignment-assign"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                            Phân công ca
                        </a>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/shift-assignment-list" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>${dept.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Ca</label>
                                <select name="shiftId" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <c:forEach var="shift" items="${shifts}">
                                        <option value="${shift.id}" ${selectedShiftId == shift.id ? 'selected' : ''}>${shift.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Từ ngày</label>
                                <input type="date" name="startDate" value="${selectedStartDate}" class="form-control input-premium" />
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Đến ngày</label>
                                <input type="date" name="endDate" value="${selectedEndDate}" class="form-control input-premium" />
                            </div>
                            <div class="col-md-2 d-flex gap-2">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Ngày</th>
                                    <th>Nhân viên</th>
                                    <th>Mã NV</th>
                                    <th>Phòng ban</th>
                                    <th>Ca</th>
                                    <th>Mã ca</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="sa" items="${assignments}">
                                    <tr>
                                        <td>${sa.date}</td>
                                        <td>${sa.userFullName}</td>
                                        <td>${sa.employeeCode}</td>
                                        <td>${sa.departmentName}</td>
                                        <td>${sa.shiftName}</td>
                                        <td><span class="badge" style="background-color: #e0e7ff; color: #3730a3;">${sa.shiftCode}</span></td>
                                        <td class="text-end">
                                            <a href="${pageContext.request.contextPath}/shift-assignment-assign?userId=${sa.userId}&date=${sa.date}"
                                               class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                               title="Sửa phân ca">
                                                <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty assignments}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-on-surface-variant">
                                            Không tìm thấy phân công ca nào.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
                            <div class="d-flex gap-1 flex-wrap">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${pageContext.request.contextPath}/shift-assignment-list?page=${i}&departmentId=${selectedDepartmentId}&shiftId=${selectedShiftId}&startDate=${selectedStartDate}&endDate=${selectedEndDate}"
                                       class="btn btn-sm ${i == currentPage ? 'fw-bold' : 'btn-light border text-on-surface-variant'}"
                                       style="${i == currentPage ? 'background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant); border: 1px solid var(--primary);' : 'background-color: var(--surface-container-lowest); border-color: var(--outline-variant) !important;'}">
                                        ${i}
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
