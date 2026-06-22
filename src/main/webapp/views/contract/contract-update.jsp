<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chỉnh sửa hợp đồng - ManuHRM</title>
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
                        <li class="breadcrumb-item active" aria-current="page">Chỉnh sửa</li>
                    </ol>
                </nav>

                <h2 class="h3 text-on-surface fw-bold mb-1">Chỉnh sửa hợp đồng lao động</h2>
                <p class="body-md text-on-surface-variant mb-4">
                    Chỉ có thể chỉnh sửa các hợp đồng đang ở trạng thái <strong>Đang hiệu lực</strong>.
                    Không thể thay đổi nhân viên của hợp đồng.
                </p>

                <div class="card-premium p-3 mb-3" style="border-left: 4px solid var(--outline-variant);">
                    <h3 class="h6 fw-bold text-on-surface mb-2">Hợp đồng hiện tại</h3>
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
                        <dt class="col-4 text-on-surface-variant">Mức lương</dt>
                        <dd class="col-8">${contract.salary}</dd>
                    </dl>
                </div>

                <form action="${ctx}/contract-update" method="POST" enctype="multipart/form-data" class="card-premium p-4">
                    <input type="hidden" name="id" value="${contract.id}" />

                    <div class="mb-3">
                        <label for="contractTypeId" class="form-label fw-medium">Loại hợp đồng <span class="text-danger">*</span></label>
                        <select id="contractTypeId" name="contractTypeId" class="form-select" required>
                            <c:forEach var="ct" items="${contractTypes}">
                                <option value="${ct.id}" ${(selectedContractTypeId != null ? selectedContractTypeId : contract.contractTypeId) == ct.id ? 'selected' : ''}>
                                    ${ct.name} (${ct.code})
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="row g-3 mb-3">
                        <div class="col-12 col-md-6">
                            <label for="startDate" class="form-label fw-medium">Ngày bắt đầu <span class="text-danger">*</span></label>
                            <input type="date" id="startDate" name="startDate"
                                value="${startDate != null ? startDate : contract.startDate}"
                                class="input-premium w-100" required />
                        </div>
                        <div class="col-12 col-md-6">
                            <label for="endDate" class="form-label fw-medium">Ngày kết thúc</label>
                            <input type="date" id="endDate" name="endDate" value="${endDate != null ? endDate : contract.endDate}"
                                class="input-premium w-100" />
                            <div class="form-text">Để trống nếu là hợp đồng không xác định thời hạn.</div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="salary" class="form-label fw-medium">Mức lương (VNĐ)</label>
                        <input type="number" id="salary" name="salary"
                            value="${salary != null ? salary : contract.salary}"
                            min="0" step="1000" class="input-premium w-100"
                            placeholder="Ví dụ: 8000000" />
                    </div>

                    <div class="mb-4">
                        <label for="contractFile" class="form-label fw-medium">File hợp đồng (PDF)</label>
                        <input type="file" id="contractFile" name="contractFile" accept=".pdf,application/pdf"
                            class="input-premium w-100" style="padding: 0.375rem 0.75rem;" />
                        <c:if test="${not empty contract.filePath}">
                            <div class="form-text">
                                Đã có file: <code>${contract.filePath}</code>. Tải file mới lên sẽ thay thế file cũ.
                            </div>
                        </c:if>
                        <div class="form-text">Không bắt buộc. Chấp nhận file PDF, tối đa 5MB.</div>
                    </div>

                    <div class="alert alert-light border d-flex align-items-start gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined text-primary">info</span>
                        <div class="label-sm">
                            Việc chỉnh sửa chỉ áp dụng cho hợp đồng hiện tại. Các hợp đồng trước đó (nếu có) vẫn giữ nguyên thông tin.
                        </div>
                    </div>

                    <div class="d-flex justify-content-end gap-2">
                        <a href="${ctx}/contract-detail?id=${contract.id}" class="btn btn-light border">Hủy</a>
                        <button type="submit" class="btn-primary-gradient text-decoration-none px-3 py-2 border-0 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                            Lưu thay đổi
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
