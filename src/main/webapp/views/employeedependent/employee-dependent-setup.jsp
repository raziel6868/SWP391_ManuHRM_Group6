<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Thiết lập người phụ thuộc - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 860px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">
                        ${empty employeeDependent.id ? 'Thêm người phụ thuộc' : 'Cập nhật người phụ thuộc'}
                    </h2>
                    <p class="body-md text-on-surface-variant mb-0">HR thiết lập trực tiếp người phụ thuộc để hệ thống tự đếm giảm trừ theo tháng payroll.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/employee-dependent-setup" method="POST">
                        <input type="hidden" name="id" value="${employeeDependent.id}" />

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Nhân viên <span class="text-danger">*</span></label>
                                <select name="userId" class="form-select input-premium" required>
                                    <option value="">Chọn nhân viên</option>
                                    <c:forEach var="user" items="${users}">
                                        <option value="${user.id}" ${user.id == employeeDependent.userId ? 'selected' : ''}>
                                            ${user.employeeCode} - ${user.fullName}
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tên người phụ thuộc <span class="text-danger">*</span></label>
                                <input type="text" name="fullName" class="form-control input-premium"
                                       value="${employeeDependent.fullName}" required maxlength="150" />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Mối quan hệ</label>
                                <input type="text" name="relationship" class="form-control input-premium"
                                       value="${employeeDependent.relationship}" maxlength="50" placeholder="VD: CHILD" />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Mã số thuế</label>
                                <input type="text" name="taxCode" class="form-control input-premium"
                                       value="${employeeDependent.taxCode}" maxlength="50" />
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày sinh</label>
                                <input type="date" name="dateOfBirth" class="form-control input-premium"
                                       value="${employeeDependent.dateOfBirth}" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Hiệu lực từ <span class="text-danger">*</span></label>
                                <input type="date" name="effectiveFrom" class="form-control input-premium"
                                       value="${employeeDependent.effectiveFrom}" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Hiệu lực đến</label>
                                <input type="date" name="effectiveTo" class="form-control input-premium"
                                       value="${employeeDependent.effectiveTo}" />
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                Lưu người phụ thuộc
                            </button>
                            <a href="${pageContext.request.contextPath}/employee-dependent-list"
                               class="btn btn-light border px-4 py-2 d-flex align-items-center gap-2">
                                Hủy bỏ
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
