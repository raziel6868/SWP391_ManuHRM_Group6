<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Đổi mật khẩu - ManuHRM</title>
    <jsp:include page="/components/head.jsp" />
</head>
<body class="bg-background text-on-surface">

<c:choose>
<%-- Layout 1: Bắt buộc (centered, fullscreen) --%>
<c:when test="${isRequired}">
    <body class="bg-background text-on-surface d-flex align-items-center justify-content-center" style="min-height: 100vh;">
    <div class="card-premium p-4 p-md-5" style="max-width: 450px; width: 100%; margin: 20px; position: relative; overflow: hidden;">
        <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>

        <div class="text-center mb-4">
            <span class="material-symbols-outlined text-primary mb-2" style="font-size: 3rem;">lock_reset</span>
            <h1 class="h4 fw-bold text-primary">Đổi mật khẩu</h1>
            <p class="body-md text-muted mt-2">
                Xin chào <strong>${authUser.fullName}</strong>! Mật khẩu của bạn đã được đặt lại.
                Vui lòng đổi mật khẩu mới để tiếp tục.
            </p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-error d-flex align-items-center gap-2 mb-3" role="alert">
                <span class="material-symbols-outlined">error</span>
                ${error}
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/change-password" method="POST">
            <div class="mb-3">
                <label class="form-label fw-bold small text-uppercase text-muted">Mật khẩu hiện tại</label>
                <input type="password" class="form-control p-3" name="currentPassword" required
                       placeholder="Nhập mật khẩu hiện tại">
            </div>

            <div class="mb-3">
                <label class="form-label fw-bold small text-uppercase text-muted">Mật khẩu mới</label>
                <input type="password" class="form-control p-3" name="newPassword" required minlength="6"
                       placeholder="Nhập mật khẩu mới (ít nhất 6 ký tự)">
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold small text-uppercase text-muted">Xác nhận mật khẩu mới</label>
                <input type="password" class="form-control p-3" name="confirmPassword" required minlength="6"
                       placeholder="Nhập lại mật khẩu mới">
            </div>

            <button type="submit" class="btn btn-primary-gradient w-100 py-3 text-white fw-bold border-0 shadow-sm" style="border-radius: 4px;">
                <span class="material-symbols-outlined me-2">check</span>
                Đổi mật khẩu
            </button>
        </form>
    </div>

<%-- Layout 2: Tự nguyện (có sidebar) --%>
<c:otherwise>
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 600px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h1 class="h2 text-on-surface fw-bold">Đổi mật khẩu</h1>
                    <p class="body-md text-on-surface-variant mt-1">Đảm bảo tài khoản của bạn sử dụng mật khẩu dài và ngẫu nhiên để bảo mật.</p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        ${error}
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5 position-relative overflow-hidden">
                    <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>

                    <form action="${pageContext.request.contextPath}/change-password" method="POST">
                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Mật khẩu hiện tại</label>
                            <input type="password" class="form-control p-3" name="currentPassword" required placeholder="Nhập mật khẩu hiện tại">
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Mật khẩu mới</label>
                            <input type="password" class="form-control p-3" name="newPassword" required minlength="6" placeholder="Tối thiểu 6 ký tự">
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Xác nhận mật khẩu mới</label>
                            <input type="password" class="form-control p-3" name="confirmPassword" required minlength="6" placeholder="Nhập lại mật khẩu mới">
                        </div>

                        <div class="d-flex justify-content-end gap-2 mt-4">
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-secondary px-4 py-2 fw-bold text-decoration-none">Hủy bỏ</a>
                            <button type="submit" class="btn-primary-gradient border-0 px-4 py-2 text-white fw-bold" style="border-radius: 4px;">Cập nhật mật khẩu</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</c:otherwise>
</c:choose>

<jsp:include page="/components/foot.jsp" />
</body>
</html>
