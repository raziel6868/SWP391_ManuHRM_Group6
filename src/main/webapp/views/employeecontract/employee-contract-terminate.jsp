<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chấm dứt hợp đồng - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 720px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Chấm dứt hợp đồng</h2>
                    <p class="body-md text-on-surface-variant mb-0">Chấm dứt hợp đồng lao động của nhân viên.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <div class="alert alert-warning d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">warning</span>
                        <div>
                            Hành động này sẽ chấm dứt hợp đồng của <strong>${contract.userFullName}</strong>.
                            Hành động này không thể hoàn tác.
                        </div>
                    </div>

                    <form action="${pageContext.request.contextPath}/contract-terminate" method="POST">
                        <input type="hidden" name="id" value="${contract.id}" />

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Nhân viên</label>
                                <input type="text" class="form-control input-premium"
                                       value="${contract.userFullName} (${contract.employeeCode})" disabled
                                       style="background-color: var(--surface-container-high);" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại hợp đồng</label>
                                <input type="text" class="form-control input-premium"
                                       value="${contract.contractTypeName}" disabled
                                       style="background-color: var(--surface-container-high);" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày bắt đầu</label>
                                <input type="text" class="form-control input-premium"
                                       value="${contract.startDate}" disabled
                                       style="background-color: var(--surface-container-high);" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày kết thúc</label>
                                <input type="text" class="form-control input-premium"
                                       value="${contract.endDate != null ? contract.endDate : 'Không giới hạn'}" disabled
                                       style="background-color: var(--surface-container-high);" />
                            </div>
                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày chấm dứt <span class="text-danger">*</span></label>
                                <input type="date" name="terminatedAt" class="form-control input-premium" required />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Chọn ngày chấm dứt hợp đồng.
                                </div>
                            </div>
                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Lý do chấm dứt <span class="text-danger">*</span></label>
                                <textarea name="terminationReason" class="form-control input-premium" rows="4"
                                          placeholder="VD: Nhân viên tự ý nghỉ việc, Công ty không gia hạn hợp đồng..."
                                          required></textarea>
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-danger px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">cancel</span>
                                Xác nhận chấm dứt
                            </button>
                            <a href="${pageContext.request.contextPath}/contract-detail?id=${contract.id}"
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
