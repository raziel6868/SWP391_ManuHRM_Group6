<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chấm công của tôi - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <style>
        /* ── Calendar grid ── */
        .att-cal-nav {
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }
        .att-cal-nav a {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 2rem;
            height: 2rem;
            border-radius: 50%;
            border: 1px solid var(--outline-variant);
            background: var(--surface-container-lowest);
            color: var(--on-surface-variant);
            text-decoration: none;
            transition: background 0.15s;
        }
        .att-cal-nav a:hover {
            background: var(--surface-container);
        }

        .att-legend {
            display: flex;
            gap: 1rem;
            flex-wrap: wrap;
        }
        .att-legend-item {
            display: flex;
            align-items: center;
            gap: 0.375rem;
            font-size: 0.8rem;
            color: var(--on-surface-variant);
        }
        .att-legend-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            flex-shrink: 0;
        }

        .att-grid {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            gap: 0.5rem;
            padding: 1rem;
        }
        .att-dow {
            text-align: center;
            font-size: 0.72rem;
            font-weight: 600;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            color: var(--on-surface-variant);
            padding-bottom: 0.25rem;
        }
        .att-dow.sun { color: #dc2626; }

        .att-cell {
            min-height: 88px;
            border-radius: 10px;
            border: 1px solid var(--outline-variant);
            background: var(--surface-container-lowest);
            padding: 0.5rem 0.5rem 0.375rem;
            display: flex;
            flex-direction: column;
            gap: 0.25rem;
            position: relative;
            transition: box-shadow 0.15s;
        }
        .att-cell:hover {
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }
        .att-cell.empty {
            background: transparent;
            border-color: transparent;
            pointer-events: none;
        }
        .att-cell.today {
            border-color: var(--primary);
            background: color-mix(in srgb, var(--primary) 5%, var(--surface-container-lowest));
        }
        .att-cell.no-data {
            background: var(--surface-container-lowest);
            opacity: 0.6;
        }

        /* Status bar along the top of each cell */
        .att-cell::before {
            content: '';
            position: absolute;
            top: 0; left: 0; right: 0;
            height: 3px;
            border-radius: 10px 10px 0 0;
        }
        .att-cell.status-NORMAL::before  { background: #10b981; }
        .att-cell.status-LATE::before    { background: #f59e0b; }
        .att-cell.status-ABSENT::before  { background: #ef4444; }
        .att-cell.status-WEEKEND::before { background: transparent; }

        .att-day-num {
            font-size: 0.8rem;
            font-weight: 600;
            color: var(--on-surface-variant);
            line-height: 1;
        }
        .att-cell.today .att-day-num {
            color: var(--primary);
        }
        .att-cell .att-day-num.sun-num { color: #dc2626; }

        .att-status-badge {
            font-size: 0.68rem;
            font-weight: 600;
            padding: 1px 6px;
            border-radius: 999px;
            display: inline-block;
            width: fit-content;
        }
        .badge-normal  { background: #d1fae5; color: #065f46; }
        .badge-late    { background: #fef3c7; color: #92400e; }
        .badge-absent  { background: #fee2e2; color: #991b1b; }
        .badge-weekend { background: var(--surface-container); color: var(--on-surface-variant); }

        .att-time {
            font-size: 0.68rem;
            color: var(--on-surface-variant);
            display: flex;
            align-items: center;
            gap: 2px;
            line-height: 1.3;
        }
        .att-time .material-symbols-outlined {
            font-size: 0.8rem;
        }

        .att-adj-btn {
            margin-top: auto;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 3px;
            font-size: 0.65rem;
            font-weight: 500;
            padding: 2px 6px;
            border-radius: 6px;
            border: 1px solid var(--outline-variant);
            background: var(--surface-container-lowest);
            color: var(--on-surface-variant);
            cursor: pointer;
            width: 100%;
            transition: background 0.12s, border-color 0.12s;
            white-space: nowrap;
        }
        .att-adj-btn:hover {
            background: var(--surface-container);
            border-color: var(--primary);
            color: var(--primary);
        }
        .att-adj-btn .material-symbols-outlined {
            font-size: 0.82rem;
        }

        /* Summary strip */
        .att-summary {
            display: flex;
            gap: 0;
            border-top: 1px solid var(--outline-variant);
        }
        .att-summary-item {
            flex: 1;
            text-align: center;
            padding: 0.75rem 0.5rem;
            border-right: 1px solid var(--outline-variant);
        }
        .att-summary-item:last-child { border-right: none; }
        .att-summary-val {
            font-size: 1.25rem;
            font-weight: 700;
            line-height: 1;
        }
        .att-summary-lbl {
            font-size: 0.7rem;
            color: var(--on-surface-variant);
            margin-top: 2px;
        }
        .val-normal { color: #10b981; }
        .val-late   { color: #f59e0b; }
        .val-absent { color: #ef4444; }
        .val-neutral { color: var(--on-surface); }
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
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <c:out value="${errorMsg}" />
                    </div>
                </c:if>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Chấm công của tôi</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem lịch sử chấm công và gửi yêu cầu điều chỉnh nếu có sai sót.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden mb-4 w-100">

                    <%-- ── Header: nav tháng + legend ── --%>
                    <div class="p-3 bg-surface border-bottom border-outline-variant d-flex align-items-center justify-content-between flex-wrap gap-3">
                        <div class="att-cal-nav">
                            <a href="${pageContext.request.contextPath}/attendance-my?year=${prevYear}&month=${prevMonth}"
                               title="Tháng trước">
                                <span class="material-symbols-outlined" style="font-size:1.1rem;">chevron_left</span>
                            </a>
                            <span class="fw-bold text-on-surface" style="min-width: 130px; text-align: center;">
                                Tháng ${selectedMonth}/${selectedYear}
                            </span>
                            <a href="${pageContext.request.contextPath}/attendance-my?year=${nextYear}&month=${nextMonth}"
                               title="Tháng sau">
                                <span class="material-symbols-outlined" style="font-size:1.1rem;">chevron_right</span>
                            </a>
                        </div>

                        <div class="att-legend">
                            <div class="att-legend-item">
                                <div class="att-legend-dot" style="background:#10b981;"></div> Bình thường
                            </div>
                            <div class="att-legend-item">
                                <div class="att-legend-dot" style="background:#f59e0b;"></div> Đi muộn
                            </div>
                            <div class="att-legend-item">
                                <div class="att-legend-dot" style="background:#ef4444;"></div> Vắng
                            </div>
                            <div class="att-legend-item">
                                <div class="att-legend-dot" style="background:var(--outline-variant);"></div> Cuối tuần / Không có dữ liệu
                            </div>
                        </div>
                    </div>

                    <%-- ── Calendar grid (built by JS from server data) ── --%>
                    <div class="att-grid" id="calGrid">
                        <%-- Day-of-week headers --%>
                        <div class="att-dow sun">CN</div>
                        <div class="att-dow">T2</div>
                        <div class="att-dow">T3</div>
                        <div class="att-dow">T4</div>
                        <div class="att-dow">T5</div>
                        <div class="att-dow">T6</div>
                        <div class="att-dow">T7</div>
                    </div>

                    <%-- ── Summary strip ── --%>
                    <div class="att-summary" id="attSummary">
                        <div class="att-summary-item">
                            <div class="att-summary-val val-normal" id="sumNormal">—</div>
                            <div class="att-summary-lbl">Bình thường</div>
                        </div>
                        <div class="att-summary-item">
                            <div class="att-summary-val val-late" id="sumLate">—</div>
                            <div class="att-summary-lbl">Đi muộn</div>
                        </div>
                        <div class="att-summary-item">
                            <div class="att-summary-val val-absent" id="sumAbsent">—</div>
                            <div class="att-summary-lbl">Vắng</div>
                        </div>
                        <div class="att-summary-item">
                            <div class="att-summary-val val-neutral" id="sumHours">—</div>
                            <div class="att-summary-lbl">Tổng giờ công</div>
                        </div>
                    </div>
                </div>
            </div>


            <%-- Yêu cầu điều chỉnh đang chờ --%>
            <c:if test="${not empty myCorrections}">
                <div class="card-premium overflow-hidden mb-4">
                    <div class="p-3 bg-surface border-bottom border-outline-variant d-flex align-items-center gap-2">
                        <span class="material-symbols-outlined text-on-surface-variant">pending_actions</span>
                        <h3 class="h5 fw-bold mb-0">Yêu cầu điều chỉnh của tôi</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-premium mb-0">
                            <thead>
                                <tr>
                                    <th>Ngày</th>
                                    <th>Đề nghị sửa</th>
                                    <th>Lý do</th>
                                    <th>Quản đốc</th>
                                    <th>HR</th>
                                    <th>Ghi chú từ chối</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="cr" items="${myCorrections}">
                                    <tr>
                                        <td class="fw-medium"><c:out value="${cr.attendanceDate}" /></td>
                                        <td class="body-sm">
                                            <c:out value="${cr.newCheckIn}" default="—" /> →
                                            <c:out value="${cr.newCheckOut}" default="—" />
                                        </td>
                                        <td class="body-sm" style="max-width:180px;"><c:out value="${cr.reason}" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${cr.supervisorStatus == 'PENDING'}"><span class="badge" style="background:#fef3c7;color:#92400e;">Chờ duyệt</span></c:when>
                                                <c:when test="${cr.supervisorStatus == 'APPROVED'}"><span class="badge" style="background:#d1fae5;color:#065f46;">Đã duyệt</span></c:when>
                                                <c:when test="${cr.supervisorStatus == 'REJECTED'}"><span class="badge" style="background:#fee2e2;color:#991b1b;">Từ chối</span></c:when>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${cr.supervisorStatus != 'APPROVED'}"><span class="text-on-surface-variant body-sm">—</span></c:when>
                                                <c:when test="${cr.status == 'PENDING'}"><span class="badge" style="background:#dbeafe;color:#1e40af;">Chờ HR</span></c:when>
                                                <c:when test="${cr.status == 'APPROVED'}"><span class="badge" style="background:#d1fae5;color:#065f46;">Đã duyệt</span></c:when>
                                                <c:when test="${cr.status == 'REJECTED'}"><span class="badge" style="background:#fee2e2;color:#991b1b;">Từ chối</span></c:when>
                                            </c:choose>
                                        </td>
                                        <td class="body-sm text-on-surface-variant" style="max-width:200px;">
                                            <c:choose>
                                                <c:when test="${not empty cr.supervisorRejectReason}"><span class="fw-medium">Quản đốc:</span> <c:out value="${cr.supervisorRejectReason}" /></c:when>
                                                <c:when test="${not empty cr.hrRejectReason}"><span class="fw-medium">HR:</span> <c:out value="${cr.hrRejectReason}" /></c:when>
                                                <c:otherwise>—</c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:if>
            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <%-- ── Modal điều chỉnh ── --%>
    <div id="correctionModalOverlay"
         style="display:none; position:fixed; inset:0; background:rgba(11,28,48,0.45); z-index:1050; align-items:center; justify-content:center;">
        <div class="card-premium" style="max-width: 480px; width: 92%; padding: 1.5rem;">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold text-on-surface mb-0">Yêu cầu điều chỉnh công</h5>
                <button type="button" onclick="closeCorrectionModal()" class="btn btn-sm btn-icon text-on-surface-variant" aria-label="Đóng">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>

            <form action="${pageContext.request.contextPath}/attendance-correction-request" method="POST">
                <input type="hidden" name="attendanceRecordId" id="modalRecordId" />

                <div class="mb-3">
                    <label class="form-label text-on-surface fw-medium mb-1">Ngày chấm công</label>
                    <input type="text" id="modalDateDisplay" class="form-control input-premium" disabled />
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <label class="form-label text-on-surface fw-medium mb-1">Giờ vào hiện tại</label>
                        <input type="text" id="modalCurrentCheckIn" class="form-control input-premium" disabled />
                    </div>
                    <div class="col-md-6">
                        <label class="form-label text-on-surface fw-medium mb-1">Giờ ra hiện tại</label>
                        <input type="text" id="modalCurrentCheckOut" class="form-control input-premium" disabled />
                    </div>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <label class="form-label text-on-surface fw-medium mb-1">Giờ vào mới <span class="text-danger">*</span></label>
                        <input type="time" name="newCheckIn" class="form-control input-premium" required />
                    </div>
                    <div class="col-md-6">
                        <label class="form-label text-on-surface fw-medium mb-1">Giờ ra mới <span class="text-danger">*</span></label>
                        <input type="time" name="newCheckOut" class="form-control input-premium" required />
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label text-on-surface fw-medium mb-1">Lý do điều chỉnh <span class="text-danger">*</span></label>
                    <textarea name="reason" rows="3" class="form-control input-premium"
                              placeholder="Ví dụ: Quên chấm công ra do quên mang thẻ..." required></textarea>
                </div>

                <div class="d-flex justify-content-end gap-2">
                    <button type="button" onclick="closeCorrectionModal()" class="btn btn-light border">Hủy</button>
                    <button type="submit" class="btn btn-primary">Gửi yêu cầu</button>
                </div>
            </form>
        </div>
    </div>

    <%-- ── Dữ liệu từ server inject vào JS ── --%>
    <script>
    // Map ngày → record từ server
    console.log("NEW FILE LOADED");
    const recordMap = {};
    <c:forEach var="r" items="${records}">
    recordMap["${r.date}"] = {
        id:       "${r.id}",
        status:   "${r.status}",
        checkIn:  "<c:out value='${r.checkIn}'  default=''/>",
        checkOut: "<c:out value='${r.checkOut}' default=''/>",
        hours:    "<c:out value='${r.workingHours}' default=''/>",
        shift:    "<c:out value='${r.shiftName}' default=''/>"
    };
    console.log("row:", "${r.date}", "${r.status}");
    </c:forEach>
    console.log("Total records from server:", ${fn:length(records)});


    // Normalize keys: trim whitespace
    const normalizedMap = {};
    for (const k of Object.keys(recordMap)) { normalizedMap[k.trim()] = recordMap[k]; }
    Object.assign(recordMap, normalizedMap);
    console.log("recordMap first 3 keys:", Object.keys(recordMap).slice(0,3));
    console.log("selectedYear:", ${selectedYear}, "selectedMonth:", ${selectedMonth});
    const year  = ${selectedYear};
    const month = ${selectedMonth}; // 1-based
    const todayStr = new Date().toLocaleDateString('sv'); // yyyy-MM-dd

    // Prev / next month links (servlet handles the URL, we just need the grid)
    // Build calendar
    const grid = document.getElementById('calGrid');

    const firstDay = new Date(year, month - 1, 1).getDay(); // 0=Sun
    const daysInMonth = new Date(year, month, 0).getDate();

    // Pad empty cells before day 1
    for (let i = 0; i < firstDay; i++) {
        const empty = document.createElement('div');
        empty.className = 'att-cell empty';
        grid.appendChild(empty);
    }

    let sumNormal = 0, sumLate = 0, sumAbsent = 0, totalHours = 0;

    for (let d = 1; d <= daysInMonth; d++) {
        const mm   = String(month).padStart(2, '0');
        const dd   = String(d).padStart(2, '0');
        const dateStr = `${year}-${mm}-${dd}`;
        const dow  = new Date(year, month - 1, d).getDay(); // 0=Sun,6=Sat
        const isWeekend = (dow === 0 || dow === 6);
        const rec  = recordMap[dateStr];
        const isToday = (dateStr === todayStr);

        const cell = document.createElement('div');
        cell.className = 'att-cell';
        if (isToday) cell.classList.add('today');

        let statusClass = '';
        let badgeHtml   = '';
        let timeHtml    = '';
        let btnHtml     = '';

        if (isWeekend && !rec) {
            // Weekend, no data
            cell.classList.add('status-WEEKEND');
            const label = dow === 0 ? 'Chủ nhật' : 'Thứ 7';
            badgeHtml = `<span class="att-status-badge badge-weekend">${label}</span>`;
        } else if (!rec) {
            // Weekday, no import data yet
            cell.classList.add('no-data');
            badgeHtml = `<span class="att-status-badge badge-weekend">—</span>`;
        } else {
            const st = rec.status;
            cell.classList.add(`status-${st}`);

            if (st === 'NORMAL') {
                sumNormal++;
                badgeHtml = `<span class="att-status-badge badge-normal">Bình thường</span>`;
            } else if (st === 'LATE') {
                sumLate++;
                badgeHtml = `<span class="att-status-badge badge-late">Đi muộn</span>`;
            } else if (st === 'ABSENT') {
                sumAbsent++;
                badgeHtml = `<span class="att-status-badge badge-absent">Vắng</span>`;
            }

            if (rec.checkIn) {
                const cin  = rec.checkIn.substring(0,5);
                const cout = rec.checkOut ? rec.checkOut.substring(0,5) : '—';
                timeHtml = `
                    <div class="att-time">
                        <span class="material-symbols-outlined">login</span>${cin}
                        &nbsp;
                        <span class="material-symbols-outlined">logout</span>${cout}
                    </div>`;
            }
            if (rec.hours) {
                const h = parseFloat(rec.hours);
                if (!isNaN(h)) totalHours += h;
                timeHtml += `<div class="att-time"><span class="material-symbols-outlined">schedule</span>${rec.hours}h</div>`;
            }

            // Adjustment button
            const safeDate    = dateStr;
            const safeCheckIn  = rec.checkIn  || '';
            const safeCheckOut = rec.checkOut || '';
            btnHtml = `
                <button class="att-adj-btn"
                        onclick="openCorrectionModal('${rec.id}','${safeDate}','${safeCheckIn}','${safeCheckOut}')"
                        title="Yêu cầu điều chỉnh">
                    <span class="material-symbols-outlined">edit_calendar</span>
                    Điều chỉnh
                </button>`;
        }

        // Day number — Sunday in red
        const dayNumClass = (dow === 0) ? 'att-day-num sun-num' : 'att-day-num';
        cell.innerHTML = `<div class="${dayNumClass}">${d}</div>${badgeHtml}${timeHtml}${btnHtml}`;
        grid.appendChild(cell);
    }

    // Summary
    document.getElementById('sumNormal').textContent = sumNormal;
    document.getElementById('sumLate').textContent   = sumLate;
    document.getElementById('sumAbsent').textContent = sumAbsent;
    document.getElementById('sumHours').textContent  = totalHours.toFixed(1) + 'h';

    // Modal
    function openCorrectionModal(id, date, checkIn, checkOut) {
        document.getElementById('modalRecordId').value        = id;
        document.getElementById('modalDateDisplay').value     = date;
        document.getElementById('modalCurrentCheckIn').value  = checkIn  || '—';
        document.getElementById('modalCurrentCheckOut').value = checkOut || '—';
        document.getElementById('correctionModalOverlay').style.display = 'flex';
    }
    function closeCorrectionModal() {
        document.getElementById('correctionModalOverlay').style.display = 'none';
    }
    </script>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
