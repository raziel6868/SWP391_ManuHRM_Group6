<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thiết lập ngày nghỉ phép - ManuHRM</title>
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

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Thiết lập ngày nghỉ phép</h2>
                        <p class="body-md text-on-surface-variant mb-0">Thiết lập số ngày nghỉ phép cho nhân viên theo năm.</p>
                    </div>
                    <form method="GET" class="d-flex align-items-center gap-2">
                        <label class="body-sm text-on-surface-variant fw-medium">Năm:</label>
                        <input type="number" name="year" value="${selectedYear}" min="2020" max="2100"
                               class="form-control input-premium" style="width: 120px;" />
                        <button type="submit" class="btn btn-primary">Xem</button>
                    </form>
                </div>

                <div class="card-premium p-4 mb-4">
                    <h3 class="h5 text-on-surface fw-bold mb-3">Thêm / Cập nhật ngày nghỉ phép</h3>
                    <form action="${pageContext.request.contextPath}/leave-balance-setup" method="POST">
                        <input type="hidden" name="year" value="${selectedYear}" />
                        <div class="row g-3 align-items-end">
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Nhân viên <span class="text-danger">*</span></label>
                                <select name="userId" class="form-select input-premium" required>
                                    <option value="">-- Chọn nhân viên --</option>
                                    <c:forEach var="u" items="${users}">
                                        <option value="${u.id}">${u.fullName} (${u.employeeCode})</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại nghỉ <span class="text-danger">*</span></label>
                                <select name="leaveTypeId" class="form-select input-premium" required>
                                    <option value="">-- Chọn loại nghỉ --</option>
                                    <c:forEach var="lt" items="${leaveTypes}">
                                        <option value="${lt.id}">${lt.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Tổng ngày <span class="text-danger">*</span></label>
                                <input type="number" name="totalDays" step="0.5" min="0" max="365"
                                       class="form-control input-premium" placeholder="VD: 12" required />
                            </div>
                            <div class="col-md-2">
                                <button type="submit" class="btn btn-primary w-100 d-flex align-items-center justify-content-center gap-2">
                                    <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                    Lưu
                                </button>
                            </div>
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
