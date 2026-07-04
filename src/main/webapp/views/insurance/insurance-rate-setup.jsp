<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thiết lập mức đóng bảo hiểm - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 980px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">
                        ${empty insuranceRate.id ? 'Thêm mức đóng bảo hiểm' : 'Cập nhật mức đóng bảo hiểm'}
                    </h2>
                    <p class="body-md text-on-surface-variant mb-0">Thiết lập tỷ lệ người lao động đóng, công ty đóng và các mức trần áp dụng theo thời gian.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/insurance-rate-setup" method="POST">
                        <input type="hidden" name="id" value="${insuranceRate.id}" />

                        <div class="row g-4 mb-4">
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">BHXH NLĐ đóng <span class="text-danger">*</span></label>
                                <input type="number" name="socialInsuranceEmployeeRate" class="form-control input-premium"
                                       min="0" max="1" step="0.0001" required value="${insuranceRate.socialInsuranceEmployeeRate}" />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">BHYT NLĐ đóng <span class="text-danger">*</span></label>
                                <input type="number" name="healthInsuranceEmployeeRate" class="form-control input-premium"
                                       min="0" max="1" step="0.0001" required value="${insuranceRate.healthInsuranceEmployeeRate}" />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">BHTN NLĐ đóng <span class="text-danger">*</span></label>
                                <input type="number" name="unemploymentInsuranceEmployeeRate" class="form-control input-premium"
                                       min="0" max="1" step="0.0001" required value="${insuranceRate.unemploymentInsuranceEmployeeRate}" />
                            </div>

                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">BHXH công ty đóng <span class="text-danger">*</span></label>
                                <input type="number" name="socialInsuranceEmployerRate" class="form-control input-premium"
                                       min="0" max="1" step="0.0001" required value="${insuranceRate.socialInsuranceEmployerRate}" />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">BHYT công ty đóng <span class="text-danger">*</span></label>
                                <input type="number" name="healthInsuranceEmployerRate" class="form-control input-premium"
                                       min="0" max="1" step="0.0001" required value="${insuranceRate.healthInsuranceEmployerRate}" />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">BHTN công ty đóng <span class="text-danger">*</span></label>
                                <input type="number" name="unemploymentInsuranceEmployerRate" class="form-control input-premium"
                                       min="0" max="1" step="0.0001" required value="${insuranceRate.unemploymentInsuranceEmployerRate}" />
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Trần BHXH/BHYT</label>
                                <input type="number" name="socialHealthInsuranceCap" class="form-control input-premium"
                                       min="0" step="1000" value="${insuranceRate.socialHealthInsuranceCap}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Trần BHTN</label>
                                <input type="number" name="unemploymentInsuranceCap" class="form-control input-premium"
                                       min="0" step="1000" value="${insuranceRate.unemploymentInsuranceCap}" />
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Hiệu lực từ <span class="text-danger">*</span></label>
                                <input type="date" name="effectiveFrom" class="form-control input-premium"
                                       value="${insuranceRate.effectiveFrom}" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Hiệu lực đến</label>
                                <input type="date" name="effectiveTo" class="form-control input-premium"
                                       value="${insuranceRate.effectiveTo}" />
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                Lưu mức đóng
                            </button>
                            <a href="${pageContext.request.contextPath}/insurance-rate-list"
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
