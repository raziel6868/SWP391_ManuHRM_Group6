<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Tạo yêu cầu nghỉ phép - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 720px; margin: 40px auto; width: 100%;">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Tạo yêu cầu nghỉ phép</h2>
                        <p class="body-md text-on-surface-variant mb-0">Gửi yêu cầu nghỉ phép mới.</p>
                    </div>
                    <a href="${pageContext.request.contextPath}/leave-request-my"
                       class="btn btn-light border px-3 py-2 d-flex align-items-center gap-2">
                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">arrow_back</span>
                        Quay lại
                    </a>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-4" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <div>${errorMsg}</div>
                    </div>
                </c:if>

                <div class="card-premium p-4 p-md-5">
                    <form action="${pageContext.request.contextPath}/leave-request-create" method="POST">
                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Loại nghỉ phép <span class="text-danger">*</span></label>
                                <select name="leaveTypeId" class="form-select input-premium">
                                    <option value="">-- Chọn loại nghỉ --</option>
                                    <c:forEach var="lt" items="${leaveTypes}">
                                        <option value="${lt.id}">${lt.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày bắt đầu <span class="text-danger">*</span></label>
                                <input type="date" name="startDate" class="form-control input-premium" required />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-on-surface fw-medium mb-1">Ngày kết thúc <span class="text-danger">*</span></label>
                                <input type="date" name="endDate" class="form-control input-premium" required />
                            </div>
                            <div class="col-12">
                                <label class="form-label text-on-surface fw-medium mb-1">Lý do</label>
                                <textarea name="reason" class="form-control input-premium" rows="3"
                                          placeholder="VD: Nghỉ phép năm, nghỉ ốm, công tác..."></textarea>
                            </div>
                        </div>

                        <div class="d-flex gap-3 pt-3 border-top border-outline-variant">
                            <button type="submit" class="btn btn-primary px-4 py-2 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">send</span>
                                Gửi yêu cầu
                            </button>
                            <a href="${pageContext.request.contextPath}/leave-request-my"
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
