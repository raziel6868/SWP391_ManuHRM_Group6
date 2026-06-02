<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Quản lý phòng ban - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">

                <%-- Flash messages --%>
                <c:if test="${not empty successMsg}">
                    <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">check_circle</span>
                        <div>${successMsg}</div>
                    </div>
                </c:if>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <%-- Page header --%>
                <div class="d-flex justify-content-between align-items-end mb-4">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý phòng ban</h2>
                        <p class="body-md text-on-surface-variant mb-0">Danh sách phòng ban (${totalRecords} phòng ban)</p>
                    </div>
                    <c:if test="${canCreate}">
                        <a href="${pageContext.request.contextPath}/department-create"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add_business</span>
                            Thêm phòng ban
                        </a>
                    </c:if>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">

                    <%-- Filter form --%>
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/department-list" method="GET"
                              class="row g-2 align-items-center">
                            <div class="col-md-4 position-relative">
                                <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                      style="left: 12px; font-size: 1.25rem;">search</span>
                                <input type="text" name="keyword" value="${keyword}"
                                       class="input-premium w-100 py-1"
                                       placeholder="Tìm tên phòng ban..."
                                       style="padding-left: 2.5rem;"/>
                            </div>
                            <div class="col-md-3">
                                <select name="departmentType" class="form-select input-premium">
                                    <option value="">-- Loại --</option>
                                    <option value="OFFICE"  ${selectedType == 'OFFICE'  ? 'selected' : ''}>Văn phòng</option>
                                    <option value="FACTORY" ${selectedType == 'FACTORY' ? 'selected' : ''}>Nhà máy</option>
                                </select>
                            </div>
                            <div class="col-md-3 d-flex gap-2">
                                <button type="submit" class="btn btn-primary px-3 flex-grow-1">Tìm kiếm</button>
                                <a href="${pageContext.request.contextPath}/department-list"
                                   class="btn btn-light border">Đặt lại</a>
                            </div>
                        </form>
                    </div>

                    <%-- Table --%>
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Tên phòng ban</th>
                                    <th>Loại</th>
                                    <th>Phòng ban cha</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty departments}">
                                        <tr>
                                            <td colspan="5" class="text-center py-4 text-on-surface-variant">
                                                Không tìm thấy phòng ban nào.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="d" items="${departments}">
                                            <tr <c:if test="${!d.isActive}">style="opacity: 0.6;"</c:if>>
                                                <td class="fw-bold">${d.name}</td>
                                                <td>
                                                    <span class="badge border bg-surface-container-high text-on-surface">
                                                        ${d.departmentType == 'OFFICE' ? 'Văn phòng' : 'Nhà máy'}
                                                    </span>
                                                </td>
                                                <td class="text-on-surface-variant">
                                                    ${not empty d.parentName ? d.parentName : '—'}
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${d.isActive}">
                                                            <span class="badge" style="background-color: #d1fae5; color: #065f46;">Hoạt động</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Không hoạt động</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end">
                                                    <div class="d-flex justify-content-end gap-1">
                                                        <c:if test="${canUpdate}">
                                                            <a href="${pageContext.request.contextPath}/department-update?id=${d.id}"
                                                               class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                               title="Chỉnh sửa">
                                                                <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                            </a>
                                                        </c:if>
                                                        <c:if test="${canChangeStatus}">
                                                            <c:choose>
                                                                <c:when test="${d.isActive}">
                                                                    <form method="post"
                                                                          action="${pageContext.request.contextPath}/department-status"
                                                                          class="d-inline m-0"
                                                                          onsubmit="return confirm('Vô hiệu hóa phòng ban: ${d.name}?')">
                                                                        <input type="hidden" name="id" value="${d.id}" />
                                                                        <input type="hidden" name="isActive" value="false" />
                                                                        <button type="submit"
                                                                                class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                                                title="Vô hiệu hóa">
                                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">block</span>
                                                                        </button>
                                                                    </form>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <form method="post"
                                                                          action="${pageContext.request.contextPath}/department-status"
                                                                          class="d-inline m-0"
                                                                          onsubmit="return confirm('Kích hoạt phòng ban: ${d.name}?')">
                                                                        <input type="hidden" name="id" value="${d.id}" />
                                                                        <input type="hidden" name="isActive" value="true" />
                                                                        <button type="submit"
                                                                                class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                                                title="Kích hoạt">
                                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">check_circle</span>
                                                                        </button>
                                                                    </form>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <%-- Pagination --%>
                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-between">
                            <span class="body-sm text-on-surface-variant">Trang ${currentPage} / ${totalPages}</span>
                            <div class="d-flex gap-1">
                                <c:if test="${currentPage > 1}">
                                    <a href="${pageContext.request.contextPath}/department-list?page=${currentPage - 1}&keyword=${keyword}&departmentType=${selectedType}"
                                       class="btn btn-sm btn-light border text-on-surface-variant">«</a>
                                </c:if>
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${pageContext.request.contextPath}/department-list?page=${i}&keyword=${keyword}&departmentType=${selectedType}"
                                       class="btn btn-sm ${i == currentPage ? 'fw-bold' : 'btn-light border text-on-surface-variant'}"
                                       style="${i == currentPage ? 'background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant); border: 1px solid var(--primary);' : 'background-color: var(--surface-container-lowest); border-color: var(--outline-variant) !important;'}">
                                        ${i}
                                    </a>
                                </c:forEach>
                                <c:if test="${currentPage < totalPages}">
                                    <a href="${pageContext.request.contextPath}/department-list?page=${currentPage + 1}&keyword=${keyword}&departmentType=${selectedType}"
                                       class="btn btn-sm btn-light border text-on-surface-variant">»</a>
                                </c:if>
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
