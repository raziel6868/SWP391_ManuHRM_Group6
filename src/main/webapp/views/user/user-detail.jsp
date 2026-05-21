<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Thông tin nhân viên - ManuHRM</title>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container d-flex flex-column" style="max-width: 800px; margin: 0 auto; width: 100%;">
                <div class="d-flex justify-content-between align-items-start mb-4">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Hồ sơ nhân viên</h2>
                        <a href="${pageContext.request.contextPath}/user-list" class="text-primary text-decoration-none label-md d-flex align-items-center gap-1">
                            <span class="material-symbols-outlined" style="font-size: 1rem;">arrow_back</span>
                            Quay lại danh sách
                        </a>
                    </div>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/user-update?id=${user.id}" class="btn btn-outline-primary px-3 py-2 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">edit</span>
                            Chỉnh sửa
                        </a>
                        <c:choose>
                            <c:when test="${user.id == sessionScope.authUser.id}">
                                <button class="btn btn-secondary px-3 py-2 d-flex align-items-center gap-2" disabled title="Bạn không thể thay đổi trạng thái tài khoản của chính mình">
                                    <span class="material-symbols-outlined" style="font-size: 1.125rem;">lock</span>
                                    Khóa tài khoản
                                </button>
                            </c:when>
                            <c:otherwise>
                                <form method="post" action="${pageContext.request.contextPath}/user-status" class="m-0">
                                    <input type="hidden" name="id" value="${user.id}" />
                                    <input type="hidden" name="referer" value="detail" />
                                    <input type="hidden" name="isActive" value="${!user.isActive}" />
                                    <button type="submit" class="btn ${user.isActive ? 'btn-danger' : 'btn-success'} px-3 py-2 d-flex align-items-center gap-2"
                                        onclick="return confirm('Bạn có chắc muốn ${user.isActive ? 'khóa' : 'mở khóa'} tài khoản của ${user.fullName}?')">
                                        <span class="material-symbols-outlined" style="font-size: 1.125rem;">${user.isActive ? 'lock' : 'lock_open'}</span>
                                        ${user.isActive ? 'Khóa tài khoản' : 'Mở tài khoản'}
                                    </button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="card-premium p-4 p-md-5 mb-4 position-relative overflow-hidden">
                    <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>
                    <div class="d-flex flex-column flex-md-row align-items-center align-items-md-start gap-4 mb-5">
                        <div class="position-relative flex-shrink-0">
                            <img alt="${user.fullName}" class="rounded-circle object-fit-cover shadow-sm" src="https://ui-avatars.com/api/?name=${user.fullName}&size=96&background=0D8ABC&color=fff" style="width: 96px; height: 96px; border: 4px solid var(--surface);"/>
                            <div class="position-absolute bottom-0 end-0 rounded-circle d-flex align-items-center justify-content-center shadow-sm" style="width: 24px; height: 24px; background-color: var(--surface); border: 2px solid var(--surface-container-lowest);">
                                <div class="rounded-circle" style="width: 12px; height: 12px; background-color: ${user.isActive ? '#10b981' : '#ef4444'};"></div>
                            </div>
                        </div>

                        <div class="flex-grow-1 text-center text-md-start">
                            <div class="d-flex flex-column flex-md-row align-items-center gap-3 mb-2">
                                <h3 class="h4 text-on-surface fw-bold mb-0">${user.fullName}</h3>
                                <c:choose>
                                    <c:when test="${user.isActive}">
                                        <span class="badge border" style="background-color: #d1fae5; color: #065f46;">Đang làm việc</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge border" style="background-color: #fee2e2; color: #991b1b;">Đã nghỉ</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <p class="body-lg text-on-surface-variant mb-3">${user.jobTitle}</p>
                            <div class="d-flex flex-wrap justify-content-center justify-content-md-start gap-3">
                                <div class="d-flex align-items-center gap-2 text-on-surface-variant">
                                    <span class="material-symbols-outlined" style="font-size: 1.125rem;">badge</span>
                                    <span class="label-md font-monospace">${user.employeeCode}</span>
                                </div>
                                <div class="d-flex align-items-center gap-2 text-on-surface-variant">
                                    <span class="material-symbols-outlined" style="font-size: 1.125rem;">domain</span>
                                    <span class="body-md">${user.departmentName}</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="row g-4">
                        <div class="col-md-6">
                            <h4 class="label-md text-on-surface text-uppercase mb-3 pb-2 border-bottom">Thông tin cá nhân</h4>
                            <div class="d-flex flex-column gap-3">
                                <div>
                                    <span class="label-sm text-on-surface-variant text-uppercase">Tên đăng nhập</span>
                                    <div class="body-md text-on-surface fw-medium mt-1">${user.username}</div>
                                </div>
                                <div>
                                    <span class="label-sm text-on-surface-variant text-uppercase">Số điện thoại</span>
                                    <div class="body-md text-on-surface fw-medium mt-1">${not empty user.phone ? user.phone : 'N/A'}</div>
                                </div>
                                <div>
                                    <span class="label-sm text-on-surface-variant text-uppercase">Ngày sinh</span>
                                    <div class="body-md text-on-surface fw-medium mt-1">${not empty user.dob ? user.dob : 'N/A'}</div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <h4 class="label-md text-on-surface text-uppercase mb-3 pb-2 border-bottom">Thông tin tổ chức</h4>
                            <div class="d-flex flex-column gap-3">
                                <div>
                                    <span class="label-sm text-on-surface-variant text-uppercase">Quản lý trực tiếp</span>
                                    <div class="body-md text-on-surface fw-medium mt-1">${not empty user.managerName ? user.managerName : 'Không có'}</div>
                                </div>
                                <div>
                                    <span class="label-sm text-on-surface-variant text-uppercase">Vai trò hệ thống</span>
                                    <div class="body-md text-on-surface fw-medium mt-1">${user.roleDisplayName}</div>
                                </div>
                                <div>
                                    <span class="label-sm text-on-surface-variant text-uppercase">Loại nhân sự</span>
                                    <div class="body-md text-on-surface fw-medium mt-1">${user.employeeType == 'OFFICE' ? 'Văn phòng' : 'Công nhân'}</div>
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
