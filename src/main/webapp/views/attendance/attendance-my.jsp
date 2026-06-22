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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Chấm công của tôi</h2>
                        <p class="body-md text-on-surface-variant mb-0">Xem lịch sử chấm công và yêu cầu sửa nếu có lỗi.</p>
                    </div>
                </div>

                <div class="card-premium overflow-hidden d-flex flex-column mb-4 w-100">
                    <div class="p-3 bg-surface border-bottom border-outline-variant">
                        <form action="${pageContext.request.contextPath}/attendance-my" method="GET" class="row g-3 align-items-end">
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Năm</label>
                                <select name="year" class="form-select input-premium">
                                    <c:forEach var="y" begin="2020" end="2030">
                                        <option value="${y}" ${selectedYear == y ? 'selected' : ''}>${y}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label text-on-surface fw-medium mb-1">Tháng</label>
                                <select name="month" class="form-select input-premium">
                                    <c:forEach var="m" begin="1" end="12">
                                        <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>${m}</option>
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
                                    <th>Ca</th>
                                    <th>Check-in</th>
                                    <th>Check-out</th>
                                    <th>Giờ làm</th>
                                    <th>Trạng thái</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty records}">
                                        <tr>
                                            <td colspan="7" class="text-center py-4 text-on-surface-variant">
                                                Không có dữ liệu chấm công.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="record" items="${records}">
                                            <tr>
                                                <td>${record.date}</td>
                                                <td>${record.shiftName}</td>
                                                <td>${record.checkIn}</td>
                                                <td>${record.checkOut}</td>
                                                <td>${record.workingHours}</td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${record.status == 'NORMAL'}">
                                                            <span class="badge" style="background-color: #d1fae5; color: #065f46;">Bình thường</span>
                                                        </c:when>
                                                        <c:when test="${record.status == 'LATE'}">
                                                            <span class="badge" style="background-color: #fef3c7; color: #92400e;">Đi trễ</span>
                                                        </c:when>
                                                        <c:when test="${record.status == 'ABSENT'}">
                                                            <span class="badge" style="background-color: #fee2e2; color: #991b1b;">Vắng mặt</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">${record.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-end">
                                                    <button type="button" class="btn btn-sm btn-outline-primary"
                                                            data-bs-toggle="modal" data-bs-target="#correctionModal${record.id}">
                                                        Sửa chấm công
                                                    </button>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td colspan="7" class="p-0 border-0">
                                                    <div class="modal fade" id="correctionModal${record.id}" tabindex="-1">
                                                        <div class="modal-dialog">
                                                            <div class="modal-content">
                                                                <div class="modal-header">
                                                                    <h5 class="modal-title">Yêu cầu sửa chấm công</h5>
                                                                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                                                </div>
                                                                <form action="${pageContext.request.contextPath}/attendance-correction-request" method="POST">
                                                                    <div class="modal-body">
                                                                        <p class="mb-3"><strong>Ngày:</strong> ${record.date} | <strong>Ca:</strong> ${record.shiftName}</p>
                                                                        <p class="mb-3"><strong>Check-in cũ:</strong> ${record.checkIn} | <strong>Check-out cũ:</strong> ${record.checkOut}</p>
                                                                        <input type="hidden" name="attendanceRecordId" value="${record.id}">
                                                                        <div class="mb-3">
                                                                            <label class="form-label text-on-surface fw-medium mb-1">Check-in mới (HH:mm)</label>
                                                                            <input type="time" name="newCheckIn" class="form-control input-premium" value="${record.checkIn}">
                                                                        </div>
                                                                        <div class="mb-3">
                                                                            <label class="form-label text-on-surface fw-medium mb-1">Check-out mới (HH:mm)</label>
                                                                            <input type="time" name="newCheckOut" class="form-control input-premium" value="${record.checkOut}">
                                                                        </div>
                                                                        <div class="mb-3">
                                                                            <label class="form-label text-on-surface fw-medium mb-1">Lý do</label>
                                                                            <textarea name="reason" class="form-control input-premium" rows="3" placeholder="Nhập lý do yêu cầu sửa..."></textarea>
                                                                        </div>
                                                                    </div>
                                                                    <div class="modal-footer">
                                                                        <button type="button" class="btn btn-light border" data-bs-dismiss="modal">Hủy</button>
                                                                        <button type="submit" class="btn btn-primary-gradient">Gửi yêu cầu</button>
                                                                    </div>
                                                                </form>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
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
