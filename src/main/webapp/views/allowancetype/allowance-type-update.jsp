<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Cập nhật loại phụ cấp - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 960px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Cập nhật loại phụ cấp</h2>
                    <p class="body-md text-on-surface-variant mb-0">Chỉnh sửa danh mục phụ cấp dùng trong tính lương.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/allowance-type-update" method="POST">
                        <input type="hidden" name="id" value="${allowanceType.id}" />

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Mã loại phụ cấp <span class="text-danger">*</span></label>
                                <input type="text" name="code" class="form-control input-premium"
                                       value="${allowanceType.code}" required maxlength="30" pattern="^[A-Z][A-Z0-9_]*$" />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Dùng chữ in hoa, số và dấu gạch dưới.
                                </div>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tên phụ cấp <span class="text-danger">*</span></label>
                                <input type="text" name="name" class="form-control input-premium"
                                       value="${allowanceType.name}" required maxlength="100" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tính vào thu nhập chịu thuế</label>
                                <select name="isTaxable" class="form-select input-premium">
                                    <option value="true" ${allowanceType.isTaxable ? 'selected' : ''}>Có</option>
                                    <option value="false" ${!allowanceType.isTaxable ? 'selected' : ''}>Không</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tính vào nền bảo hiểm</label>
                                <select name="isInsuranceBased" class="form-select input-premium">
                                    <option value="false" ${!allowanceType.isInsuranceBased ? 'selected' : ''}>Không</option>
                                    <option value="true" ${allowanceType.isInsuranceBased ? 'selected' : ''}>Có</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Trạng thái hiện tại</label>
                                <input type="text" class="form-control input-premium"
                                       value="${allowanceType.isActive ? 'Hoạt động' : 'Vô hiệu hóa'}"
                                       disabled style="background-color: var(--surface-container-high);" />
                            </div>
                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Mô tả</label>
                                <textarea name="description" class="form-control input-premium"
                                          rows="4" placeholder="Nhập mô tả nếu có...">${allowanceType.description}</textarea>
                            </div>
                        </div>

                        <%@ include file="/views/allowancetype/allowance-rule-form.jspf" %>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant mt-4">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                Lưu thay đổi
                            </button>
                            <a href="${pageContext.request.contextPath}/allowance-type-list"
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
