<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Quên mật khẩu - ManuHRM</title>
    <jsp:include page="/components/head.jsp" />
</head>
<body class="bg-background text-on-surface d-flex align-items-center justify-content-center" style="min-height: 100vh;">

    <div class="card-premium p-4 p-md-5" style="max-width: 450px; width: 100%; margin: 20px; position: relative; overflow: hidden;">
        <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>

        <div class="text-center mb-4">
            <h1 class="h3 fw-bold text-primary">Khôi phục mật khẩu</h1>
            <p class="body-md text-muted mt-2">Nhập mã nhân viên hoặc tên tài khoản để gửi yêu cầu đặt lại mật khẩu đến Ban Quản trị HR.</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger border-0 small shadow-sm mb-3">${error}</div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success border-0 small shadow-sm mb-3">${success}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/forgot-password" method="POST">
            <div class="mb-4">
                <label class="form-label fw-bold small text-uppercase text-muted">Mã nhân viên / Tài khoản</label>
                <input type="text" class="form-control p-3 font-monospace" name="employeeCode" required placeholder="VD: NV001 hoặc admin">
            </div>

            <button type="submit" class="btn btn-primary-gradient w-100 py-3 text-white fw-bold border-0 shadow-sm mb-3" style="border-radius: 4px;">
                Gửi yêu cầu
            </button>

            <div class="text-center">
                <a href="${pageContext.request.contextPath}/login" class="text-decoration-none small fw-bold text-secondary">
                    Quay lại đăng nhập
                </a>
            </div>
        </form>
    </div>

</body>
</html>
