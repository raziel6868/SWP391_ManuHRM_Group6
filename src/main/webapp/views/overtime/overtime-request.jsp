<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Tạo yêu cầu tăng ca - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 700px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Tạo yêu cầu tăng ca</h2>
                    <p class="body-md text-on-surface-variant mb-0">Điền thông tin yêu cầu tăng ca cho nhân viên dưới quyền.</p>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div><c:out value="${errorMsg}" /></div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/overtime-request" method="POST">
                        <div class="row g-4 mb-4">

                            <div class="col-md-12">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Nhân viên <span class="text-danger">*</span>
                                </label>
                                <select name="userId" class="form-select input-premium" required>
                                    <option value="">-- Chọn nhân viên --</option>
                                    <c:forEach var="emp" items="${subordinates}">
                                        <option value="${emp.id}">
                                            <c:out value="${emp.fullName}" /> (<c:out value="${emp.employeeCode}" />)
                                        </option>
                                    </c:forEach>
                                </select>
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Chỉ hiển thị nhân viên dưới quyền của bạn.
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Ngày tăng ca <span class="text-danger">*</span>
                                </label>
                                <input type="date" name="date" class="form-control input-premium"
                                       value="${param.date}"
                                       min="${minOtDate}"
                                       max="${maxOtDate}"
                                       required />
                                <div class="form-text mt-1 text-on-surface-variant">
                                    Chỉ chọn từ hôm nay (<c:out value="${minOtDate}" />)
                                    đến <c:out value="${maxOtDate}" /> — không chọn ngày quá khứ hoặc tháng đã chốt công.
                                </div>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Số giờ đề nghị <span class="text-danger">*</span>
                                </label>
                                <input type="number" name="requestedHours" class="form-control input-premium"
                                       value="${param.requestedHours}"
                                       min="0.5" max="24" step="0.5"
                                       placeholder="VD: 2.5" required />
                                <div class="form-text mt-1 text-on-surface-variant">Từ 0.5 đến 24 giờ.</div>
                            </div>

                            <div class="col-md-12">
                                <label class="form-label text-on-surface fw-medium mb-1">
                                    Lý do tăng ca <span class="text-danger">*</span>
                                </label>
                                <textarea name="reason" class="form-control input-premium"
                                          rows="3" maxlength="500"
                                          placeholder="Nhập lý do tăng ca..." required><c:out value="${param.reason}" /></textarea>
                            </div>

                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">send</span>
                                Gửi yêu cầu
                            </button>
                            <a href="${pageContext.request.contextPath}/overtime-list"
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
