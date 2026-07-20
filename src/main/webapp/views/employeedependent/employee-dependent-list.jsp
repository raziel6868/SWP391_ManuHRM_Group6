<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Người phụ thuộc - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Người phụ thuộc</h2>
                        <p class="body-md text-on-surface-variant mb-0">Quản lý danh sách người phụ thuộc để payroll tính giảm trừ gia cảnh theo tháng.</p>
                    </div>
                    <c:if test="${canSetup}">
                        <a href="${pageContext.request.contextPath}/employee-dependent-setup"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                            Thêm người phụ thuộc
                        </a>
                    </c:if>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/employee-dependent-list" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-5">
                                <label class="form-label text-on-surface fw-medium mb-1">Từ khóa</label>
                                <div class="position-relative">
                                    <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-on-surface-variant"
                                          style="left: 12px; font-size: 1.25rem;">search</span>
                                    <input type="text" name="keyword" value="${keyword}"
                                           class="form-control input-premium ps-5"
                                           placeholder="Mã NV, tên NV, tên người phụ thuộc, mã số thuế..." />
                                </div>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Phòng ban</label>
                                <select name="departmentId" class="form-select input-premium">
                                    <option value="">Tất cả</option>
                                    <c:forEach var="department" items="${departments}">
                                        <option value="${department.id}" ${department.id == selectedDepartmentId ? 'selected' : ''}>
                                            ${department.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-2">
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
                                    <th>Người phụ thuộc</th>
                                    <th>Mối quan hệ</th>
                                    <th>Mã số thuế</th>
                                    <th>Hiệu lực</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="dependent" items="${employeeDependents}">
                                    <tr <c:if test="${!dependent.isActive}">style="background-color: rgba(255,255,255,0.5); opacity: 0.8;"</c:if>>
                                        <td class="fw-medium text-on-surface">${dependent.employeeCode}</td>
                                        <td>${dependent.userFullName}</td>
                                        <td>${dependent.departmentName}</td>
                                        <td>
                                            <div class="fw-medium text-on-surface">${dependent.fullName}</div>
                                            <c:if test="${not empty dependent.dateOfBirth}">
                                                <div class="label-sm text-on-surface-variant"><fmt:formatDate value="${dependent.dateOfBirth}" pattern="dd/MM/yyyy" /></div>
                                            </c:if>
                                        </td>
                                        <td>${dependent.relationship}</td>
                                        <td>${dependent.taxCode}</td>
                                        <td>
                                            <fmt:formatDate value="${dependent.effectiveFrom}" pattern="dd/MM/yyyy" />
                                            <span class="text-on-surface-variant">-</span>
                                            <c:choose>
                                                <c:when test="${not empty dependent.effectiveTo}">
                                                    <fmt:formatDate value="${dependent.effectiveTo}" pattern="dd/MM/yyyy" />
                                                </c:when>
                                                <c:otherwise>Đến nay</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${dependent.isActive}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Hoạt động</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Vô hiệu hóa</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <div class="d-flex justify-content-end gap-1">
                                                <c:if test="${canSetup}">
                                                    <a href="${pageContext.request.contextPath}/employee-dependent-setup?id=${dependent.id}"
                                                       class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                       title="Sửa người phụ thuộc">
                                                        <span class="material-symbols-outlined" style="font-size: 1.25rem;">edit</span>
                                                    </a>
                                                </c:if>
                                                <c:if test="${canChangeStatus}">
                                                    <form action="${pageContext.request.contextPath}/employee-dependent-status" method="POST" class="d-inline m-0">
                                                        <input type="hidden" name="id" value="${dependent.id}" />
                                                        <input type="hidden" name="isActive" value="${!dependent.isActive}" />
                                                        <button type="submit"
                                                                class="btn btn-sm btn-icon text-on-surface-variant hover-primary"
                                                                title="${dependent.isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}"
                                                                onclick="return confirm('${dependent.isActive ? 'Bạn có chắc muốn vô hiệu hóa người phụ thuộc này?' : 'Bạn có chắc muốn kích hoạt lại người phụ thuộc này?'}');">
                                                            <span class="material-symbols-outlined" style="font-size: 1.25rem;">${dependent.isActive ? 'lock' : 'lock_open'}</span>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty employeeDependents}">
                                    <tr>
                                        <td colspan="9" class="text-center py-4 text-on-surface-variant">
                                            Không tìm thấy người phụ thuộc phù hợp.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="p-3 bg-surface border-top border-outline-variant d-flex align-items-center justify-content-center">
                            <div class="d-flex gap-1 flex-wrap">
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <a href="${pageContext.request.contextPath}/employee-dependent-list?page=${i}&keyword=${keyword}&departmentId=${selectedDepartmentId}"
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
