<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thiết lập payroll - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 820px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">
                        ${empty payrollSetting.id ? 'Thêm cấu hình payroll' : 'Cập nhật cấu hình payroll'}
                    </h2>
                    <p class="body-md text-on-surface-variant mb-0">
                        Cấu hình tham số chung cho giờ công, OT và thưởng chuyên cần theo thời gian áp dụng.
                    </p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/payroll-setting-setup" method="POST">
                        <input type="hidden" name="id" value="${payrollSetting.id}" />

                        <div class="row g-4 mb-4">
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Giờ công/ngày <span class="text-danger">*</span></label>
                                <input type="number" name="standardWorkHoursPerDay" class="form-control input-premium"
                                       min="0.01" step="0.01" required value="${standardWorkHoursPerDayValue}" />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Hệ số OT <span class="text-danger">*</span></label>
                                <input type="number" name="normalOvertimeRate" class="form-control input-premium"
                                       min="0.01" step="0.01" required value="${normalOvertimeRateValue}" />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">M&#7913;c th&#432;&#7903;ng chuy&ecirc;n c&#7847;n <span class="text-danger">*</span></label>
                                <input type="number" name="attendanceBonusAmount" class="form-control input-premium"
                                       min="0" step="1000" required value="${attendanceBonusAmountValue}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Hiệu lực từ <span class="text-danger">*</span></label>
                                <input type="date" name="effectiveFrom" class="form-control input-premium"
                                       value="${payrollSetting.effectiveFrom}" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Hiệu lực đến</label>
                                <input type="date" name="effectiveTo" class="form-control input-premium"
                                       value="${payrollSetting.effectiveTo}" />
                            </div>
                        </div>

                        <div class="alert alert-light border d-flex align-items-start gap-2 mb-4" role="status">
                            <span class="material-symbols-outlined text-primary">info</span>
                            <div class="small">
                                Payroll tự tính ngày công chuẩn theo lịch làm việc T2-T6 của từng tháng. Công thức OT hiện tại: giờ OT duyệt x lương giờ x hệ số OT.
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                Lưu cấu hình
                            </button>
                            <a href="${pageContext.request.contextPath}/payroll-setting-list"
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
