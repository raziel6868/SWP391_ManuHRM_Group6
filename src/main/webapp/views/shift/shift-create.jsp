<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thêm ca làm việc - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 720px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Thêm ca làm việc mới</h2>
                    <p class="body-md text-on-surface-variant mb-0">Tạo mới danh mục ca làm việc cho hệ thống.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/shift-create" method="POST">
                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mã ca <span class="text-danger">*</span></label>
                                <input type="text" name="code" class="form-control input-premium"
                                       value="${code}" required maxlength="30" pattern="^[A-Z][A-Z0-9_]*$"
                                       placeholder="VD: OFFICE, MORNING, NIGHT" />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Dùng chữ in hoa, số và dấu gạch dưới.
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tên ca <span class="text-danger">*</span></label>
                                <input type="text" name="name" class="form-control input-premium"
                                       value="${name}" required maxlength="100"
                                       placeholder="VD: Ca hành chính" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Giờ bắt đầu <span class="text-danger">*</span></label>
                                <input type="time" name="startTime" class="form-control input-premium"
                                       value="${startTime}" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Giờ kết thúc <span class="text-danger">*</span></label>
                                <input type="time" name="endTime" class="form-control input-premium"
                                       value="${endTime}" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Số phút nghỉ</label>
                                <input type="number" name="breakMinutes" class="form-control input-premium"
                                       value="${not empty breakMinutes ? breakMinutes : '0'}" min="0" max="480"
                                       placeholder="VD: 30, 60" />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Để trống hoặc 0 nếu không có giờ nghỉ.
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại ca</label>
                                <select name="isNightShift" class="form-select input-premium">
                                    <option value="false" ${!isNightShift ? 'selected' : ''}>Ca ngày</option>
                                    <option value="true" ${isNightShift ? 'selected' : ''}>Ca đêm</option>
                                </select>
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                                Thêm ca làm việc
                            </button>
                            <a href="${pageContext.request.contextPath}/shift-list"
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
