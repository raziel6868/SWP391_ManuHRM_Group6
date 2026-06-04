<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý ca làm việc - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <c:if test="${not empty successMsg}">
                    <div class="alert alert-success d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">check_circle</span>
                        ${successMsg}
                    </div>
                </c:if>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        ${errorMsg}
                    </div>
                </c:if>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý ca làm việc</h2>
                        <p class="body-md text-on-surface-variant mb-0">Quản lý danh mục ca làm việc cho hệ thống.</p>
                    </div>
                    <c:if test="${canCreate}">
                        <a href="${pageContext.request.contextPath}/shift-create"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                            Thêm ca làm việc
                        </a>
                    </c:if>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/shift-list" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-5">
                                <label class="form-label text-on-surface fw-medium mb-1">Từ khóa</label>
                                <div class="position-relative">
                                    <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                          style="left: 12px; font-size: 1.25rem;">search</span>
                                    <input type="text" name="keyword" value="${keyword}"
                                           class="form-control input-premium ps-5"
                                           placeholder="Tìm theo mã hoặc tên ca..." />
                                </div>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại ca</label>
                                <select name="isNightShift" class="form-select input-premium">
                                    <option value="" ${empty selectedIsNightShift ? 'selected' : ''}>Tất cả</option>
                                    <option value="true" ${selectedIsNightShift == 'true' ? 'selected' : ''}>Ca đêm</option>
                                    <option value="false" ${selectedIsNightShift == 'false' ? 'selected' : ''}>Ca ngày</option>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Trạng thái</label>
                                <select name="status" class="form-select input-premium">
                                    <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả</option>
                                    <option value="true" ${selectedStatus == 'true' ? 'selected' : ''}>Hoạt động</option>
                                    <option value="false" ${selectedStatus == 'false' ? 'selected' : ''}>Vô hiệu hóa</option>
                                </select>
                            </div>
                            <div class="col-md-1 d-flex gap-2">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã</th>
                                    <th>Tên ca</th>
                                    <th>Giờ bắt đầu</th>
                                    <th>Giờ kết thúc</th>
                                    <th>Nghỉ (phút)</th>
                                    <th>Loại ca</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="shift" items="${shifts}">
                                    <tr <c:if test="${!shift.isActive}">style="background-color: rgba(255,255,255,0.5); opacity: 0.8;"</c:if>>
                                        <td class="fw-medium text-on-surface">${shift.code}</td>
                                        <td>${shift.name}</td>
                                        <td>${shift.startTime}</td>
                                        <td>${shift.endTime}</td>
                                        <td class="text-center">${shift.breakMinutes}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${shift.isNightShift}">
                                                    <span class="badge" style="background-color: #e0e7ff; color: #3730a3;">Ca đêm</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">Ca ngày</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${shift.isActive}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Vô hiệu hóa</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-flex justify-content-end gap-1">
                                                <c:if test="${canUpdate}">
                                                    <a href="${pageContext.request.contextPath}/shift-update?id=${shift.id}"
                                                       class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                       title="Sửa ca làm việc">
                                                        <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                    </a>
                                                </c:if>
                                                <c:if test="${canChangeStatus}">
                                                    <form action="${pageContext.request.contextPath}/shift-status" method="POST" class="d-inline m-0">
                                                        <input type="hidden" name="id" value="${shift.id}" />
                                                        <input type="hidden" name="isActive" value="${!shift.isActive}" />
                                                        <button type="submit"
                                                                class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                                title="${shift.isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}"
                                                                onclick="return confirm('${shift.isActive ? 'Bạn có chắc muốn vô hiệu hóa ca làm việc này?' : 'Bạn có chắc muốn kích hoạt lại ca làm việc này?'}');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">${shift.isActive ? 'lock' : 'lock_open'}</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty shifts}">
                                    <tr>
                                        <td colspan="8" class="text-center py-4 text-on-surface-variant">
                                            Không tìm thấy ca làm việc phù hợp.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
                            <div class="d-flex gap-1 flex-wrap">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${pageContext.request.contextPath}/shift-list?page=${i}&keyword=${keyword}&isNightShift=${selectedIsNightShift}&status=${selectedStatus}"
                                       class="btn btn-sm ${i == currentPage ? 'fw-bold' : 'btn-light border text-on-surface-variant'}"
                                       style="${i == currentPage ? 'background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant); border: 1px solid var(--primary);' : 'background-color: var(--surface-container-lowest); border-color: var(--outline-variant) !important;'}">
                                        ${i}
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
