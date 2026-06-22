<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Danh sách ngày nghỉ phép - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Danh sách ngày nghỉ phép</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem số ngày nghỉ phép của nhân viên theo năm.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/leave-balance-setup" class="btn btn-primary d-flex align-items-center gap-2">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">settings</span>
                        Thiết lập ngày nghỉ
                    </a>
                </div>

                <div class="card-premium p-4 mb-4">
                    <form method="GET" action="${pageContext.request.contextPath}/leave-balance-list" class="row g-3 align-items-end">
                        <div class="col-md-3">
                            <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                            <input type="number" name="year" value="${selectedYear}" min="2020" max="2100"
                                   class="form-control input-premium" />
                        </div>
                        <div class="col-md-4">
                            <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                            <select name="departmentId" class="form-select input-premium">
                                <option value="">Tất cả</option>
                                <c:forEach var="dept" items="${departments}">
                                    <option value="${dept.id}" ${dept.id == selectedDepartmentId ? 'selected' : ''}>${dept.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary w-100 d-flex align-items-center justify-content-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1rem;">search</span>
                                Lọc
                            </button>
                        </div>
                    </form>
                </div>

                <div class="card-premium overflow-hidden">
                    <c:choose>
                        <c:when test="${empty balances}">
                            <div class="text-center py-5">
                                <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                                <p class="body-md text-on-surface-variant mt-2">Không có dữ liệu ngày nghỉ phép.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-premium mb-0">
                                    <thead>
                                        <tr>
                                            <th>Mã NV</th>
                                            <th>Nhân viên</th>
                                            <th>Loại nghỉ</th>
                                            <th>Tổng ngày</th>
                                            <th>Đã dùng</th>
                                            <th>Còn lại</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="b" items="${balances}">
                                            <tr>
                                                <td>${b.employeeCode}</td>
                                                <td>${b.userFullName}</td>
                                                <td>${b.leaveTypeName}</td>
                                                <td><fmt:formatNumber value="${b.totalDays}" pattern="#,##0.0" /></td>
                                                <td><fmt:formatNumber value="${b.usedDays}" pattern="#,##0.0" /></td>
                                                <td>
                                                    <fmt:formatNumber value="${b.remainingDays}" pattern="#,##0.0" />
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
