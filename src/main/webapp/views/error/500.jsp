<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>500 Lỗi máy chủ - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="d-flex align-items-center justify-content-center min-vh-100 bg-background text-on-background">
    <main class="w-100 text-center d-flex flex-column align-items-center" style="max-width: 800px; padding: 2rem;">

        <div class="mb-4 rounded-circle bg-error-container d-flex align-items-center justify-content-center shadow-sm border" style="width: 96px; height: 96px; border-color: var(--error-container) !important;">
            <span class="material-symbols-outlined text-error" style="font-size: 3rem; font-variation-settings: 'FILL' 1;">engineering</span>
        </div>

        <h1 class="display-4 fw-bold text-on-background mb-3 d-none d-md-block">500 Lỗi máy chủ nội bộ</h1>
        <h1 class="h2 fw-bold text-on-background mb-3 d-md-none">Lỗi 500</h1>

        <p class="body-lg text-on-surface-variant mb-5" style="max-width: 450px;">
            Đã xảy ra lỗi phía máy chủ. Đội ngũ kỹ thuật đã được thông báo và đang xem xét vấn đề.
        </p>

        <div class="d-flex flex-column flex-sm-row align-items-center gap-3">
            <a href="#" class="btn-primary-gradient text-decoration-none w-100 w-sm-auto px-4 py-2 d-flex align-items-center justify-content-center gap-2">
                <span class="material-symbols-outlined" style="font-size: 1.125rem;">support_agent</span>
                Liên hệ quản trị IT
            </a>
            <a href="${pageContext.request.contextPath}/" class="btn btn-light border w-100 w-sm-auto px-4 py-2 d-flex align-items-center justify-content-center gap-2 shadow-sm text-on-surface" style="background-color: var(--surface); border-color: var(--outline) !important;">
                <span class="material-symbols-outlined" style="font-size: 1.125rem;">arrow_back</span>
                Quay lại trang chủ
            </a>
        </div>
    </main>
</body>
</html>
