<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng nhập | ManuHRM</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
        <!-- Google Fonts Preconnect & Load -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
</head>
    <body class="d-flex align-items-center justify-content-center min-vh-100 bg-background text-on-background">
        <main class="w-100 px-3" style="max-width: 440px;">
            <div class="d-flex justify-content-center mb-4">
                <div class="d-flex align-items-center justify-content-center shadow-sm"
                     style="width: 56px; height: 56px; border-radius: 12px; background: var(--primary-gradient);">
                    <span class="material-symbols-outlined text-white" style="font-size: 2rem; font-variation-settings: 'FILL' 1;">factory</span>
                </div>
            </div>

            <section class="card-premium overflow-hidden">
                <div style="height: 4px; background: var(--primary-gradient); width: 100%;"></div>

                <div class="card-body p-4 p-md-5">
                    <div class="text-center mb-4">
                        <h1 class="h2 text-on-surface mb-2">Đăng nhập ManuHRM</h1>
                        <p class="body-md text-on-surface-variant mb-0">Sử dụng username hoặc mã nhân viên để truy cập hệ thống.</p>
                    </div>

                    <jsp:include page="/components/alert.jsp" />

                    <form method="post" action="${pageContext.request.contextPath}/login" autocomplete="on"
                          class="d-flex flex-column gap-3">
                        <div>
                            <label for="identifier" class="form-label label-md text-on-surface mb-1">
                                Username hoặc mã nhân viên
                            </label>
                            <input id="identifier"
                                   name="identifier"
                                   type="text"
                                   class="input-premium"
                                   value="${fn:escapeXml(identifier)}"
                                   placeholder="admin hoặc AD001"
                                   autocomplete="username"
                                   required>
                        </div>

                        <div>
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <label for="password" class="form-label label-md text-on-surface mb-0">Mật khẩu</label>
                                <a href="${pageContext.request.contextPath}/forgot-password"
                                   class="body-sm text-primary text-decoration-none">Quên mật khẩu?</a>
                            </div>
                            <input id="password"
                                   name="password"
                                   type="password"
                                   class="input-premium"
                                   autocomplete="current-password"
                                   required>
                        </div>

                        <button class="btn-primary-gradient w-100 mt-2" type="submit">
                            <span>Đăng nhập</span>
                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">login</span>
                        </button>
                    </form>
                </div>

                <div class="bg-surface-container-low border-top py-3 text-center"
                     style="border-color: var(--outline-variant) !important;">
                    <p class="label-md text-on-surface-variant mb-0 d-flex align-items-center justify-content-center gap-2">
                        <span class="material-symbols-outlined" style="font-size: 1rem;">lock</span>
                        Tài khoản mock data dùng mật khẩu mặc định: 123456
                    </p>
                </div>
            </section>
        </main>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>


