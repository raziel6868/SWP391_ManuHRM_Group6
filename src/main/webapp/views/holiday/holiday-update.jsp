<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Sửa Ngày lễ - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <c:if test="${not empty successMsg}">
                    <div class="alert alert-success d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">check_circle</span>
                        ${successMsg}
                    </div>
                </c:if>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        ${errorMsg}
                    </div>
                </c:if>

                <div class="d-flex align-items-center gap-3 mb-4">
                    <a href="${pageContext.request.contextPath}/holiday-list"
                        class="btn btn-sm btn-icon text-on-surface-variant hover-primary">
                        <span class="material-symbols-outlined" style="font-size: 1.25rem;">arrow_back</span>
                    </a>
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Sửa Ngày lễ</h2>
                        <p class="body-md text-on-surface-variant mb-0">Cập nhật thông tin ngày lễ.</p>
                    </div>
                </div>

                <div class="card-premium p-4" style="max-width: 600px;">
                    <form action="${pageContext.request.contextPath}/holiday-update" method="POST">
                        <input type="hidden" name="id" value="${holiday.id}" />

                        <div class="mb-3">
                            <label for="date" class="form-label text-on-surface fw-medium">
                                Ngày <span class="text-danger">*</span>
                            </label>
                            <input type="date" id="date" name="date" value="${holiday.date}" class="input-premium w-100" required />
                        </div>

                        <div class="mb-3">
                            <label for="name" class="form-label text-on-surface fw-medium">
                                Tên ngày lễ <span class="text-danger">*</span>
                            </label>
                            <input type="text" id="name" name="name" value="${holiday.name}" class="input-premium w-100"
                                placeholder="Ví dụ: Tết Dương lịch" maxlength="100" required />
                        </div>

                        <div class="mb-3">
                            <label class="form-label text-on-surface fw-medium">Lặp lại hàng năm</label>
                            <div class="form-check form-switch">
                                <input type="checkbox" id="isRecurring" name="isRecurring" value="true"
                                    class="form-check-input" role="switch"
                                    ${holiday.recurring ? 'checked' : ''} />
                                <label for="isRecurring" class="form-check-label text-on-surface-variant">
                                    Ngày lễ sẽ tự động áp dụng vào cùng ngày mỗi năm
                                </label>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="description" class="form-label text-on-surface fw-medium">Mô tả</label>
                            <textarea id="description" name="description" class="input-premium w-100"
                                rows="3" placeholder="Nhập mô tả (tùy chọn)">${holiday.description}</textarea>
                        </div>

                        <div class="d-flex gap-2">
                            <button type="submit" class="btn-primary-gradient px-4 py-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                Lưu
                            </button>
                            <a href="${pageContext.request.contextPath}/holiday-list" class="btn-secondary px-4 py-2">
                                Hủy
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
