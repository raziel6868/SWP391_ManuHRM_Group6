<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý Chức danh - ManuHRM</title>
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

                <div class="d-flex justify-content-between align-items-end mb-4">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý Chức danh</h2>
                        <p class="body-md text-on-surface-variant mb-0">Quản lý danh mục chức danh trong công ty.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/job-title-create"
                        class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                        Thêm chức danh
                    </a>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant d-flex justify-content-between align-items-center">
                        <form action="${pageContext.request.contextPath}/job-title-list" method="GET"
                            class="d-flex position-relative" style="width: 280px;">
                            <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                style="left: 12px; font-size: 1.25rem;">search</span>
                            <input type="text" name="keyword" value="${keyword}"
                                class="input-premium w-100 py-1" placeholder="Tìm kiếm chức danh..."
                                style="padding-left: 2.5rem;" />
                            <button type="submit" class="d-none"></button>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Tên chức danh</th>
                                    <th>Mô tả</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="jt" items="${jobTitles}">
                                    <tr <c:if test="${!jt.isActive}">style="background-color: rgba(255,255,255,0.5); opacity: 0.8;"</c:if>>
                                        <td class="fw-medium text-on-surface">${jt.id}</td>
                                        <td class="fw-medium text-on-surface">${jt.name}</td>
                                        <td class="text-on-surface-variant text-truncate" style="max-width: 300px;">${jt.description}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${jt.isActive}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Kích hoạt</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Vô hiệu hóa</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-flex justify-content-end gap-1">
                                                <c:if test="${hasUpdatePerm}">
                                                    <a href="${pageContext.request.contextPath}/job-title-update?id=${jt.id}"
                                                        class="btn btn-sm btn-icon text-on-surface-variant hover-primary" title="Sửa">
                                                        <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                    </a>
                                                </c:if>
                                                <c:if test="${hasStatusPerm}">
                                                    <form action="${pageContext.request.contextPath}/job-title-status" method="POST" class="d-inline m-0">
                                                        <input type="hidden" name="id" value="${jt.id}" />
                                                        <input type="hidden" name="isActive" value="${!jt.isActive}" />
                                                        <button type="submit" class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                            title="${jt.isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">${jt.isActive ? 'lock' : 'lock_open'}</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty jobTitles}">
                                    <tr>
                                        <td colspan="5" class="text-center py-4 text-on-surface-variant">Không tìm thấy chức danh nào.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
                            <div class="d-flex gap-1">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${pageContext.request.contextPath}/job-title-list?page=${i}&keyword=${keyword}"
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
