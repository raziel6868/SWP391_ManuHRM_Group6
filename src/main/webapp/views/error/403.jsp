<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>403 Không có quyền truy cập - ManuHRM</title>
    <jsp:include page="/components/head.jsp" />
</head>
<body class="d-flex align-items-center justify-content-center min-vh-100 bg-surface-container-low text-on-surface">
    <main class="w-100 text-center d-flex flex-column align-items-center" style="max-width: 600px; padding: 2rem;">

        <div class="mb-4 rounded-circle bg-error-container d-flex align-items-center justify-content-center" style="width: 96px; height: 96px; box-shadow: var(--shadow-premium);">
            <span class="material-symbols-outlined text-error" style="font-size: 3rem; font-variation-settings: 'FILL' 1;">block</span>
        </div>

        <h1 class="display-4 fw-bold text-on-surface mb-3">403</h1>
        <h2 class="h3 text-on-surface-variant mb-4">Không có quyền truy cập</h2>

        <p class="body-lg text-on-surface-variant mb-5" style="max-width: 480px;">
            Bạn không có quyền xem trang này hoặc thực hiện thao tác này trong ManuHRM. Vui lòng liên hệ quản trị viên nếu bạn cho rằng đây là lỗi.
        </p>

        <a href="${pageContext.request.contextPath}/" class="btn-primary-gradient text-decoration-none px-4 py-3">
            <span class="material-symbols-outlined" style="font-size: 1.25rem;">arrow_back</span>
            Quay lại trang chủ
        </a>
    </main>
</body>
</html>
