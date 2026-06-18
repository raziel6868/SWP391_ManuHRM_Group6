<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Quản lý hạn mức nghỉ - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <jsp:include page="/components/alert.jsp" />

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Quản lý hạn mức nghỉ</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Theo dõi và thiết lập số ngày nghỉ theo nhân viên, loại nghỉ và năm áp dụng.
                        </p>
                    </div>
                    <c:if test="${canSetup}">
                        <a href="${pageContext.request.contextPath}/leave-balance-setup"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">tune</span>
                            Thiết lập hạn mức
                        </a>
                    </c:if>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/leave-balance-list" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                <input type="number" name="year" value="${selectedYear}"
                                       class="form-control input-premium"
                                       min="2000" max="2100" placeholder="${currentYear}" />
                            </div>
                            <div class="col-md-5">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="" ${empty selectedDepartmentId ? 'selected' : ''}>Tất cả phòng ban</option>
                                    <c:forEach var="department" items="${departments}">
                                        <option value="${department.id}" ${selectedDepartmentId == department.id ? 'selected' : ''}>
                                            <c:out value="${department.name}" />
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2 d-flex gap-2">
                                <button type="submit" class="btn btn-primary w-100">
                                    <span class="material-symbols-outlined align-middle" style="font-size: 1rem;">filter_list</span>
                                    Lọc
                                </button>
                            </div>
                            <div class="col-md-2 d-flex gap-2">
                                <a href="${pageContext.request.contextPath}/leave-balance-list"
                                   class="btn btn-light border w-100">Xóa lọc</a>
                            </div>
                        </form>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Nhân viên</th>
                                    <th>Phòng ban</th>
                                    <th>Loại nghỉ</th>
                                    <th>Năm</th>
                                    <th class="text-end">Tổng ngày</th>
                                    <th class="text-end">Đã dùng</th>
                                    <th class="text-end">Còn lại</th>
                                    <c:if test="${canSetup}">
                                        <th class="text-end">Thao tác</th>
                                    </c:if>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="balance" items="${balances}">
                                    <tr>
                                        <td>
                                            <div class="fw-medium text-on-surface">
                                                <c:out value="${balance.employeeName}" />
                                            </div>
                                            <div class="body-sm text-on-surface-variant">
                                                <c:out value="${balance.employeeCode}" />
                                            </div>
                                        </td>
                                        <td><c:out value="${empty balance.departmentName ? '-' : balance.departmentName}" /></td>
                                        <td>
                                            <span class="badge" style="background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant);">
                                                <c:out value="${balance.leaveTypeCode}" />
                                            </span>
                                            <span class="ms-2"><c:out value="${balance.leaveTypeName}" /></span>
                                        </td>
                                        <td>${balance.year}</td>
                                        <td class="text-end fw-medium">${balance.totalDays}</td>
                                        <td class="text-end">${balance.usedDays}</td>
                                        <td class="text-end fw-medium">${balance.totalDays - balance.usedDays}</td>
                                        <c:if test="${canSetup}">
                                            <td class="text-end">
                                                <a href="${pageContext.request.contextPath}/leave-balance-setup?userId=${balance.userId}&leaveTypeId=${balance.leaveTypeId}&year=${balance.year}"
                                                   class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                   title="Cập nhật hạn mức">
                                                    <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                </a>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty balances}">
                                    <tr>
                                        <td colspan="${canSetup ? 8 : 7}" class="text-center py-4 text-on-surface-variant">
                                            Chưa có hạn mức nghỉ phù hợp với bộ lọc hiện tại.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-between flex-wrap gap-3">
                            <div class="body-sm text-on-surface-variant">
                                Tổng số bản ghi: ${totalRecords}
                            </div>
                            <div class="d-flex gap-1 flex-wrap">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${pageContext.request.contextPath}/leave-balance-list?page=${i}&year=${selectedYear}&departmentId=${selectedDepartmentId}"
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
