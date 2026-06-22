<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>${not empty assignment ? 'Sửa' : 'Tạo'} Phân ca - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 720px; margin: 40px auto; width: 100%;">
                <div class="d-flex align-items-center gap-3 mb-4">
                    <a href="${pageContext.request.contextPath}/shift-assignment-list"
                       class="btn btn-light border px-2 py-2 d-flex align-items-center justify-content-center">
                        <span class="material-symbols-outlined">arrow_back</span>
                    </a>
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">${not empty assignment ? 'Sửa' : 'Tạo'} Phân ca</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            ${not empty assignment ? 'Cập nhật phân ca cho nhân viên' : 'Tạo phân ca mới cho nhân viên'}.
                        </p>
                    </div>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium overflow-hidden">
                    <div class="p-4">
                        <form action="${pageContext.request.contextPath}/shift-assignment-assign" method="POST" class="row g-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Nhân viên <span class="text-danger">*</span>
                                </label>
                                <select name="userId" class="form-select input-premium" required>
                                    <option value="">-- Chọn nhân viên --</option>
                                    <c:forEach var="user" items="${users}">
                                        <option value="${user.id}" ${not empty assignment && assignment.userId == user.id ? 'selected' : ''}>
                                            ${user.fullName} (${user.employeeCode})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Ca làm việc <span class="text-danger">*</span>
                                </label>
                                <select name="shiftId" class="form-select input-premium" required>
                                    <option value="">-- Chọn ca --</option>
                                    <c:forEach var="shift" items="${shifts}">
                                        <option value="${shift.id}" ${not empty assignment && assignment.shiftId == shift.id ? 'selected' : ''}>
                                            ${shift.name} (${shift.startTime} - ${shift.endTime})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Ngày <span class="text-danger">*</span>
                                </label>
                                <input type="date" name="date" class="form-control input-premium"
                                       value="${not empty assignment ? assignment.date : ''}" required>
                            </div>

                            <div class="col-12 pt-3 border-top border-outline-variant">
                                <div class="d-flex gap-3 justify-content-end">
                                    <a href="${pageContext.request.contextPath}/shift-assignment-list"
                                       class="btn btn-light border px-4 py-2">Hủy</a>
                                    <button type="submit" class="btn-primary-gradient px-4 py-2 d-flex align-items-center gap-2">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                        ${not empty assignment ? 'Cập nhật' : 'Tạo mới'}
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
