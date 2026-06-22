<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Nhập dữ liệu chấm công - ManuHRM</title>
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
                        <c:out value="${successMsg}" />
                    </div>
                </c:if>
                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <c:out value="${errorMsg}" />
                    </div>
                </c:if>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Nhập dữ liệu chấm công</h2>
                        <p class="body-md text-on-surface-variant mb-0">Tải file Excel xuất từ máy chấm công để ghi nhận công theo nhân viên và ngày.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/attendance-list"
                       class="btn btn-light border text-decoration-none px-3 py-2 d-flex align-items-center gap-2">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">list_alt</span>
                        Danh sách chấm công
                    </a>
                </div>

                <div class="card-premium overflow-hidden mb-4">
                    <div class="p-4 bg-surface">
                        <form action="${pageContext.request.contextPath}/attendance-import" method="POST"
                              enctype="multipart/form-data" class="row g-3 align-items-end">
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm <span class="text-danger">*</span></label>
                                <input type="number" name="year" min="2000" max="2100"
                                       value="${selectedYear}" class="form-control input-premium" required />
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng <span class="text-danger">*</span></label>
                                <select name="month" class="form-select input-premium" required>
                                    <c:forEach begin="1" end="12" var="m">
                                        <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>Tháng ${m}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">File Excel <span class="text-danger">*</span></label>
                                <input type="file" name="attendanceFile" accept=".xlsx"
                                       class="form-control input-premium" required />
                            </div>
                            <div class="col-md-2">
                                <button type="submit" class="btn-primary-gradient w-100 px-3 py-2 d-flex align-items-center justify-content-center gap-2">
                                    <span class="material-symbols-outlined" style="font-size: 1.125rem;">upload_file</span>
                                    Import
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <c:if test="${not empty errorLogs}">
                    <div class="card-premium overflow-hidden mb-4">
                        <div class="p-3 bg-surface border-bottom border-outline-variant d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined text-danger">error</span>
                            <h3 class="h5 mb-0 fw-bold">Log lỗi import</h3>
                        </div>
                        <div class="table-responsive">
                            <table class="table table-premium mb-0 w-100">
                                <thead>
                                    <tr>
                                        <th style="width: 80px;">STT</th>
                                        <th>Nội dung lỗi</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="log" items="${errorLogs}" varStatus="status">
                                        <tr>
                                            <td>${status.index + 1}</td>
                                            <td><c:out value="${log}" /></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </c:if>

                <div class="card-premium p-3 bg-surface">
                    <p class="body-sm text-on-surface-variant mb-2 fw-medium">Định dạng file</p>
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>employee_code</th>
                                    <th>date</th>
                                    <th>check_in</th>
                                    <th>check_out</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>AD001</td>
                                    <td>2026-06-10</td>
                                    <td>07:55</td>
                                    <td>17:05</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
