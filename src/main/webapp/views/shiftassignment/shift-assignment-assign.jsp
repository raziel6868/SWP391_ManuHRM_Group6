<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Phân công ca - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <nav aria-label="breadcrumb" class="mb-3">
                    <ol class="breadcrumb mb-0">
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/shift-assignment-list" class="text-decoration-none">Phân công ca</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Thêm mới</li>
                    </ol>
                </nav>

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

                <div class="d-flex justify-content-between align-items-end mb-4">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Phân công ca</h2>
                        <p class="body-md text-on-surface-variant mb-0">Gán ca làm việc cho nhân viên.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden">
                    <div class="p-4">
                        <form action="${pageContext.request.contextPath}/shift-assignment-assign" method="POST">
                            <div class="row g-4">
                                <div class="col-md-4">
                                    <label class="form-label text-on-surface fw-medium mb-1">
                                        Phòng ban <span class="text-danger">*</span>
                                    </label>
                                    <select id="departmentSelect" class="form-select input-premium">
                                        <option value="">-- Chọn phòng ban --</option>
                                        <c:forEach var="dept" items="${departments}">
                                            <option value="${dept.id}">${dept.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label text-on-surface fw-medium mb-1">
                                        Nhân viên <span class="text-danger">*</span>
                                    </label>
                                    <select name="userId" id="userSelect" class="form-select input-premium" required>
                                        <option value="">-- Chọn nhân viên --</option>
                                    </select>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label text-on-surface fw-medium mb-1">
                                        Ca làm việc <span class="text-danger">*</span>
                                    </label>
                                    <select name="shiftId" class="form-select input-premium" required>
                                        <option value="">-- Chọn ca --</option>
                                        <c:forEach var="shift" items="${shifts}">
                                            <option value="${shift.id}" ${selectedShiftId == shift.id ? 'selected' : ''}>
                                                ${shift.name} (${shift.code})
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label text-on-surface fw-medium mb-1">
                                        Ngày <span class="text-danger">*</span>
                                    </label>
                                    <input type="date" name="date" value="${selectedDate}" class="form-control input-premium" required />
                                </div>
                            </div>

                            <div class="d-flex gap-2 mt-4">
                                <button type="submit" class="btn btn-primary px-4">
                                    <span class="material-symbols-outlined me-1" style="font-size: 1.125rem;">save</span>
                                    Lưu
                                </button>
                                <a href="${pageContext.request.contextPath}/shift-assignment-list" class="btn btn-light border px-4">
                                    Hủy
                                </a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
