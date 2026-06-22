<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chấm công của tôi - ManuHRM</title>
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
                    <div class="alert alert-danger d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        <c:out value="${errorMsg}" />
                    </div>
                </c:if>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Chấm công của tôi</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem lịch sử chấm công và gửi yêu cầu điều chỉnh nếu có sai sót.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/attendance-my" method="GET"
                              class="row g-3 align-items-end">
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                <input type="number" name="year" min="2000" max="2100"
                                       value="${selectedYear}" class="form-control input-premium" />
                            </div>
                            <div class="col-md-3">
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng</label>
                                <select name="month" class="form-select input-premium">
                                    <c:forEach begin="1" end="12" var="m">
                                        <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>Tháng ${m}</option>
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
                                    <th>Ngày</th>
                                    <th>Ca làm</th>
                                    <th>Giờ vào</th>
                                    <th>Giờ ra</th>
                                    <th>Giờ công</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="record" items="${records}">
                                    <tr>
                                        <td>${record.date}</td>
                                        <td><c:out value="${record.shiftName}" default="—" /></td>
                                        <td>${record.checkIn}</td>
                                        <td>${record.checkOut}</td>
                                        <td>${record.workingHours}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${record.status == 'NORMAL'}">
                                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Bình thường</span>
                                                </c:when>
                                                <c:when test="${record.status == 'LATE'}">
                                                    <span class="badge" style="background-color: #fef3c7; color: #92400e;">Đi muộn</span>
                                                </c:when>
                                                <c:when test="${record.status == 'ABSENT'}">
                                                    <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Vắng</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">
                                                        <c:out value="${record.status}" />
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <button type="button" class="btn btn-sm btn-light border text-on-surface-variant"
                                                    title="Yêu cầu điều chỉnh"
                                                    data-record-id="${record.id}"
                                                    data-date="<c:out value="${record.date}" />"
                                                    data-checkin="<c:out value="${record.checkIn}" />"
                                                    data-checkout="<c:out value="${record.checkOut}" />"
                                                    onclick="openCorrectionModal(this)">
                                                <span class="material-symbols-outlined" style="font-size: 1.125rem; vertical-align: middle;">edit_calendar</span>
                                                Điều chỉnh
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty records}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-on-surface-variant">
                                            Không có dữ liệu chấm công trong tháng đã chọn.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <%-- Modal: Yêu cầu điều chỉnh công --%>
    <div id="correctionModalOverlay"
         style="display:none; position:fixed; inset:0; background:rgba(11,28,48,0.45); z-index:1050; align-items:center; justify-content:center;">
        <div class="card-premium" style="max-width: 480px; width: 92%; padding: 1.5rem;">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h5 class="fw-bold text-on-surface mb-0">Yêu cầu điều chỉnh công</h5>
                <button type="button" onclick="closeCorrectionModal()" class="btn btn-sm btn-icon text-on-surface-variant" aria-label="Đóng">
                    <span class="material-symbols-outlined">close</span>
                </button>
            </div>

            <form action="${pageContext.request.contextPath}/attendance-correction-request" method="POST">
                <input type="hidden" name="attendanceRecordId" id="modalRecordId" />

                <div class="mb-3">
                    <label class="form-label text-on-surface fw-medium mb-1">Ngày chấm công</label>
                    <input type="text" id="modalDateDisplay" class="form-control input-premium" disabled />
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <label class="form-label text-on-surface fw-medium mb-1">Giờ vào hiện tại</label>
                        <input type="text" id="modalCurrentCheckIn" class="form-control input-premium" disabled />
                    </div>
                    <div class="col-md-6">
                        <label class="form-label text-on-surface fw-medium mb-1">Giờ ra hiện tại</label>
                        <input type="text" id="modalCurrentCheckOut" class="form-control input-premium" disabled />
                    </div>
                </div>

                <div class="row g-3 mb-3">
                    <div class="col-md-6">
                        <label class="form-label text-on-surface fw-medium mb-1">Giờ vào mới <span class="text-danger">*</span></label>
                        <input type="time" name="newCheckIn" class="form-control input-premium" required />
                    </div>
                    <div class="col-md-6">
                        <label class="form-label text-on-surface fw-medium mb-1">Giờ ra mới <span class="text-danger">*</span></label>
                        <input type="time" name="newCheckOut" class="form-control input-premium" required />
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label text-on-surface fw-medium mb-1">Lý do điều chỉnh <span class="text-danger">*</span></label>
                    <textarea name="reason" rows="3" class="form-control input-premium"
                              placeholder="Ví dụ: Quên chấm công ra do quên mang thẻ..." required></textarea>
                </div>

                <div class="d-flex justify-content-end gap-2">
                    <button type="button" onclick="closeCorrectionModal()" class="btn btn-light border">Hủy</button>
                    <button type="submit" class="btn btn-primary">Gửi yêu cầu</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openCorrectionModal(btn) {
            document.getElementById('modalRecordId').value = btn.getAttribute('data-record-id');
            document.getElementById('modalDateDisplay').value = btn.getAttribute('data-date');
            document.getElementById('modalCurrentCheckIn').value = btn.getAttribute('data-checkin') || '—';
            document.getElementById('modalCurrentCheckOut').value = btn.getAttribute('data-checkout') || '—';
            document.getElementById('correctionModalOverlay').style.display = 'flex';
        }
        function closeCorrectionModal() {
            document.getElementById('correctionModalOverlay').style.display = 'none';
        }
    </script>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
