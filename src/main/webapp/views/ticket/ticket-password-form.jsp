<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Đặt mật khẩu mới - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <style>
        .password-card {
            max-width: 500px;
            margin: 0 auto;
        }
    </style>
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <div class="d-flex align-items-center gap-3 mb-4">
                    <a href="${pageContext.request.contextPath}/admin/tickets" class="btn btn-icon">
                        <span class="material-symbols-outlined">arrow_back</span>
                    </a>
                    <div>
                        <h1 class="h3 text-on-surface fw-bold mb-1">Đặt mật khẩu mới</h1>
                        <p class="body-md text-on-surface-variant mb-0">Yêu cầu đặt lại mật khẩu cho nhân viên.</p>
                    </div>
                </div>

                <c:if test="${not empty errorMsg}">
                    <div class="alert alert-error d-flex align-items-center gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined">error</span>
                        ${errorMsg}
                    </div>
                </c:if>

                <div class="card-premium p-4 password-card">
                    <c:if test="${not empty ticket}">
                        <div class="mb-4 p-3" style="background-color: var(--surface-container-low); border-radius: 8px;">
                            <div class="d-flex align-items-center gap-2 mb-2">
                                <span class="material-symbols-outlined text-primary">person</span>
                                <span class="fw-medium">${ticket.fullName}</span>
                            </div>
                            <div class="text-muted small">
                                Mã nhân viên: <span class="font-monospace fw-medium">${ticket.employeeCode}</span>
                            </div>
                        </div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/admin/tickets" method="POST">
                        <div class="mb-3">
                            <label class="form-label fw-bold small text-uppercase text-muted">Mật khẩu mới</label>
                            <input type="password" name="newPassword" id="newPassword" class="form-control font-monospace"
                                   required minlength="6" placeholder="Nhập mật khẩu (tối thiểu 6 ký tự)">
                        </div>

                        <div class="form-check mb-4">
                            <input class="form-check-input" type="checkbox" id="showPassword"
                                   onchange="document.getElementById('newPassword').type = this.checked ? 'text' : 'password'">
                            <label class="form-check-label small text-muted" for="showPassword">
                                Hiện mật khẩu
                            </label>
                        </div>

                        <p class="text-muted small mb-4">Nhân viên sẽ được yêu cầu đổi mật khẩu sau khi đăng nhập lần đầu.</p>

                        <input type="hidden" name="ticketId" value="${pendingTicketId}" />

                        <div class="d-flex gap-3 justify-content-end">
                            <a href="${pageContext.request.contextPath}/admin/tickets" class="btn btn-secondary px-4">Hủy</a>
                            <button type="submit" name="action" value="approve" class="btn btn-primary-gradient text-white px-4">
                                <span class="material-symbols-outlined me-1">check</span>
                                Duyệt & Gửi mật khẩu
                            </button>
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
