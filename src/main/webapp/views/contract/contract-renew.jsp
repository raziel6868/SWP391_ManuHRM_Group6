<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Gia hạn hợp đồng - ManuHRM</title>
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
                        <li class="breadcrumb-item"><a href="${ctx}/contract-detail?id=${previous.id}" class="text-primary text-decoration-none">Chi tiết #${previous.id}</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Gia hạn</li>
                    </ol>
                </nav>

                <h2 class="h3 text-on-surface fw-bold mb-1">Gia hạn hợp đồng lao động</h2>
                <p class="body-md text-on-surface-variant mb-4">
                    Hợp đồng hiện tại sẽ được chuyển sang trạng thái <strong>Hết hạn</strong>, và một hợp đồng mới sẽ được tạo.
                </p>

                <div class="card-premium p-3 mb-3" style="border-left: 4px solid var(--outline-variant);">
                    <h3 class="h6 fw-bold text-on-surface mb-2">Hợp đồng hiện tại</h3>
                    <dl class="row mb-0 label-sm">
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
                            <label for="startDate" class="form-label fw-medium">Ngày bắt đầu mới <span class="text-danger">*</span></label>
                            <input type="date" id="startDate" name="startDate"
                                value="${startDate != null ? startDate : defaultStartDate}"
                                class="input-premium w-100" required />
                            <c:if test="${not empty defaultStartDate}">
                                <div class="form-text">Mặc định: ngày sau khi hợp đồng cũ kết thúc.</div>
                            </c:if>
                        </div>
                        <div class="col-12 col-md-6">
                            <label for="endDate" class="form-label fw-medium">Ngày kết thúc mới</label>
                            <input type="date" id="endDate" name="endDate" value="${endDate}"
                                class="input-premium w-100" />
                            <div class="form-text">Để trống nếu là hợp đồng không xác định thời hạn.</div>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="salary" class="form-label fw-medium">Mức lương mới (VNĐ)</label>
                        <input type="number" id="salary" name="salary" value="${salary != null ? salary : previous.salary}"
                            min="0" step="1000" class="input-premium w-100" />
                        <div class="form-text">Mặc định lấy theo mức lương của hợp đồng cũ.</div>
                    </div>

                    <div class="mb-3">
                        <label for="contractFile" class="form-label fw-medium">File hợp đồng (PDF)</label>
                        <input type="file" id="contractFile" name="contractFile" accept=".pdf,application/pdf"
                            class="input-premium w-100" style="padding: 0.375rem 0.75rem;" />
                        <div class="form-text">Không bắt buộc. Chấp nhận file PDF, tối đa 5MB.</div>
                    </div>

                    <div class="alert alert-light border d-flex align-items-start gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined text-primary">info</span>
                        <div class="label-sm">
                            Sau khi gia hạn, hợp đồng #${previous.id} sẽ chuyển sang <strong>Hết hạn</strong> và không thể chỉnh sửa nữa. Bạn vẫn có thể xem lại lịch sử trong chi tiết hợp đồng mới.
                        </div>
                    </div>

                    <div class="d-flex justify-content-end gap-2">
                        <a href="${ctx}/contract-detail?id=${previous.id}" class="btn btn-light border">Hủy</a>
                        <button type="submit" class="btn-primary-gradient text-decoration-none px-3 py-2 border-0 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">autorenew</span>
                            Gia hạn
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
