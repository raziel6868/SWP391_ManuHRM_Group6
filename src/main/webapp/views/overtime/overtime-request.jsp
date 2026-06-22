<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Tạo yêu cầu tăng ca - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 720px; margin: 40px auto; width: 100%;">
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        ${errorMsg}
                    </div>
                </c:if>
                <c:if test="${not empty successMsg}">
                    <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">check_circle</span>
                        ${successMsg}
                    </div>
                </c:if>

                <div class="d-flex align-items-center gap-3 mb-4">
                    <a href="${pageContext.request.contextPath}/overtime-list"
                       class="btn btn-light border px-2 py-2 d-flex align-items-center justify-content-center">
                        <span class="material-symbols-outlined">arrow_back</span>
                    </a>
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Tạo yêu cầu tăng ca</h2>
                        <p class="body-md text-on-surface-variant mb-0">Tạo yêu cầu tăng ca cho nhân viên.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden">
                    <div class="p-4">
                        <form action="${pageContext.request.contextPath}/overtime-request" method="POST" class="row g-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Nhân viên <span class="text-danger">*</span></label>
                                <select name="userId" class="form-select input-premium" required>
                                    <option value="">-- Chọn nhân viên --</option>
                                    <c:forEach var="emp" items="${users}">
                                        <option value="${emp.id}">${emp.fullName} (${emp.employeeCode})</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày <span class="text-danger">*</span></label>
                                <input type="date" name="date" class="form-control input-premium" required>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Số giờ <span class="text-danger">*</span></label>
                                <input type="number" name="requestedHours" class="form-control input-premium"
                                       step="0.5" min="0.5" max="24" placeholder="VD: 2.5" required>
                            </div>

                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Lý do</label>
                                <textarea name="reason" class="form-control input-premium" rows="3"
                                          placeholder="Nhập lý do tăng ca..."></textarea>
                            </div>

                            <div class="col-12 pt-3 border-top border-outline-variant">
                                <div class="d-flex gap-3 justify-content-end">
                                    <a href="${pageContext.request.contextPath}/overtime-list" class="btn btn-light border px-4 py-2">Hủy</a>
                                    <button type="submit" class="btn btn-primary-gradient px-4 py-2 d-flex align-items-center gap-2">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">send</span>
                                        Gửi yêu cầu
                                    </button>
                                </div>
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
