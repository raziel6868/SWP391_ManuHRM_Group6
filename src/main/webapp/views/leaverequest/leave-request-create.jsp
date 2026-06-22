<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Tạo đơn nghỉ - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 920px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <a href="${pageContext.request.contextPath}/leave-request-my"
                       class="text-decoration-none d-inline-flex align-items-center gap-2 text-on-surface-variant mb-3">
                        <span class="material-symbols-outlined" style="font-size: 1.25rem;">arrow_back</span>
                        Quay lại đơn nghỉ của tôi
                    </a>
                    <h2 class="h3 text-on-surface fw-bold mb-1">Tạo đơn nghỉ</h2>
                    <p class="body-md text-on-surface-variant mb-0">
                        Gửi đơn nghỉ để quản lý sản xuất và HR phê duyệt theo quy trình.
                    </p>
                </div>

                <jsp:include page="/components/alert.jsp" />

                <div class="row g-4">
                    <div class="col-lg-8">
                        <div class="card-premium p-4 p-md-5">
                            <form action="${pageContext.request.contextPath}/leave-request-create" method="POST">
                                <div class="row g-4 mb-4">
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

                                    <div class="col-md-3">
                                        <label class="form-label text-on-surface fw-medium mb-1">
                                            Từ ngày <span class="text-danger">*</span>
                                        </label>
                                        <input type="date" name="startDate" class="form-control input-premium"
                                               value="${startDate}" required />
                                    </div>

                                    <div class="col-md-3">
                                        <label class="form-label text-on-surface fw-medium mb-1">
                                            Đến ngày <span class="text-danger">*</span>
                                        </label>
                                        <input type="date" name="endDate" class="form-control input-premium"
                                               value="${endDate}" required />
                                    </div>

                                    <div class="col-12">
                                        <label class="form-label text-on-surface fw-medium mb-1">Lý do</label>
                                        <textarea name="reason" class="form-control input-premium" rows="4"
                                                  maxlength="1000"
                                                  placeholder="Nhập lý do nghỉ nếu có...">${reason}</textarea>
                                        <div class="form-text mt-1 text-on-surface-variant">
                                            Số ngày nghỉ được tính theo ngày lịch, bao gồm cả ngày bắt đầu và kết thúc.
                                        </div>
                                    </div>
                                </div>

                                <div class="d-flex gap-3 pt-3 border-top border-outline-variant flex-wrap">
                                    <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">send</span>
                                        Gửi đơn nghỉ
                                    </button>
                                    <a href="${pageContext.request.contextPath}/leave-request-my"
                                       class="btn btn-light border px-4 py-2 d-flex align-items-center gap-2">
                                        Hủy bỏ
                                    </a>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="col-lg-4">
                        <div class="card-premium p-4">
                            <div class="d-flex align-items-center justify-content-between mb-3">
                                <h3 class="h5 fw-bold mb-0">Hạn mức năm ${balanceYear}</h3>
                                <span class="material-symbols-outlined text-on-surface-variant">event_available</span>
                            </div>
                            <div class="d-flex flex-column gap-3">
                                <c:forEach var="balance" items="${balances}">
                                    <div class="p-3 rounded-3 border border-outline-variant bg-surface">
                                        <div class="d-flex justify-content-between align-items-start gap-3">
                                            <div>
                                                <div class="fw-semibold">
                                                    <c:out value="${balance.leaveTypeName}" />
                                                </div>
                                                <div class="body-sm text-on-surface-variant">
                                                    <c:out value="${balance.leaveTypeCode}" />
                                                </div>
                                            </div>
                                            <div class="text-end">
                                                <div class="fw-bold">
                                                    <fmt:formatNumber value="${balance.totalDays - balance.usedDays}" minFractionDigits="0" maxFractionDigits="2" />
                                                </div>
                                                <div class="body-sm text-on-surface-variant">còn lại</div>
                                            </div>
                                        </div>
                                        <div class="body-sm text-on-surface-variant mt-2">
                                            Đã dùng <fmt:formatNumber value="${balance.usedDays}" minFractionDigits="0" maxFractionDigits="2" />
                                            / <fmt:formatNumber value="${balance.totalDays}" minFractionDigits="0" maxFractionDigits="2" /> ngày
                                        </div>
                                    </div>
                                </c:forEach>
                                <c:if test="${empty balances}">
                                    <div class="text-on-surface-variant body-sm">
                                        Chưa có hạn mức nghỉ cho năm này. Vui lòng liên hệ HR.
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
