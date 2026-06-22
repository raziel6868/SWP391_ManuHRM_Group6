<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Lương cơ sở - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Lương cơ sở</h2>
                        <p class="body-md text-on-surface-variant mb-0">Danh sách lương cơ sở của nhân viên.</p>
                    </div>
                    <c:if test="${hasSetupPermission}">
                        <a href="${pageContext.request.contextPath}/salary-base-setup" class="btn btn-primary d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                            Thiết lập lương
                        </a>
                    </c:if>
                </div>

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

                <div class="card-premium p-4 mb-4">
                    <form method="GET" action="${pageContext.request.contextPath}/salary-base-list" class="row g-3 align-items-end">
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
                        <c:when test="${empty salaryBases}">
                            <div class="text-center py-5">
                                <span class="material-symbols-outlined" style="font-size: 3rem; color: var(--on-surface-variant);">inbox</span>
                                <p class="body-md text-on-surface-variant mt-2">Không có lương cơ sở nào.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-premium mb-0">
                                    <thead>
                                        <tr>
                                            <th>Mã NV</th>
                                            <th>Nhân viên</th>
                                            <th>Phòng ban</th>
                                            <th>Lương cơ sở</th>
                                            <th>Ngày có hiệu lực</th>
                                            <th></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="sb" items="${salaryBases}">
                                            <tr>
                                                <td>${sb.employeeCode}</td>
                                                <td>${sb.userFullName}</td>
                                                <td>${sb.departmentName}</td>
                                                <td><fmt:formatNumber value="${sb.baseSalary}" pattern="#,##0" /> VND</td>
                                                <td><fmt:formatDate value="${sb.effectiveFrom}" pattern="dd/MM/yyyy" /></td>
                                                <td>
                                                    <c:if test="${hasSetupPermission}">
                                                        <a href="${pageContext.request.contextPath}/salary-base-setup?userId=${sb.userId}" class="btn btn-sm btn-outline-primary">
                                                            Sửa
                                                        </a>
                                                    </c:if>
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
