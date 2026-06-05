<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Cập nhật thông tin nhân viên - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css?v=sidebar-fix-20260605-1349" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />
            <div class="page-container d-flex flex-column" style="max-width: 700px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Cập nhật thông tin nhân viên</h2>
                    <p class="body-md text-on-surface-variant mb-0">Chỉnh sửa thông tin nhân viên trong hệ thống.</p>
                </div>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>
                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/user-update" method="POST">
                        <input type="hidden" name="id" value="${user.id}" />
                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mã Nhân Viên</label>
                                <input type="text" class="form-control input-premium"
                                    value="${user.employeeCode}"
                                    disabled style="background-color: var(--surface-container-high);" />
                                <div class="form-text mt-1 text-on-surface-variant">Mã nhân viên không thể thay đổi.</div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tên đăng nhập</label>
                                <input type="text" class="form-control input-premium"
                                    value="${user.username}"
                                    disabled style="background-color: var(--surface-container-high);" />
                                <div class="form-text mt-1 text-on-surface-variant">Tên đăng nhập không thể thay đổi.</div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Họ và Tên <span class="text-danger">*</span></label>
                                <input type="text" name="fullName" class="form-control input-premium"
                                    value="${user.fullName}" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mật khẩu</label>
                                <input type="password" name="password" class="form-control input-premium"
                                    minlength="6" />
                                <div class="form-text mt-1 text-primary">Để trống nếu không muốn đổi mật khẩu.</div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Số điện thoại</label>
                                <input type="text" name="phone" class="form-control input-premium"
                                    value="${user.phone}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày sinh</label>
                                <input type="date" name="dob" class="form-control input-premium"
                                    value="${user.dob}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Chức danh</label>
                                <select name="jobTitleId" class="form-select input-premium">
                                    <option value="">-- Không có chức danh --</option>
                                    <c:forEach var="jt" items="${jobTitles}">
                                        <option value="${jt.id}" ${jt.id == user.jobTitleId ? 'selected' : ''}>${jt.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại nhân sự</label>
                                <select name="employeeType" class="form-select input-premium">
                                    <option value="OFFICE" ${user.employeeType.name() == 'OFFICE' ? 'selected' : ''}>Văn phòng</option>
                                    <option value="WORKER" ${user.employeeType.name() == 'WORKER' ? 'selected' : ''}>Công nhân</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">-- Không thuộc phòng ban --</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${user.departmentId == dept.id ? 'selected' : ''}>${dept.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Vai trò (Role)</label>
                                <select name="roleId" class="form-select input-premium">
                                    <option value="">-- Chọn vai trò --</option>
                                    <c:forEach var="r" items="${roles}">
                                        <option value="${r.id}" ${user.roleId == r.id ? 'selected' : ''}>${r.displayName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Người quản lý</label>
                                <select name="managerId" class="form-select input-premium">
                                    <option value="">-- Không có quản lý --</option>
                                    <c:forEach var="mgr" items="${managers}">
                                        <option value="${mgr.id}" ${user.managerId == mgr.id ? 'selected' : ''}>${mgr.fullName} (${mgr.employeeCode})</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6 d-flex align-items-center mt-4">
                                <div class="form-check form-switch mt-3">
                                    <input class="form-check-input" type="checkbox" role="switch" id="isActive"
                                        name="isActive" ${user.isActive ? 'checked' : ''}>
                                    <label class="form-check-label fw-medium ms-2" for="isActive">Trạng thái hoạt động</label>
                                </div>
                            </div>
                        </div>
                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                Lưu thay đổi
                            </button>
                            <a href="${pageContext.request.contextPath}/user-list"
                                class="btn btn-light border px-4 py-2 d-flex align-items-center gap-2">
                                Hủy bỏ
                            </a>
                        </div>
                    </form>
                </div>
            </div>
            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>
    <jsp:include page="/components/foot.jsp" />
</body>
</html>
