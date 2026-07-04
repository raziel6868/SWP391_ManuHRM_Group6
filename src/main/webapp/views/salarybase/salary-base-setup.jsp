<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thiết lập lương cơ sở - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container" style="max-width: 720px; margin: 40px auto; width: 100%;">
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
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Thiết lập lương cơ sở</h2>
                        <p class="body-md text-on-surface-variant mb-0">Nhập lương cơ bản và mức lương đóng bảo hiểm cho nhân viên.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden">
                    <div class="p-4">
                        <form action="${pageContext.request.contextPath}/salary-base-setup" method="POST" class="row g-4">
                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Nhân viên <span class="text-danger">*</span></label>
                                <select name="userId" class="form-select input-premium" required>
                                    <option value="">-- Chọn nhân viên --</option>
                                    <c:forEach var="u" items="${users}">
                                        <option value="${u.id}" ${u.id == selectedUserId ? 'selected' : ''}>${u.fullName} (${u.employeeCode})</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Lương cơ sở (VND) <span class="text-danger">*</span></label>
                                <input type="number" name="baseSalary" class="form-control input-premium"
                                       min="0" step="1000" placeholder="VD: 15000000"
                                       value="${currentBase.baseSalary}" required>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày hiệu lực <span class="text-danger">*</span></label>
                                <input type="date" name="effectiveFrom" class="form-control input-premium"
                                       value="${currentBase.effectiveFrom}" required>
                            </div>

                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Lương đóng bảo hiểm (VND)</label>
                                <input type="number" name="insuranceSalary" class="form-control input-premium"
                                       min="0" step="1000" placeholder="Bỏ trống để hệ thống tự tính"
                                       value="${currentBase.insuranceSalary}">
                                <div class="form-text text-on-surface-variant mt-2">
                                    Nếu bỏ trống, hệ thống tự tính bằng lương cơ bản + phụ cấp tính bảo hiểm.
                                </div>
                            </div>

                            <div class="col-12 pt-3 border-top border-outline-variant">
                                <div class="d-flex gap-3 justify-content-end">
                                    <button type="submit" class="btn btn-primary-gradient px-4 py-2 d-flex align-items-center gap-2">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                        Lưu
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
