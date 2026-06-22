<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thiết lập hạn mức nghỉ - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 820px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <a href="${pageContext.request.contextPath}/leave-balance-list"
                       class="text-decoration-none d-inline-flex align-items-center gap-2 text-on-surface-variant mb-3">
                        <span class="material-symbols-outlined" style="font-size: 1.25rem;">arrow_back</span>
                        Quay lại danh sách hạn mức
                    </a>
                    <h2 class="h3 text-on-surface fw-bold mb-1">Thiết lập hạn mức nghỉ</h2>
                    <p class="body-md text-on-surface-variant mb-0">
                        Cấu hình số ngày nghỉ theo từng nhân viên, loại nghỉ và năm áp dụng.
                    </p>
                </div>

                <jsp:include page="/components/alert.jsp" />

                <c:set var="displayTotalDays" value="${totalDays}" />
                <c:if test="${empty displayTotalDays and not empty existingBalance}">
                    <c:set var="displayTotalDays" value="${existingBalance.totalDays}" />
                </c:if>

                <div class="card-premium p-4 p-md-5 mb-4">
                    <form action="${pageContext.request.contextPath}/leave-balance-setup" method="POST">
                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Nhân viên <span class="text-danger">*</span>
                                </label>
                                <select name="userId" class="form-select input-premium" required>
                                    <option value="" ${empty selectedUserId ? 'selected' : ''}>Chọn nhân viên</option>
                                    <c:forEach var="user" items="${users}">
                                        <option value="${user.id}" ${selectedUserId == user.id ? 'selected' : ''}>
                                            <c:out value="${user.employeeCode}" /> - <c:out value="${user.fullName}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Loại nghỉ <span class="text-danger">*</span>
                                </label>
                                <select name="leaveTypeId" class="form-select input-premium" required>
                                    <option value="" ${empty selectedLeaveTypeId ? 'selected' : ''}>Chọn loại nghỉ</option>
                                    <c:forEach var="leaveType" items="${leaveTypes}">
                                        <option value="${leaveType.id}" ${selectedLeaveTypeId == leaveType.id ? 'selected' : ''}>
                                            <c:out value="${leaveType.code}" /> - <c:out value="${leaveType.name}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Năm áp dụng <span class="text-danger">*</span>
                                </label>
                                <input type="number" name="year" class="form-control input-premium"
                                       value="${empty selectedYear ? currentYear : selectedYear}"
                                       min="2000" max="2100" required />
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Tổng số ngày <span class="text-danger">*</span>
                                </label>
                                <input type="number" name="totalDays" class="form-control input-premium"
                                       value="${displayTotalDays}" min="0" max="999.99" step="0.5"
                                       placeholder="VD: 12" required />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Không được nhỏ hơn số ngày đã sử dụng nếu hạn mức đã tồn tại.
                                </div>
                            </div>
                        </div>

                        <c:if test="${not empty existingBalance}">
                            <div class="row g-3 mb-4">
                                <div class="col-md-4">
                                    <div class="p-3 rounded-3 border border-outline-variant bg-surface">
                                        <div class="label-sm text-on-surface-variant text-uppercase mb-1">Đã thiết lập</div>
                                        <div class="h5 mb-0">
                                            <fmt:formatNumber value="${existingBalance.totalDays}" minFractionDigits="0" maxFractionDigits="2" /> ngày
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="p-3 rounded-3 border border-outline-variant bg-surface">
                                        <div class="label-sm text-on-surface-variant text-uppercase mb-1">Đã sử dụng</div>
                                        <div class="h5 mb-0">
                                            <fmt:formatNumber value="${existingBalance.usedDays}" minFractionDigits="0" maxFractionDigits="2" /> ngày
                                        </div>
                                    </div>
                                </div>
                                <div class="col-md-4">
                                    <div class="p-3 rounded-3 border border-outline-variant bg-surface">
                                        <div class="label-sm text-on-surface-variant text-uppercase mb-1">Còn lại</div>
                                        <div class="h5 mb-0">
                                            <fmt:formatNumber value="${existingBalance.totalDays - existingBalance.usedDays}" minFractionDigits="0" maxFractionDigits="2" /> ngày
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant flex-wrap">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                Lưu hạn mức
                            </button>
                            <a href="${pageContext.request.contextPath}/leave-balance-list"
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
