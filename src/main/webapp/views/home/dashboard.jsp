<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="user" value="${sessionScope.authUser}" />
<c:set var="activeMenu" value="home" scope="request" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang chủ | ManuHRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <!-- Google Fonts Preconnect & Load -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
<div class="layout-wrapper">
    <jsp:include page="/components/sidebar.jsp" />

    <div class="main-content">
        <jsp:include page="/components/header.jsp" />

        <main class="page-container">
            <section class="mb-4">
                <div class="d-flex flex-column flex-md-row justify-content-between gap-3">
                    <div>
                        <h1 class="h2 text-on-surface fw-bold mb-2">
                            Xin chào, <c:out value="${user.fullName}" />
                        </h1>
                        <p class="body-md text-on-surface-variant mb-0">
                            Cổng thông tin nội bộ dành cho nhân sự nhà máy.
                        </p>
                    </div>
                    <div class="align-self-start align-self-md-center badge-premium badge-primary">
                        <span class="material-symbols-outlined me-2" style="font-size: 1rem;">calendar_month</span>
                        <c:out value="${currentDate}" />
                    </div>
                </div>
            </section>

            <section class="row g-4 mb-5" aria-label="Thống kê nhanh">
                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <p class="label-md text-on-surface-variant mb-1">Đang hoạt động</p>
                                <p class="h2 text-on-surface fw-bold mb-0"><c:out value="${stats.activeUsers}" /></p>
                            </div>
                            <div class="d-flex align-items-center justify-content-center rounded-circle"
                                 style="width: 40px; height: 40px; background-color: rgba(37, 99, 235, 0.1); color: var(--primary);">
                                <span class="material-symbols-outlined">badge</span>
                            </div>
                        </div>
                        <p class="body-sm text-on-surface-variant mt-4 mb-0">
                            <c:out value="${stats.activePercentage}" />% trên tổng nhân sự.
                        </p>
                    </article>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <p class="label-md text-on-surface-variant mb-1">Văn phòng</p>
                                <p class="h2 text-on-surface fw-bold mb-0"><c:out value="${stats.officeUsers}" /></p>
                            </div>
                            <div class="d-flex align-items-center justify-content-center rounded-circle"
                                 style="width: 40px; height: 40px; background-color: rgba(100, 94, 251, 0.16); color: var(--secondary);">
                                <span class="material-symbols-outlined">work</span>
                            </div>
                        </div>
                        <p class="body-sm text-on-surface-variant mt-4 mb-0">
                            <c:out value="${stats.officePercentage}" />% trong nhóm đang hoạt động.
                        </p>
                    </article>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <p class="label-md text-on-surface-variant mb-1">Sản xuất</p>
                                <p class="h2 text-on-surface fw-bold mb-0"><c:out value="${stats.workerUsers}" /></p>
                            </div>
                            <div class="d-flex align-items-center justify-content-center rounded-circle"
                                 style="width: 40px; height: 40px; background-color: rgba(148, 55, 0, 0.12); color: var(--tertiary);">
                                <span class="material-symbols-outlined">precision_manufacturing</span>
                            </div>
                        </div>
                        <p class="body-sm text-on-surface-variant mt-4 mb-0">
                            <c:out value="${stats.workerPercentage}" />% trong nhóm đang hoạt động.
                        </p>
                    </article>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <article class="card-premium h-100 p-4">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <p class="label-md text-on-surface-variant mb-1">Đơn vị</p>
                                <p class="h2 text-on-surface fw-bold mb-0"><c:out value="${stats.departments}" /></p>
                            </div>
                            <div class="d-flex align-items-center justify-content-center rounded-circle"
                                 style="width: 40px; height: 40px; background-color: rgba(75, 175, 80, 0.12); color: #2e7d32;">
                                <span class="material-symbols-outlined">corporate_fare</span>
                            </div>
                        </div>
                        <p class="body-sm text-on-surface-variant mt-4 mb-0">
                            Phòng ban/xưởng đang mở.
                        </p>
                    </article>
                </div>
            </section>

            <section class="row g-4">
                <div class="col-lg-8">
                    <article class="card-premium p-4 mb-4">
                        <div class="d-flex justify-content-between align-items-end mb-4 border-bottom border-outline-variant pb-3">
                            <div>
                                <h2 class="h4 text-on-surface fw-bold mb-1">Tổng quan hiệu suất nhân sự</h2>
                                <p class="body-sm text-on-surface-variant mb-0">Theo dõi nhanh tình trạng tài khoản và cơ cấu nguồn lực.</p>
                            </div>
                        </div>

                        <div class="row g-4">
                            <div class="col-md-6">
                                <div class="p-4 rounded-4 bg-surface-container-low h-100">
                                    <p class="label-md text-on-surface-variant mb-2">Tình trạng tài khoản</p>
                                    <p class="h2 fw-bold text-primary mb-1"><c:out value="${stats.activePercentage}" />%</p>
                                    <p class="body-sm text-on-surface-variant mb-3">
                                        <c:out value="${stats.activeUsers}" /> / <c:out value="${stats.totalUsers}" />
                                        nhân sự đang hoạt động.
                                    </p>
                                    <div class="progress" role="progressbar" aria-label="Tỷ lệ tài khoản hoạt động"
                                         aria-valuenow="${stats.activePercentage}" aria-valuemin="0" aria-valuemax="100"
                                         style="height: 10px;">
                                        <div class="progress-bar" style="width: ${stats.activePercentage}%; background: var(--primary-gradient);"></div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-6">
                                <div class="p-4 rounded-4 bg-surface-container-low h-100">
                                    <p class="label-md text-on-surface-variant mb-2">Cơ cấu nguồn lực</p>
                                    <div class="progress mb-3" role="progressbar" aria-label="Tỷ lệ văn phòng và sản xuất"
                                         style="height: 14px;">
                                        <div class="progress-bar" style="width: ${stats.officePercentage}%; background-color: var(--primary);"></div>
                                        <div class="progress-bar" style="width: ${stats.workerPercentage}%; background-color: var(--tertiary);"></div>
                                    </div>
                                    <div class="d-flex flex-column gap-2 body-sm">
                                        <div class="d-flex justify-content-between">
                                            <span>Văn phòng</span>
                                            <strong><c:out value="${stats.officePercentage}" />%</strong>
                                        </div>
                                        <div class="d-flex justify-content-between">
                                            <span>Sản xuất</span>
                                            <strong><c:out value="${stats.workerPercentage}" />%</strong>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>

                    <article class="card-premium p-4">
                        <h2 class="h4 text-on-surface fw-bold mb-3">Chỉ số vận hành chung</h2>
                        <div class="list-group list-group-flush">
                            <div class="list-group-item d-flex justify-content-between px-0 bg-transparent">
                                <span>Tổng nhân sự trên hệ thống</span>
                                <strong><c:out value="${stats.totalUsers}" /></strong>
                            </div>
                            <div class="list-group-item d-flex justify-content-between px-0 bg-transparent">
                                <span>Nhân sự văn phòng đang hoạt động</span>
                                <strong><c:out value="${stats.officeUsers}" /></strong>
                            </div>
                            <div class="list-group-item d-flex justify-content-between px-0 bg-transparent">
                                <span>Nhân sự sản xuất đang hoạt động</span>
                                <strong><c:out value="${stats.workerUsers}" /></strong>
                            </div>
                            <div class="list-group-item d-flex justify-content-between px-0 bg-transparent">
                                <span>Phòng ban/xưởng đang mở</span>
                                <strong><c:out value="${stats.departments}" /></strong>
                            </div>
                        </div>
                    </article>
                </div>

                <div class="col-lg-4">
                    <article class="card-premium p-4 mb-4">
                        <h2 class="h4 text-on-surface fw-bold mb-3">Thông báo nội bộ</h2>
                        <div class="d-flex flex-column gap-3">
                            <c:forEach var="announcement" items="${announcements}">
                                <div class="p-3 rounded-4 bg-surface-container-low">
                                    <span class="badge-premium badge-primary mb-2">HR</span>
                                    <p class="body-sm text-on-surface mb-0"><c:out value="${announcement}" /></p>
                                </div>
                            </c:forEach>
                        </div>
                    </article>

                    <article class="card-premium p-4">
                        <h2 class="h4 text-on-surface fw-bold mb-3">Tài khoản</h2>
                        <div class="d-flex flex-column gap-3">
                            <div class="d-flex justify-content-between gap-3">
                                <span class="text-on-surface-variant">Username</span>
                                <strong><c:out value="${user.username}" /></strong>
                            </div>
                            <div class="d-flex justify-content-between gap-3">
                                <span class="text-on-surface-variant">Vai trò</span>
                                <strong><c:out value="${user.roleName}" /></strong>
                            </div>
                            <div class="d-flex justify-content-between gap-3">
                                <span class="text-on-surface-variant">Phòng ban</span>
                                <strong><c:out value="${user.departmentName}" /></strong>
                            </div>
                            <div class="d-flex justify-content-between gap-3">
                                <span class="text-on-surface-variant">Số điện thoại</span>
                                <strong>
                                    <c:choose>
                                        <c:when test="${not empty user.phone}"><c:out value="${user.phone}" /></c:when>
                                        <c:otherwise>Chưa cập nhật</c:otherwise>
                                    </c:choose>
                                </strong>
                            </div>
                        </div>
                    </article>
                </div>
            </section>
        </main>

        <jsp:include page="/components/footer.jsp" />
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>