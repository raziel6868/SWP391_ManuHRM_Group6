<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Yêu cầu Nghỉ phép - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Yêu cầu Nghỉ phép</h2>
                        <p class="body-md text-on-surface-variant mb-0">Danh sách yêu cầu nghỉ phép của nhân viên.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/leave-request-list" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Từ khóa</label>
                                <div class="position-relative">
                                    <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                          style="left: 12px; font-size: 1.25rem;">search</span>
                                    <input type="text" name="keyword" value="${keyword}"
                                           class="form-control input-premium ps-5"
                                           placeholder="Tìm theo tên, mã NV..." />
                                </div>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Trạng thái</label>
                                <select name="status" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>Chờ duyệt</option>
                                    <option value="APPROVED_LEVEL_1" ${selectedStatus == 'APPROVED_LEVEL_1' ? 'selected' : ''}>Đã duyệt Cấp 1</option>
                                    <option value="APPROVED" ${selectedStatus == 'APPROVED' ? 'selected' : ''}>Đã duyệt</option>
                                    <option value="REJECTED" ${selectedStatus == 'REJECTED' ? 'selected' : ''}>Từ chối</option>
                                    <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>${dept.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-1 d-flex gap-2">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                            <div class="col-md-1">
                                <a href="${pageContext.request.contextPath}/leave-request-list" class="btn btn-light border w-100">Reset</a>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã NV</th>
                                    <th>Nhân viên</th>
                                    <th>Loại nghỉ</th>
                                    <th>Từ ngày</th>
                                    <th>Đến ngày</th>
                                    <th>Số ngày</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="req" items="${requests}">
                                    <tr>
                                        <td class="fw-medium text-on-surface">${req.employeeCode}</td>
                                        <td>${req.userFullName}</td>
                                        <td>${req.leaveTypeName}</td>
                                        <td>${req.startDate}</td>
                                        <td>${req.endDate}</td>
                                        <td>${req.days.toBigInteger()}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${req.status == 'PENDING'}">
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">Chờ duyệt</span>
                                                </c:when>
                                                <c:when test="${req.status == 'APPROVED_LEVEL_1'}">
                                                    <span class="badge" style="background-color: #dbeafe; color: #1e40af;">Đã duyệt Cấp 1</span>
                                                </c:when>
                                                <c:when test="${req.status == 'APPROVED'}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Đã duyệt</span>
                                                </c:when>
                                                <c:when test="${req.status == 'REJECTED'}">
                                                    <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Từ chối</span>
                                                </c:when>
                                                <c:when test="${req.status == 'CANCELLED'}">
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Đã hủy</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-flex justify-content-end gap-1">
                                                <c:if test="${req.status == 'PENDING'}">
                                                    <form action="${pageContext.request.contextPath}/leave-request-approve" method="POST" class="d-inline m-0">
                                                        <input type="hidden" name="id" value="${req.id}" />
                                                        <button type="submit" class="btn btn-sm btn-icon text-success hover-primary" title="Duyệt Cấp 1"
                                                                onclick="return confirm('Duyệt yêu cầu này?');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">check</span>
                                                        </button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/leave-request-reject" method="POST" class="d-inline m-0">
                                                        <input type="hidden" name="id" value="${req.id}" />
                                                        <button type="submit" class="btn btn-sm btn-icon text-danger hover-danger" title="Từ chối"
                                                                onclick="return confirm('Từ chối yêu cầu này?');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">close</span>
                                                        </button>
                                                </c:if>
                                                <c:if test="${req.status == 'APPROVED_LEVEL_1'}">
                                                    <form action="${pageContext.request.contextPath}/leave-request-final-approve" method="POST" class="d-inline m-0">
                                                        <input type="hidden" name="id" value="${req.id}" />
                                                        <button type="submit" class="btn btn-sm btn-icon text-success hover-primary" title="Phê duyệt cuối"
                                                                onclick="return confirm('Phê duyệt yêu cầu này?');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">done_all</span>
                                                        </button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/leave-request-reject" method="POST" class="d-inline m-0">
                                                        <input type="hidden" name="id" value="${req.id}" />
                                                        <button type="submit" class="btn btn-sm btn-icon text-danger hover-danger" title="Từ chối"
                                                                onclick="return confirm('Từ chối yêu cầu này?');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">close</span>
                                                        </button>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty requests}">
                                    <tr>
                                        <td colspan="8" class="text-center py-4 text-on-surface-variant">
                                            Không có yêu cầu nghỉ phép nào.
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
                                    <a href="${pageContext.request.contextPath}/leave-request-list?page=${i}&keyword=${keyword}&status=${selectedStatus}&departmentId=${selectedDepartmentId}"
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
