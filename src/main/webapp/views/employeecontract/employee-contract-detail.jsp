<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chi tiết Hợp đồng - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 800px; margin: 40px auto; width: 100%;">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Chi tiết Hợp đồng</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem thông tin chi tiết hợp đồng lao động.</p>
                    </div>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/contract-list"
                           class="btn btn-light border px-3 py-2 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">arrow_back</span>
                            Quay lại
                        </a>
                    </div>
                </div>

                <c:if test="${not empty successMsg}">
                    <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">check_circle</span>
                        ${successMsg}
                    </div>
                </c:if>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 mb-4">
                    <div class="row g-4">
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">Mã nhân viên</label>
                            <p class="fw-medium text-on-surface mb-0">${contract.employeeCode}</p>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">Nhân viên</label>
                            <p class="fw-medium text-on-surface mb-0">${contract.userFullName}</p>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">Loại hợp đồng</label>
                            <p class="fw-medium text-on-surface mb-0">${contract.contractTypeName}</p>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">Trạng thái</label>
                            <p class="mb-0">
                                <c:choose>
                                    <c:when test="${contract.status == 'ACTIVE'}">
                                        <span class="badge" style="background-color: #d1fae5; color: #065f46;">Hoạt động</span>
                                    </c:when>
                                    <c:when test="${contract.status == 'EXPIRED'}">
                                        <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Hết hạn</span>
                                    </c:when>
                                    <c:when test="${contract.status == 'PENDING_RENEWAL'}">
                                        <span class="badge" style="background-color: #fef3c7; color: #92400e;">Chờ gia hạn</span>
                                    </c:when>
                                    <c:when test="${contract.status == 'TERMINATED'}">
                                        <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Đã chấm dứt</span>
                                    </c:when>
                                </c:choose>
                            </p>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">Ngày bắt đầu</label>
                            <p class="fw-medium text-on-surface mb-0">
                                <fmt:formatDate value="${contract.startDate}" pattern="dd/MM/yyyy" />
                            </p>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">Ngày kết thúc</label>
                            <p class="fw-medium text-on-surface mb-0">
                                <c:choose>
                                    <c:when test="${contract.endDate != null}">
                                        <fmt:formatDate value="${contract.endDate}" pattern="dd/MM/yyyy" />
                                    </c:when>
                                    <c:otherwise>Không giới hạn</c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">Mức lương</label>
                            <p class="fw-medium text-on-surface mb-0">
                                <c:choose>
                                    <c:when test="${contract.salary != null}">
                                        <fmt:formatNumber value="${contract.salary}" pattern="#,###" /> VNĐ
                                    </c:when>
                                    <c:otherwise>-</c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">File hợp đồng</label>
                            <p class="mb-0">
                                <c:choose>
                                    <c:when test="${contract.filePath != null}">
                                        <a href="${pageContext.request.contextPath}/${contract.filePath}" target="_blank"
                                           class="btn btn-sm btn-outline-primary d-inline-flex align-items-center gap-1">
                                            <span class="material-symbols-outlined" style="font-size: 1rem;">download</span>
                                            Tải về PDF
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-on-surface-variant">Chưa có file</span>
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                        <c:if test="${contract.status == 'TERMINATED'}">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface-variant mb-1">Ngày chấm dứt</label>
                                <p class="fw-medium text-on-surface mb-0">
                                    <fmt:formatDate value="${contract.terminatedAt}" pattern="dd/MM/yyyy" />
                                </p>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface-variant mb-1">Người chấm dứt</label>
                                <p class="fw-medium text-on-surface mb-0">
                                    <c:out value="${contract.terminatedByName != null ? contract.terminatedByName : '-'}" />
                                </p>
                            </div>
                            <div class="col-12">
                                <label class="form-label text-on-surface-variant mb-1">Lý do chấm dứt</label>
                                <p class="text-on-surface mb-0"><c:out value="${contract.terminationReason}" /></p>
                            </div>
                        </c:if>
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">Ngày tạo</label>
                            <p class="text-on-surface mb-0">
                                <fmt:formatDate value="${contract.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                            </p>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label text-on-surface-variant mb-1">Cập nhật lần cuối</label>
                            <p class="text-on-surface mb-0">
                                <fmt:formatDate value="${contract.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                            </p>
                        </div>
                    </div>
                </div>

                <div class="d-flex gap-3 pt-3 border-top border-outline-variant flex-wrap">
                    <c:if test="${canUpdate}">
                        <a href="${pageContext.request.contextPath}/contract-update?id=${contract.id}"
                           class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">edit</span>
                            Chỉnh sửa
                        </a>
                    </c:if>
                    <c:if test="${canRenew && contract.status == 'ACTIVE'}">
                        <button type="button" class="btn btn-outline-primary px-4 py-2 d-flex align-items-center gap-2"
                                data-bs-toggle="modal" data-bs-target="#renewModal">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">autorenew</span>
                            Gia hạn
                        </button>
                    </c:if>
                    <c:if test="${canUpload}">
                        <button type="button" class="btn btn-outline-secondary px-4 py-2 d-flex align-items-center gap-2"
                                data-bs-toggle="modal" data-bs-target="#uploadModal">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">upload_file</span>
                            Tải lên PDF
                        </button>
                    </c:if>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <div class="modal fade" id="renewModal" tabindex="-1" aria-labelledby="renewModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="renewModalLabel">Gia hạn hợp đồng</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form action="${pageContext.request.contextPath}/contract-renew" method="POST">
                    <input type="hidden" name="id" value="${contract.id}" />
                    <div class="modal-body">
                        <p class="text-muted mb-3">Hợp đồng hiện tại sẽ được đánh dấu là hết hạn và một hợp đồng mới sẽ được tạo.</p>
                        <div class="mb-3">
                            <label class="form-label">Ngày bắt đầu mới <span class="text-danger">*</span></label>
                            <input type="date" name="newStartDate" class="form-control" required />
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Ngày kết thúc mới</label>
                            <input type="date" name="newEndDate" class="form-control" />
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Mức lương mới</label>
                            <input type="number" name="newSalary" class="form-control" min="0" step="1000"
                                   placeholder="VD: 8500000" />
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary">Gia hạn</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <div class="modal fade" id="uploadModal" tabindex="-1" aria-labelledby="uploadModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="uploadModalLabel">Tải lên hợp đồng (PDF)</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form action="${pageContext.request.contextPath}/contract-upload" method="POST" enctype="multipart/form-data">
                    <input type="hidden" name="id" value="${contract.id}" />
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label">Chọn file PDF <span class="text-danger">*</span></label>
                            <input type="file" name="file" class="form-control" accept=".pdf,application/pdf" required />
                            <div class="form-text mt-1">Kích thước tối đa: 5MB</div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary">Tải lên</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
