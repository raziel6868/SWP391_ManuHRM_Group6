<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Bảng công tháng - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Bảng công tháng</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem, đóng và khóa bảng công theo tháng.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/monthly-sheet-list" method="GET" class="row g-3 align-items-end">
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                <select name="year" class="form-select input-premium">
                                    <c:forEach var="y" begin="2020" end="2030">
                                        <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng</label>
                                <select name="month" class="form-select input-premium">
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>${dept.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Trạng thái</label>
                                <select name="status" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <option value="OPEN" ${selectedStatus == 'OPEN' ? 'selected' : ''}>Mở</option>
                                    <option value="CLOSED" ${selectedStatus == 'CLOSED' ? 'selected' : ''}>Đã đóng</option>
                                </select>
                            </div>
                            <div class="col-md-1">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                            <div class="col-md-1">
                                <a href="${pageContext.request.contextPath}/monthly-sheet-list" class="btn btn-light border w-100">Reset</a>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Phòng ban</th>
                                    <th>Tháng</th>
                                    <th>Ngày công chuẩn</th>
                                    <th>Ngày công thực tế</th>
                                    <th>Trạng thái</th>
                                    <th>Người đóng</th>
                                    <th>Ngày đóng</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty sheets}">
                                        <tr>
                                            <td colspan="8" class="text-center py-4 text-on-surface-variant">
                                                Không có bảng công tháng.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="sheet" items="${sheets}">
                                            <tr>
                                                <td>${sheet.departmentName}</td>
                                                <td>${sheet.month}/${sheet.year}</td>
                                                <td>${sheet.totalWorkDays}</td>
                                                <td>${sheet.totalActualDays}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${sheet.status == 'OPEN'}">
                                                            <span class="badge" style="background-color: #d1fae5; color: #065f46;">Mở</span>
                                                        </c:when>
                                                        <c:when test="${sheet.status == 'CLOSED'}">
                                                            <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Đã đóng</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">${sheet.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>${sheet.closedByName != null ? sheet.closedByName : '-'}</td>
                                                <td>${sheet.closedAt != null ? sheet.closedAt : '-'}</td>
                                                <td class="text-end">
                                                    <c:if test="${sheet.status == 'OPEN'}">
                                                        <form action="${pageContext.request.contextPath}/monthly-sheet-close" method="POST" class="d-inline">
                                                            <input type="hidden" name="id" value="${sheet.id}">
                                                            <button type="submit" class="btn btn-sm" style="background-color: #d1fae5; color: #065f46; border: 1px solid #a7f3d0;">Đóng bảng</button>
                                                        </form>
                                                    </c:if>
                                                    <c:if test="${sheet.status == 'CLOSED'}">
                                                        <form action="${pageContext.request.contextPath}/monthly-sheet-reopen" method="POST" class="d-inline">
                                                            <input type="hidden" name="id" value="${sheet.id}">
                                                            <button type="submit" class="btn btn-sm" style="background-color: #fef3c7; color: #92400e; border: 1px solid #fde68a;">Mở lại</button>
                                                        </form>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
                            <div class="d-flex gap-1 flex-wrap">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="?page=${i}&year=${selectedYear}&month=${selectedMonth}&departmentId=${selectedDepartmentId}&status=${selectedStatus}"
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
