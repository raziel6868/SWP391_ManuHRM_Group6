<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chi tiết hợp đồng - ManuHRM</title>
    <link href="${ctx}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <jsp:include page="/components/alert.jsp" />

                <nav aria-label="breadcrumb" class="mb-3">
                    <ol class="breadcrumb label-sm mb-0">
                        <li class="breadcrumb-item"><a href="${ctx}/contract-list" class="text-primary text-decoration-none">Hợp đồng</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Chi tiết #${contract.id}</li>
                    </ol>
                </nav>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-2">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Chi tiết hợp đồng lao động</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Mã hợp đồng #${contract.id} - ${contract.contractTypeName}
                        </p>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <a href="${ctx}/contract-list" class="btn btn-light border d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">arrow_back</span>
                            Quay lại
                        </a>
                        <c:if test="${hasContractUpdatePerm and contract.status == 'ACTIVE'}">
                            <a href="${ctx}/contract-update?id=${contract.id}" class="btn btn-light border d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">edit</span>
                                Chỉnh sửa
                            </a>
                        </c:if>
                        <c:if test="${hasContractUploadPerm and contract.status == 'ACTIVE'}">
                            <a href="${ctx}/contract-upload?id=${contract.id}" class="btn btn-light border d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">upload_file</span>
                                Tải PDF
                            </a>
                        </c:if>
                        <c:if test="${hasContractRenewPerm and (contract.status == 'ACTIVE' or contract.status == 'EXPIRED' or contract.status == 'PENDING_RENEWAL')}">
                            <a href="${ctx}/contract-renew?id=${contract.id}" class="btn btn-light border d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">autorenew</span>
                                Gia hạn
                            </a>
                        </c:if>
                        <c:if test="${hasContractTerminatePerm and contract.status != 'TERMINATED'}">
                            <a href="${ctx}/contract-terminate?id=${contract.id}" class="btn btn-danger d-flex align-items-center gap-2 text-white">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">block</span>
                                Chấm dứt
                            </a>
                        </c:if>
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-12 col-md-6">
                        <div class="card-premium p-4 h-100">
                            <h3 class="h6 fw-bold text-on-surface mb-3 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined text-primary">person</span>
                                Thông tin nhân viên
                            </h3>
                            <dl class="row mb-0">
                                <dt class="col-5 label-md text-on-surface-variant">Mã nhân viên</dt>
                                <dd class="col-7 fw-medium">${contract.employeeCode}</dd>

                                <dt class="col-5 label-md text-on-surface-variant">Họ và tên</dt>
                                <dd class="col-7 fw-medium">${contract.fullName}</dd>

                                <dt class="col-5 label-md text-on-surface-variant">Phòng ban</dt>
                                <dd class="col-7">${contract.departmentName}</dd>

                                <dt class="col-5 label-md text-on-surface-variant">Chức danh</dt>
                                <dd class="col-7">${contract.jobTitleName}</dd>

                                <dt class="col-5 label-md text-on-surface-variant">Quản lý trực tiếp</dt>
                                <dd class="col-7">${contract.managerName}</dd>
                            </dl>
                        </div>
                    </div>

                    <div class="col-12 col-md-6">
                        <div class="card-premium p-4 h-100">
                            <h3 class="h6 fw-bold text-on-surface mb-3 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined text-primary">description</span>
                                Thông tin hợp đồng
                            </h3>
                            <dl class="row mb-0">
                                <dt class="col-5 label-md text-on-surface-variant">Loại hợp đồng</dt>
                                <dd class="col-7 fw-medium">${contract.contractTypeName}
                                    <span class="label-sm text-on-surface-variant">(${contract.contractTypeCode})</span>
                                </dd>

                                <dt class="col-5 label-md text-on-surface-variant">Ngày bắt đầu</dt>
                                <dd class="col-7">${contract.startDate}</dd>

                                <dt class="col-5 label-md text-on-surface-variant">Ngày kết thúc</dt>
                                <dd class="col-7">
                                    <c:choose>
                                        <c:when test="${empty contract.endDate}">
                                            <span class="text-on-surface-variant">Không xác định</span>
                                        </c:when>
                                        <c:otherwise>${contract.endDate}</c:otherwise>
                                    </c:choose>
                                </dd>

                                <dt class="col-5 label-md text-on-surface-variant">Mức lương</dt>
                                <dd class="col-7 fw-medium">${contract.salary}</dd>

                                <dt class="col-5 label-md text-on-surface-variant">Trạng thái</dt>
                                <dd class="col-7">
                                    <c:choose>
                                        <c:when test="${contract.status == 'ACTIVE'}">
                                            <span class="badge" style="background-color: #d1fae5; color: #065f46;">Đang hiệu lực</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'EXPIRED'}">
                                            <span class="badge" style="background-color: #e5e7eb; color: #374151;">Hết hạn</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'PENDING_RENEWAL'}">
                                            <span class="badge" style="background-color: #fef3c7; color: #92400e;">Chờ gia hạn</span>
                                        </c:when>
                                        <c:when test="${contract.status == 'TERMINATED'}">
                                            <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Đã chấm dứt</span>
                                        </c:when>
                                    </c:choose>
                                </dd>
                            </dl>
                        </div>
                    </div>
                </div>

                <c:if test="${contract.status == 'TERMINATED'}">
                    <div class="card-premium p-4 mb-4" style="border-left: 4px solid #b91c1c;">
                        <h3 class="h6 fw-bold mb-3 d-flex align-items-center gap-2" style="color: #991b1b;">
                            <span class="material-symbols-outlined">block</span>
                            Thông tin chấm dứt
                        </h3>
                        <dl class="row mb-0">
                            <dt class="col-3 label-md text-on-surface-variant">Ngày chấm dứt</dt>
                            <dd class="col-9">${contract.terminatedAt}</dd>
                            <dt class="col-3 label-md text-on-surface-variant">Người chấm dứt</dt>
                            <dd class="col-9">${contract.terminatedByName}</dd>
                            <dt class="col-3 label-md text-on-surface-variant">Lý do</dt>
                            <dd class="col-9">${contract.terminateReason}</dd>
                        </dl>
                    </div>
                </c:if>

                <c:if test="${not empty contract.renewalOfId}">
                    <div class="card-premium p-4 mb-4" style="border-left: 4px solid var(--primary);">
                        <h3 class="h6 fw-bold text-on-surface mb-3 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined text-primary">autorenew</span>
                            Hợp đồng trước đó
                        </h3>
                        <p class="mb-2">
                            Hợp đồng này là bản gia hạn của
                            <a href="${ctx}/contract-detail?id=${contract.renewalOfId}" class="text-primary fw-medium">
                                hợp đồng #${contract.renewalOfCode}
                            </a>.
                        </p>
                        <dl class="row mb-0">
                            <dt class="col-3 label-md text-on-surface-variant">Thời hạn cũ</dt>
                            <dd class="col-9">
                                ${contract.renewalOfStartDate}
                                <c:if test="${not empty contract.renewalOfEndDate}">
                                    &rarr; ${contract.renewalOfEndDate}
                                </c:if>
                            </dd>
                        </dl>
                    </div>
                </c:if>

                <div class="card-premium p-4 mb-4">
                    <h3 class="h6 fw-bold text-on-surface mb-3 d-flex align-items-center gap-2">
                        <span class="material-symbols-outlined text-primary">attach_file</span>
                        Tệp hợp đồng
                    </h3>
                    <c:choose>
                        <c:when test="${empty contract.filePath}">
                            <p class="text-on-surface-variant mb-0">
                                Chưa có tệp PDF. Nhấn <strong>Tải PDF</strong> ở trên để tải lên.
                            </p>
                        </c:when>
                        <c:otherwise>
                            <p class="mb-2">
                                <span class="material-symbols-outlined align-middle me-1" style="color: #b91c1c;">picture_as_pdf</span>
                                <code>${contract.filePath}</code>
                            </p>
                            <a href="${ctx}${contract.filePath}" target="_blank" rel="noopener" class="btn btn-sm btn-light border">
                                Mở tệp
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
