<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Sửa yêu cầu OT - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <div class="mb-4">
                    <h2 class="h3 text-on-surface fw-bold mb-1">Sửa yêu cầu OT</h2>
                    <p class="body-md text-on-surface-variant mb-0">
                        Chỉnh sửa số giờ / lý do tăng ca đã tạo.
                    </p>
                </div>

                <div class="row g-4">
                    <div class="col-lg-7">
                        <div class="card-premium overflow-hidden">
                            <div class="p-3 bg-surface border-bottom border-outline-variant">
                                <h5 class="mb-0 fw-semibold">Thông tin OT</h5>
                            </div>
                            <div class="p-4">
                                <dl class="row mb-4">
                                    <dt class="col-sm-4 text-on-surface-variant fw-normal">Mã nhân viên</dt>
                                    <dd class="col-sm-8 fw-medium"><c:out value="${record.employeeCode}" /></dd>

                                    <dt class="col-sm-4 text-on-surface-variant fw-normal">Nhân viên</dt>
                                    <dd class="col-sm-8 fw-medium"><c:out value="${record.employeeName}" /></dd>

                                    <dt class="col-sm-4 text-on-surface-variant fw-normal">Ngày OT</dt>
                                    <dd class="col-sm-8 fw-medium">${record.date}</dd>
                                </dl>

                                <form id="editForm" action="${pageContext.request.contextPath}/overtime-edit" method="POST">
                                    <input type="hidden" name="id" value="${record.id}" />

                                    <div class="mb-3">
                                        <label class="form-label text-on-surface fw-medium mb-1">Số giờ OT</label>
                                        <input type="number" name="requestedHours" class="form-control input-premium"
                                               value="${record.requestedHours}" min="0.5" max="2" step="0.5" required />
                                        <div class="form-text text-on-surface-variant">
                                            Tối đa 2h/ngày (giờ làm chuẩn 08:00-17:00, OT từ 17:00 đến 19:00),
                                            tối đa 40h/tháng và 200h/năm cho mỗi nhân viên.
                                        </div>
                                    </div>

                                    <div class="mb-0">
                                        <label class="form-label text-on-surface fw-medium mb-1">Lý do tăng ca</label>
                                        <textarea name="reason" class="form-control input-premium" rows="3"
                                                  required>${record.reason}</textarea>
                                    </div>
                                </form>

                                <form id="cancelForm" action="${pageContext.request.contextPath}/overtime-reject" method="POST"
                                      onsubmit="return confirm('Hủy yêu cầu OT này? Không thể hoàn tác.');">
                                    <input type="hidden" name="id" value="${record.id}" />
                                </form>

                                <div class="d-flex gap-2 flex-wrap mt-4">
                                    <button type="submit" form="editForm" class="btn btn-primary d-flex align-items-center gap-2">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                                        Lưu thay đổi
                                    </button>
                                    <button type="submit" form="cancelForm" class="btn btn-outline-danger d-flex align-items-center gap-2">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">cancel</span>
                                        Hủy OT
                                    </button>
                                    <a href="${pageContext.request.contextPath}/overtime-list?year=${recYear}&month=${recMonth}"
                                       class="btn btn-light border text-on-surface-variant d-flex align-items-center gap-2">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">arrow_back</span>
                                        Quay lại
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
