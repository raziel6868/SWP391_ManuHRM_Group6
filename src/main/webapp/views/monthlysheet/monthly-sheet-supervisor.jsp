<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Bảng công tháng - Quản đốc - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <style>
        /* ── Stepper ── */
        .approval-stepper {
            display: flex;
            gap: 0.5rem;
            flex-wrap: wrap;
        }
        .approval-step {
            display: flex;
            align-items: center;
            gap: 0.375rem;
            padding: 4px 10px;
            border-radius: 999px;
            font-size: 0.75rem;
            font-weight: 600;
            border: 1px solid var(--outline-variant);
            background: var(--surface-container-lowest);
            color: var(--on-surface-variant);
        }
        .approval-step.approved {
            background: #d1fae5;
            color: #065f46;
            border-color: #6ee7b7;
        }
        .approval-step.pending {
            background: #fef3c7;
            color: #92400e;
            border-color: #fcd34d;
        }
        .approval-step .dot {
            width: 7px; height: 7px;
            border-radius: 50%;
            background: currentColor;
        }

        /* ── Stats strip ── */
        .stats-strip {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            border-bottom: 1px solid var(--outline-variant);
        }
        .stat-box {
            text-align: center;
            padding: 0.75rem 0.5rem;
            border-right: 1px solid var(--outline-variant);
        }
        .stat-box:last-child { border-right: none; }
        .stat-val { font-size: 1.25rem; font-weight: 700; line-height: 1; }
        .stat-lbl { font-size: 0.7rem; color: var(--on-surface-variant); margin-top: 2px; }
        .val-green  { color: #10b981; }
        .val-yellow { color: #f59e0b; }
        .val-red    { color: #ef4444; }
        .val-blue   { color: #3b82f6; }

        /* ── Attendance table ── */
        .att-table-wrapper {
            overflow-x: auto;
            -webkit-overflow-scrolling: touch;
        }
        .att-table {
            border-collapse: separate;
            border-spacing: 0;
            min-width: max-content;
            width: 100%;
            font-size: 0.8rem;
        }
        .att-table th, .att-table td {
            border-bottom: 1px solid var(--outline-variant);
            border-right: 1px solid var(--outline-variant);
            white-space: nowrap;
        }
        .att-table th:last-child,
        .att-table td:last-child { border-right: none; }

        /* Sticky cột tên */
        .col-employee {
            position: sticky;
            left: 0;
            background: var(--surface-container-lowest);
            z-index: 2;
            min-width: 160px;
            padding: 0.5rem 0.75rem;
            font-weight: 600;
            color: var(--on-surface);
        }
        .att-table thead .col-employee {
            background: var(--surface-container);
            color: var(--on-surface-variant);
            font-size: 0.72rem;
            text-transform: uppercase;
            letter-spacing: 0.04em;
            z-index: 3;
        }

        /* Header ngày */
        .att-table thead th.day-col {
            background: var(--surface-container);
            color: var(--on-surface-variant);
            font-size: 0.72rem;
            text-align: center;
            padding: 0.375rem 0.25rem;
            min-width: 36px;
        }
        .att-table thead th.day-col.weekend { color: #dc2626; }

        /* Badge trong ô */
        .att-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 26px; height: 26px;
            border-radius: 50%;
            font-size: 0.65rem;
            font-weight: 700;
            cursor: default;
            position: relative;
        }
        .att-badge.p     { background: #dcfce7; color: #166534; }
        .att-badge.late  { background: #ffedd5; color: #9a3412; }
        .att-badge.a     { background: #fee2e2; color: #991b1b; }

        /* Tooltip */
        .att-badge[data-tooltip]:hover::after {
            content: attr(data-tooltip);
            position: absolute;
            bottom: calc(100% + 6px);
            left: 50%;
            transform: translateX(-50%);
            background: #1e293b;
            color: #fff;
            font-size: 0.68rem;
            font-weight: 400;
            padding: 4px 8px;
            border-radius: 6px;
            white-space: nowrap;
            z-index: 100;
            pointer-events: none;
        }

        .att-table td.day-cell {
            text-align: center;
            padding: 0.375rem 0.25rem;
        }
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
                    <h2 class="h3 fw-bold mb-1">Bảng công tháng</h2>
                    <p class="body-md text-on-surface-variant mb-0">Xem và xác nhận bảng công cấp dưới trước khi chuyển HR.</p>
                </div>
            </div>

            <%-- ── Tab navigation ── --%>
            <ul class="nav nav-tabs mb-3">
                <li class="nav-item">
                    <a class="nav-link active"
                       href="?tab=attendance&year=${selectedYear}&month=${selectedMonth}">
                        Bảng công
                    </a>
                </li>
            </ul>

            <%-- ── TAB: Bảng công ── --%>

                <%-- Filter tháng + nhân viên luôn hiển thị để quản đốc đổi kỳ --%>
                <div class="card-premium overflow-hidden mb-3">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/monthly-sheet-supervisor"
                              method="GET" class="row g-3 align-items-end">
                            <input type="hidden" name="tab" value="attendance" />
                            <div class="col-md-2">
                                <label class="form-label fw-medium mb-1">Tháng</label>
                                <select name="month" class="form-select input-premium">
                                    <c:forEach begin="1" end="12" var="m">
                                        <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label fw-medium mb-1">Năm</label>
                                <select name="year" class="form-select input-premium">
                                    <c:forEach begin="2024" end="2027" var="y">
                                        <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label fw-medium mb-1">Nhân viên</label>
                                <select name="userId" class="form-select input-premium">
                                    <option value="">-- Tất cả --</option>
                                    <c:forEach var="sub" items="${subordinates}">
                                        <option value="${sub.id}"
                                            ${selectedUserId == sub.id ? 'selected' : ''}>
                                            ${sub.employeeCode} - ${sub.fullName}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                        </form>
                    </div>
                </div>

                <%-- Sheet chưa tồn tại hoặc không đúng trạng thái --%>
                <c:if test="${empty sheet || sheet.status == 'OPEN'}">
                    <div class="card-premium p-4 text-center text-on-surface-variant">
                        <span class="material-symbols-outlined" style="font-size:2rem;">pending_actions</span>
                        <p class="mt-2 mb-0">Bảng công tháng ${selectedMonth}/${selectedYear} chưa được HR gửi duyệt.</p>
                    </div>
                </c:if>

                <c:if test="${not empty sheet && sheet.status != 'OPEN'}">
                    <div class="card-premium overflow-hidden mb-3">
                        <%-- Stepper quản đốc --%>
                        <div class="p-3 border-bottom border-outline-variant d-flex align-items-center justify-content-between flex-wrap gap-2">
                            <div class="approval-stepper">
                                <c:forEach var="ap" items="${allApprovals}">
                                    <div class="approval-step ${ap.status == 'APPROVED' ? 'approved' : 'pending'}">
                                        <div class="dot"></div>
                                        <c:out value="${ap.supervisorName}" /> (${ap.departmentName})
                                    </div>
                                </c:forEach>
                            </div>
                            <span class="body-sm text-on-surface-variant">
                                ${sheet.approvedSupervisors}/${sheet.totalSupervisors} quản đốc đã chốt
                            </span>
                        </div>

                        <%-- Stats strip --%>
                        <div class="stats-strip" id="statsStrip">
                            <div class="stat-box">
                                <div class="stat-val val-blue" id="statHours">—</div>
                                <div class="stat-lbl">Tổng giờ công</div>
                            </div>
                            <div class="stat-box">
                                <div class="stat-val val-green" id="statPresent">—</div>
                                <div class="stat-lbl">Bình thường</div>
                            </div>
                            <div class="stat-box">
                                <div class="stat-val val-red" id="statAbsent">—</div>
                                <div class="stat-lbl">Vắng</div>
                            </div>
                            <div class="stat-box">
                                <div class="stat-val val-yellow" id="statLate">—</div>
                                <div class="stat-lbl">Đi muộn</div>
                            </div>
                        </div>

                        <%-- Bảng công calendar --%>
                        <div class="att-table-wrapper">
                            <table class="att-table" id="attTable">
                                <thead id="attThead"></thead>
                                <tbody id="attTbody"></tbody>
                            </table>
                        </div>

                        <%-- Nút chốt --%>
                        <c:if test="${sheet.status == 'PENDING_SUPERVISOR'}">
                            <div class="p-3 border-top border-outline-variant d-flex justify-content-end gap-2">
                                <c:choose>
                                    <c:when test="${canApprove && !hasPendingCorrections}">
                                        <form action="${pageContext.request.contextPath}/monthly-sheet-supervisor-approve"
                                              method="POST">
                                            <input type="hidden" name="id" value="${sheet.id}" />
                                            <button type="submit" class="btn btn-primary d-flex align-items-center gap-2">
                                                <span class="material-symbols-outlined" style="font-size:1rem;">task_alt</span>
                                                Xác nhận chốt bảng công
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:when test="${hasPendingCorrections}">
                                        <a class="btn btn-primary d-flex align-items-center gap-2"
                                           href="${pageContext.request.contextPath}/attendance-correction-list?tab=supervisor">
                                            <span class="material-symbols-outlined" style="font-size:1rem;">lock</span>
                                            Còn yêu cầu điều chỉnh chưa xử lý — xử lý tại đây
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-success" disabled>
                                            <span class="material-symbols-outlined" style="font-size:1rem;">check_circle</span>
                                            Đã chốt
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>
                    </div>
                </c:if>
        </div>

        <jsp:include page="/components/footer.jsp" />
    </div>
</div>


<%-- Inject dữ liệu attendance vào JS --%>
<script>
const YEAR  = ${selectedYear};
const MONTH = ${selectedMonth};
const FILTER_USER_ID = '${selectedUserId}';

// Map: "userId|date" → record
const recMap = {};
<c:forEach var="r" items="${records}">
    <c:set var="rKey" value="${r.userId}|${r.date}" />
    recMap['${rKey}'] = {
        status:   '${r.status}',
        checkIn:  '<c:out value="${r.checkIn}"  default=""/>',
        checkOut: '<c:out value="${r.checkOut}" default=""/>',
        hours:    '<c:out value="${r.workingHours}" default=""/>'
    };
</c:forEach>

// Danh sách nhân viên cấp dưới
const employees = [
    <c:forEach var="sub" items="${subordinates}" varStatus="st">
    { id: '${sub.id}', code: '<c:out value="${sub.employeeCode}"/>', name: '<c:out value="${sub.fullName}"/>' }<c:if test="${!st.last}">,</c:if>
    </c:forEach>
];

// Build calendar table
(function () {
    const thead = document.getElementById('attThead');
    const tbody = document.getElementById('attTbody');
    const statHours = document.getElementById('statHours');
    const statPresent = document.getElementById('statPresent');
    const statAbsent = document.getElementById('statAbsent');
    const statLate = document.getElementById('statLate');

    if (!thead || !tbody || !statHours || !statPresent || !statAbsent || !statLate) {
        return;
    }

    const daysInMonth = new Date(YEAR, MONTH, 0).getDate();
    const DOW_LABELS  = ['CN','T2','T3','T4','T5','T6','T7'];

    // Filter employees nếu đang lọc 1 người
    const visibleEmps = FILTER_USER_ID
        ? employees.filter(e => e.id === FILTER_USER_ID)
        : employees;

    // Header
    let thHTML = '<tr><th class="col-employee">Nhân viên</th>';
    for (let d = 1; d <= daysInMonth; d++) {
        const dow = new Date(YEAR, MONTH - 1, d).getDay();
        const isWE = dow === 0 || dow === 6;
        thHTML += '<th class="day-col' + (isWE ? ' weekend' : '') + '">'
                + d
                + '<br><span style="font-weight:400;font-size:0.65rem">'
                + DOW_LABELS[dow]
                + '</span></th>';
    }
    thHTML += '</tr>';
    thead.innerHTML = thHTML;

    // Body + stats
    let totalHours = 0, totalPresent = 0, totalAbsent = 0, totalLate = 0;

    visibleEmps.forEach(emp => {
        let row = '<td class="col-employee"><div class="fw-medium">'
                + emp.name
                + '</div><div class="body-sm text-on-surface-variant">'
                + emp.code
                + '</div></td>';

        for (let d = 1; d <= daysInMonth; d++) {
            const mm  = String(MONTH).padStart(2, '0');
            const dd  = String(d).padStart(2, '0');
            const key = emp.id + '|' + YEAR + '-' + mm + '-' + dd;
            const dow = new Date(YEAR, MONTH - 1, d).getDay();
            const isWE = dow === 0 || dow === 6;
            const rec = recMap[key];

            let badge = '';
            if (isWE) {
                // Cuối tuần: hiển thị chữ "W" dạng text, không dùng badge tròn — giống attendance-list
                badge = '<span class="text-on-surface-variant" style="font-size:0.75rem;">W</span>';
            } else if (!rec) {
                // Không có dữ liệu trong ngày làm việc: để trống — giống attendance-list
                badge = '';
            } else {
                const st = rec.status;
                let cls = '', lbl = '';
                if (st === 'NORMAL') { cls = 'p';    lbl = 'P'; totalPresent++; }
                else if (st === 'LATE')   { cls = 'late'; lbl = 'P'; totalLate++;    }
                else if (st === 'ABSENT') { cls = 'a';    lbl = 'A'; totalAbsent++;  }
                const h = parseFloat(rec.hours);
                if (!isNaN(h)) totalHours += h;
                const tip = rec.checkIn
                    ? rec.checkIn.substring(0,5) + ' → '
                        + (rec.checkOut ? rec.checkOut.substring(0,5) : '?')
                        + ' | ' + (rec.hours || '—') + 'h'
                    : 'Vắng';
                badge = cls ? '<span class="att-badge ' + cls + '" data-tooltip="' + tip + '">' + lbl + '</span>' : '';
            }
            row += '<td class="day-cell">' + badge + '</td>';
        }
        tbody.innerHTML += '<tr>' + row + '</tr>';
    });

    // Stats
    statHours.textContent = totalHours.toFixed(1) + 'h';
    statPresent.textContent = totalPresent;
    statAbsent.textContent = totalAbsent;
    statLate.textContent = totalLate;
})();

</script>

<jsp:include page="/components/foot.jsp" />
</body>
</html>
