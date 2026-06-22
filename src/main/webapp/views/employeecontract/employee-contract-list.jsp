<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý Hợp đồng - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý Hợp đồng</h2>
                        <p class="body-md text-on-surface-variant mb-0">Danh sách hợp đồng lao động của nhân viên.</p>
                    </div>
                    <c:if test="${canCreate}">
                        <a href="${pageContext.request.contextPath}/contract-create"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                            Thêm hợp đồng
                        </a>
                    </c:if>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/contract-list" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Từ khóa</label>
                                <div class="position-relative">
                                    <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                          style="left: 12px; font-size: 1.25rem;">search</span>
                                    <input type="text" name="keyword" value="${keyword}"
                                           class="form-control input-premium ps-5"
                                           placeholder="Tìm theo tên, mã NV, loại hợp đồng..." />
                                </div>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Trạng thái</label>
                                <select name="status" class="form-select input-premium">
                                    <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả</option>
                                    <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Hoạt động</option>
                                    <option value="EXPIRED" ${selectedStatus == 'EXPIRED' ? 'selected' : ''}>Hết hạn</option>
                                    <option value="PENDING_RENEWAL" ${selectedStatus == 'PENDING_RENEWAL' ? 'selected' : ''}>Chờ gia hạn</option>
                                    <option value="TERMINATED" ${selectedStatus == 'TERMINATED' ? 'selected' : ''}>Đã chấm dứt</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>${dept.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-1 d-flex gap-2">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                            <div class="col-md-1">
                                <a href="${pageContext.request.contextPath}/contract-list"
                                   class="btn btn-light border w-100">Reset</a>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã NV</th>
                                    <th>Nhân viên</th>
                                    <th>Loại hợp đồng</th>
                                    <th>Ngày bắt đầu</th>
                                    <th>Ngày kết thúc</th>
                                    <th>Mức lương</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="contract" items="${contracts}">
                                    <tr>
                                        <td class="fw-medium text-on-surface">${contract.employeeCode}</td>
                                        <td>${contract.userFullName}</td>
                                        <td>${contract.contractTypeName}</td>
                                        <td>${contract.startDate}</td>
                                        <td>${contract.endDate != null ? contract.endDate : 'Không giới hạn'}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${contract.salary != null}">
                                                    ${contract.salary} VNĐ
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${contract.status == 'ACTIVE'}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Hoạt động</span>
                                                </c:when>
                                                <c:when test="${contract.status == 'EXPIRED'}">
                                                    <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Hết hạn</span>
                                                </c:when>
                                                <c:when test="${contract.status == 'PENDING_RENEWAL'}">
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">Chờ gia hạn</span>
                                                </c:when>
                                                <c:when test="${contract.status == 'TERMINATED'}">
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Đã chấm dứt</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-flex justify-content-end gap-1">
                                                <a href="${pageContext.request.contextPath}/contract-detail?id=${contract.id}"
                                                   class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                   title="Xem chi tiết">
                                                    <span class="material-symbols-outlined" style="font-size: 1.25rem;">visibility</span>
                                                </a>
                                                <c:if test="${canUpdate}">
                                                    <a href="${pageContext.request.contextPath}/contract-update?id=${contract.id}"
                                                       class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                       title="Sửa hợp đồng">
                                                        <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                    </a>
                                                    <c:if test="${contract.status == 'ACTIVE'}">
                                                        <a href="${pageContext.request.contextPath}/contract-terminate?id=${contract.id}"
                                                           class="btn btn-sm btn-icon text-on-surface-variant hover-danger"
                                                           title="Chấm dứt hợp đồng">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">cancel</span>
                                                        </a>
                                                    </c:if>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty contracts}">
                                    <tr>
                                        <td colspan="8" class="text-center py-4 text-on-surface-variant">
                                            Không tìm thấy hợp đồng nào.
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
                                    <a href="${pageContext.request.contextPath}/contract-list?page=${i}&keyword=${keyword}&status=${selectedStatus}&departmentId=${selectedDepartmentId}"
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
