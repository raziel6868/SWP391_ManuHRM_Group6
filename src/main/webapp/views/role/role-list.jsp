<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>Role Management - ManuHRM</title>
            <!-- Bootstrap 5 CSS -->
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
            <!-- Custom CSS -->
            <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet" />
            <!-- Google Fonts Preconnect & Load -->
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
                rel="stylesheet">
            <link
                href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"
                rel="stylesheet">
        </head>

        <body class="bg-background text-on-surface">

            <div class="layout-wrapper">
                <jsp:include page="/components/sidebar.jsp" />

                <div class="main-content">
                    <jsp:include page="/components/header.jsp" />

                    <div class="page-container">
                        <!-- Page Header -->
                        <div class="d-flex justify-content-between align-items-end mb-4">
                            <div>
                                <h2 class="h3 text-on-surface fw-bold mb-1">Role Management</h2>
                                <p class="body-md text-on-surface-variant mb-0">Manage system access levels and
                                    functional permissions.</p>
                            </div>
                            <a href="${pageContext.request.contextPath}/role-update"
                                class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                                Create Role
                            </a>
                        </div>

                        <!-- Data Table Container -->
                        <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                            <!-- Table Controls -->
                            <div
                                class="p-3 bg-surface border-bottom border-outline-variant d-flex justify-content-between align-items-center">
                                <form action="${pageContext.request.contextPath}/role-list" method="GET"
                                    class="d-flex position-relative" style="width: 280px;">
                                    <span
                                        class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                        style="left: 12px; font-size: 1.25rem;">search</span>
                                    <input type="text" name="keyword" value="${keyword}"
                                        class="input-premium w-100 py-1" placeholder="Filter roles..."
                                        style="padding-left: 2.5rem;" />
                                    <button type="submit" class="d-none"></button>
                                </form>
                            </div>

                            <!-- Table -->
                            <div class="table-responsive">
                                <table class="table table-premium mb-0 w-100">
                                    <thead>
                                        <tr>
                                            <th>Name</th>
                                            <th>Display Name</th>
                                            <th>Description</th>
                                            <th>System Role</th>
                                            <th>Status</th>
                                            <th class="text-end">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="r" items="${roles}">
                                            <tr <c:if test="${!r.isActive}">style="background-color:
                                                rgba(255,255,255,0.5); opacity: 0.8;"</c:if>>
                                                <td class="fw-medium text-on-surface">${r.name}</td>
                                                <td>${r.displayName}</td>
                                                <td class="text-on-surface-variant text-truncate"
                                                    style="max-width: 250px;">${r.description}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${r.isSystem}">
                                                            <span
                                                                class="badge border d-inline-flex align-items-center text-on-surface fw-medium"
                                                                style="background-color: var(--surface-container-high); border-color: var(--outline-variant) !important;">
                                                                <span class="material-symbols-outlined me-1"
                                                                    style="font-size: 0.875rem;">shield</span> System
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
                                                            <span class="badge"
                                                                style="background-color: #d1fae5; color: #065f46;">Active</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge"
                                                                style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Inactive</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end">
                                                    <div class="d-flex justify-content-end gap-1">
                                                        <a href="${pageContext.request.contextPath}/role-update?id=${r.id}"
                                                            class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                            title="Edit">
                                                            <span class="material-symbols-outlined"
                                                                style="font-size: 1.25rem;">edit</span>
                                                        </a>
                                                        <a href="${pageContext.request.contextPath}/role-permission?id=${r.id}"
                                                            class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                            title="Permissions">
                                                            <span class="material-symbols-outlined"
                                                                style="font-size: 1.25rem;">key</span>
                                                        </a>
                                                        <c:if test="${!r.isSystem}">
                                                            <form
                                                                action="${pageContext.request.contextPath}/role-status"
                                                                method="POST" class="d-inline m-0">
                                                                <input type="hidden" name="id" value="${r.id}" />
                                                                <input type="hidden" name="isActive"
                                                                    value="${!r.isActive}" />
                                                                <button type="submit"
                                                                    class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                                    title="${r.isActive ? 'Deactivate' : 'Activate'}">
                                                                    <span class="material-symbols-outlined"
                                                                        style="font-size: 1.25rem;">${r.isActive ?
                                                                        'lock' : 'lock_open'}</span>
                                                                </button>
                                                            </form>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty roles}">
                                            <tr>
                                                <td colspan="6" class="text-center py-4 text-on-surface-variant">No
                                                    roles found.</td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>

                            <!-- Pagination -->
                            <c:if test="${totalPages > 1}">
                                <div
                                    class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
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

            <!-- Bootstrap JS -->
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
            <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
        </body>

        </html>