<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>Cập nhật thông tin Vai trò - ManuHRM</title>
            <!-- Bootstrap 5 CSS -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Custom CSS -->
            <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet" />
            <!-- Google Fonts Preconnect & Load -->
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
                rel="stylesheet">
            <link
                href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"
                rel="stylesheet">
        </head>

        <body class="bg-background text-on-surface">

            <div class="layout-wrapper">
                <jsp:include page="/components/sidebar.jsp" />

                <div class="main-content">
                    <jsp:include page="/components/header.jsp" />

                    <div class="page-container d-flex flex-column"
                        style="max-width: 700px; margin: 40px auto; width: 100%;">
                        <div class="mb-4">
                            <h2 class="h3 text-on-surface fw-bold mb-1">Cập Nhật Thông Tin Vai Trò</h2>
                            <p class="body-md text-on-surface-variant mb-0">Thay đổi thông tin chi tiết của vai trò trên
                                hệ thống.</p>
                        </div>

                        <c:if test="${param.msg == 'failed'}">
                            <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                                <span class="material-symbols-outlined">error</span>
                                <div>Đã có lỗi xảy ra. Vui lòng kiểm tra lại dữ liệu!</div>
                            </div>
                        </c:if>

                        <div class="card-premium p-4 p-md-5">
                            <form action="${pageContext.request.contextPath}/role-update" method="POST">
                                <input type="hidden" name="id" value="${role.id}" />

                                <div class="row g-4 mb-4">
                                    <div class="col-12">
                                        <label class="form-label text-on-surface fw-medium mb-1">Mã hệ thống (Không cho
                                            phép sửa)</label>
                                        <input type="text" class="form-control input-premium" value="${role.name}"
                                            disabled style="background-color: var(--surface-container-high);" />
                                    </div>

                                    <div class="col-12">
                                        <label class="form-label text-on-surface fw-medium mb-1">Tên hiển thị <span
                                                class="text-danger">*</span></label>
                                        <input type="text" name="displayName" class="form-control input-premium"
                                            value="${role.displayName}" required maxlength="100" />
                                    </div>

                                    <div class="col-12">
                                        <label class="form-label text-on-surface fw-medium mb-1">Mô tả chi tiết</label>
                                        <textarea name="description" class="form-control input-premium"
                                            rows="4">${role.description}</textarea>
                                    </div>
                                </div>

                                <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                                    <button type="submit"
                                        class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                        Lưu thay đổi
                                    </button>
                                    <a href="${pageContext.request.contextPath}/role-list"
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

            <!-- Bootstrap JS -->
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
            <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
        </body>

        </html>