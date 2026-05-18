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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
</head>
<body>
<div class="app-shell">
    <jsp:include page="/components/sidebar.jsp" />

    <main class="content">
        <jsp:include page="/components/header.jsp" />

        <section class="page-heading">
            <div>
                <h1>Xin chào, <c:out value="${user.fullName}" /></h1>
                <p>Cổng thông tin nội bộ dành cho nhân sự nhà máy.</p>
            </div>
            <div class="date-pill"><c:out value="${currentDate}" /></div>
        </section>

        <section class="stats-grid" aria-label="Thống kê nhanh">
            <article class="stat-card">
                <div class="stat-value"><c:out value="${stats.activeUsers}" /></div>
                <div class="stat-label">Nhân sự đang hoạt động</div>
            </article>
            <article class="stat-card">
                <div class="stat-value"><c:out value="${stats.officeUsers}" /></div>
                <div class="stat-label">Khối văn phòng</div>
            </article>
            <article class="stat-card">
                <div class="stat-value"><c:out value="${stats.workerUsers}" /></div>
                <div class="stat-label">Khối sản xuất</div>
            </article>
            <article class="stat-card">
                <div class="stat-value"><c:out value="${stats.departments}" /></div>
                <div class="stat-label">Phòng ban/xưởng</div>
            </article>
        </section>

        <section class="dashboard-grid">
            <div>
                <h2 class="section-title">Tổng quan hiệu suất nhân sự</h2>
                <div class="performance-grid">
                    <article class="dashboard-panel highlight-panel">
                        <div class="panel-kicker">Tình trạng tài khoản</div>
                        <div class="panel-value"><c:out value="${stats.activePercentage}" />%</div>
                        <div class="panel-note">
                            <c:out value="${stats.activeUsers}" /> / <c:out value="${stats.totalUsers}" />
                            nhân sự đang hoạt động trong hệ thống.
                        </div>
                        <div class="bar-track" aria-label="Tỷ lệ tài khoản hoạt động">
                            <span class="bar-fill" style="width: ${stats.activePercentage}%"></span>
                        </div>
                    </article>

                    <article class="dashboard-panel">
                        <div class="panel-kicker">Cơ cấu nguồn lực</div>
                        <div class="composition-bar" aria-label="Tỷ lệ văn phòng và sản xuất">
                            <span class="composition-segment office" style="width: ${stats.officePercentage}%"></span>
                            <span class="composition-segment worker" style="width: ${stats.workerPercentage}%"></span>
                        </div>
                        <div class="legend-grid">
                            <div>
                                <span class="legend-dot office"></span>
                                Văn phòng: <strong><c:out value="${stats.officePercentage}" />%</strong>
                            </div>
                            <div>
                                <span class="legend-dot worker"></span>
                                Sản xuất: <strong><c:out value="${stats.workerPercentage}" />%</strong>
                            </div>
                        </div>
                    </article>
                </div>

                <section class="dashboard-panel operation-panel">
                    <h2 class="section-title">Chỉ số vận hành chung</h2>
                    <div class="operation-list">
                        <div class="operation-item">
                            <span class="operation-label">Tổng nhân sự trên hệ thống</span>
                            <strong><c:out value="${stats.totalUsers}" /></strong>
                        </div>
                        <div class="operation-item">
                            <span class="operation-label">Nhân sự văn phòng đang hoạt động</span>
                            <strong><c:out value="${stats.officeUsers}" /></strong>
                        </div>
                        <div class="operation-item">
                            <span class="operation-label">Nhân sự sản xuất đang hoạt động</span>
                            <strong><c:out value="${stats.workerUsers}" /></strong>
                        </div>
                        <div class="operation-item">
                            <span class="operation-label">Phòng ban/xưởng đang mở</span>
                            <strong><c:out value="${stats.departments}" /></strong>
                        </div>
                    </div>
                </section>
            </div>

            <aside>
                <h2 class="section-title">Thông báo nội bộ</h2>
                <div class="announcement-list">
                    <c:forEach var="announcement" items="${announcements}">
                        <article class="announcement-item"><c:out value="${announcement}" /></article>
                    </c:forEach>
                </div>

                <section class="profile-panel">
                    <h2 class="section-title">Tài khoản</h2>
                    <div class="profile-line">
                        <span class="profile-label">Username</span>
                        <span class="profile-value"><c:out value="${user.username}" /></span>
                    </div>
                    <div class="profile-line">
                        <span class="profile-label">Vai trò</span>
                        <span class="profile-value"><c:out value="${user.roleName}" /></span>
                    </div>
                    <div class="profile-line">
                        <span class="profile-label">Phòng ban</span>
                        <span class="profile-value"><c:out value="${user.departmentName}" /></span>
                    </div>
                    <div class="profile-line">
                        <span class="profile-label">Email</span>
                        <span class="profile-value">
                            <c:choose>
                                <c:when test="${not empty user.email}"><c:out value="${user.email}" /></c:when>
                                <c:otherwise>Chưa cập nhật</c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                </section>
            </aside>
        </section>

        <jsp:include page="/components/footer.jsp" />
    </main>
</div>
</body>
</html>
