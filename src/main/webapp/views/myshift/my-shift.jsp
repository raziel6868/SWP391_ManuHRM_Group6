<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Ca làm việc của tôi - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Ca làm việc của tôi</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem lịch ca làm việc cá nhân.</p>
                    </div>
                </div>

                <div class="row g-4 mb-4">
                    <div class="col-md-4">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="rounded-circle d-flex align-items-center justify-content-center"
                                         style="width: 48px; height: 48px; background: linear-gradient(135deg, #e0e7ff, #c7d2fe);">
                                        <span class="material-symbols-outlined" style="color: #3730a3;">calendar_month</span>
                                    </div>
                                    <div>
                                        <p class="text-on-surface-variant mb-0" style="font-size: 0.75rem;">Ngày làm việc</p>
                                        <h3 class="mb-0 fw-bold text-primary">${workingDays}</h3>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="rounded-circle d-flex align-items-center justify-content-center"
                                         style="width: 48px; height: 48px; background: linear-gradient(135deg, #dcfce7, #bbf7d0);">
                                        <span class="material-symbols-outlined" style="color: #166534;">schedule</span>
                                    </div>
                                    <div>
                                        <p class="text-on-surface-variant mb-0" style="font-size: 0.75rem;">Tổng giờ làm</p>
                                        <h3 class="mb-0 fw-bold" style="color: #166534;">${totalHours}h</h3>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card-premium overflow-hidden h-100">
                            <div class="card-body p-3">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="rounded-circle d-flex align-items-center justify-content-center"
                                         style="width: 48px; height: 48px; background: linear-gradient(135deg, #fef3c7, #fde68a);">
                                        <span class="material-symbols-outlined" style="color: #92400e;">event</span>
                                    </div>
                                    <div>
                                        <p class="text-on-surface-variant mb-0" style="font-size: 0.75rem;">Phòng ban</p>
                                        <h5 class="mb-0 fw-semibold">${sessionScope.authUser.departmentName != null ? sessionScope.authUser.departmentName : 'Chưa phân'}</h5>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/my-shift" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng</label>
                                <select name="month" class="form-select input-premium">
                                    <c:forEach begin="1" end="12" var="m">
                                        <option value="${m}" ${currentMonth == m ? 'selected' : ''}>Tháng ${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                <input type="number" name="year" value="${currentYear}" class="form-control input-premium" min="2020" max="2100" />
                            </div>
                            <div class="col-md-2 d-flex gap-2">
                                <button type="submit" class="btn btn-primary w-100">Xem</button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="card-premium overflow-hidden mb-4">
                    <div class="card-header-custom p-3 bg-surface border-bottom border-outline-variant">
                        <div class="d-flex justify-content-between align-items-center">
                            <h4 class="mb-0 fw-semibold">Lịch ca tháng ${currentMonth}/${currentYear}</h4>
                            <div class="d-flex gap-2">
                                <a href="${pageContext.request.contextPath}/my-shift?month=${prevMonth}&year=${prevYear}"
                                   class="btn btn-sm btn-light border text-on-surface-variant">
                                    <span class="material-symbols-outlined">chevron_left</span>
                                </a>
                                <a href="${pageContext.request.contextPath}/my-shift?month=${nextMonth}&year=${nextYear}"
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
                                <div class="calendar-day ${currentDate.dayOfWeek.value == 7 || currentDate.dayOfWeek.value == 1 ? 'weekend-day' : ''}">
                                    <div class="calendar-day-header">
                                        <span class="day-number">${day}</span>
                                        <span class="day-weekday">${currentDate.dayOfWeek.name().substring(0,3)}</span>
                                    </div>
                                    <div class="calendar-day-content">
                                        <c:forEach var="sa" items="${myAssignments}">
                                            <c:if test="${sa.date.toLocalDate() == currentDate}">
                                                <div class="shift-item" title="${sa.shiftName}">
                                                    <span class="shift-badge">${sa.shiftCode}</span>
                                                    <span class="shift-time">
                                                        <c:forEach var="s" items="${shifts}">
                                                            <c:if test="${s.id == sa.shiftId}">
                                                                ${s.startTime.toLocalTime().toString().substring(0,5)} - ${s.endTime.toLocalTime().toString().substring(0,5)}
                                                            </c:if>
                                                        </c:forEach>
                                                    </span>
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
                        <h5 class="mb-0">Danh sách ca làm việc</h5>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Ngày</th>
                                    <th>Thứ</th>
                                    <th>Ca</th>
                                    <th>Giờ</th>
                                    <th>Mã ca</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="sa" items="${myAssignments}">
                                    <tr>
                                        <td>${sa.date}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 1}"><span class="badge bg-danger-subtle text-danger">Thứ 2</span></c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 2}">Thứ 3</c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 3}">Thứ 4</c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 4}">Thứ 5</c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 5}">Thứ 6</c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 6}"><span class="badge bg-warning-subtle text-warning">Thứ 7</span></c:when>
                                                <c:otherwise><span class="badge bg-danger-subtle text-danger">CN</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${sa.shiftName}</td>
                                        <td>
                                            <c:forEach var="s" items="${shifts}">
                                                <c:if test="${s.id == sa.shiftId}">
                                                    ${s.startTime.toLocalTime().toString().substring(0,5)} - ${s.endTime.toLocalTime().toString().substring(0,5)}
                                                </c:if>
                                            </c:forEach>
                                        </td>
                                        <td><span class="badge" style="background-color: #e0e7ff; color: #3730a3;">${sa.shiftCode}</span></td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty myAssignments}">
                                    <tr>
                                        <td colspan="5" class="text-center py-4 text-on-surface-variant">
                                            Không có ca làm việc trong tháng này.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <c:if test="${not empty shifts}">
                <div class="card-premium overflow-hidden mt-4">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <h5 class="mb-0">Danh sách ca làm việc</h5>
                    </div>
                    <div class="card-body p-3">
                        <div class="row g-3">
                            <c:forEach var="s" items="${shifts}">
                                <div class="col-md-4">
                                    <div class="shift-info-card">
                                        <div class="d-flex justify-content-between align-items-start mb-2">
                                            <span class="badge" style="background-color: #e0e7ff; color: #3730a3;">${s.code}</span>
                                            <span class="badge ${s.isNightShift ? 'bg-dark' : 'bg-light text-dark'}">
                                                ${s.isNightShift ? 'Ca đêm' : 'Ca ngày'}
                                            </span>
                                        </div>
                                        <h6 class="mb-1">${s.name}</h6>
                                        <p class="text-on-surface-variant mb-0" style="font-size: 0.875rem;">
                                            <span class="material-symbols-outlined" style="font-size: 1rem; vertical-align: middle;">schedule</span>
                                            ${s.startTime.toLocalTime().toString().substring(0,5)} - ${s.endTime.toLocalTime().toString().substring(0,5)}
                                        </p>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
                </c:if>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

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
        .calendar-day:hover {
            border-color: var(--primary);
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .calendar-day.weekend-day {
            background: rgba(220, 53, 69, 0.03);
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
            min-height: 50px;
        }
        .shift-item {
            background: linear-gradient(135deg, #e0e7ff, #c7d2fe);
            color: #3730a3;
            padding: 4px 6px;
            border-radius: 6px;
            font-size: 0.75rem;
        }
        .shift-badge {
            font-weight: 700;
            margin-right: 4px;
            background: rgba(55, 48, 163, 0.2);
            padding: 1px 4px;
            border-radius: 3px;
        }
        .shift-time {
            display: block;
            font-size: 0.65rem;
            opacity: 0.8;
            margin-top: 2px;
        }
        .shift-info-card {
            background: var(--surface-container-lowest);
            border: 1px solid var(--outline-variant);
            border-radius: 8px;
            padding: 12px;
        }
        .shift-info-card:hover {
            border-color: var(--primary);
        }
        .bg-danger-subtle {
            background-color: rgba(220, 53, 69, 0.1) !important;
        }
        .bg-warning-subtle {
            background-color: rgba(234, 179, 8, 0.1) !important;
        }
    </style>
</body>
</html>
