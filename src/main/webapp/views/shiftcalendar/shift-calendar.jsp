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
                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Lịch phân ca</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem lịch phân ca theo tháng.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/shift-calendar" method="GET"
                              class="row g-3 align-items-end">
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
                                <div class="calendar-day">
                                    <div class="calendar-day-header">
                                        <span class="day-number">${day}</span>
                                    </div>
                                    <div class="calendar-day-content">
                                        <c:forEach var="entry" items="${assignments}">
                                            <c:if test="${fn:contains(entry.key, '_'.concat(startDate.plusDays(day - 1)))}">
                                                <div class="shift-item" title="${entry.value.shiftName} - ${entry.value.userFullName}">
                                                    <span class="shift-name">${entry.value.shiftCode}</span>
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
                        <h5 class="mb-0">Danh sách phân ca tháng</h5>
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
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${assignments}">
                                    <tr>
                                        <td>${entry.value.date}</td>
                                        <td>${entry.value.userFullName}</td>
                                        <td>${entry.value.employeeCode}</td>
                                        <td>${entry.value.departmentName}</td>
                                        <td><span class="badge" style="background-color: #e0e7ff; color: #3730a3;">${entry.value.shiftName}</span></td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty assignments}">
                                    <tr>
                                        <td colspan="5" class="text-center py-4 text-on-surface-variant">
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
    <style>
        .calendar-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
            gap: 8px;
        }
        .calendar-day {
            border: 1px solid var(--outline-variant);
            border-radius: 8px;
            min-height: 80px;
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
            max-height: 60px;
            overflow-y: auto;
        }
        .shift-item {
            background: var(--primary-fixed-dim);
            color: var(--on-primary-fixed);
            padding: 2px 4px;
            border-radius: 4px;
            margin-bottom: 2px;
            font-size: 0.75rem;
        }
        .shift-name {
            font-weight: 600;
            margin-right: 4px;
        }
        .shift-user {
            opacity: 0.8;
        }
    </style>
</body>
</html>
