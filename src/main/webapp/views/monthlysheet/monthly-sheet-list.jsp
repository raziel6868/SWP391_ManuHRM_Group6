<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Bảng công tháng - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <style>
        .status-badge {
            display: inline-flex; align-items: center; gap: 5px;
            padding: 3px 10px; border-radius: 999px;
            font-size: 0.75rem; font-weight: 600;
        }
        .status-badge .dot { width:7px; height:7px; border-radius:50%; background:currentColor; }
        .badge-open       { background:#d1fae5; color:#065f46; }
        .badge-pending-sup{ background:#fef3c7; color:#92400e; }
        .badge-pending-hr { background:#dbeafe; color:#1e40af; }
        .badge-pending-dir{ background:#ede9fe; color:#4c1d95; }
        .badge-closed     { background:var(--surface-container-high); color:var(--on-surface-variant); }

        .sup-progress { font-size: 0.72rem; color: var(--on-surface-variant); }
    </style>
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
                <div class="alert alert-error d-flex align-items-center gap-2 mb-3" role="alert">
                    <span class="material-symbols-outlined">error</span>
                    <c:out value="${errorMsg}" />
                </div>
            </c:if>

            <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                <div>
                    <h2 class="h3 text-on-surface fw-bold mb-1">Bảng công tháng</h2>
                    <p class="body-md text-on-surface-variant mb-0">Quản lý quy trình duyệt và đóng sổ bảng công.</p>
                </div>
            </div>

            <div class="card-premium overflow-hidden mb-4 w-100">
                <%-- Filter --%>
                <div class="p-3 bg-surface border-bottom border-outline-variant">
                    <form action="${pageContext.request.contextPath}/monthly-sheet-list" method="GET"
                          class="row g-3 align-items-end">
                        <div class="col-md-2">
                            <label class="form-label fw-medium mb-1">Năm</label>
                            <select name="year" class="form-select input-premium">
                                <c:forEach var="y" begin="2024" end="2027">
                                    <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label fw-medium mb-1">Tháng</label>
                            <select name="month" class="form-select input-premium">
                                <option value="">-- Tất cả --</option>
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label fw-medium mb-1">Trạng thái</label>
                            <select name="status" class="form-select input-premium">
                                <option value="">-- Tất cả --</option>
                                <option value="OPEN"             ${selectedStatus == 'OPEN'             ? 'selected' : ''}>Mở</option>
                                <option value="PENDING_SUPERVISOR" ${selectedStatus == 'PENDING_SUPERVISOR' ? 'selected' : ''}>Chờ quản đốc</option>
                                <option value="PENDING_HR"       ${selectedStatus == 'PENDING_HR'       ? 'selected' : ''}>Chờ HR</option>
                                <option value="PENDING_DIRECTOR" ${selectedStatus == 'PENDING_DIRECTOR' ? 'selected' : ''}>Chờ Giám đốc</option>
                                <option value="CLOSED"           ${selectedStatus == 'CLOSED'           ? 'selected' : ''}>Đã đóng</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary w-100">Lọc</button>
                        </div>
                        <div class="col-md-2">
                            <a href="${pageContext.request.contextPath}/monthly-sheet-list"
                               class="btn btn-light border w-100">Reset</a>
                        </div>
                    </form>
                </div>

                <%-- Table --%>
                <div class="table-responsive">
                    <table class="table table-premium mb-0 w-100">
                        <thead>
                            <tr>
                                <th>Tháng</th>
                                <th>Trạng thái</th>
                                <th>Quản đốc xác nhận</th>
                                <th>HR chốt</th>
                                <th>Giám đốc đóng sổ</th>
                                <th class="text-end">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty sheets}">
                                    <tr>
                                        <td colspan="6" class="text-center py-4 text-on-surface-variant">
                                            Không có bảng công nào.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="sheet" items="${sheets}">
                                        <tr>
                                            <td class="fw-medium">Tháng ${sheet.month}/${sheet.year}</td>

                                            <%-- Status badge --%>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${sheet.status == 'OPEN'}">
                                                        <span class="status-badge badge-open"><div class="dot"></div>Mở</span>
                                                    </c:when>
                                                    <c:when test="${sheet.status == 'PENDING_SUPERVISOR'}">
                                                        <span class="status-badge badge-pending-sup"><div class="dot"></div>Chờ quản đốc</span>
                                                    </c:when>
                                                    <c:when test="${sheet.status == 'PENDING_HR'}">
                                                        <span class="status-badge badge-pending-hr"><div class="dot"></div>Chờ HR</span>
                                                    </c:when>
                                                    <c:when test="${sheet.status == 'PENDING_DIRECTOR'}">
                                                        <span class="status-badge badge-pending-dir"><div class="dot"></div>Chờ Giám đốc</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge badge-closed"><div class="dot"></div>Đã đóng</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <%-- Supervisor progress --%>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${sheet.totalSupervisors > 0}">
                                                        <span class="sup-progress">
                                                            ${sheet.approvedSupervisors}/${sheet.totalSupervisors} đã chốt
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-on-surface-variant body-sm">—</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <%-- HR chốt --%>
                                            <td class="body-sm">
                                                <c:choose>
                                                    <c:when test="${not empty sheet.hrApprovedByName}">
                                                        <div>${sheet.hrApprovedByName}</div>
                                                        <div class="text-on-surface-variant">${sheet.hrApprovedAt}</div>
                                                    </c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </td>

                                            <%-- Giám đốc đóng sổ --%>
                                            <td class="body-sm">
                                                <c:choose>
                                                    <c:when test="${not empty sheet.closedByName}">
                                                        <div>${sheet.closedByName}</div>
                                                        <div class="text-on-surface-variant">${sheet.closedAt}</div>
                                                    </c:when>
                                                    <c:otherwise>—</c:otherwise>
                                                </c:choose>
                                            </td>

                                            <%-- Thao tác --%>
                                            <td class="text-end">
                                                <div class="d-flex gap-2 justify-content-end flex-wrap">

                                                    <%-- HR: gửi duyệt --%>
                                                    <c:if test="${sheet.status == 'OPEN' && canSubmit}">
                                                        <form action="${pageContext.request.contextPath}/monthly-sheet-submit"
                                                              method="POST" class="d-inline">
                                                            <input type="hidden" name="id" value="${sheet.id}" />
                                                            <button type="submit" class="btn btn-sm btn-primary">
                                                                Gửi duyệt
                                                            </button>
                                                        </form>
                                                    </c:if>

                                                    <%-- HR: chốt bước 2 --%>
                                                    <c:if test="${sheet.status == 'PENDING_HR' && canHrApprove}">
                                                        <form action="${pageContext.request.contextPath}/monthly-sheet-hr-approve"
                                                              method="POST" class="d-inline">
                                                            <input type="hidden" name="id" value="${sheet.id}" />
                                                            <button type="submit" class="btn btn-sm"
                                                                    style="background:#dbeafe;color:#1e40af;border:1px solid #93c5fd;">
                                                                Chốt (HR)
                                                            </button>
                                                        </form>
                                                    </c:if>

                                                    <%-- Giám đốc: đóng sổ --%>
                                                    <c:if test="${sheet.status == 'PENDING_DIRECTOR' && canDirectorApprove}">
                                                        <form action="${pageContext.request.contextPath}/monthly-sheet-director-approve"
                                                              method="POST" class="d-inline">
                                                            <input type="hidden" name="id" value="${sheet.id}" />
                                                            <button type="submit" class="btn btn-sm"
                                                                    style="background:#ede9fe;color:#4c1d95;border:1px solid #c4b5fd;">
                                                                Đóng sổ
                                                            </button>
                                                        </form>
                                                    </c:if>

                                                    <%-- HR hoặc GĐ: reject về OPEN --%>
                                                    <c:if test="${sheet.status != 'OPEN' && sheet.status != 'CLOSED'
                                                                  && canReject}">
                                                        <button type="button" class="btn btn-sm btn-light border"
                                                                onclick="openRejectModal(${sheet.id}, ${sheet.month}, ${sheet.year})">
                                                            Từ chối
                                                        </button>
                                                    </c:if>

                                                    <%-- HR: mở lại nếu CLOSED --%>
                                                    <c:if test="${sheet.status == 'CLOSED' && canReopen}">
                                                        <form action="${pageContext.request.contextPath}/monthly-sheet-reopen"
                                                              method="POST" class="d-inline">
                                                            <input type="hidden" name="id" value="${sheet.id}" />
                                                            <button type="submit" class="btn btn-sm"
                                                                    style="background:#fef3c7;color:#92400e;border:1px solid #fde68a;">
                                                                Mở lại
                                                            </button>
                                                        </form>
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

                <c:if test="${totalPages > 1}">
                    <div class="p-3 bg-surface border-top border-outline-variant d-flex justify-content-center">
                        <div class="d-flex gap-1 flex-wrap">
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <a href="?page=${i}&year=${selectedYear}&month=${selectedMonth}&status=${selectedStatus}"
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

<%-- Modal reject --%>
<div id="rejectModalOverlay"
     style="display:none; position:fixed; inset:0; background:rgba(11,28,48,0.45); z-index:1050; align-items:center; justify-content:center;">
    <div class="card-premium" style="max-width:460px; width:92%; padding:1.5rem;">
        <h5 class="fw-bold mb-1">Từ chối bảng công</h5>
        <p class="body-sm text-on-surface-variant mb-3" id="rejectSheetLabel"></p>
        <form action="${pageContext.request.contextPath}/monthly-sheet-reject" method="POST">
            <input type="hidden" name="id" id="rejectSheetId" />
            <div class="mb-3">
                <label class="form-label fw-medium mb-1">Reset approval của</label>
                <select name="departmentId" class="form-select input-premium">
                    <option value="">Tất cả quản đốc</option>
                    <c:forEach var="dept" items="${departments}">
                        <option value="${dept.id}"><c:out value="${dept.name}" /></option>
                    </c:forEach>
                </select>
                <div class="body-sm text-on-surface-variant mt-1">
                    Chọn phòng ban cụ thể nếu chỉ muốn reset quản đốc của phòng đó. Để trống sẽ reset tất cả.
                </div>
            </div>
            <div class="d-flex justify-content-end gap-2">
                <button type="button" onclick="closeRejectModal()" class="btn btn-light border">Hủy</button>
                <button type="submit" class="btn btn-danger">Từ chối & Reset</button>
            </div>
        </form>
    </div>
</div>

<script>
function openRejectModal(id, month, year) {
    document.getElementById('rejectSheetId').value = id;
    document.getElementById('rejectSheetLabel').textContent =
        'Bảng công tháng ' + month + '/' + year + ' sẽ được reset về trạng thái OPEN.';
    document.getElementById('rejectModalOverlay').style.display = 'flex';
}
function closeRejectModal() {
    document.getElementById('rejectModalOverlay').style.display = 'none';
}
</script>

<jsp:include page="/components/foot.jsp" />
</body>
</html>
