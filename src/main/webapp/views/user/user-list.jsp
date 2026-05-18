<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Quản lý nhân viên - ManuHRM</title>
        <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"/>
    <!-- Google Fonts Preconnect & Load -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">

    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />
            
            <div class="page-container">
                <!-- Page Header -->
                <div class="d-flex justify-content-between align-items-end mb-4">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý nhân sự</h2>
                        <p class="body-md text-on-surface-variant mb-0">Danh sách nhân viên (${totalRecords} người)</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/user-create" class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">person_add</span>
                        Thêm nhân viên
                    </a>
                </div>
                
                <!-- Data Table Container -->
                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <!-- Table Controls -->
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/user-list" method="GET" class="row g-3 align-items-center">
                            <div class="col-md-3 position-relative">
                                <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant" style="left: 12px; font-size: 1.25rem;">search</span>
                                <input type="text" name="keyword" value="${keyword}" class="input-premium w-100 py-1" placeholder="Tìm mã NV, tên..." style="padding-left: 2.5rem;"/>
                            </div>
                            
                            <div class="col-md-2">
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">-- Phòng ban --</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>
                                            ${dept.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            
                            <div class="col-md-2">
                                <select name="roleId" class="form-select input-premium">
                                    <option value="">-- Vai trò --</option>
                                    <c:forEach var="role" items="${roles}">
                                        <option value="${role.id}" ${selectedRoleId == role.id ? 'selected' : ''}>
                                            ${role.displayName}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            
                            <div class="col-md-2">
                                <select name="isActive" class="form-select input-premium">
                                    <option value="">-- Trạng thái --</option>
                                    <option value="1" ${selectedStatus == '1' ? 'selected' : ''}>Đang làm</option>
                                    <option value="0" ${selectedStatus == '0' ? 'selected' : ''}>Đã nghỉ</option>
                                </select>
                            </div>

                            <div class="col-md-3 d-flex gap-2">
                                <button type="submit" class="btn btn-primary px-3 flex-grow-1">Tìm kiếm</button>
                                <a href="${pageContext.request.contextPath}/user-list" class="btn btn-light border">Reset</a>
                            </div>
                        </form>
                    </div>
                    
                    <!-- Table -->
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã NV</th>
                                    <th>Họ tên</th>
                                    <th>Username</th>
                                    <th>Chức danh</th>
                                    <th>Phòng ban</th>
                                    <th>Loại</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty users}">
                                        <tr>
                                            <td colspan="8" class="text-center py-4 text-on-surface-variant">Không tìm thấy nhân viên nào.</td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="u" items="${users}">
                                            <tr <c:if test="${!u.isActive}">style="background-color: rgba(255,255,255,0.5); opacity: 0.8;"</c:if>>
                                                <td class="fw-medium text-on-surface">${u.employeeCode}</td>
                                                <td class="fw-bold">${u.fullName}</td>
                                                <td>${u.username}</td>
                                                <td class="text-on-surface-variant">${u.jobTitle}</td>
                                                <td>${u.departmentName}</td>
                                                <td>
                                                    <span class="badge border bg-surface-container-high text-on-surface">
                                                        ${u.employeeType}
                                                    </span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${u.isActive}">
                                                            <span class="badge" style="background-color: #d1fae5; color: #065f46;">Active</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Inactive</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end">
                                                    <div class="d-flex justify-content-end gap-1">
                                                        <a href="${pageContext.request.contextPath}/user-detail?id=${u.id}" class="btn btn-sm btn-icon text-on-surface-variant hover-primary" title="Xem chi tiết">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">visibility</span>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/user-update?id=${u.id}" class="btn btn-sm btn-icon text-on-surface-variant hover-primary" title="Chỉnh sửa">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                        </a>
                                                        <form method="post" action="${pageContext.request.contextPath}/user-status" class="d-inline m-0">
                                                            <input type="hidden" name="id" value="${u.id}" />
                                                            <input type="hidden" name="referer" value="list" />
                                                            <input type="hidden" name="isActive" value="${!u.isActive}" />
                                                            <button type="submit" class="btn btn-sm btn-icon text-on-surface-variant hover-primary" title="${u.isActive ? 'Khóa tài khoản' : 'Mở tài khoản'}" onclick="return confirm('${u.isActive ? 'Khóa' : 'Mở'} tài khoản ${u.fullName}?')">
                                                                <span class="material-symbols-outlined" style="font-size: 1.25rem;">${u.isActive ? 'lock' : 'lock_open'}</span>
                                                            </button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-between">
                            <span class="body-sm text-on-surface-variant">Trang ${currentPage} / ${totalPages}</span>
                            <div class="d-flex gap-1">
                                <c:if test="${currentPage > 1}">
                                    <a href="${pageContext.request.contextPath}/user-list?page=${currentPage - 1}&keyword=${keyword}&departmentId=${selectedDepartmentId}&roleId=${selectedRoleId}&isActive=${selectedStatus}&employeeType=${selectedEmployeeType}" class="btn btn-sm btn-light border text-on-surface-variant">«</a>
                                </c:if>
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${pageContext.request.contextPath}/user-list?page=${i}&keyword=${keyword}&departmentId=${selectedDepartmentId}&roleId=${selectedRoleId}&isActive=${selectedStatus}&employeeType=${selectedEmployeeType}" 
                                       class="btn btn-sm ${i == currentPage ? 'fw-bold' : 'btn-light border text-on-surface-variant'}" 
                                       style="${i == currentPage ? 'background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant); border: 1px solid var(--primary);' : 'background-color: var(--surface-container-lowest); border-color: var(--outline-variant) !important;'}">
                                        ${i}
                                    </a>
                                </c:forEach>
                                <c:if test="${currentPage < totalPages}">
                                    <a href="${pageContext.request.contextPath}/user-list?page=${currentPage + 1}&keyword=${keyword}&departmentId=${selectedDepartmentId}&roleId=${selectedRoleId}&isActive=${selectedStatus}&employeeType=${selectedEmployeeType}" class="btn btn-sm btn-light border text-on-surface-variant">»</a>
                                </c:if>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
            
            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>


