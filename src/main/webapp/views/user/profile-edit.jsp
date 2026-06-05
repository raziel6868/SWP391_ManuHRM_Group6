<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Chỉnh sửa hồ sơ - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css?v=sidebar-fix-20260605-1349" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">

    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />

        <div class="main-content">
            <jsp:include page="/components/header.jsp" />
            <div class="page-container d-flex flex-column" style="max-width: 700px; margin: 40px auto; width: 100%;">

                <div class="mb-4">
                    <h1 class="h2 text-on-surface fw-bold">Chỉnh sửa hồ sơ cá nhân</h1>
                    <p class="body-md text-on-surface-variant mt-1">Cập nhật thông tin cá nhân của bạn. Thông tin liên quan đến công việc chỉ có thể được sửa bởi HR Admin.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger shadow-sm border-0 mb-4">${errorMsg}</div>
                </c:if>

                <div class="card-premium p-4 p-md-5 mb-4 position-relative overflow-hidden">
                    <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>

                    <form action="${pageContext.request.contextPath}/profile/edit" method="POST">

                        <input type="hidden" name="id" value="${user.id}"/>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Họ và tên</label>
                            <input type="text" class="form-control p-3" name="fullName" value="${user.fullName}" required placeholder="Nhập họ và tên của bạn">
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Số điện thoại</label>
                            <input type="text" class="form-control p-3" name="phone" value="${user.phone}" placeholder="Nhập số điện thoại">
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Ngày sinh</label>
                            <fmt:formatDate var="formattedDob" value="${user.dob}" pattern="yyyy-MM-dd"/>
                            <input type="date" class="form-control p-3" name="dob" value="${formattedDob}">
                        </div>

                        <hr class="my-4 border-outline-variant opacity-25">

                        <div class="bg-surface-container-low p-3 rounded mb-4">
                            <div class="row g-3">
                                <div class="col-6">
                                    <span class="text-muted small">Mã nhân viên:</span> <strong class="font-monospace text-on-surface">${user.employeeCode}</strong>
                                </div>
                                <div class="col-6">
                                    <span class="text-muted small">Tên đăng nhập:</span> <strong class="text-on-surface">${user.username}</strong>
                                </div>
                                <div class="col-6">
                                    <span class="text-muted small">Phòng ban:</span> <strong class="text-on-surface">${user.departmentName}</strong>
                                </div>
                                <div class="col-6">
                                    <span class="text-muted small">Chức danh:</span> <strong class="text-on-surface">${user.jobTitleName}</strong>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-2 mt-4">
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-secondary px-4 py-2 fw-bold text-decoration-none">
                                Hủy bỏ
                            </a>
                            <button type="submit" class="btn-primary-gradient border-0 px-4 py-2 shadow-sm text-white fw-bold" style="border-radius: 4px;">
                                Lưu thay đổi
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
