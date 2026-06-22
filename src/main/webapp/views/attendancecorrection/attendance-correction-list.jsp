<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Yêu cầu sửa chấm công - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Yêu cầu sửa chấm công</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem và xử lý yêu cầu sửa chấm công.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/attendance-correction-list" method="GET" class="row g-3 align-items-end">
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Trạng thái</label>
                                <select name="status" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <option value="PENDING" ${filterStatus == 'PENDING' ? 'selected' : ''}>Đang chờ</option>
                                    <option value="APPROVED" ${filterStatus == 'APPROVED' ? 'selected' : ''}>Đã duyệt</option>
                                    <option value="REJECTED" ${filterStatus == 'REJECTED' ? 'selected' : ''}>Đã từ chối</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${filterDepartmentId == dept.id ? 'selected' : ''}>${dept.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-1">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                            <div class="col-md-1">
                                <a href="${pageContext.request.contextPath}/attendance-correction-list" class="btn btn-light border w-100">Reset</a>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Nhân viên</th>
                                    <th>Ngày chấm công</th>
                                    <th>Check-in mới</th>
                                    <th>Check-out mới</th>
                                    <th>Lý do</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty corrections}">
                                        <tr>
                                            <td colspan="7" class="text-center py-4 text-on-surface-variant">
                                                Không có yêu cầu sửa chấm công.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="correction" items="${corrections}">
                                            <tr>
                                                <td>${correction.attendanceUserName}</td>
                                                <td>${correction.attendanceDate}</td>
                                                <td>${correction.newCheckIn}</td>
                                                <td>${correction.newCheckOut}</td>
                                                <td>${correction.reason}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${correction.status == 'PENDING'}">
                                                            <span class="badge" style="background-color: #fef3c7; color: #92400e;">Đang chờ</span>
                                                        </c:when>
                                                        <c:when test="${correction.status == 'APPROVED'}">
                                                            <span class="badge" style="background-color: #d1fae5; color: #065f46;">Đã duyệt</span>
                                                        </c:when>
                                                        <c:when test="${correction.status == 'REJECTED'}">
                                                            <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Đã từ chối</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">${correction.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end">
                                                    <c:if test="${correction.status == 'PENDING'}">
                                                        <form action="${pageContext.request.contextPath}/attendance-correction-approve" method="POST" class="d-inline">
                                                            <input type="hidden" name="id" value="${correction.id}">
                                                            <button type="submit" class="btn btn-sm me-1" style="background-color: #d1fae5; color: #065f46; border: 1px solid #a7f3d0;">Duyệt</button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/attendance-correction-reject" method="POST" class="d-inline">
                                                            <input type="hidden" name="id" value="${correction.id}">
                                                            <button type="submit" class="btn btn-sm" style="background-color: #fee2e2; color: #991b1b; border: 1px solid #fecaca;">Từ chối</button>
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
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
