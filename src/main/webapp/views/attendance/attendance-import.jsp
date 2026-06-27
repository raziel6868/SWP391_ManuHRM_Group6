<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Nhập dữ liệu chấm công - ManuHRM</title>
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
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <c:out value="${errorMsg}" />
                    </div>
                </c:if>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Nhập dữ liệu chấm công</h2>
                        <p class="body-md text-on-surface-variant mb-0">Tải file Excel xuất từ máy chấm công để ghi nhận công theo nhân viên và ngày.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/attendance-list"
                       class="btn btn-light border text-decoration-none px-3 py-2 d-flex align-items-center gap-2">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">list_alt</span>
                        Danh sách chấm công
                    </a>
                </div>

                <div class="card-premium overflow-hidden mb-4">
                    <div class="p-4 bg-surface">
                        <c:choose>
                            <c:when test="${empty availableMonths}">
                                <div class="alert alert-error d-flex align-items-center gap-2" role="alert">
                                    <span class="material-symbols-outlined">lock</span>
                                    Không có tháng nào có thể import — tất cả các tháng đã được đóng sổ.
                                </div>
                            </c:when>
                            <c:otherwise>
                                <form action="${pageContext.request.contextPath}/attendance-import" method="POST"
                                      enctype="multipart/form-data" class="row g-3 align-items-end">
                                    <div class="col-md-3">
                                        <label class="form-label text-on-surface fw-medium mb-1">Tháng import <span class="text-danger">*</span></label>
                                        <select name="yearMonth" class="form-select input-premium" required
                                                onchange="syncYearMonth(this)">
                                            <c:forEach var="entry" items="${availableMonths}">
                                                <c:set var="parts" value="${fn:split(entry.key, '-')}" />
                                                <c:set var="entryYear" value="${parts[0]}" />
                                                <c:set var="entryMonth" value="${parts[1]}" />
                                                <option value="${entry.key}"
                                                    ${entryYear == selectedYear && entryMonth == selectedMonth ? 'selected' : ''}>
                                                    ${entry.value}
                                                </option>
                                            </c:forEach>
                                        </select>
                                        <%-- Hidden inputs để servlet đọc year/month riêng như cũ --%>
                                        <input type="hidden" name="year" id="hiddenYear" value="${selectedYear}" />
                                        <input type="hidden" name="month" id="hiddenMonth" value="${selectedMonth}" />
                                    </div>
                                    <div class="col-md-5">
                                        <label class="form-label text-on-surface fw-medium mb-1">File Excel <span class="text-danger">*</span></label>
                                        <input type="file" name="attendanceFile" accept=".xlsx"
                                               class="form-control input-premium" required />
                                    </div>
                                    <div class="col-md-2">
                                        <button type="submit" class="btn-primary-gradient w-100 px-3 py-2 d-flex align-items-center justify-content-center gap-2">
                                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">upload_file</span>
                                            Import
                                        </button>
                                    </div>
                                </form>
                                <script>
                                    function syncYearMonth(sel) {
                                        const parts = sel.value.split('-');
                                        document.getElementById('hiddenYear').value = parts[0];
                                        document.getElementById('hiddenMonth').value = parts[1];
                                    }
                                    // Sync on load so hidden inputs match the default selected option
                                    document.addEventListener('DOMContentLoaded', function () {
                                        const sel = document.querySelector('select[name="yearMonth"]');
                                        if (sel) syncYearMonth(sel);
                                    });
                                </script>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <c:if test="${not empty errorLogs}">
                    <c:set var="hasConflict" value="false" />
                    <c:set var="hasDataError" value="false" />
                    <c:forEach var="log" items="${errorLogs}">
                        <c:if test="${fn:contains(log, 'Conflict')}"><c:set var="hasConflict" value="true" /></c:if>
                        <c:if test="${not fn:contains(log, 'Conflict')}"><c:set var="hasDataError" value="true" /></c:if>
                    </c:forEach>

                    <c:if test="${hasConflict}">
                        <div class="card-premium overflow-hidden mb-4">
                            <div class="p-3 border-bottom border-outline-variant d-flex align-items-start gap-3"
                                 style="background-color: #fffbeb;">
                                <span class="material-symbols-outlined mt-1" style="color: #b45309;">warning</span>
                                <div>
                                    <h3 class="h5 mb-1 fw-bold" style="color: #92400e;">Phát hiện conflict nghiệp vụ</h3>
                                    <p class="body-sm mb-0" style="color: #78350f;">
                                        File <strong>chưa được lưu</strong>. Các dòng dưới đây mâu thuẫn với dữ liệu
                                        đã có trong hệ thống — HR cần xử lý từng trường hợp rồi import lại.
                                    </p>
                                </div>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-premium mb-0 w-100">
                                    <thead>
                                        <tr>
                                            <th style="width: 50px;">STT</th>
                                            <th style="width: 220px;">Loại conflict</th>
                                            <th>Chi tiết</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:set var="ci" value="0" />
                                        <c:forEach var="log" items="${errorLogs}">
                                            <c:if test="${fn:contains(log, 'Conflict')}">
                                                <c:set var="ci" value="${ci + 1}" />
                                                <tr>
                                                    <td>${ci}</td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${fn:contains(log, 'ATTENDANCE_ON_APPROVED_LEAVE')}">
                                                                <span class="badge" style="background-color: #fef3c7; color: #92400e;">
                                                                    Chấm công trong ngày nghỉ phép
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${fn:contains(log, 'APPROVED_OT_WITHOUT_MATCHING_ATTENDANCE')}">
                                                                <span class="badge" style="background-color: #ede9fe; color: #4c1d95;">
                                                                    OT duyệt — checkout chưa đủ
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${fn:contains(log, 'ATTENDANCE_WITHOUT_SHIFT_ASSIGNMENT')}">
                                                                <span class="badge" style="background-color: #fce7f3; color: #831843;">
                                                                    Chấm công không có phân ca
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${fn:contains(log, 'WRONG_SHIFT_ATTENDANCE')}">
                                                                <span class="badge" style="background-color: #ecfdf5; color: #065f46;">
                                                                    Chấm công sai ca
                                                                </span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge" style="background-color: #fee2e2; color: #991b1b;">
                                                                    Conflict khác
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="body-sm text-on-surface"><c:out value="${log}" /></td>
                                                </tr>
                                            </c:if>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:if>

                    <c:if test="${hasDataError}">
                        <div class="card-premium overflow-hidden mb-4">
                            <div class="p-3 bg-surface border-bottom border-outline-variant d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined text-danger">error</span>
                                <div>
                                    <h3 class="h5 mb-1 fw-bold">Lỗi dữ liệu trong file</h3>
                                    <p class="body-sm text-on-surface-variant mb-0">Vui lòng sửa các dòng bên dưới trong file rồi import lại.</p>
                                </div>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-premium mb-0 w-100">
                                    <thead>
                                        <tr>
                                            <th style="width: 50px;">STT</th>
                                            <th>Nội dung lỗi</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:set var="di" value="0" />
                                        <c:forEach var="log" items="${errorLogs}">
                                            <c:if test="${not fn:contains(log, 'Conflict')}">
                                                <c:set var="di" value="${di + 1}" />
                                                <tr>
                                                    <td>${di}</td>
                                                    <td class="body-sm text-on-surface"><c:out value="${log}" /></td>
                                                </tr>
                                            </c:if>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:if>
                </c:if>

                <div class="card-premium p-3 bg-surface">
                    <p class="body-sm text-on-surface-variant mb-2 fw-medium">Định dạng file</p>
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>employee_code</th>
                                    <th>date</th>
                                    <th>check_in</th>
                                    <th>check_out</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>AD001</td>
                                    <td>2026-06-10</td>
                                    <td>07:55</td>
                                    <td>17:05</td>
                                </tr>
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
