<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý đơn nghỉ - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <jsp:include page="/components/alert.jsp" />

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý đơn nghỉ</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Theo dõi và duyệt đơn nghỉ phép theo quy trình phân quyền.
                        </p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/leave-request-list" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Từ khóa</label>
                                <input type="text" name="keyword" value="${keyword}"
                                       class="form-control input-premium"
                                       placeholder="Mã NV, tên NV, loại nghỉ" />
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Trạng thái</label>
                                <select name="status" class="form-select input-premium">
                                    <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả trạng thái</option>
                                    <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>Chờ duyệt</option>
                                    <option value="APPROVED_LEVEL_1" ${selectedStatus == 'APPROVED_LEVEL_1' ? 'selected' : ''}>Đã duyệt cấp 1</option>
                                    <option value="APPROVED" ${selectedStatus == 'APPROVED' ? 'selected' : ''}>Đã duyệt</option>
                                    <option value="REJECTED" ${selectedStatus == 'REJECTED' ? 'selected' : ''}>Từ chối</option>
                                    <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="" ${empty selectedDepartmentId ? 'selected' : ''}>Tất cả phòng ban</option>
                                    <c:forEach var="department" items="${departments}">
                                        <option value="${department.id}" ${selectedDepartmentId == department.id ? 'selected' : ''}>
                                            <c:out value="${department.name}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <button type="submit" class="btn btn-primary w-100">
                                    <span class="material-symbols-outlined align-middle" style="font-size: 1rem;">filter_list</span>
                                    Lọc
                                </button>
                            </div>
                            <div class="col-md-1">
                                <a href="${pageContext.request.contextPath}/leave-request-list"
                                   class="btn btn-light border w-100">Xóa</a>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã đơn</th>
                                    <th>Nhân viên</th>
                                    <th>Phòng ban</th>
                                    <th>Loại nghỉ</th>
                                    <th>Thời gian</th>
                                    <th class="text-end">Số ngày</th>
                                    <th>Trạng thái</th>
                                    <th>Tiến trình duyệt</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="leaveRequest" items="${leaveRequests}">
                                    <tr>
                                        <td class="fw-medium text-on-surface">#${leaveRequest.id}</td>
                                        <td>
                                            <div class="fw-medium text-on-surface">
                                                <c:out value="${leaveRequest.employeeName}" />
                                            </div>
                                            <div class="body-sm text-on-surface-variant">
                                                <c:out value="${leaveRequest.employeeCode}" />
                                            </div>
                                        </td>
                                        <td><c:out value="${empty leaveRequest.departmentName ? '-' : leaveRequest.departmentName}" /></td>
                                        <td>
                                            <span class="badge" style="background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant);">
                                                <c:out value="${leaveRequest.leaveTypeCode}" />
                                            </span>
                                            <span class="ms-2"><c:out value="${leaveRequest.leaveTypeName}" /></span>
                                        </td>
                                        <td>
                                            <div><c:out value="${leaveRequest.startDate}" /></div>
                                            <div class="body-sm text-on-surface-variant">đến <c:out value="${leaveRequest.endDate}" /></div>
                                        </td>
                                        <td class="text-end fw-medium">
                                            <fmt:formatNumber value="${leaveRequest.days}" minFractionDigits="0" maxFractionDigits="2" />
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${leaveRequest.status == 'PENDING'}">
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">Chờ duyệt</span>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'APPROVED_LEVEL_1'}">
                                                    <span class="badge" style="background-color: #dbeafe; color: #1d4ed8;">Đã duyệt cấp 1</span>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'APPROVED'}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Đã duyệt</span>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'REJECTED'}">
                                                    <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Từ chối</span>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'CANCELLED'}">
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Đã hủy</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">
                                                        <c:out value="${leaveRequest.status}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${leaveRequest.status == 'PENDING'}">
                                                    <c:choose>
                                                        <c:when test="${leaveRequest.requesterRole == 'EMPLOYEE'}">
                                                            <span class="body-sm text-on-surface-variant">
                                                                Chờ cấp 1:
                                                                <c:out value="${empty leaveRequest.requesterManagerName ? '-' : leaveRequest.requesterManagerName}" />
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="body-sm text-on-surface-variant">
                                                                Chờ duyệt:
                                                                <c:out value="${empty leaveRequest.requesterManagerName ? '-' : leaveRequest.requesterManagerName}" />
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'APPROVED_LEVEL_1'}">
                                                    <div class="body-sm">
                                                        <span class="text-success">Cấp 1:</span>
                                                        <c:out value="${leaveRequest.level1ApproverName}" />
                                                    </div>
                                                    <div class="body-sm text-on-surface-variant">Chờ duyệt cuối: HR Manager</div>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'APPROVED'}">
                                                    <c:choose>
                                                        <c:when test="${leaveRequest.requesterRole == 'EMPLOYEE'}">
                                                            <div class="body-sm">
                                                                <span class="text-success">Cấp 1:</span>
                                                                <c:out value="${leaveRequest.level1ApproverName}" />
                                                            </div>
                                                            <div class="body-sm">
                                                                <span class="text-success">Duyệt cuối:</span>
                                                                <c:out value="${leaveRequest.approverName}" />
                                                            </div>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="body-sm text-success">
                                                                Duyệt bởi: <c:out value="${leaveRequest.approverName}" />
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'REJECTED'}">
                                                    <span class="body-sm text-danger">
                                                        Từ chối bởi: <c:out value="${leaveRequest.approverName}" />
                                                    </span>
                                                </c:when>
                                                <c:when test="${leaveRequest.status == 'CANCELLED'}">
                                                    <span class="body-sm text-on-surface-variant">Đã hủy</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-inline-flex gap-2 justify-content-end">
                                                <c:if test="${canApproveLevel1 and leaveRequest.status == 'PENDING' and currentUserId == leaveRequest.requesterManagerId and currentUserId != leaveRequest.userId}">
                                                    <form action="${pageContext.request.contextPath}/leave-request-approve" method="POST" class="d-inline">
                                                        <input type="hidden" name="id" value="${leaveRequest.id}" />
                                                        <button type="submit"
                                                                class="btn btn-sm btn-icon text-success"
                                                                title="Duyệt"
                                                                onclick="return confirm('Xác nhận duyệt đơn nghỉ này?');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">task_alt</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${canFinalApprove and leaveRequest.status == 'APPROVED_LEVEL_1' and leaveRequest.requesterRole == 'EMPLOYEE' and currentUserId != leaveRequest.userId}">
                                                    <form action="${pageContext.request.contextPath}/leave-request-final-approve" method="POST" class="d-inline">
                                                        <input type="hidden" name="id" value="${leaveRequest.id}" />
                                                        <button type="submit"
                                                                class="btn btn-sm btn-icon text-success"
                                                                title="Duyệt cuối"
                                                                onclick="return confirm('Xác nhận duyệt cuối đơn nghỉ này?');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">task_alt</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${canReject and currentUserId != leaveRequest.userId and ((leaveRequest.status == 'PENDING' and currentUserId == leaveRequest.requesterManagerId) or (leaveRequest.status == 'APPROVED_LEVEL_1' and canFinalApprove))}">
                                                    <form action="${pageContext.request.contextPath}/leave-request-reject" method="POST" class="d-inline">
                                                        <input type="hidden" name="id" value="${leaveRequest.id}" />
                                                        <button type="submit"
                                                                class="btn btn-sm btn-icon text-danger"
                                                                title="Từ chối"
                                                                onclick="return confirm('Xác nhận từ chối đơn nghỉ này?');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">block</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty leaveRequests}">
                                    <tr>
                                        <td colspan="9" class="text-center py-4 text-on-surface-variant">
                                            Chưa có đơn nghỉ nào phù hợp với bộ lọc hiện tại.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-between flex-wrap gap-3">
                            <div class="body-sm text-on-surface-variant">
                                Tổng số đơn: ${totalRecords}
                            </div>
                            <div class="d-flex gap-1 flex-wrap">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <c:url var="pageUrl" value="/leave-request-list">
                                        <c:param name="page" value="${i}" />
                                        <c:param name="keyword" value="${keyword}" />
                                        <c:param name="status" value="${selectedStatus}" />
                                        <c:param name="departmentId" value="${selectedDepartmentId}" />
                                    </c:url>
                                    <a href="${pageUrl}"
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
