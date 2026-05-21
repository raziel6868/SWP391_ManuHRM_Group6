<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý vai trò - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <%-- Notifications --%>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý vai trò</h2>
                        <p class="body-md text-on-surface-variant mb-0">Quản lý quyền truy cập và phân quyền chức năng.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/role-create"
                        class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                        Thêm vai trò
                    </a>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant d-flex justify-content-between align-items-center">
                        <form action="${pageContext.request.contextPath}/role-list" method="GET"
                            class="d-flex position-relative" style="width: 280px;">
                            <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                style="left: 12px; font-size: 1.25rem;">search</span>
                            <input type="text" name="keyword" value="${keyword}"
                                class="input-premium w-100 py-1" placeholder="Tìm kiếm vai trò..."
                                style="padding-left: 2.5rem;" />
                            <button type="submit" class="d-none"></button>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã hệ thống</th>
                                    <th>Tên hiển thị</th>
                                    <th>Mô tả</th>
                                    <th>Vai trò hệ thống</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="r" items="${roles}">
                                    <tr <c:if test="${!r.isActive}">style="background-color: rgba(255,255,255,0.5); opacity: 0.8;"</c:if>>
                                        <td class="fw-medium text-on-surface">${r.name}</td>
                                        <td>${r.displayName}</td>
                                        <td class="text-on-surface-variant text-truncate" style="max-width: 250px;">${r.description}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${r.isSystem}">
                                                    <span class="badge border d-inline-flex align-items-center text-on-surface fw-medium"
                                                        style="background-color: var(--surface-container-high); border-color: var(--outline-variant) !important;">
                                                        <span class="material-symbols-outlined me-1" style="font-size: 0.875rem;">shield</span> Hệ thống
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-on-surface-variant fw-bold">-</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${r.isActive}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Không hoạt động</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-flex justify-content-end gap-1">
                                                <a href="${pageContext.request.contextPath}/role-update?id=${r.id}"
                                                    class="btn btn-sm btn-icon text-on-surface-variant hover-primary" title="Sửa">
                                                    <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                </a>
                                                <a href="${pageContext.request.contextPath}/role-permission?id=${r.id}"
                                                    class="btn btn-sm btn-icon text-on-surface-variant hover-primary" title="Phân quyền">
                                                    <span class="material-symbols-outlined" style="font-size: 1.25rem;">key</span>
                                                </a>
                                                <c:if test="${r.name == 'LINE_MANAGER' || r.name == 'EMPLOYEE'}">
                                                    <form action="${pageContext.request.contextPath}/role-status" method="POST" class="d-inline m-0">
                                                        <input type="hidden" name="id" value="${r.id}" />
                                                        <input type="hidden" name="isActive" value="${!r.isActive}" />
                                                        <button type="submit" class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                            title="${r.isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">${r.isActive ? 'lock' : 'lock_open'}</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty roles}">
                                    <tr>
                                        <td colspan="6" class="text-center py-4 text-on-surface-variant">Không tìm thấy vai trò nào.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
                            <div class="d-flex gap-1">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${pageContext.request.contextPath}/role-list?page=${i}&keyword=${keyword}"
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
