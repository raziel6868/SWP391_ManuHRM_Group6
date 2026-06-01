<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thêm loại hợp đồng - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 600px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Thêm loại hợp đồng</h2>
                    <p class="body-md text-on-surface-variant mb-0">Tạo danh mục loại hợp đồng lao động mới.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/contract-type-create" method="POST">
                        <div class="row g-4 mb-4">
                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Mã loại hợp đồng <span class="text-danger">*</span></label>
                                <input type="text" name="code" class="form-control input-premium"
                                    value="${not empty code ? code : param.code}" required maxlength="30"
                                    pattern="^[A-Z][A-Z0-9_]*$"
                                    placeholder="VD: PROBATION, FULL_TIME, SEASONAL" />
                                <div class="form-text mt-1 text-on-surface-variant">Chữ in hoa, không dấu, dùng dấu gạch dưới. VD: FULL_TIME</div>
                            </div>
                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Tên loại hợp đồng <span class="text-danger">*</span></label>
                                <input type="text" name="name" class="form-control input-premium"
                                    value="${not empty name ? name : param.name}" required maxlength="100"
                                    placeholder="VD: Hợp đồng chính thức" />
                            </div>
                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Mô tả</label>
                                <textarea name="description" class="form-control input-premium"
                                    rows="4" maxlength="500"
                                    placeholder="Mô tả ngắn về loại hợp đồng...">${not empty description ? description : param.description}</textarea>
                                <div class="form-text mt-1 text-on-surface-variant">Không bắt buộc.</div>
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                                Thêm loại hợp đồng
                            </button>
                            <a href="${pageContext.request.contextPath}/contract-type-list"
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

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
