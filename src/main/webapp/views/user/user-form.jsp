<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${not empty user.id ? 'Cập nhật' : 'Thêm'} Người Dùng - ManuHRM</title>
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
            
            <div class="page-container d-flex flex-column" style="max-width: 700px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">${not empty user.id ? 'Cập nhật thông tin nhân viên' : 'Thêm nhân viên mới'}</h2>
                    <p class="body-md text-on-surface-variant mb-0">Điền thông tin chi tiết vào biểu mẫu bên dưới.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/${not empty user.id ? 'user-update' : 'user-create'}" method="POST">
                        <c:if test="${not empty user.id}">
                            <input type="hidden" name="id" value="${user.id}" />
                        </c:if>

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mã Nhân Viên <span class="text-danger">*</span></label>
                                <input type="text" name="employeeCode" class="form-control input-premium" 
                                       value="${not empty user ? user.employeeCode : param.employeeCode}" required maxlength="20"
                                       ${not empty user.id ? readonly style="background-color: var(--surface-container-high);"' : ''} />
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tên đăng nhập <span class="text-danger">*</span></label>
                                <input type="text" name="username" class="form-control input-premium" 
                                       value="${not empty user ? user.username : param.username}" required maxlength="50" pattern="^[a-zA-Z0-9_]+$"
                                       ${not empty user.id ? readonly style="background-color: var(--surface-container-high);"' : ''} />
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Họ và Tên <span class="text-danger">*</span></label>
                                <input type="text" name="fullName" class="form-control input-premium" value="${not empty user ? user.fullName : param.fullName}" required />
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mật khẩu <c:if test="${empty user.id}"><span class="text-danger">*</span></c:if></label>
                                <input type="password" name="password" class="form-control input-premium" ${not empty user.id ? '' : 'required'} minlength="6" />
                                <c:if test="${not empty user.id}">
                                    <div class="form-text mt-1 text-primary">Để trống nếu không muốn đổi</div>
                                </c:if>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Số điện thoại</label>
                                <input type="text" name="phone" class="form-control input-premium" value="${not empty user ? user.phone : param.phone}" />
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày sinh</label>
                                <input type="date" name="dob" class="form-control input-premium" value="${not empty user ? user.dob : param.dob}" />
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Chức danh</label>
                                <input type="text" name="jobTitle" class="form-control input-premium" value="${not empty user ? user.jobTitle : param.jobTitle}" />
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại nhân sự</label>
                                <select name="employeeType" class="form-select input-premium">
                                    <option value="OFFICE" ${(not empty user && user.employeeType.name() == 'OFFICE') ? 'selected' : ''}>Văn phòng (OFFICE)</option>
                                    <option value="WORKER" ${(not empty user && user.employeeType.name() == 'WORKER') ? 'selected' : ''}>Công nhân (WORKER)</option>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">-- Không thuộc phòng ban --</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${(not empty user ? user.departmentId : param.departmentId) == dept.id ? 'selected' : ''}>
                                            ${dept.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Phân quyền (Role)</label>
                                <select name="roleId" class="form-select input-premium">
                                    <option value="">-- Chọn Role --</option>
                                    <c:forEach var="r" items="${roles}">
                                        <option value="${r.id}" ${(not empty user ? user.roleId : param.roleId) == r.id ? 'selected' : ''}>
                                            ${r.displayName}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Người quản lý</label>
                                <select name="managerId" class="form-select input-premium">
                                    <option value="">-- Không có quản lý --</option>
                                    <c:forEach var="mgr" items="${managers}">
                                        <option value="${mgr.id}" ${(not empty user ? user.managerId : param.managerId) == mgr.id ? 'selected' : ''}>
                                            ${mgr.fullName} (${mgr.employeeCode})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6 d-flex align-items-center mt-4">
                                <div class="form-check form-switch mt-3">
                                    <input class="form-check-input" type="checkbox" role="switch" id="isActive" name="isActive" 
                                           ${(empty user || user.isActive) ? checked : }>
                                    <label class="form-check-label fw-medium ms-2" for="isActive">Trạng thái hoạt động</label>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                ${not empty user.id ? 'Lưu cập nhật' : 'Thêm nhân viên'}
                            </button>
                            <a href="${pageContext.request.contextPath}/user-list" class="btn btn-light border px-4 py-2 d-flex align-items-center gap-2">
                                Hủy bỏ
                            </a>
                        </div>
                    </form>
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

