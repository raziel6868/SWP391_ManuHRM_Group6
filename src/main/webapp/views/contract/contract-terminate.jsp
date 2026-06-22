<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chấm dứt hợp đồng - ManuHRM</title>
    <link href="${ctx}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container" style="max-width: 720px;">
                <jsp:include page="/components/alert.jsp" />

                <nav aria-label="breadcrumb" class="mb-3">
                    <ol class="breadcrumb label-sm mb-0">
                        <li class="breadcrumb-item"><a href="${ctx}/contract-list" class="text-primary text-decoration-none">Hợp đồng</a></li>
                        <li class="breadcrumb-item"><a href="${ctx}/contract-detail?id=${contract.id}" class="text-primary text-decoration-none">Chi tiết #${contract.id}</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Chấm dứt</li>
                    </ol>
                </nav>

                <h2 class="h3 text-on-surface fw-bold mb-1">Chấm dứt hợp đồng lao động</h2>
                <p class="body-md text-on-surface-variant mb-4">
                    Hành động này sẽ chuyển hợp đồng sang trạng thái <strong>Đã chấm dứt</strong> và không thể hoàn tác.
                </p>

                <div class="card-premium p-3 mb-3" style="border-left: 4px solid var(--outline-variant);">
                    <h3 class="h6 fw-bold text-on-surface mb-2">Hợp đồng sẽ bị chấm dứt</h3>
                    <dl class="row mb-0 label-sm">
                        <dt class="col-4 text-on-surface-variant">Nhân viên</dt>
                        <dd class="col-8 fw-medium">${contract.fullName} (${contract.employeeCode})</dd>
                        <dt class="col-4 text-on-surface-variant">Loại HĐ</dt>
                        <dd class="col-8">${contract.contractTypeName}</dd>
                        <dt class="col-4 text-on-surface-variant">Thời hạn</dt>
                        <dd class="col-8">
                            ${contract.startDate}
                            <c:choose>
                                <c:when test="${empty contract.endDate}">
                                    - không xác định
                                </c:when>
                                <c:otherwise>
                                    &rarr; ${contract.endDate}
                                </c:otherwise>
                            </c:choose>
                        </dd>
                    </dl>
                </div>

                <form action="${ctx}/contract-terminate" method="POST" class="card-premium p-4">
                    <input type="hidden" name="id" value="${contract.id}" />

                    <div class="alert d-flex align-items-start gap-2 mb-4" role="alert"
                        style="background: rgba(220,53,69,.08); border: 1px solid rgba(220,53,69,.3); color: #b02a37;">
                        <span class="material-symbols-outlined">warning</span>
                        <div class="label-sm">
                            Hợp đồng sẽ chuyển sang trạng thái <strong>Đã chấm dứt</strong> và <strong>không thể hoàn tác</strong>. Nhân viên vẫn có thể được tạo hợp đồng mới ngay sau khi chấm dứt.
                        </div>
                    </div>

                    <div class="row g-3 mb-4">
                        <div class="col-12 col-md-6">
                            <label for="effectiveDate" class="form-label fw-medium">
                                Ngày chấm dứt <span class="text-danger">*</span>
                            </label>
                            <input type="date" id="effectiveDate" name="effectiveDate"
                                class="input-premium w-100"
                                value="${effectiveDate != null ? effectiveDate : ''}"
                                min="${contract.startDate}"
                                required />
                            <div class="form-text">Không được trước ngày bắt đầu hợp đồng (${contract.startDate}).</div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="reason" class="form-label fw-medium">Lý do chấm dứt <span class="text-danger">*</span></label>
                        <textarea id="reason" name="reason" rows="4" class="input-premium w-100" required
                            placeholder="Ví dụ: Nhân viên xin nghỉ việc theo đơn từ ngày..."><c:out value="${reason}" /></textarea>
                        <div class="form-text">Tối thiểu 5 ký tự, tối đa 500 ký tự.</div>
                    </div>

                    <div class="d-flex justify-content-end gap-2">
                        <a href="${ctx}/contract-detail?id=${contract.id}" class="btn btn-light border">Hủy</a>
                        <button type="submit" class="btn btn-danger d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">block</span>
                            Chấm dứt hợp đồng
                        </button>
                    </div>
                </form>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
