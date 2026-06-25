<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Kiểm tra xung đột ca - ManuHRM</title>
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
                <c:if test="${not empty warningMsg}">
                    <div class="alert d-flex align-items-center gap-2 mb-3" role="alert" style="background: #fef3c7; color: #92400e;">
                        <span class="material-symbols-outlined">warning</span>
                        ${warningMsg}
                    </div>
                </c:if>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Kiểm tra xung đột ca</h2>
                        <p class="body-md text-on-surface-variant mb-0">Kiểm tra xung đột lịch làm việc của nhân viên.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <h5 class="mb-3">Kiểm tra xung đột</h5>
                        <form action="${pageContext.request.contextPath}/shift-conflict" method="POST">
                            <div class="row g-3 align-items-end">
                                <div class="col-md-4">
                                    <label class="form-label text-on-surface fw-medium mb-1">
                                        Nhân viên <span class="text-danger">*</span>
                                    </label>
                                    <select name="userId" class="form-select input-premium" required>
                                        <option value="">-- Chọn nhân viên --</option>
                                        <c:forEach var="user" items="${users}">
                                            <option value="${user.id}" ${selectedUserId == user.id ? 'selected' : ''}>
                                                ${user.fullName} (${user.employeeCode})
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label text-on-surface fw-medium mb-1">
                                        Ngày bắt đầu <span class="text-danger">*</span>
                                    </label>
                                    <input type="date" name="startDate" value="${selectedStartDate}" class="form-control input-premium" required />
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label text-on-surface fw-medium mb-1">
                                        Ngày kết thúc <span class="text-danger">*</span>
                                    </label>
                                    <input type="date" name="endDate" value="${selectedEndDate}" class="form-control input-premium" required />
                                </div>
                                <div class="col-md-2 d-flex gap-2">
                                    <button type="submit" class="btn btn-primary w-100">
                                        <span class="material-symbols-outlined">check</span>
                                        Kiểm tra
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>

                <c:if test="${hasConflict}">
                    <div class="alert d-flex align-items-center gap-2 mb-4" role="alert" style="background: #fee2e2; color: #991b1b;">
                        <span class="material-symbols-outlined">warning</span>
                        <div>
                            <strong>Xung đột phát hiện!</strong><br>
                            Nhân viên đã có lịch làm việc vào ngày <strong>${conflictAssignment.date}</strong>
                            với ca <strong>${conflictAssignment.shiftName}</strong>.
                        </div>
                    </div>
                </c:if>

                <div class="card-premium overflow-hidden">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <h5 class="mb-0">Lịch sử phân ca</h5>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Ngày</th>
                                    <th>Ca</th>
                                    <th>Mã ca</th>
                                    <th>Ghi nhận</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="sa" items="${existingAssignments}">
                                    <tr>
                                        <td>${sa.date}</td>
                                        <td>${sa.shiftName}</td>
                                        <td><span class="badge" style="background-color: #e0e7ff; color: #3730a3;">${sa.shiftCode}</span></td>
                                        <td>${sa.createdAt}</td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty existingAssignments}">
                                    <tr>
                                        <td colspan="4" class="text-center py-4 text-on-surface-variant">
                                            Chưa có phân ca trong khoảng thời gian này.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
