<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Tăng ca (OT) - ManuHRM</title>
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

                <c:if test="${not empty importSuccessCount}">
                    <div class="alert alert-success d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">check_circle</span>
                        Import xong: <strong class="mx-1">${importSuccessCount}</strong> thành công
                        <c:if test="${importDuplicateCount > 0}">
                            , <strong class="mx-1">${importDuplicateCount}</strong> trùng (đã bỏ qua)
                        </c:if>
                        <c:if test="${importErrorCount > 0}">
                            , <strong class="mx-1">${importErrorCount}</strong> lỗi (đã bỏ qua)
                        </c:if>
                    </div>
                    <c:if test="${not empty importErrorMessages}">
                        <div class="alert alert-danger mb-2" role="alert" style="max-height: 180px; overflow-y: auto;">
                            <small>
                                <c:forEach var="err" items="${importErrorMessages}">
                                    <div><c:out value="${err}" /></div>
                                </c:forEach>
                            </small>
                        </div>
                    </c:if>
                    <c:if test="${not empty importDuplicateMessages}">
                        <div class="alert mb-3" role="alert" style="background-color:#fef3c7; color:#92400e; max-height: 150px; overflow-y: auto;">
                            <small>
                                <c:forEach var="dup" items="${importDuplicateMessages}">
                                    <div><c:out value="${dup}" /></div>
                                </c:forEach>
                            </small>
                        </div>
                    </c:if>
                </c:if>

                <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Tăng ca (OT)</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Bảng số giờ OT tháng ${selectedMonth}/${selectedYear}.
                        </p>
                    </div>
                    <c:if test="${canRequest}">
                        <button type="button"
                                class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm border-0 flex-shrink-0"
                                onclick="document.getElementById('otImportModal').classList.add('show')">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">upload_file</span>
                            Tạo yêu cầu OT
                        </button>
                    </c:if>
                </div>

                <div class="row g-4 mb-4">
                    <div class="col-md-6">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="rounded-circle d-flex align-items-center justify-content-center"
                                         style="width: 48px; height: 48px; background: linear-gradient(135deg, #e0e7ff, #c7d2fe);">
                                        <span class="material-symbols-outlined" style="color: #3730a3;">calendar_month</span>
                                    </div>
                                    <div>
                                        <p class="text-on-surface-variant mb-0" style="font-size: 0.75rem;">Số ngày có OT</p>
                                        <h3 class="mb-0 fw-bold text-primary">${otDays}</h3>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="rounded-circle d-flex align-items-center justify-content-center"
                                         style="width: 48px; height: 48px; background: linear-gradient(135deg, #dcfce7, #bbf7d0);">
                                        <span class="material-symbols-outlined" style="color: #166534;">schedule</span>
                                    </div>
                                    <div>
                                        <p class="text-on-surface-variant mb-0" style="font-size: 0.75rem;">Tổng số giờ OT trong tháng</p>
                                        <h3 class="mb-0 fw-bold" style="color: #166534;"><fmt:formatNumber value="${totalHoursAll}" pattern="0.##" />h</h3>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/overtime-list" method="GET"
                              class="d-flex flex-wrap align-items-end gap-3">
                            <div style="width: 130px;">
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng</label>
                                <select name="month" class="form-select input-premium">
                                    <c:forEach begin="1" end="12" var="m">
                                        <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div style="width: 110px;">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                <input type="number" name="year" value="${selectedYear}"
                                       class="form-control input-premium" min="2020" max="2100" />
                            </div>
                            <c:if test="${viewAll}">
                                <div style="width: 160px;">
                                    <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                    <select name="departmentId" class="form-select input-premium">
                                        <option value="">Tất cả phòng ban</option>
                                        <c:forEach var="dept" items="${departments}">
                                            <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>
                                                <c:out value="${dept.name}" />
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </c:if>
                            <div style="width: 130px;">
                                <label class="form-label text-on-surface fw-medium mb-1">Tìm nhân viên</label>
                                <input type="text" name="keyword" value="${keyword}"
                                       class="form-control input-premium" placeholder="Mã NV/tên" />
                            </div>
                            <button type="submit" class="btn btn-primary px-4">Xem</button>
                        </form>
                    </div>

                    <div class="d-flex justify-content-between align-items-center p-3 bg-surface border-bottom border-outline-variant">
                        <h5 class="mb-0 fw-semibold">Bảng OT tháng ${selectedMonth}/${selectedYear}</h5>
                        <div class="d-flex gap-2">
                            <a href="${pageContext.request.contextPath}/overtime-list?year=${prevYear}&month=${prevMonth}&keyword=${keyword}&departmentId=${selectedDepartmentId}"
                               class="btn btn-sm btn-light border text-on-surface-variant">
                                <span class="material-symbols-outlined">chevron_left</span>
                            </a>
                            <a href="${pageContext.request.contextPath}/overtime-list?year=${nextYear}&month=${nextMonth}&keyword=${keyword}&departmentId=${selectedDepartmentId}"
                               class="btn btn-sm btn-light border text-on-surface-variant">
                                <span class="material-symbols-outlined">chevron_right</span>
                            </a>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 ot-grid-table">
                            <thead>
                                <tr>
                                    <th>Nhân viên</th>
                                    <c:forEach begin="1" end="${daysInMonth}" var="d">
                                        <th class="text-center">${d}</th>
                                    </c:forEach>
                                    <th class="text-center">Tổng</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="emp" items="${employees}">
                                    <c:set var="empRow" value="${gridData[emp.id]}" />
                                    <tr>
                                        <td>
                                            <div class="fw-medium text-on-surface"><c:out value="${emp.fullName}" /></div>
                                            <div class="body-sm text-on-surface-variant"><c:out value="${emp.employeeCode}" /></div>
                                        </td>
                                        <c:forEach begin="1" end="${daysInMonth}" var="d">
                                            <c:set var="otCell" value="${empRow[d]}" />
                                            <td class="text-center">
                                                <c:if test="${not empty otCell}">
                                                    <c:choose>
                                                        <c:when test="${canUpdate}">
                                                            <a href="${pageContext.request.contextPath}/overtime-edit?id=${otCell.id}"
                                                               class="ot-cell-link" title="${otCell.reason}"><fmt:formatNumber value="${otCell.requestedHours}" pattern="0.##" />h</a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="fw-medium" title="${otCell.reason}"><fmt:formatNumber value="${otCell.requestedHours}" pattern="0.##" />h</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:if>
                                            </td>
                                        </c:forEach>
                                        <td class="text-center fw-bold"><fmt:formatNumber value="${totals[emp.id]}" pattern="0.##" />h</td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty employees}">
                                    <tr>
                                        <td colspan="${daysInMonth + 2}" class="text-center py-4 text-on-surface-variant">
                                            Không có nhân viên nào.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <style>
        .page-container {
            max-width: 100%;
            overflow-x: hidden;
        }
        .ot-grid-table {
            border-collapse: collapse;
        }
        .ot-grid-table th, .ot-grid-table td {
            white-space: nowrap;
            padding: 0.5rem 0.6rem;
            border: 1px solid var(--outline-variant, #d1d5db);
        }
        .ot-grid-table th:first-child, .ot-grid-table td:first-child {
            position: sticky;
            left: 0;
            z-index: 2;
            background: var(--surface, #fff);
            min-width: 190px;
            text-align: left;
        }
        .ot-grid-table th {
            position: sticky;
            top: 0;
            z-index: 3;
            background: var(--surface-container-low, #f3f4f6);
        }
        .ot-grid-table th:first-child {
            z-index: 4;
        }
        .ot-grid-table td:not(:first-child) {
            min-width: 46px;
        }
        .ot-grid-table td:last-child, .ot-grid-table th:last-child {
            background: var(--surface-container-low, #f8fafc);
        }
        .ot-cell-link {
            color: var(--primary, #4f46e5);
            font-weight: 600;
            text-decoration: none;
        }
        .ot-cell-link:hover {
            text-decoration: underline;
        }
        .modal-overlay {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.5);
            z-index: 9999;
            justify-content: center;
            align-items: center;
        }
        .modal-overlay.show {
            display: flex;
        }
        .modal-content {
            background: var(--surface, #fff);
            border-radius: 12px;
            max-width: 480px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
        }
    </style>

    <script>
        function closeOtModalOnOverlay(event) {
            if (event.target.id === 'otImportModal') {
                document.getElementById('otImportModal').classList.remove('show');
            }
        }
    </script>

    <c:if test="${canRequest}">
        <div id="otImportModal" class="modal-overlay" onclick="closeOtModalOnOverlay(event)">
            <div class="modal-content card-premium">
                <div class="d-flex justify-content-between align-items-center p-3 border-bottom">
                    <h5 class="mb-0">Import Excel</h5>
                    <button type="button" class="btn-close" onclick="document.getElementById('otImportModal').classList.remove('show')"></button>
                </div>
                <div class="p-4">
                    <form action="${pageContext.request.contextPath}/overtime-request" method="POST"
                          enctype="multipart/form-data">
                        <input type="hidden" name="year" value="${selectedYear}" />
                        <input type="hidden" name="month" value="${selectedMonth}" />
                        <div class="mb-3">
                            <label class="form-label text-on-surface fw-medium mb-2">Chọn file Excel (.xlsx)</label>
                            <input type="file" name="excelFile" class="form-control" accept=".xlsx" required />
                        </div>
                        <div class="d-flex gap-2 justify-content-end mt-4">
                            <button type="button" class="btn btn-light border"
                                    onclick="document.getElementById('otImportModal').classList.remove('show')">
                                Hủy
                            </button>
                            <button type="submit" class="btn btn-primary">
                                <span class="material-symbols-outlined me-1">upload</span>
                                Import
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </c:if>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
