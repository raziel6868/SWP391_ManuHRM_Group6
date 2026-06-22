<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Danh sách chấm công - ManuHRM</title>
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

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Danh sách chấm công</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem và quản lý chấm công nhân viên.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/attendance-import"
                       class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">upload</span>
                        Nhập Excel
                    </a>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/attendance-list" method="GET" class="row g-3 align-items-end">
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                <select name="year" class="form-select input-premium">
                                    <c:forEach var="y" begin="2020" end="2030">
                                        <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng</label>
                                <select name="month" class="form-select input-premium">
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <c:forEach var="dept" items="${departments}">
                                        <option value="${dept.id}" ${selectedDepartmentId == dept.id ? 'selected' : ''}>${dept.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-1">
                                <button type="submit" class="btn btn-primary w-100">Lọc</button>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Mã NV</th>
                                    <th>Nhân viên</th>
                                    <th>Phòng ban</th>
                                    <th>Ngày</th>
                                    <th>Ca</th>
                                    <th>Check-in</th>
                                    <th>Check-out</th>
                                    <th>Giờ làm</th>
                                    <th>Trạng thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty records}">
                                        <tr>
                                            <td colspan="9" class="text-center py-4 text-on-surface-variant">
                                                Không có dữ liệu chấm công.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="record" items="${records}">
                                            <tr>
                                                <td class="fw-medium text-on-surface">${record.employeeCode}</td>
                                                <td>${record.userFullName}</td>
                                                <td>${record.departmentName}</td>
                                                <td>${record.date}</td>
                                                <td>${record.shiftName}</td>
                                                <td>${record.checkIn}</td>
                                                <td>${record.checkOut}</td>
                                                <td>${record.workingHours}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${record.status == 'NORMAL'}">
                                                            <span class="badge" style="background-color: #d1fae5; color: #065f46;">Bình thường</span>
                                                        </c:when>
                                                        <c:when test="${record.status == 'LATE'}">
                                                            <span class="badge" style="background-color: #fef3c7; color: #92400e;">Đi trễ</span>
                                                        </c:when>
                                                        <c:when test="${record.status == 'ABSENT'}">
                                                            <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Vắng mặt</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">${record.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
                            <div class="d-flex gap-1 flex-wrap">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="?page=${i}&year=${selectedYear}&month=${selectedMonth}&departmentId=${selectedDepartmentId}"
                                       class="btn btn-sm ${i == currentPage ? 'fw-bold' : 'btn-light border text-on-surface-variant'}"
                                       style="${i == currentPage ? 'background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant); border: 1px solid var(--primary);' : 'background-color: var(--surface-container-lowest); border-color: var(--outline-variant) !important;'}">
                                        ${i}
                                    </a>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
