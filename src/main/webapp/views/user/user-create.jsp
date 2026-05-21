<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thêm nhân viên mới - ManuHRM</title>
    <jsp:include page="/components/head.jsp" />
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />
            <div class="page-container d-flex flex-column" style="max-width: 700px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Thêm nhân viên mới</h2>
                    <p class="body-md text-on-surface-variant mb-0">Điền thông tin nhân viên vào biểu mẫu bên dưới.</p>
                </div>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>
                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/user-create" method="POST">
                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mã Nhân Viên <span class="text-danger">*</span></label>
                                <input type="text" name="employeeCode" class="form-control input-premium"
                                    value="${param.employeeCode}"
                                    required maxlength="20" pattern="^[A-Z0-9]+$"
                                    placeholder="VD: NV001" />
                                <div class="form-text mt-1 text-on-surface-variant">In hoa, không dấu. VD: HR001, CN002</div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tên đăng nhập <span class="text-danger">*</span></label>
                                <input type="text" name="username" class="form-control input-premium"
                                    value="${param.username}"
                                    required maxlength="50" pattern="^[a-z0-9_]+$"
                                    placeholder="VD: nguyen_van_a" />
                                <div class="form-text mt-1 text-on-surface-variant">Chữ thường, số và dấu gạch dưới</div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Họ và Tên <span class="text-danger">*</span></label>
                                <input type="text" name="fullName" class="form-control input-premium"
                                    value="${param.fullName}" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mật khẩu <span class="text-danger">*</span></label>
                                <input type="password" name="password" class="form-control input-premium"
                                    required minlength="6" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Số điện thoại</label>
                                <input type="text" name="phone" class="form-control input-premium"
                                    value="${param.phone}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày sinh</label>
                                <input type="date" name="dob" class="form-control input-premium"
                                    value="${param.dob}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Chức danh</label>
                                <input type="text" name="jobTitle" class="form-control input-premium"
                                    value="${param.jobTitle}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại nhân sự</label>
                                <select name="employeeType" class="form-select input-premium">
                                    <option value="OFFICE">Văn phòng</option>
                                    <option value="WORKER">Công nhân</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">-- Không thuộc phòng ban --</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}">${dept.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Vai trò (Role)</label>
                                <select name="roleId" class="form-select input-premium">
                                    <option value="">-- Chọn vai trò --</option>
                                    <c:forEach var="r" items="${roles}">
                                        <option value="${r.id}">${r.displayName}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Người quản lý</label>
                                <select name="managerId" class="form-select input-premium">
                                    <option value="">-- Không có quản lý --</option>
                                    <c:forEach var="mgr" items="${managers}">
                                        <option value="${mgr.id}">${mgr.fullName} (${mgr.employeeCode})</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">person_add</span>
                                Thêm nhân viên
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
