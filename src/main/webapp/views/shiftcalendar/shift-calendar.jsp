<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Lịch phân ca - ManuHRM</title>
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

                <c:if test="${not empty importSuccessCount}">
                    <div class="alert alert-success d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">check_circle</span>
                        Import thành công ${importSuccessCount}/${importTotal} bản ghi!
                        <c:if test="${importErrorCount > 0}">
                            <span class="text-danger ms-2">(${importErrorCount} lỗi)</span>
                        </c:if>
                    </div>
                    <c:if test="${not empty importErrors}">
                        <div class="alert alert-error mb-3" role="alert" style="max-height: 150px; overflow-y: auto;">
                            <small>
                                <c:forEach var="err" items="${importErrors}">
                                    <div>${err}</div>
                                </c:forEach>
                            </small>
                        </div>
                    </c:if>
                </c:if>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Lịch phân ca</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem và phân công ca làm việc theo tháng.</p>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <button type="button" class="btn btn-outline-primary px-3 py-2 d-flex align-items-center gap-2"
                                onclick="document.getElementById('importModal').classList.add('show')">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">upload</span>
                            Import Excel
                        </button>
                    </div>
                </div>

                <div class="row g-4 mb-4">
                    <div class="col-lg-8">
                        <div class="card-premium overflow-hidden">
                            <div class="p-3 bg-surface border-bottom border-outline-variant">
                                <form action="${pageContext.request.contextPath}/shift-calendar" method="GET"
                                      class="row g-3 align-items-end">
                                    <input type="hidden" name="action" value="filter" />
                                    <div class="col-md-4">
                                        <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                        <select name="departmentId" class="form-select input-premium">
                                            <option value="">Tất cả phòng ban</option>
                                            <c:forEach var="dept" items="${departments}">
                                                <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>${dept.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label text-on-surface fw-medium mb-1">Tháng</label>
                                        <select name="month" class="form-select input-premium">
                                            <c:forEach begin="1" end="12" var="m">
                                                <option value="${m}" ${currentMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-md-3">
                                        <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                        <input type="number" name="year" value="${currentYear}" class="form-control input-premium" min="2020" max="2100" />
                                    </div>
                                    <div class="col-md-2 d-flex gap-2">
                                        <button type="submit" class="btn btn-primary w-100">Xem</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="p-3 bg-surface border-bottom border-outline-variant">
                                <h5 class="mb-2 text-on-surface fw-semibold">Phân ca nhanh</h5>
                            </div>
                            <div class="p-3">
                                <form action="${pageContext.request.contextPath}/shift-calendar" method="POST" id="assignForm">
                                    <input type="hidden" name="action" value="assign" />
                                    <input type="hidden" name="departmentId" value="${selectedDepartmentId}" />
                                    <input type="hidden" name="month" value="${currentMonth}" />
                                    <input type="hidden" name="year" value="${currentYear}" />
                                    <div class="row g-2">
                                        <div class="col-12">
                                            <label class="form-label text-on-surface fw-medium mb-1">Nhân viên</label>
                                            <select name="userId" id="assignUserSelect" class="form-select input-premium" required>
                                                <option value="">-- Chọn nhân viên --</option>
                                                <c:forEach var="u" items="${users}">
                                                    <option value="${u.id}" data-dept="${u.departmentId}">
                                                        ${u.fullName} (${u.employeeCode})
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-6">
                                            <label class="form-label text-on-surface fw-medium mb-1">Ca</label>
                                            <select name="shiftId" class="form-select input-premium" required>
                                                <option value="">-- Chọn ca --</option>
                                                <c:forEach var="shift" items="${shifts}">
                                                    <option value="${shift.id}">${shift.name} (${shift.code})</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-6">
                                            <label class="form-label text-on-surface fw-medium mb-1">Ngày</label>
                                            <input type="date" name="date" class="form-control input-premium" required />
                                        </div>
                                        <div class="col-12 mt-2">
                                            <button type="submit" class="btn btn-primary w-100">
                                                <span class="material-symbols-outlined me-1">add</span>
                                                Phân ca
                                            </button>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card-premium overflow-hidden mb-4">
                    <div class="card-header-custom p-3 bg-surface border-bottom border-outline-variant">
                        <div class="d-flex justify-content-between align-items-center">
                            <h4 class="mb-0 fw-semibold">Tháng ${currentMonth}/${currentYear}</h4>
                            <div class="d-flex gap-2">
                                <c:choose>
                                    <c:when test="${currentMonth == 1}">
                                        <c:set var="prevMonth" value="12" />
                                        <c:set var="prevYear" value="${currentYear - 1}" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="prevMonth" value="${currentMonth - 1}" />
                                        <c:set var="prevYear" value="${currentYear}" />
                                    </c:otherwise>
                                </c:choose>
                                <c:choose>
                                    <c:when test="${currentMonth == 12}">
                                        <c:set var="nextMonth" value="1" />
                                        <c:set var="nextYear" value="${currentYear + 1}" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="nextMonth" value="${currentMonth + 1}" />
                                        <c:set var="nextYear" value="${currentYear}" />
                                    </c:otherwise>
                                </c:choose>
                                <a href="${pageContext.request.contextPath}/shift-calendar?month=${prevMonth}&year=${prevYear}&departmentId=${selectedDepartmentId}"
                                   class="btn btn-sm btn-light border text-on-surface-variant">
                                    <span class="material-symbols-outlined">chevron_left</span>
                                </a>
                                <a href="${pageContext.request.contextPath}/shift-calendar?month=${nextMonth}&year=${nextYear}&departmentId=${selectedDepartmentId}"
                                   class="btn btn-sm btn-light border text-on-surface-variant">
                                    <span class="material-symbols-outlined">chevron_right</span>
                                </a>
                            </div>
                        </div>
                    </div>
                    <div class="card-body p-3">
                        <div class="calendar-grid">
                            <c:forEach var="day" begin="1" end="${endDate.dayOfMonth}">
                                <c:set var="currentDate" value="${startDate.plusDays(day - 1)}" />
                                <div class="calendar-day" data-date="${currentDate}">
                                    <div class="calendar-day-header">
                                        <span class="day-number">${day}</span>
                                        <span class="day-weekday">${currentDate.dayOfWeek.name().substring(0,3)}</span>
                                    </div>
                                    <div class="calendar-day-content">
                                        <c:forEach var="entry" items="${assignments}">
                                            <c:if test="${fn:contains(entry.key, '_'.concat(currentDate))}">
                                                <div class="shift-item" data-id="${entry.value.id}"
                                                     onclick="showShiftOptions(this, '${entry.value.id}', '${entry.value.userFullName}', '${entry.value.shiftName}', '${entry.value.shiftCode}')">
                                                    <span class="shift-badge">${entry.value.shiftCode}</span>
                                                    <span class="shift-user">${entry.value.userFullName}</span>
                                                </div>
                                            </c:if>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>

                <div class="card-premium overflow-hidden">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <div class="d-flex justify-content-between align-items-center">
                            <h5 class="mb-0">Danh sách phân ca tháng</h5>
                            <div class="d-flex align-items-center gap-2">
                                <span class="badge bg-primary-subtle text-primary-emphasis">
                                    ${fn:length(assignmentsList)} bản ghi
                                </span>
                                <c:if test="${fn:length(assignmentsList) > 0}">
                                    <form action="${pageContext.request.contextPath}/shift-calendar" method="POST" class="d-inline">
                                        <input type="hidden" name="action" value="deleteAll" />
                                        <input type="hidden" name="departmentId" value="${selectedDepartmentId}" />
                                        <input type="hidden" name="month" value="${currentMonth}" />
                                        <input type="hidden" name="year" value="${currentYear}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger"
                                                title="Xóa tất cả phân ca"
                                                onclick="return confirm('Bạn có chắc muốn xóa tất cả phân ca?')">
                                            <span class="material-symbols-outlined" style="font-size: 1rem;">delete_sweep</span>
                                            Xóa tất cả
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Ngày</th>
                                    <th>Nhân viên</th>
                                    <th>Mã NV</th>
                                    <th>Phòng ban</th>
                                    <th>Ca</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="sa" items="${assignmentsList}">
                                    <tr>
                                        <td>${sa.date}</td>
                                        <td>${sa.userFullName}</td>
                                        <td>${sa.employeeCode}</td>
                                        <td>${sa.departmentName}</td>
                                        <td><span class="badge" style="background-color: #e0e7ff; color: #3730a3;">${sa.shiftName}</span></td>
                                        <td class="text-end">
                                            <form action="${pageContext.request.contextPath}/shift-calendar" method="POST" class="d-inline">
                                                <input type="hidden" name="action" value="delete" />
                                                <input type="hidden" name="id" value="${sa.id}" />
                                                <input type="hidden" name="departmentId" value="${selectedDepartmentId}" />
                                                <input type="hidden" name="month" value="${currentMonth}" />
                                                <input type="hidden" name="year" value="${currentYear}" />
                                                <button type="submit" class="btn btn-sm btn-icon text-danger hover-bg-danger-subtle"
                                                        title="Xóa phân ca"
                                                        onclick="return confirm('Xóa phân ca này?')">
                                                    <span class="material-symbols-outlined" style="font-size: 1.125rem;">delete</span>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty assignmentsList}">
                                    <tr>
                                        <td colspan="6" class="text-center py-4 text-on-surface-variant">
                                            Không có phân ca trong tháng này.
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

    <jsp:include page="/components/foot.jsp" />

    <div id="importModal" class="modal-overlay" onclick="closeModalOnOverlay(event)">
        <div class="modal-content card-premium">
            <div class="d-flex justify-content-between align-items-center p-3 border-bottom">
                <h5 class="mb-0">Import Excel phân ca</h5>
                <button type="button" class="btn-close" onclick="document.getElementById('importModal').classList.remove('show')"></button>
            </div>
            <div class="p-4">
                <div class="mb-3">
                    <p class="text-on-surface-variant mb-2">Tải file mẫu để biết định dạng:</p>
                    <a href="${pageContext.request.contextPath}/assets/templates/shift-assignment-sample.csv"
                       class="btn btn-outline-primary btn-sm" download>
                        <span class="material-symbols-outlined me-1">download</span>
                        Tải file mẫu
                    </a>
                </div>
                <hr class="my-3">
                <form action="${pageContext.request.contextPath}/shift-calendar-import" method="POST"
                      enctype="multipart/form-data">
                    <div class="mb-3">
                        <label class="form-label text-on-surface fw-medium mb-2">Chọn file Excel (.xlsx)</label>
                        <input type="file" name="excelFile" class="form-control" accept=".xlsx" required />
                    </div>
                    <div class="alert alert-info d-flex align-items-start gap-2">
                        <span class="material-symbols-outlined">info</span>
                        <small>
                            <strong>Định dạng cột:</strong><br>
                            Cột A: Mã nhân viên<br>
                            Cột B: Ngày (yyyy-MM-dd hoặc dd/MM/yyyy)<br>
                            Cột C: Mã ca làm việc
                        </small>
                    </div>
                    <div class="d-flex gap-2 justify-content-end mt-4">
                        <button type="button" class="btn btn-light border"
                                onclick="document.getElementById('importModal').classList.remove('show')">
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

    <style>
        .calendar-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
            gap: 8px;
        }
        .calendar-day {
            border: 1px solid var(--outline-variant);
            border-radius: 8px;
            min-height: 100px;
            background: var(--surface-container-lowest);
            transition: all 0.2s;
        }
        .calendar-day:hover {
            border-color: var(--primary);
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .calendar-day-header {
            padding: 6px 8px;
            border-bottom: 1px solid var(--outline-variant);
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: var(--surface-container-low);
            border-radius: 7px 7px 0 0;
        }
        .day-number {
            font-weight: 600;
            font-size: 0.875rem;
        }
        .day-weekday {
            font-size: 0.7rem;
            color: var(--on-surface-variant);
            text-transform: uppercase;
        }
        .calendar-day-content {
            padding: 4px;
            max-height: 80px;
            overflow-y: auto;
        }
        .shift-item {
            background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
            color: #3730a3;
            padding: 4px 6px;
            border-radius: 4px;
            margin-bottom: 3px;
            font-size: 0.75rem;
            cursor: pointer;
            transition: all 0.2s;
        }
        .shift-item:hover {
            transform: translateY(-1px);
            box-shadow: 0 2px 4px rgba(0,0,0,0.15);
        }
        .shift-badge {
            font-weight: 700;
            margin-right: 4px;
            background: rgba(55, 48, 163, 0.15);
            padding: 1px 4px;
            border-radius: 3px;
        }
        .shift-user {
            opacity: 0.9;
            font-size: 0.7rem;
        }
        .empty-day {
            display: none;
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
            background: var(--surface);
            border-radius: 12px;
            max-width: 500px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
        }
        .hover-bg-danger-subtle:hover {
            background: rgba(220, 53, 69, 0.1);
        }
    </style>

    <script>

        function showShiftOptions(element, id, userName, shiftName, shiftCode) {
            const confirmed = confirm('Xóa phân ca: ' + shiftName + ' - ' + userName + '?');
            if (confirmed) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '${pageContext.request.contextPath}/shift-calendar';
                form.innerHTML = `
                    <input type="hidden" name="action" value="delete" />
                    <input type="hidden" name="id" value="${id}" />
                    <input type="hidden" name="departmentId" value="${selectedDepartmentId}" />
                    <input type="hidden" name="month" value="${currentMonth}" />
                    <input type="hidden" name="year" value="${currentYear}" />
                `;
                document.body.appendChild(form);
                form.submit();
            }
        }

        function closeModalOnOverlay(event) {
            if (event.target.id === 'importModal') {
                document.getElementById('importModal').classList.remove('show');
            }
        }

        // Filter users by department in assign form
        document.getElementById('departmentSelect')?.addEventListener('change', function() {
            filterUsersByDept();
        });

        function filterUsersByDept() {
            const deptId = document.querySelector('select[name="departmentId"]')?.value || '';
            const userSelect = document.getElementById('assignUserSelect');

            if (!userSelect) return;

            for (let i = 0; i < userSelect.options.length; i++) {
                const option = userSelect.options[i];
                const userDeptId = option.getAttribute('data-dept');

                if (option.value === '') continue;

                if (deptId === '' || userDeptId === deptId) {
                    option.style.display = '';
                } else {
                    option.style.display = 'none';
                }
            }
        }
    </script>
</body>
</html>
