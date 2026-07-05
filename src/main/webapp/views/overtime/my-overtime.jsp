<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Tăng ca của tôi - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Tăng ca của tôi</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem lịch tăng ca (OT) cá nhân theo tháng.</p>
                    </div>
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
                                        <p class="text-on-surface-variant mb-0" style="font-size: 0.75rem;">Tổng giờ OT trong tháng</p>
                                        <h3 class="mb-0 fw-bold" style="color: #166534;"><fmt:formatNumber value="${totalHours}" pattern="0.##" />h</h3>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/my-overtime" method="GET"
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
                            <div class="col-md-2">
                                <button type="submit" class="btn btn-primary w-100">Xem</button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="card-premium overflow-hidden mb-4">
                    <div class="card-header-custom p-3 bg-surface border-bottom border-outline-variant">
                        <div class="d-flex justify-content-between align-items-center">
                            <h4 class="mb-0 fw-semibold">Lịch OT tháng ${currentMonth}/${currentYear}</h4>
                            <div class="d-flex gap-2">
                                <a href="${pageContext.request.contextPath}/my-overtime?month=${prevMonth}&year=${prevYear}"
                                   class="btn btn-sm btn-light border text-on-surface-variant">
                                    <span class="material-symbols-outlined">chevron_left</span>
                                </a>
                                <a href="${pageContext.request.contextPath}/my-overtime?month=${nextMonth}&year=${nextYear}"
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
                                <div class="calendar-day ${currentDate.dayOfWeek.value == 6 || currentDate.dayOfWeek.value == 7 ? 'weekend-day' : ''}">
                                    <div class="calendar-day-header">
                                        <span class="day-number">${day}</span>
                                        <span class="day-weekday">${currentDate.dayOfWeek.name().substring(0,3)}</span>
                                    </div>
                                    <div class="calendar-day-content">
                                        <c:forEach var="ot" items="${myOvertimes}">
                                            <c:if test="${ot.date.toLocalDate() == currentDate}">
                                                <div class="ot-item" title="${ot.reason}">
                                                    <span class="ot-hours"><fmt:formatNumber value="${ot.requestedHours}" pattern="0.##" />h</span>
                                                </div>
                                            </c:if>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </div>
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
        .ot-item {
            background: linear-gradient(135deg, #dcfce7, #bbf7d0);
            color: #166534;
            padding: 4px 6px;
            border-radius: 6px;
            font-size: 0.8rem;
            text-align: center;
            font-weight: 700;
        }
    </style>
</body>
</html>
