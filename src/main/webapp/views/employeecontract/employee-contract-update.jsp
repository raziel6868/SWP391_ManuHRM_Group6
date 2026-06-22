<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Cập nhật hợp đồng - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 720px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Cập nhật hợp đồng</h2>
                    <p class="body-md text-on-surface-variant mb-0">Chỉnh sửa thông tin hợp đồng lao động.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/contract-update" method="POST">
                        <input type="hidden" name="id" value="${contract.id}" />

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Nhân viên</label>
                                <input type="text" class="form-control input-premium"
                                       value="${contract.userFullName} (${contract.employeeCode})" disabled
                                       style="background-color: var(--surface-container-high);" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Trạng thái</label>
                                <input type="text" class="form-control input-premium"
                                       value="${contract.status}" disabled
                                       style="background-color: var(--surface-container-high);" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại hợp đồng <span class="text-danger">*</span></label>
                                <select name="contractTypeId" class="form-select input-premium">
                                    <c:forEach var="ct" items="${contractTypes}">
                                        <option value="${ct.id}" ${contract.contractTypeId == ct.id ? 'selected' : ''}>
                                            ${ct.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mức lương</label>
                                <input type="number" name="salary" class="form-control input-premium"
                                       value="${contract.salary}" min="0" step="1000"
                                       placeholder="VD: 8000000" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày bắt đầu <span class="text-danger">*</span></label>
                                <input type="date" name="startDate" class="form-control input-premium"
                                       value="${contract.startDate}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày kết thúc</label>
                                <input type="date" name="endDate" class="form-control input-premium"
                                       value="${contract.endDate}" />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Để trống nếu không có ngày kết thúc (hợp đồng không xác định thời hạn).
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface-variant mb-1">File hợp đồng hiện tại</label>
                                <p class="mb-0">
                                    <c:choose>
                                        <c:when test="${contract.filePath != null}">
                                            <a href="${pageContext.request.contextPath}/${contract.filePath}" target="_blank"
                                               class="text-primary text-decoration-none d-inline-flex align-items-center gap-1">
                                                <span class="material-symbols-outlined" style="font-size: 1rem;">description</span>
                                                Đã có file
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-on-surface-variant">Chưa có file</span>
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                Lưu thay đổi
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
