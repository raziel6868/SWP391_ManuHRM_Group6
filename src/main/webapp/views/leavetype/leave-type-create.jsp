<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thêm loại nghỉ - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 720px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Thêm loại nghỉ mới</h2>
                    <p class="body-md text-on-surface-variant mb-0">Tạo mới danh mục loại nghỉ phép cho hệ thống.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/leave-type-create" method="POST">
                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mã loại nghỉ <span class="text-danger">*</span></label>
                                <input type="text" name="code" class="form-control input-premium"
                                       value="${code}" required maxlength="30" pattern="^[A-Z][A-Z0-9_]*$"
                                       placeholder="VD: ANNUAL, SICK, UNPAID" />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Dùng chữ in hoa, số và dấu gạch dưới.
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tên loại nghỉ <span class="text-danger">*</span></label>
                                <input type="text" name="name" class="form-control input-premium"
                                       value="${name}" required maxlength="100"
                                       placeholder="VD: Nghỉ phép năm" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Hưởng lương</label>
                                <select name="isPaid" class="form-select input-premium">
                                    <option value="true" ${isPaid ? 'selected' : ''}>Có hưởng lương</option>
                                    <option value="false" ${!isPaid ? 'selected' : ''}>Không hưởng lương</option>
                                </select>
                            </div>
                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Mô tả</label>
                                <textarea name="description" class="form-control input-premium"
                                          rows="4" placeholder="Nhập mô tả loại nghỉ nếu có...">${description}</textarea>
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                                Thêm loại nghỉ
                            </button>
                            <a href="${pageContext.request.contextPath}/leave-type-list"
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
