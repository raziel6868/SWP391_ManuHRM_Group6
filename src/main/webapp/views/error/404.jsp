<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>404 Không tìm thấy - ManuHRM</title>
    <jsp:include page="/components/head.jsp" />
</head>
<body class="d-flex align-items-center justify-content-center min-vh-100 bg-background text-on-background">
    <main class="w-100 text-center d-flex flex-column align-items-center" style="max-width: 600px; padding: 2rem;">

        <div class="position-relative d-flex align-items-center justify-content-center mb-5 rounded-circle bg-surface-container shadow-sm" style="width: 128px; height: 128px; border: 1px solid var(--outline-variant);">
            <div class="position-absolute rounded-circle" style="inset: 8px; border: 1px dashed rgba(195, 198, 215, 0.5);"></div>
            <span class="material-symbols-outlined text-primary" style="font-size: 4rem; font-variation-settings: 'FILL' 0;">troubleshoot</span>
        </div>

        <div class="mb-3 d-flex flex-column align-items-center">
            <span class="label-md text-outline mb-2" style="letter-spacing: 0.2em;">LỖI HỆ THỐNG: 404</span>
            <h1 class="display-4 fw-bolder text-on-background mb-0">Không tìm thấy trang</h1>
        </div>

        <div class="mb-4 rounded-pill" style="width: 64px; height: 4px; background-color: rgba(195, 198, 215, 0.3);"></div>

        <p class="body-lg text-on-surface-variant mb-5" style="max-width: 450px;">
            Trang bạn đang tìm kiếm có thể đã bị xóa, đổi tên hoặc tạm thời không khả dụng trong hệ thống.
        </p>

        <a href="${pageContext.request.contextPath}/" class="btn-primary-gradient text-decoration-none px-4 py-3">
            <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;">dashboard</span>
            Quay lại trang chủ
        </a>

        <div class="mt-5 pt-4 border-top w-100 mx-auto" style="max-width: 320px; border-color: rgba(195, 198, 215, 0.3) !important;">
            <a href="#" class="d-inline-flex align-items-center gap-2 text-secondary text-decoration-none body-md">
                <span class="material-symbols-outlined" style="font-size: 1rem;">help</span>
                Liên hệ hỗ trợ IT
            </a>
        </div>
    </main>
</body>
</html>
