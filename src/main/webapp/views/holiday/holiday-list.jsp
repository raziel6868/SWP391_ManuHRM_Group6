<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý Ngày lễ - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý Ngày lễ</h2>
                        <p class="body-md text-on-surface-variant mb-0">Quản lý danh mục ngày lễ trong công ty.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/holiday-create"
                        class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                        Thêm ngày lễ
                    </a>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/holiday-list" method="GET"
                            class="d-flex gap-3 align-items-center flex-wrap">
                            <div class="position-relative" style="flex: 1; min-width: 200px;">
                                <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                    style="left: 12px; font-size: 1.25rem;">search</span>
                                <input type="text" name="keyword" value="${keyword}"
                                    class="input-premium w-100 py-1" placeholder="Tìm kiếm ngày lễ..."
                                    style="padding-left: 2.5rem;" />
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <select name="year" class="input-premium">
                                    <c:forEach var="y" begin="2020" end="2030">
                                        <option value="${y}" ${y == year ? 'selected' : ''}>${y}</option>
                                    </c:forEach>
                                </select>
                                <button type="submit" class="btn btn-primary">Tìm kiếm</button>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Ngày</th>
                                    <th>Tên ngày lễ</th>
                                    <th>Mô tả</th>
                                    <th>Lặp lại hàng năm</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="h" items="${holidays}">
                                    <tr>
                                        <td class="fw-medium text-on-surface">${h.id}</td>
                                        <td class="fw-medium text-on-surface">
                                            <fmt:formatDate value="${h.date}" pattern="dd/MM/yyyy" />
                                        </td>
                                        <td class="fw-medium text-on-surface">${h.name}</td>
                                        <td class="text-on-surface-variant text-truncate" style="max-width: 250px;">${h.description}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${h.recurring}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Có</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Không</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${h.active}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Không hoạt động</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-flex justify-content-end gap-1">
                                                <a href="${pageContext.request.contextPath}/holiday-update?id=${h.id}"
                                                    class="btn btn-sm btn-icon text-on-surface-variant hover-primary" title="Sửa">
                                                    <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                </a>
                                                <c:choose>
                                                    <c:when test="${h.active}">
                                                        <form action="${pageContext.request.contextPath}/holiday-toggle" method="POST" class="d-inline m-0">
                                                            <input type="hidden" name="id" value="${h.id}" />
                                                            <input type="hidden" name="action" value="deactivate" />
                                                            <button type="submit" class="btn btn-sm btn-icon text-on-surface-variant hover-error"
                                                                title="Vô hiệu hóa" onclick="return confirm('Bạn có chắc muốn vô hiệu hóa ngày lễ này?')">
                                                                <span class="material-symbols-outlined" style="font-size: 1.25rem;">toggle_off</span>
                                                            </button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form action="${pageContext.request.contextPath}/holiday-toggle" method="POST" class="d-inline m-0">
                                                            <input type="hidden" name="id" value="${h.id}" />
                                                            <input type="hidden" name="action" value="activate" />
                                                            <button type="submit" class="btn btn-sm btn-icon hover-success"
                                                                title="Kích hoạt">
                                                                <span class="material-symbols-outlined text-success" style="font-size: 1.25rem;">toggle_on</span>
                                                            </button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty holidays}">
                                    <tr>
                                        <td colspan="7" class="text-center text-on-surface-variant py-4">
                                            Không có ngày lễ nào trong năm ${year}.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 border-top border-outline-variant d-flex justify-content-center">
                            <nav>
                                <ul class="pagination mb-0">
                                    <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="?page=${currentPage - 1}&year=${year}&keyword=${keyword}">Trước</a>
                                    </li>
                                    <c:forEach begin="1" end="${totalPages}" var="i">
                                        <li class="page-item ${i == currentPage ? 'active' : ''}">
                                            <a class="page-link" href="?page=${i}&year=${year}&keyword=${keyword}">${i}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="?page=${currentPage + 1}&year=${year}&keyword=${keyword}">Sau</a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
