<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Cấu hình payroll - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Cấu hình payroll</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Quản lý ngày công chuẩn, giờ công chuẩn và hệ số OT theo thời gian áp dụng.
                        </p>
                    </div>
                    <c:if test="${canSetup}">
                        <a href="${pageContext.request.contextPath}/payroll-setting-setup"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                            Thêm cấu hình
                        </a>
                    </c:if>
                </div>

                <div class="card-premium p-4 mb-4">
                    <div class="row g-4">
                        <div class="col-md-4">
                            <div class="small text-on-surface-variant mb-1">Công thức OT đang dùng</div>
                            <div class="fw-semibold">OT pay = giờ OT duyệt x lương giờ x hệ số OT</div>
                        </div>
                        <div class="col-md-4">
                            <div class="small text-on-surface-variant mb-1">Số ngày công chuẩn</div>
                            <div>Lấy từ <code>payroll_settings.standard_work_days</code></div>
                        </div>
                        <div class="col-md-4">
                            <div class="small text-on-surface-variant mb-1">Số giờ công chuẩn mỗi ngày</div>
                            <div>Lấy từ <code>payroll_settings.standard_work_hours_per_day</code></div>
                        </div>
                    </div>
                </div>

                <div class="card-premium overflow-hidden">
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Hiệu lực từ</th>
                                    <th>Hiệu lực đến</th>
                                    <th class="text-end">Ngày công chuẩn</th>
                                    <th class="text-end">Giờ/ngày</th>
                                    <th class="text-end">Hệ số OT</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="setting" items="${payrollSettings}">
                                    <tr>
                                        <td><fmt:formatDate value="${setting.effectiveFrom}" pattern="dd/MM/yyyy" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty setting.effectiveTo}">
                                                    <fmt:formatDate value="${setting.effectiveTo}" pattern="dd/MM/yyyy" />
                                                </c:when>
                                                <c:otherwise>Đang áp dụng</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end"><fmt:formatNumber value="${setting.standardWorkDays}" pattern="#,##0.##" /></td>
                                        <td class="text-end"><fmt:formatNumber value="${setting.standardWorkHoursPerDay}" pattern="#,##0.##" /></td>
                                        <td class="text-end"><fmt:formatNumber value="${setting.normalOvertimeRate}" pattern="#,##0.##" /></td>
                                        <td class="text-end">
                                            <c:if test="${canSetup}">
                                                <a href="${pageContext.request.contextPath}/payroll-setting-setup?id=${setting.id}"
                                                   class="btn btn-sm btn-outline-primary">
                                                    Sửa
                                                </a>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty payrollSettings}">
                                    <tr>
                                        <td colspan="6" class="text-center py-4 text-on-surface-variant">
                                            Chưa có cấu hình payroll nào.
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
