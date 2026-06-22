<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thêm hợp đồng - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 720px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Thêm hợp đồng mới</h2>
                    <p class="body-md text-on-surface-variant mb-0">Tạo hợp đồng lao động cho nhân viên.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/contract-create" method="POST">
                        <div class="row g-4 mb-4">
                            <div class="col-md-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Nhân viên <span class="text-danger">*</span></label>
                                <select name="userId" class="form-select input-premium">
                                    <option value="">-- Chọn nhân viên --</option>
                                    <c:forEach var="emp" items="${employees}">
                                        <option value="${emp.id}" ${userId == emp.id ? 'selected' : ''}>
                                            ${emp.employeeCode} - ${emp.fullName}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại hợp đồng <span class="text-danger">*</span></label>
                                <select name="contractTypeId" class="form-select input-premium">
                                    <option value="">-- Chọn loại hợp đồng --</option>
                                    <c:forEach var="ct" items="${contractTypes}">
                                        <option value="${ct.id}" ${contractTypeId == ct.id ? 'selected' : ''}>
                                            ${ct.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mức lương</label>
                                <input type="number" name="salary" class="form-control input-premium"
                                       value="${salary}" min="0" step="1000"
                                       placeholder="VD: 8000000" />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Để trống nếu không có thông tin lương.
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày bắt đầu <span class="text-danger">*</span></label>
                                <input type="date" name="startDate" class="form-control input-premium"
                                       value="${startDate}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày kết thúc</label>
                                <input type="date" name="endDate" class="form-control input-premium"
                                       value="${endDate}" />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Để trống nếu không có ngày kết thúc (hợp đồng không xác định thời hạn).
                                </div>
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                                Thêm hợp đồng
                            </button>
                            <a href="${pageContext.request.contextPath}/contract-list"
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
