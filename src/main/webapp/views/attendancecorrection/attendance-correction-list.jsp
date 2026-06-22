<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Yêu cầu điều chỉnh công - ManuHRM</title>
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
                        <c:out value="${successMsg}" />
                    </div>
                </c:if>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <c:out value="${errorMsg}" />
                    </div>
                </c:if>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Yêu cầu điều chỉnh công</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem xét và phê duyệt các yêu cầu điều chỉnh chấm công từ nhân viên.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/attendance-correction-list" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Trạng thái</label>
                                <select name="status" class="form-select input-premium">
                                    <option value="">-- Tất cả --</option>
                                    <option value="PENDING"  ${selectedStatus == 'PENDING'  ? 'selected' : ''}>Đang chờ</option>
                                    <option value="APPROVED" ${selectedStatus == 'APPROVED' ? 'selected' : ''}>Đã duyệt</option>
                                    <option value="REJECTED" ${selectedStatus == 'REJECTED' ? 'selected' : ''}>Đã từ chối</option>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                            <div class="col-md-4 text-md-end text-on-surface-variant body-sm">
                                Tổng số: <strong>${totalRecords}</strong>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã NV</th>
                                    <th>Nhân viên</th>
                                    <th>Ngày công</th>
                                    <th>Giờ hiện tại</th>
                                    <th>Giờ đề nghị</th>
                                    <th>Lý do</th>
                                    <th>Người gửi</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="cr" items="${corrections}">
                                    <tr>
                                        <td class="fw-medium text-on-surface"><c:out value="${cr.employeeCode}" /></td>
                                        <td><c:out value="${cr.employeeName}" /></td>
                                        <td>${cr.attendanceDate}</td>
                                        <td>
                                            ${cr.currentCheckIn} &rarr; ${cr.currentCheckOut}
                                        </td>
                                        <td class="fw-medium text-on-surface">
                                            ${cr.newCheckIn} &rarr; ${cr.newCheckOut}
                                        </td>
                                        <td style="max-width: 220px;"><c:out value="${cr.reason}" /></td>
                                        <td><c:out value="${cr.requesterName}" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${cr.status == 'PENDING'}">
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">Đang chờ</span>
                                                </c:when>
                                                <c:when test="${cr.status == 'APPROVED'}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Đã duyệt</span>
                                                </c:when>
                                                <c:when test="${cr.status == 'REJECTED'}">
                                                    <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Đã từ chối</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <c:if test="${cr.status == 'PENDING'}">
                                                <div class="d-flex justify-content-end gap-1">
                                                    <c:if test="${canApprove}">
                                                        <form method="post"
                                                              action="${pageContext.request.contextPath}/attendance-correction-approve"
                                                              class="d-inline m-0"
                                                              onsubmit="return confirm('Duyệt yêu cầu điều chỉnh công của ${cr.employeeName}?')">
                                                            <input type="hidden" name="id" value="${cr.id}" />
                                                            <button type="submit"
                                                                    class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                                    title="Duyệt">
                                                                <span class="material-symbols-outlined" style="font-size: 1.25rem; color: #065f46;">check_circle</span>
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                    <c:if test="${canReject}">
                                                        <form method="post"
                                                              action="${pageContext.request.contextPath}/attendance-correction-reject"
                                                              class="d-inline m-0"
                                                              onsubmit="return confirm('Từ chối yêu cầu điều chỉnh công của ${cr.employeeName}?')">
                                                            <input type="hidden" name="id" value="${cr.id}" />
                                                            <button type="submit"
                                                                    class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                                    title="Từ chối">
                                                                <span class="material-symbols-outlined" style="font-size: 1.25rem; color: #991b1b;">cancel</span>
                                                            </button>
                                                        </form>
                                                    </c:if>
                                                </div>
                                            </c:if>
                                            <c:if test="${cr.status != 'PENDING'}">
                                                <span class="text-on-surface-variant body-sm">
                                                    <c:out value="${cr.approverName}" default="—" />
                                                </span>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty corrections}">
                                    <tr>
                                        <td colspan="9" class="text-center py-4 text-on-surface-variant">
                                            Không có yêu cầu điều chỉnh công nào.
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
                                    <a href="${pageContext.request.contextPath}/attendance-correction-list?page=${i}&status=${selectedStatus}"
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
