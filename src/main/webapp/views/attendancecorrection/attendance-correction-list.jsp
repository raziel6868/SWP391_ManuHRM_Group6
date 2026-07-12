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
                    <p class="body-md text-on-surface-variant mb-0">Xem xét và phê duyệt các yêu cầu điều chỉnh chấm công.</p>
                </div>
            </div>

            <%-- Tab navigation --%>
            <ul class="nav nav-tabs mb-3">
                <c:if test="${isSupervisor}">
                    <li class="nav-item">
                        <a class="nav-link ${tab == 'supervisor' ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/attendance-correction-list?tab=supervisor">
                            Chờ tôi duyệt (bước 1)
                        </a>
                    </li>
                </c:if>
                <c:if test="${isHR}">
                    <li class="nav-item">
                        <a class="nav-link ${tab == 'hr' ? 'active' : ''}"
                           href="${pageContext.request.contextPath}/attendance-correction-list?tab=hr">
                            Chờ HR duyệt (bước 2)
                        </a>
                    </li>
                </c:if>
            </ul>

            <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                <%-- Filter --%>
                <div class="p-3 bg-surface border-bottom border-outline-variant">
                    <form action="${pageContext.request.contextPath}/attendance-correction-list"
                          method="GET" class="row g-3 align-items-end">
                        <input type="hidden" name="tab" value="${tab}" />
                        <div class="col-md-3">
                            <label class="form-label fw-medium mb-1">Trạng thái</label>
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

                <%-- Table --%>
                <div class="table-responsive">
                    <table class="table table-premium mb-0 w-100">
                        <thead>
                            <tr>
                                <th>Nhân viên</th>
                                <th>Ngày công</th>
                                <th>Giờ hiện tại</th>
                                <th>Đề nghị sửa</th>
                                <th>Lý do</th>
                                <th>Người gửi</th>
                                <c:if test="${tab == 'supervisor'}">
                                    <th>Trạng thái (quản đốc)</th>
                                    <th>Lý do từ chối</th>
                                </c:if>
                                <c:if test="${tab == 'hr'}">
                                    <th>Quản đốc duyệt</th>
                                    <th>Trạng thái (HR)</th>
                                    <th>Lý do từ chối</th>
                                </c:if>
                                <th class="text-end">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="cr" items="${corrections}">
                                <tr>
                                    <td>
                                        <div class="fw-medium"><c:out value="${cr.employeeName}" /></div>
                                        <div class="body-sm text-on-surface-variant"><c:out value="${cr.employeeCode}" /></div>
                                    </td>
                                    <td>${cr.attendanceDate}</td>
                                    <td class="body-sm">
                                        <c:out value="${cr.currentCheckIn}" default="—" /> →
                                        <c:out value="${cr.currentCheckOut}" default="—" />
                                    </td>
                                    <td class="fw-medium">
                                        <c:out value="${cr.newCheckIn}" default="—" /> →
                                        <c:out value="${cr.newCheckOut}" default="—" />
                                    </td>
                                    <td style="max-width:200px;" class="body-sm"><c:out value="${cr.reason}" /></td>
                                    <td class="body-sm"><c:out value="${cr.requesterName}" /></td>

                                    <%-- Tab supervisor: hiện supervisor_status --%>
                                    <c:if test="${tab == 'supervisor'}">
                                        <td>
                                            <c:choose>
                                                <c:when test="${cr.supervisorStatus == 'PENDING'}">
                                                    <span class="badge" style="background:#fef3c7;color:#92400e;">Đang chờ</span>
                                                </c:when>
                                                <c:when test="${cr.supervisorStatus == 'APPROVED'}">
                                                    <span class="badge" style="background:#d1fae5;color:#065f46;">Đã duyệt</span>
                                                </c:when>
                                                <c:when test="${cr.supervisorStatus == 'REJECTED'}">
                                                    <span class="badge" style="background:#fee2e2;color:#991b1b;">Đã từ chối</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td class="body-sm text-on-surface-variant">
                                            <c:out value="${cr.supervisorRejectReason}" default="—" />
                                        </td>
                                    </c:if>

                                    <%-- Tab HR: hiện supervisor đã duyệt + hr status --%>
                                    <c:if test="${tab == 'hr'}">
                                        <td class="body-sm">
                                            <c:out value="${cr.supervisorName}" default="—" />
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${cr.status == 'PENDING'}">
                                                    <span class="badge" style="background:#fef3c7;color:#92400e;">Đang chờ</span>
                                                </c:when>
                                                <c:when test="${cr.status == 'APPROVED'}">
                                                    <span class="badge" style="background:#d1fae5;color:#065f46;">Đã duyệt</span>
                                                </c:when>
                                                <c:when test="${cr.status == 'REJECTED'}">
                                                    <span class="badge" style="background:#fee2e2;color:#991b1b;">Đã từ chối</span>
                                                </c:when>
                                            </c:choose>
                                        </td>
                                        <td class="body-sm text-on-surface-variant">
                                            <c:out value="${cr.hrRejectReason}" default="—" />
                                        </td>
                                    </c:if>

                                    <%-- Thao tác --%>
                                    <td class="text-end">
                                        <%-- Quản đốc duyệt/từ chối bước 1 --%>
                                        <c:if test="${tab == 'supervisor' && cr.supervisorStatus == 'PENDING' && canSupervisorApprove}">
                                            <div class="d-flex justify-content-end gap-1">
                                                <form method="POST"
                                                      action="${pageContext.request.contextPath}/attendance-correction-supervisor-approve"
                                                      class="d-inline m-0">
                                                    <input type="hidden" name="id" value="${cr.id}" />
                                                    <input type="hidden" name="action" value="approve" />
                                                    <input type="hidden" name="tab" value="supervisor" />
                                                    <input type="hidden" name="status" value="${selectedStatus}" />
                                                    <input type="hidden" name="page" value="${currentPage}" />
                                                    <button type="submit" class="btn btn-sm btn-icon" title="Duyệt">
                                                        <span class="material-symbols-outlined" style="font-size:1.25rem;color:#065f46;">check_circle</span>
                                                    </button>
                                                </form>
                                                <button type="button" class="btn btn-sm btn-icon" title="Từ chối"
                                                        onclick="openRejectModal(${cr.id}, '${cr.employeeName}', 'supervisor')">
                                                    <span class="material-symbols-outlined" style="font-size:1.25rem;color:#991b1b;">cancel</span>
                                                </button>
                                            </div>
                                        </c:if>

                                        <%-- HR duyệt/từ chối bước 2 --%>
                                        <c:if test="${tab == 'hr' && cr.status == 'PENDING' && canApprove}">
                                            <div class="d-flex justify-content-end gap-1">
                                                <form method="POST"
                                                      action="${pageContext.request.contextPath}/attendance-correction-approve"
                                                      class="d-inline m-0"
                                                      onsubmit="return confirm('Duyệt yêu cầu của ${cr.employeeName}?')">
                                                    <input type="hidden" name="id" value="${cr.id}" />
                                                    <input type="hidden" name="status" value="${selectedStatus}" />
                                                    <input type="hidden" name="page" value="${currentPage}" />
                                                    <button type="submit" class="btn btn-sm btn-icon" title="Duyệt">
                                                        <span class="material-symbols-outlined" style="font-size:1.25rem;color:#065f46;">check_circle</span>
                                                    </button>
                                                </form>
                                                <button type="button" class="btn btn-sm btn-icon" title="Từ chối"
                                                        onclick="openRejectModal(${cr.id}, '${cr.employeeName}', 'hr')">
                                                    <span class="material-symbols-outlined" style="font-size:1.25rem;color:#991b1b;">cancel</span>
                                                </button>
                                            </div>
                                        </c:if>

                                        <%-- Đã xử lý --%>
                                        <c:if test="${(tab == 'supervisor' && cr.supervisorStatus != 'PENDING')
                                                   || (tab == 'hr'         && cr.status != 'PENDING')}">
                                            <span class="body-sm text-on-surface-variant">
                                                <c:out value="${tab == 'hr' ? cr.approverName : cr.supervisorName}" default="—" />
                                            </span>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>

                            <c:if test="${empty corrections}">
                                <tr>
                                    <td colspan="10" class="text-center py-4 text-on-surface-variant">
                                        Không có yêu cầu điều chỉnh công nào.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <%-- Pagination --%>
                <c:if test="${totalPages > 1}">
                    <div class="p-3 bg-surface border-top border-outline-variant d-flex justify-content-center">
                        <div class="d-flex gap-1 flex-wrap">
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <a href="${pageContext.request.contextPath}/attendance-correction-list?page=${i}&tab=${tab}&status=${selectedStatus}"
                                   class="btn btn-sm ${i == currentPage ? 'fw-bold' : 'btn-light border text-on-surface-variant'}"
                                   style="${i == currentPage ? 'background-color:var(--primary-fixed);color:var(--on-primary-fixed-variant);border:1px solid var(--primary);' : 'background-color:var(--surface-container-lowest);border-color:var(--outline-variant)!important;'}">
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

<%-- Modal từ chối (dùng chung cho cả supervisor và HR) --%>
<div id="rejectModalOverlay"
     style="display:none; position:fixed; inset:0; background:rgba(11,28,48,0.45); z-index:1050; align-items:center; justify-content:center;">
    <div class="card-premium" style="max-width:440px; width:92%; padding:1.5rem;">
        <h5 class="fw-bold mb-3">Từ chối yêu cầu điều chỉnh</h5>
        <form id="rejectForm" method="POST">
            <input type="hidden" name="id" id="rejectId" />
            <input type="hidden" name="action" value="reject" />
            <input type="hidden" name="tab" id="rejectTab" />
            <input type="hidden" name="status" value="${selectedStatus}" />
            <input type="hidden" name="page" value="${currentPage}" />
            <div class="mb-3">
                <label class="form-label fw-medium mb-1">
                    Lý do từ chối yêu cầu của <strong id="rejectName"></strong>
                    <span class="text-danger">*</span>
                </label>
                <textarea name="rejectReason" rows="3" class="form-control input-premium"
                          placeholder="Nhập lý do để nhân viên / quản đốc biết cách sửa lại..."
                          required></textarea>
            </div>
            <div class="d-flex justify-content-end gap-2">
                <button type="button" onclick="closeRejectModal()" class="btn btn-light border">Hủy</button>
                <button type="submit" class="btn btn-danger">Từ chối</button>
            </div>
        </form>
    </div>
</div>

<script>
function openRejectModal(id, name, tab) {
    const form = document.getElementById('rejectForm');
    form.action = tab === 'supervisor'
        ? '${pageContext.request.contextPath}/attendance-correction-supervisor-approve'
        : '${pageContext.request.contextPath}/attendance-correction-reject';
    document.getElementById('rejectId').value   = id;
    document.getElementById('rejectName').textContent = name;
    document.getElementById('rejectTab').value  = tab;
    document.getElementById('rejectModalOverlay').style.display = 'flex';
}
function closeRejectModal() {
    document.getElementById('rejectModalOverlay').style.display = 'none';
}
</script>

<jsp:include page="/components/foot.jsp" />
</body>
</html>
