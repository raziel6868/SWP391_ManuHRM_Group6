<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Tạo hợp đồng - ManuHRM</title>
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
                        <li class="breadcrumb-item active" aria-current="page">Tạo mới</li>
                    </ol>
                </nav>

                <h2 class="h3 text-on-surface fw-bold mb-1">Tạo hợp đồng lao động</h2>
                <p class="body-md text-on-surface-variant mb-4">
                    Hợp đồng mới sẽ ở trạng thái <strong>Đang hiệu lực</strong>. Bạn có thể tải lên file PDF ở bước tiếp theo.
                </p>

                <form action="${ctx}/contract-create" method="POST" class="card-premium p-4">
                    <div class="mb-3">
                        <label for="userId" class="form-label fw-medium">Nhân viên <span class="text-danger">*</span></label>
                        <select id="userId" name="userId" class="form-select" required>
                            <option value="">-- Chọn nhân viên --</option>
                            <c:forEach var="u" items="${users}">
                                <option value="${u.id}" ${selectedUserId == u.id ? 'selected' : ''}>
                                    ${u.fullName} (${u.employeeCode}) - ${u.departmentName}
                                </option>
                            </c:forEach>
                        </select>
                        <div class="form-text">Chỉ hiển thị nhân viên đang hoạt động. Nhân viên đã có hợp đồng hiệu lực sẽ bị từ chối.</div>
                    </div>

                    <div class="mb-3">
                        <label for="contractTypeId" class="form-label fw-medium">Loại hợp đồng <span class="text-danger">*</span></label>
                        <select id="contractTypeId" name="contractTypeId" class="form-select" required>
                            <option value="">-- Chọn loại hợp đồng --</option>
                            <c:forEach var="ct" items="${contractTypes}">
                                <option value="${ct.id}" ${selectedContractTypeId == ct.id ? 'selected' : ''}>
                                    ${ct.name} (${ct.code})
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="row g-3 mb-3">
                        <div class="col-12 col-md-6">
                            <label for="startDate" class="form-label fw-medium">Ngày bắt đầu <span class="text-danger">*</span></label>
                            <input type="date" id="startDate" name="startDate" value="${startDate}"
                                class="input-premium w-100" required />
                        </div>
                        <div class="col-12 col-md-6">
                            <label for="endDate" class="form-label fw-medium">Ngày kết thúc</label>
                            <input type="date" id="endDate" name="endDate" value="${endDate}"
                                class="input-premium w-100" />
                            <div class="form-text">Để trống nếu là hợp đồng không xác định thời hạn.</div>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="salary" class="form-label fw-medium">Mức lương (VNĐ)</label>
                        <input type="number" id="salary" name="salary" value="${salary}" min="0" step="1000"
                            class="input-premium w-100" placeholder="Ví dụ: 8000000" />
                        <div class="form-text">Không bắt buộc. Có thể cập nhật sau.</div>
                    </div>

                    <div class="d-flex justify-content-end gap-2">
                        <a href="${ctx}/contract-list" class="btn btn-light border">Hủy</a>
                        <button type="submit" class="btn-primary-gradient text-decoration-none px-3 py-2 border-0 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                            Tạo hợp đồng
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
