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
    </head>
    <body class="bg-background text-on-background">
        <main class="container-fluid min-vh-100 p-0">
            <div class="row g-0 min-vh-100">
                <section class="col-lg-6 d-none d-lg-flex flex-column justify-content-between p-5 text-white"
                         style="background: var(--primary-gradient);">
                    <div class="d-flex align-items-center gap-3">
                        <div class="d-flex align-items-center justify-content-center fw-bold"
                             style="width: 48px; height: 48px; border: 2px solid rgba(255, 255, 255, 0.8); border-radius: 8px;">
                            M
                        </div>
                        <div>
                            <p class="h4 mb-0 fw-bold text-white">ManuHRM</p>
                            <p class="label-sm mb-0 text-white-50 text-uppercase">Manufacturing Ops</p>
                        </div>
                    </div>

                    <div class="pb-5 mx-auto text-center" style="max-width: 620px;">
                        <h1 class="display-5 fw-bold text-white mb-4">Cổng thông tin nhân sự nội bộ</h1>
                        <p class="body-lg mb-0 text-white-50">
                            Truy cập hồ sơ, thông báo và các chức năng quản lý theo vai trò được cấp trong hệ thống.
                        </p>
                    </div>
                </section>

                <section class="col-12 col-lg-6 d-flex align-items-center justify-content-center p-4 p-md-5">
                    <div class="w-100" style="max-width: 460px;">
                        <div class="d-flex d-lg-none align-items-center justify-content-center gap-3 mb-4">
                            <div class="d-flex align-items-center justify-content-center fw-bold text-white"
                                 style="width: 44px; height: 44px; border-radius: 8px; background: var(--primary-gradient);">
                                M
                            </div>
                            <p class="h4 text-on-surface fw-bold mb-0">ManuHRM</p>
                        </div>

                        <section class="card-premium overflow-hidden">
                            <div style="height: 4px; background: var(--primary-gradient); width: 100%;"></div>

                            <div class="card-body p-4 p-md-5">
                                <div class="mb-4 text-center">
                                    <h1 class="h2 text-on-surface mb-2">Đăng nhập</h1>
                                    <p class="body-md text-on-surface-variant mb-0">
                                        Sử dụng username hoặc mã nhân viên.
                                    </p>
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
                                        <label for="password" class="form-label label-md text-on-surface mb-1">Mật khẩu</label>
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

                                    <div class="text-center mt-1">
                                        <a href="${pageContext.request.contextPath}/forgot-password"
                                           class="body-sm text-primary text-decoration-none fw-semibold">
                                            Quên mật khẩu?
                                        </a>
                                    </div>
                                </form>
                            </div>
                        </section>
                    </div>
                </section>
            </div>
        </main>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
