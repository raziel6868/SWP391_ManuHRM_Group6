<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thiết lập giảm trừ - ManuHRM</title>
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
                        ${empty personalTaxSetting.id ? 'Thêm thiết lập giảm trừ' : 'Cập nhật thiết lập giảm trừ'}
                    </h2>
                    <p class="body-md text-on-surface-variant mb-0">Thiết lập mức giảm trừ bản thân và người phụ thuộc theo thời gian áp dụng.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/personal-tax-setting-setup" method="POST">
                        <input type="hidden" name="id" value="${personalTaxSetting.id}" />

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Giảm trừ bản thân <span class="text-danger">*</span></label>
                                <input type="number" name="personalDeduction" class="form-control input-premium"
                                       min="0" step="1000" required value="${personalTaxSetting.personalDeduction}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Giảm trừ mỗi người phụ thuộc <span class="text-danger">*</span></label>
                                <input type="number" name="dependentDeduction" class="form-control input-premium"
                                       min="0" step="1000" required value="${personalTaxSetting.dependentDeduction}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Hiệu lực từ <span class="text-danger">*</span></label>
                                <input type="date" name="effectiveFrom" class="form-control input-premium"
                                       value="${personalTaxSetting.effectiveFrom}" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Hiệu lực đến</label>
                                <input type="date" name="effectiveTo" class="form-control input-premium"
                                       value="${personalTaxSetting.effectiveTo}" />
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                Lưu thiết lập
                            </button>
                            <a href="${pageContext.request.contextPath}/personal-tax-setting-list"
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
