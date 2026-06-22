<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Nhập chấm công - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 720px; margin: 40px auto; width: 100%;">
                <div class="d-flex align-items-center gap-3 mb-4">
                    <a href="${pageContext.request.contextPath}/attendance-list"
                       class="btn btn-light border px-2 py-2 d-flex align-items-center justify-content-center">
                        <span class="material-symbols-outlined">arrow_back</span>
                    </a>
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Nhập chấm công từ Excel</h2>
                        <p class="body-md text-on-surface-variant mb-0">Tải lên file Excel từ máy chấm công.</p>
                    </div>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>
                <c:if test="${not empty successMsg}">
                    <div class="alert alert-success d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">check_circle</span>
                        <div>${successMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium overflow-hidden">
                    <div class="p-4">
                        <div class="alert alert-info d-flex align-items-start gap-2 mb-4" style="background-color: #dbeafe; color: #1e40af; border: 1px solid #bfdbfe;">
                            <span class="material-symbols-outlined">info</span>
                            <div>
                                <strong>Định dạng file Excel:</strong> Cột A = employee_code, Cột B = date (yyyy-MM-dd), Cột C = check_in (HH:mm), Cột D = check_out (HH:mm)
                            </div>
                        </div>

                        <form action="${pageContext.request.contextPath}/attendance-import" method="POST" enctype="multipart/form-data" class="row g-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm <span class="text-danger">*</span></label>
                                <select name="year" class="form-select input-premium" required>
                                    <option value="">-- Chọn năm --</option>
                                    <c:forEach var="y" begin="2020" end="2030">
                                        <option value="${y}">${y}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng <span class="text-danger">*</span></label>
                                <select name="month" class="form-select input-premium" required>
                                    <option value="">-- Chọn tháng --</option>
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}">${m}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">File Excel (.xlsx, .xls) <span class="text-danger">*</span></label>
                                <input type="file" name="file" class="form-control input-premium" accept=".xlsx,.xls" required>
                            </div>

                            <div class="col-12 pt-3 border-top border-outline-variant">
                                <div class="d-flex gap-3 justify-content-end">
                                    <a href="${pageContext.request.contextPath}/attendance-list" class="btn btn-light border px-4 py-2">Hủy</a>
                                    <button type="submit" class="btn-primary-gradient px-4 py-2 d-flex align-items-center gap-2">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">upload</span>
                                        Nhập dữ liệu
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
