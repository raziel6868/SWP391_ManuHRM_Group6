<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Ký hợp đồng mới - ManuHRM</title>
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
                        <li class="breadcrumb-item"><a href="${ctx}/contract-detail?id=${previous.id}" class="text-primary text-decoration-none">Chi tiết ${previous.contractCode}</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Ký hợp đồng mới</li>
                    </ol>
                </nav>

                <h2 class="h3 text-on-surface fw-bold mb-1">Ký hợp đồng mới</h2>
                <p class="body-md text-on-surface-variant mb-4">
                    Hệ thống sẽ tạo một hợp đồng mới với mã hợp đồng mới, khác với hợp đồng ban đầu.
                </p>

                <div class="card-premium p-3 mb-3" style="border-left: 4px solid var(--outline-variant);">
                    <h3 class="h6 fw-bold text-on-surface mb-2">Hợp đồng ban đầu</h3>
                    <dl class="row mb-0 label-sm">
                        <dt class="col-4 text-on-surface-variant">Mã hợp đồng</dt>
                        <dd class="col-8 fw-medium">${previous.contractCode}</dd>
                        <dt class="col-4 text-on-surface-variant">Nhân viên</dt>
                        <dd class="col-8 fw-medium">${previous.fullName} (${previous.employeeCode})</dd>
                        <dt class="col-4 text-on-surface-variant">Loại HĐ</dt>
                        <dd class="col-8">${previous.contractTypeName}</dd>
                        <dt class="col-4 text-on-surface-variant">Thời hạn</dt>
                        <dd class="col-8">
                            ${previous.startDate}
                            <c:choose>
                                <c:when test="${empty previous.endDate}">
                                    - không xác định
                                </c:when>
                                <c:otherwise>
                                    &rarr; ${previous.endDate}
                                </c:otherwise>
                            </c:choose>
                        </dd>
                        <dt class="col-4 text-on-surface-variant">Mức lương</dt>
                        <dd class="col-8">${previous.salary}</dd>
                    </dl>
                </div>

                <form action="${ctx}/contract-renew" method="POST" enctype="multipart/form-data" class="card-premium p-4">
                    <input type="hidden" name="id" value="${previous.id}" />

                    <div class="mb-3">
                        <label for="contractTypeId" class="form-label fw-medium">Loại hợp đồng <span class="text-danger">*</span></label>
                        <select id="contractTypeId" name="contractTypeId" class="form-select" required>
                            <c:forEach var="ct" items="${contractTypes}">
                                <option value="${ct.id}" ${(selectedContractTypeId != null ? selectedContractTypeId : previous.contractTypeId) == ct.id ? 'selected' : ''}>
                                    ${ct.name} (${ct.code})
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="row g-3 mb-3">
                        <div class="col-12 col-md-6">
                            <label for="startDate" class="form-label fw-medium">Ngày bắt đầu <span class="text-danger">*</span></label>
                            <input type="date" id="startDate" name="startDate"
                                value="${startDate != null ? startDate : defaultStartDate}"
                                class="input-premium w-100" required />
                            <c:if test="${not empty defaultStartDate}">
                                <div class="form-text">Mặc định là ngày sau khi hợp đồng ban đầu kết thúc.</div>
                            </c:if>
                        </div>
                        <div class="col-12 col-md-6">
                            <label for="endDate" class="form-label fw-medium">Ngày kết thúc</label>
                            <input type="date" id="endDate" name="endDate" value="${endDate}"
                                class="input-premium w-100" />
                            <div class="form-text">Để trống với hợp đồng không xác định thời hạn; hợp đồng xác định thời hạn phải từ đủ 12 đến 36 tháng.</div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="salary" class="form-label fw-medium">Mức lương (VNĐ)</label>
                        <input type="number" id="salary" name="salary" value="${salary != null ? salary : previous.salary}"
                            min="0" step="1000" class="input-premium w-100" />
                    </div>

                    <div class="mb-3">
                        <label for="contractFile" class="form-label fw-medium">File hợp đồng mới (PDF)</label>
                        <input type="file" id="contractFile" name="contractFile" accept=".pdf,application/pdf"
                            class="input-premium w-100" style="padding: 0.375rem 0.75rem;" />
                        <div class="form-text">Không bắt buộc. Chỉ dùng khi cần lưu bản scan PDF của hợp đồng mới, tối đa 5MB.</div>
                    </div>

                    <div class="alert alert-light border d-flex align-items-start gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined text-primary">info</span>
                        <div class="label-sm">
                            Sau khi lưu, hợp đồng ${previous.contractCode} sẽ chuyển sang <strong>Hết hạn</strong>. Hợp đồng mới sẽ có mã tự sinh riêng.
                        </div>
                    </div>

                    <div class="d-flex justify-content-end gap-2">
                        <a href="${ctx}/contract-detail?id=${previous.id}" class="btn btn-light border">Hủy</a>
                        <button type="submit" class="btn-primary-gradient text-decoration-none px-3 py-2 border-0 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">post_add</span>
                            Ký hợp đồng mới
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
