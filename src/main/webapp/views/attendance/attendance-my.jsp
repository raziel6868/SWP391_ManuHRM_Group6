<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chấm công của tôi - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Chấm công của tôi</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Xem lịch chấm công cá nhân theo tháng. Bấm vào 1 ngày đã có dữ liệu để gửi yêu cầu điều chỉnh.
                        </p>
                    </div>
                </div>

                <!-- Legend -->
                <div class="card-premium overflow-hidden mb-4">
                    <div class="p-3 d-flex flex-wrap align-items-center gap-4">
                        <span class="fw-semibold text-on-surface-variant" style="font-size: 0.8rem; letter-spacing: 0.05em;">LEGEND:</span>
                        <div class="d-flex align-items-center gap-2">
                            <span class="att-dot" style="background:#dcfce7; color:#166534;">P</span>
                            <span class="body-sm">Có mặt</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="att-dot" style="background:#ffedd5; color:#9a3412;">P</span>
                            <span class="body-sm">Đi muộn</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="att-dot" style="background:#fee2e2; color:#991b1b;">A</span>
                            <span class="body-sm">Vắng</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="att-dot" style="background:#fef9c3; color:#854d0e;">L</span>
                            <span class="body-sm">Nghỉ phép</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="att-dot" style="background:#e5e7eb; color:#4b5563;">W</span>
                            <span class="body-sm">Cuối tuần</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="att-dot" style="background:#ede9fe; color:#5b21b6;">O</span>
                            <span class="body-sm">Có OT</span>
                        </div>
                    </div>
                </div>

                <!-- Stat cards -->
                <div class="row g-3 mb-4">
                    <div class="col-6" style="flex: 0 0 20%; max-width: 20%;">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <p class="text-on-surface-variant mb-1" style="font-size: 0.75rem;">Có mặt</p>
                                <h3 class="mb-0 fw-bold" style="color:#166534;">${countPresent}</h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6" style="flex: 0 0 20%; max-width: 20%;">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <p class="text-on-surface-variant mb-1" style="font-size: 0.75rem;">Đi muộn</p>
                                <h3 class="mb-0 fw-bold" style="color:#9a3412;">${countLate}</h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6" style="flex: 0 0 20%; max-width: 20%;">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <p class="text-on-surface-variant mb-1" style="font-size: 0.75rem;">Vắng</p>
                                <h3 class="mb-0 fw-bold" style="color:#991b1b;">${countAbsent}</h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6" style="flex: 0 0 20%; max-width: 20%;">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <p class="text-on-surface-variant mb-1" style="font-size: 0.75rem;">Nghỉ phép</p>
                                <h3 class="mb-0 fw-bold" style="color:#854d0e;">${countLeave}</h3>
                            </div>
                        </div>
                    </div>
                    <div class="col-6" style="flex: 0 0 20%; max-width: 20%;">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <p class="text-on-surface-variant mb-1" style="font-size: 0.75rem;">Giờ OT trong tháng</p>
                                <h3 class="mb-0 fw-bold" style="color:#5b21b6;"><fmt:formatNumber value="${totalOtHours}" pattern="0.##" />h</h3>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card-premium overflow-hidden mb-4">
                    <div class="card-header-custom p-3 bg-surface border-bottom border-outline-variant">
                        <div class="d-flex justify-content-between align-items-center">
                            <h4 class="mb-0 fw-semibold">Lịch chấm công tháng ${selectedMonth}/${selectedYear}</h4>
                            <div class="d-flex gap-2">
                                <a href="${pageContext.request.contextPath}/attendance-my?month=${prevMonth}&year=${prevYear}"
                                   class="btn btn-sm btn-light border text-on-surface-variant">
                                    <span class="material-symbols-outlined">chevron_left</span>
                                </a>
                                <a href="${pageContext.request.contextPath}/attendance-my?month=${nextMonth}&year=${nextYear}"
                                   class="btn btn-sm btn-light border text-on-surface-variant">
                                    <span class="material-symbols-outlined">chevron_right</span>
                                </a>
                            </div>
                        </div>
                    </div>
                    <div class="card-body p-3">
                        <div class="calendar-grid">
                            <c:forEach var="day" begin="1" end="${daysInMonth}">
                                <c:set var="currentDate" value="${startDate.plusDays(day - 1)}" />
                                <c:set var="status" value="${dayStatus[day]}" />
                                <c:set var="rec" value="${dayRecord[day]}" />
                                <c:set var="checkInStr" value="" />
                                <c:set var="checkOutStr" value="" />
                                <c:if test="${not empty rec and not empty rec.checkIn}">
                                    <c:set var="checkInStr" value="${fn:substring(rec.checkIn, 0, 5)}" />
                                    <c:set var="checkOutStr" value="${fn:substring(rec.checkOut, 0, 5)}" />
                                </c:if>
                                <div class="calendar-day ${status == 'W' ? 'weekend-day' : ''} ${not empty rec ? 'clickable' : ''}"
                                     <c:if test="${not empty rec}">
                                         onclick="openCorrectionModal('${rec.id}','${currentDate}','${checkInStr}','${checkOutStr}')"
                                     </c:if>>
                                    <div class="calendar-day-header">
                                        <span class="day-number">${day}</span>
                                        <span class="day-weekday">${currentDate.dayOfWeek.name().substring(0,3)}</span>
                                    </div>
                                    <div class="calendar-day-content">
                                        <c:choose>
                                            <c:when test="${status == 'P'}">
                                                <span class="att-dot" style="background:#dcfce7; color:#166534;">P</span>
                                            </c:when>
                                            <c:when test="${status == 'P_LATE'}">
                                                <span class="att-dot" style="background:#ffedd5; color:#9a3412;" title="Đi muộn">P</span>
                                            </c:when>
                                            <c:when test="${status == 'A'}">
                                                <span class="att-dot" style="background:#fee2e2; color:#991b1b;">A</span>
                                            </c:when>
                                            <c:when test="${status == 'L'}">
                                                <span class="att-dot" style="background:#fef9c3; color:#854d0e;">L</span>
                                            </c:when>
                                            <c:when test="${status == 'O'}">
                                                <span class="att-dot" style="background:#ede9fe; color:#5b21b6;" title="Có OT được duyệt">O</span>
                                            </c:when>
                                            <c:when test="${status == 'W'}">
                                                <span class="text-on-surface-variant" style="font-size: 0.75rem;">W</span>
                                            </c:when>
                                        </c:choose>
                                        <c:if test="${not empty checkInStr}">
                                            <div class="body-sm text-on-surface-variant mt-1" style="font-size: 0.68rem;">
                                                ${checkInStr} - ${checkOutStr}
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>

            <%-- Yêu cầu điều chỉnh đang chờ --%>
            <c:if test="${not empty myCorrections}">
                <div class="page-container pt-0">
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
                </div>
            </c:if>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <%-- ── Modal điều chỉnh (không đổi so với bản cũ) ── --%>
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

    <script>
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

    <style>
        .calendar-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
            gap: 8px;
        }
        .calendar-day {
            border: 1px solid var(--outline-variant);
            border-radius: 8px;
            min-height: 90px;
            background: var(--surface-container-lowest);
            transition: all 0.2s;
        }
        .calendar-day.clickable {
            cursor: pointer;
        }
        .calendar-day.clickable:hover {
            border-color: var(--primary);
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .calendar-day.weekend-day {
            background: rgba(107, 114, 128, 0.05);
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
            padding: 6px 8px;
            min-height: 50px;
        }
        .att-dot {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 26px;
            height: 26px;
            border-radius: 50%;
            font-weight: 700;
            font-size: 0.75rem;
        }
    </style>
</body>
</html>
