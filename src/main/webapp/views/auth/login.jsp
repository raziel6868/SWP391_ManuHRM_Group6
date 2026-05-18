<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập | ManuHRM</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
</head>
<body>
<main class="login-page">
    <section class="login-visual" aria-label="ManuHRM">
        <div class="brand">
            <span class="brand-mark">M</span>
            <span>ManuHRM</span>
        </div>
        <div>
            <h1>Cổng thông tin nhân sự nội bộ</h1>
            <p>Truy cập hồ sơ, thông báo và các chức năng quản lý theo vai trò được cấp trong hệ thống.</p>
        </div>
        <p>Manufacturing Human Resource Management</p>
    </section>

    <section class="login-panel">
        <form class="login-card" method="post" action="${pageContext.request.contextPath}/login" autocomplete="on">
            <h2>Đăng nhập</h2>
            <p class="subtext">Sử dụng username hoặc mã nhân viên.</p>

            <c:if test="${not empty error}">
                <div class="alert alert-error"><c:out value="${error}" /></div>
            </c:if>

            <c:if test="${param.loggedOut == '1'}">
                <div class="alert alert-success">Bạn đã đăng xuất khỏi hệ thống.</div>
            </c:if>

            <div class="form-field">
                <label for="identifier">Username hoặc mã nhân viên</label>
                <input id="identifier"
                       name="identifier"
                       type="text"
                       value="${fn:escapeXml(identifier)}"
                       placeholder="admin hoặc AD001"
                       autocomplete="username"
                       required>
            </div>

            <div class="form-field">
                <label for="password">Mật khẩu</label>
                <input id="password"
                       name="password"
                       type="password"
                       placeholder="••••••"
                       autocomplete="current-password"
                       required>
            </div>

            <button class="primary-button" type="submit">Đăng nhập</button>
            <p class="login-meta">Tài khoản mock data dùng mật khẩu mặc định: 123456.</p>
        </form>
    </section>
</main>
</body>
</html>
