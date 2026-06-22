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
                            <h4 class="mb-0 fw-semibold">Tháng ${currentMonth}/${currentYear}</h4>
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
                                <div class="calendar-day">
                                    <div class="calendar-day-header">
                                        <span class="day-number">${day}</span>
                                    </div>
                                    <div class="calendar-day-content">
                                        <c:forEach var="sa" items="${myAssignments}">
                                            <c:if test="${sa.date.toLocalDate() == startDate.plusDays(day - 1)}">
                                                <div class="shift-item">
                                                    <span class="shift-name">${sa.shiftName}</span>
                                                    <span class="shift-time">${sa.shiftCode}</span>
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
                                    <th>Mã ca</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="sa" items="${myAssignments}">
                                    <tr>
                                        <td>${sa.date}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 1}">Thứ 2</c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 2}">Thứ 3</c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 3}">Thứ 4</c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 4}">Thứ 5</c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 5}">Thứ 6</c:when>
                                                <c:when test="${sa.date.toLocalDate().dayOfWeek.value == 6}">Thứ 7</c:when>
                                                <c:otherwise>Chủ nhật</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${sa.shiftName}</td>
                                        <td><span class="badge" style="background-color: #e0e7ff; color: #3730a3;">${sa.shiftCode}</span></td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty myAssignments}">
                                    <tr>
                                        <td colspan="4" class="text-center py-4 text-on-surface-variant">
                                            Không có ca làm việc trong tháng này.
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
    <style>
        .calendar-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
            gap: 8px;
        }
        .calendar-day {
            border: 1px solid var(--outline-variant);
            border-radius: 8px;
            min-height: 70px;
            background: var(--surface-container-lowest);
        }
        .calendar-day-header {
            padding: 4px 8px;
            border-bottom: 1px solid var(--outline-variant);
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: var(--surface-container-low);
        }
        .day-number {
            font-weight: 600;
            font-size: 0.875rem;
        }
        .calendar-day-content {
            padding: 4px;
            min-height: 40px;
        }
        .shift-item {
            background: var(--primary-fixed-dim);
            color: var(--on-primary-fixed);
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 0.75rem;
            text-align: center;
        }
        .shift-name {
            font-weight: 600;
            display: block;
        }
        .shift-time {
            opacity: 0.8;
            display: block;
        }
    </style>
</body>
</html>
