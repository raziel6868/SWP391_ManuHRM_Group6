<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Danh sách Phân ca - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Danh sách Phân ca</h2>
                        <p class="body-md text-on-surface-variant mb-0">Quản lý phân ca làm việc cho nhân viên.</p>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <c:if test="${canAssign}">
                            <a href="${pageContext.request.contextPath}/shift-assignment-assign"
                               class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                                Phân ca đơn
                            </a>
                        </c:if>
                        <c:if test="${canBulkAssign}">
                            <button type="button" class="btn btn-light border px-3 py-2 d-flex align-items-center gap-2"
                                    data-bs-toggle="modal" data-bs-target="#bulkAssignModal">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">group_add</span>
                                Phân ca hàng loạt
                            </button>
                        </c:if>
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
                                        <option value="${dept.id}" ${filterDepartmentId == dept.id ? 'selected' : ''}>${dept.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Từ ngày</label>
                                <input type="date" name="startDate" class="form-control input-premium" value="${filterStartDate}" />
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Đến ngày</label>
                                <input type="date" name="endDate" class="form-control input-premium" value="${filterEndDate}" />
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Ca</label>
                                <select name="shiftId" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <c:forEach var="shift" items="${shifts}">
                                        <option value="${shift.id}" ${filterShiftId == shift.id ? 'selected' : ''}>${shift.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-1 d-flex gap-2">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                            <div class="col-md-1">
                                <a href="${pageContext.request.contextPath}/shift-assignment-list" class="btn btn-light border w-100">Reset</a>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã NV</th>
                                    <th>Nhân viên</th>
                                    <th>Phòng ban</th>
                                    <th>Ngày</th>
                                    <th>Ca</th>
                                    <th>Giờ</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty assignments}">
                                        <tr>
                                            <td colspan="7" class="text-center py-4 text-on-surface-variant">
                                                Không có dữ liệu phân ca.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="assignment" items="${assignments}">
                                            <tr>
                                                <td class="fw-medium text-on-surface">${assignment.employeeCode}</td>
                                                <td>${assignment.userFullName}</td>
                                                <td>${assignment.departmentName}</td>
                                                <td>${assignment.date}</td>
                                                <td><span class="badge" style="background-color: #dbeafe; color: #1e40af;">${assignment.shiftName}</span></td>
                                                <td>${assignment.shiftStartTime} - ${assignment.shiftEndTime}</td>
                                                <td class="text-end">
                                                    <c:if test="${canAssign}">
                                                        <a href="${pageContext.request.contextPath}/shift-assignment-assign?edit=${assignment.id}"
                                                           class="btn btn-sm btn-icon text-primary hover-primary" title="Sửa">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                        </a>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
                            <div class="d-flex gap-1 flex-wrap">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="?page=${i}&departmentId=${filterDepartmentId}&startDate=${filterStartDate}&endDate=${filterEndDate}&shiftId=${filterShiftId}"
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

            <c:if test="${canBulkAssign}">
            <div class="modal fade" id="bulkAssignModal" tabindex="-1" aria-labelledby="bulkAssignModalLabel" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="bulkAssignModalLabel">Phân ca hàng loạt</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
                        </div>
                        <form action="${pageContext.request.contextPath}/shift-assignment-bulk" method="POST">
                            <div class="modal-body">
                                <div class="mb-3">
                                    <label class="form-label text-on-surface fw-medium mb-1" for="userIds">Nhân viên (ID, cách nhau bởi dấu phẩy)</label>
                                    <textarea id="userIds" name="userIds" class="input-premium form-control" rows="4"
                                              placeholder="1, 2, 3, 5, 6" required></textarea>
                                    <small class="body-sm text-on-surface-variant d-block mt-1">Nhập danh sách ID nhân viên, cách nhau bởi dấu phẩy.</small>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-on-surface fw-medium mb-1" for="shiftSelect">Ca làm việc</label>
                                    <select id="shiftSelect" name="shiftId" class="input-premium form-select" required>
                                        <option value="">Chọn ca</option>
                                        <c:forEach var="shift" items="${shifts}">
                                            <option value="${shift.id}">${shift.name} (${shift.startTime} - ${shift.endTime})</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <label class="form-label text-on-surface fw-medium mb-1" for="bulkDate">Ngày</label>
                                    <input id="bulkDate" type="date" name="date" class="input-premium form-control" required>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-light border" data-bs-dismiss="modal">Hủy</button>
                                <button type="submit" class="btn btn-primary-gradient px-4">Phân ca</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
            </c:if>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
