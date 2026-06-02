<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Thêm phòng ban - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 600px; margin: 40px auto; width: 100%;">

                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Thêm phòng ban mới</h2>
                    <p class="body-md text-on-surface-variant mb-0">Điền thông tin phòng ban vào biểu mẫu bên dưới.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/department-create" method="POST">

                        <div class="row g-4 mb-4">

                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Tên phòng ban <span class="text-danger">*</span>
                                </label>
                                <input type="text" name="name"
                                       class="form-control input-premium"
                                       value="${param.name}"
                                       required maxlength="100"
                                       placeholder="VD: Phòng Kế Toán" />
                                <div class="form-text mt-1 text-on-surface-variant">Tối đa 100 ký tự. Tên không được trùng.</div>
                            </div>

                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Loại <span class="text-danger">*</span>
                                </label>
                                <select name="departmentType" class="form-select input-premium" required>
                                    <option value="">-- Chọn loại --</option>
                                    <option value="OFFICE"  ${param.departmentType == 'OFFICE'  ? 'selected' : ''}>Văn phòng (OFFICE)</option>
                                    <option value="FACTORY" ${param.departmentType == 'FACTORY' ? 'selected' : ''}>Nhà máy (FACTORY)</option>
                                </select>
                            </div>

                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Phòng ban cha
                                </label>
                                <select name="parentId" class="form-select input-premium">
                                    <option value="">-- Không có --</option>
                                    <c:forEach var="dept" items="${activeDepartments}">
                                        <option value="${dept.id}"
                                            ${param.parentId == dept.id ? 'selected' : ''}>
                                            ${dept.name}
                                            <c:if test="${dept.departmentType != null}">
                                                (${dept.departmentType == 'OFFICE' ? 'Văn phòng' : 'Nhà máy'})
                                            </c:if>
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="form-text mt-1 text-on-surface-variant">Tùy chọn. Chọn nếu đây là phòng ban/tổ thuộc cấp.</div>
                            </div>

                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit"
                                    class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">add_business</span>
                                Thêm phòng ban
                            </button>
                            <a href="${pageContext.request.contextPath}/department-list"
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
