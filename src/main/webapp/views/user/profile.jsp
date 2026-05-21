<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Hồ sơ của tôi - ManuHRM</title>
    <jsp:include page="/components/head.jsp" />
</head>
<body class="bg-background text-on-surface">

    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />

        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 900px; margin: 0 auto; width: 100%;">
                <div class="d-flex justify-content-between align-items-start mb-4">
                    <div>
                        <h1 class="h2 text-on-surface fw-bold">Hồ sơ của tôi</h1>
                        <p class="body-md text-on-surface-variant mt-1">Quản lý thông tin cá nhân và bảo mật tài khoản.</p>
                    </div>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/profile/edit" class="btn btn-outline-primary px-4 py-2 d-flex align-items-center gap-2 shadow-sm fw-bold text-decoration-none">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">edit</span>
                            Chỉnh sửa
                        </a>
                        <a href="${pageContext.request.contextPath}/auth/change-password" class="btn-primary-gradient text-decoration-none px-4 py-2 d-flex align-items-center gap-2 shadow-sm text-white">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">lock_reset</span>
                            Đổi mật khẩu
                        </a>
                    </div>
                </div>

                <div class="card-premium p-4 p-md-5 mb-4 position-relative overflow-hidden d-flex flex-column flex-md-row align-items-center align-items-md-start gap-4">
                    <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>

                    <div class="position-relative flex-shrink-0">
                        <img alt="${user.fullName}" class="rounded-circle object-fit-cover shadow-sm" src="https://ui-avatars.com/api/?name=${user.fullName}&size=112&background=0D8ABC&color=fff" style="width: 112px; height: 112px; border: 4px solid var(--surface);"/>
                        <div class="position-absolute bottom-0 end-0 rounded-circle d-flex align-items-center justify-content-center shadow-sm" style="width: 24px; height: 24px; background-color: var(--surface); border: 2px solid var(--surface-container-lowest);">
                            <div class="rounded-circle" style="width: 12px; height: 12px; background-color: ${user.isActive ? '#10b981' : '#ef4444'};"></div>
                        </div>
                    </div>

                    <div class="flex-grow-1 text-center text-md-start">
                        <div class="d-flex flex-column flex-md-row align-items-center gap-3 mb-2">
                            <h2 class="h3 text-on-surface fw-bold mb-0">${user.fullName}</h2>
                            <c:choose>
                                <c:when test="${user.isActive == true}">
                                    <span class="badge border" style="background-color: #d1fae5; color: #065f46; border-color: #a7f3d0 !important;">Nhân viên đang làm</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge border" style="background-color: #fee2e2; color: #991b1b; border-color: #fca5a5 !important;">Đã nghỉ</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <p class="body-lg text-on-surface-variant mb-3">
                            <c:out value="${not empty user.jobTitle ? user.jobTitle : 'N/A'}" />
                        </p>

                        <div class="d-flex flex-wrap justify-content-center justify-content-md-start gap-3">
                            <div class="d-flex align-items-center gap-2 text-on-surface-variant">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">badge</span>
                                <span class="label-md font-monospace">${user.employeeCode}</span>
                            </div>
                            <div class="d-flex align-items-center gap-2 text-on-surface-variant">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">domain</span>
                                <span class="body-md">${user.departmentName}</span>
                            </div>
                            <div class="d-flex align-items-center gap-2 text-on-surface-variant">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">badge_visibility</span>
                                <span class="body-md">${user.employeeType == 'OFFICE' ? 'Văn phòng' : 'Công nhân'}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row g-4 mb-4">
                    <div class="col-md-6">
                        <div class="card-premium h-100 p-4">
                            <h3 class="label-md text-on-surface mb-4 pb-3 border-bottom border-outline-variant d-flex align-items-center gap-2 text-uppercase">
                                <span class="material-symbols-outlined text-primary" style="font-size: 1.125rem;">contact_mail</span>
                                Liên hệ & Thông tin cá nhân
                            </h3>
                            <div class="d-flex flex-column gap-3">
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Tên đăng nhập</div>
                                    <div class="body-md text-on-surface fw-medium">${user.username}</div>
                                </div>
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Số điện thoại</div>
                                    <div class="body-md text-on-surface fw-medium">${not empty user.phone ? user.phone : 'N/A'}</div>
                                </div>
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Ngày sinh</div>
                                    <div class="body-md text-on-surface fw-medium">
                                        <c:choose>
                                            <c:when test="${user.dob != null}">
                                                <fmt:formatDate value="${user.dob}" pattern="dd MMMM, yyyy"/>
                                            </c:when>
                                            <c:otherwise>N/A</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="card-premium h-100 p-4">
                            <h3 class="label-md text-on-surface mb-4 pb-3 border-bottom border-outline-variant d-flex align-items-center gap-2 text-uppercase">
                                <span class="material-symbols-outlined text-primary" style="font-size: 1.125rem;">work</span>
                                Thông tin công việc
                            </h3>
                            <div class="d-flex flex-column gap-3">
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Phòng ban</div>
                                    <div class="body-md text-on-surface fw-medium">${user.departmentName}</div>
                                </div>
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Quản lý trực tiếp</div>
                                    <div class="d-flex align-items-center gap-2">
                                        <div class="rounded-circle bg-surface-container-high d-flex align-items-center justify-content-center text-on-surface fw-bold" style="width: 24px; height: 24px; font-size: 10px;">M</div>
                                        <div class="body-md text-on-surface fw-medium">${not empty user.managerName ? user.managerName : 'Không có'}</div>
                                    </div>
                                </div>
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Ngày tham gia</div>
                                    <div class="body-md text-on-surface fw-medium">
                                        <c:choose>
                                            <c:when test="${user.createdAt != null}">
                                                <fmt:formatDate value="${user.createdAt}" pattern="dd MMMM, yyyy"/>
                                            </c:when>
                                            <c:otherwise>N/A</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <footer class="w-100 py-3 border-top border-outline-variant bg-surface d-flex justify-content-between align-items-center px-4 mt-auto">
                <p class="label-md fw-bold text-on-surface mb-0" style="font-size: 13px;">&copy; 2026 ManuHRM Industrial Solutions. Bảo lưu mọi quyền.</p>
                <div class="d-flex gap-4">
                    <a class="text-on-surface-variant text-decoration-none label-sm hover-primary" href="#">Chính sách bảo mật</a>
                    <a class="text-on-surface-variant text-decoration-none label-sm hover-primary" href="#">Tuân thủ</a>
                    <a class="text-on-surface-variant text-decoration-none label-sm hover-primary" href="#">Trạng thái hệ thống</a>
                </div>
            </footer>
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
