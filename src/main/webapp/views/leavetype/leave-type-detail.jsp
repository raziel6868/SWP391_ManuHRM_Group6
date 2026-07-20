<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Chi tiết loại nghỉ - ManuHRM</title>
    <link href="${ctx}/assets/css/main.css" rel="stylesheet">
    <style>
        .detail-row {
            display: flex;
            justify-content: space-between;
            gap: 1rem;
            padding: 0.75rem 0;
            border-bottom: 1px solid var(--outline-variant);
        }

        .detail-row:last-child {
            border-bottom: 0;
            padding-bottom: 0;
        }

        .detail-label {
            color: var(--on-surface-variant);
            font-size: 0.875rem;
            font-weight: 600;
        }

        .detail-value {
            color: var(--on-surface);
            font-weight: 600;
            text-align: right;
            word-break: break-word;
        }

        @media (max-width: 576px) {
            .detail-row {
                flex-direction: column;
                gap: 0.25rem;
            }

            .detail-value {
                text-align: left;
            }
        }
    </style>
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <nav aria-label="breadcrumb" class="mb-3">
                    <ol class="breadcrumb label-sm mb-0">
                        <li class="breadcrumb-item"><a href="${ctx}/leave-type-list" class="text-primary text-decoration-none">Loại nghỉ</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Chi tiết</li>
                    </ol>
                </nav>

                <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-3">
                    <div>
                        <div class="d-flex align-items-center gap-2 flex-wrap mb-2">
                            <c:choose>
                                <c:when test="${leaveType.isActive}">
                                    <span class="badge" style="background-color: #d1fae5; color: #065f46;">Hoạt động</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge" style="background-color: var(--surface-container-high); color: var(--on-surface-variant);">Vô hiệu hóa</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <h2 class="h3 text-on-surface fw-bold mb-1"><c:out value="${leaveType.name}" /></h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            <c:out value="${empty leaveType.description ? 'Chưa có mô tả.' : leaveType.description}" />
                        </p>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <a href="${ctx}/leave-type-list" class="btn btn-light border d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">arrow_back</span>
                            Quay lại
                        </a>
                        <c:if test="${canUpdate}">
                            <a href="${ctx}/leave-type-update?id=${leaveType.id}" class="btn btn-light border d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">edit</span>
                                Chỉnh sửa
                            </a>
                        </c:if>
                        <c:if test="${canChangeStatus}">
                            <form action="${ctx}/leave-type-status" method="POST" class="m-0">
                                <input type="hidden" name="id" value="${leaveType.id}" />
                                <input type="hidden" name="isActive" value="${!leaveType.isActive}" />
                                <button type="submit"
                                        class="btn btn-light border d-flex align-items-center gap-2"
                                        onclick="return confirm('${leaveType.isActive ? 'Bạn có chắc muốn vô hiệu hóa loại nghỉ này?' : 'Bạn có chắc muốn kích hoạt lại loại nghỉ này?'}');">
                                    <span class="material-symbols-outlined" style="font-size: 1.125rem;">${leaveType.isActive ? 'lock' : 'lock_open'}</span>
                                    ${leaveType.isActive ? 'Vô hiệu hóa' : 'Kích hoạt'}
                                </button>
                            </form>
                        </c:if>
                    </div>
                </div>

                <div class="row g-3 mb-4">
                    <div class="col-12 col-lg-6">
                        <section class="card-premium p-4 h-100">
                            <h3 class="h6 fw-bold text-on-surface mb-3 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined text-primary">payments</span>
                                Chi trả lương
                            </h3>
                            <div class="detail-row">
                                <span class="detail-label">Hưởng lương công ty</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${leaveType.isPaid}">Có</c:when>
                                        <c:otherwise>Không</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Nguồn chi trả</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${leaveType.salaryPaidBy == 'COMPANY'}">Công ty</c:when>
                                        <c:when test="${leaveType.salaryPaidBy == 'SOCIAL_INSURANCE'}">BHXH</c:when>
                                        <c:otherwise>Không chi trả</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </section>
                    </div>

                    <div class="col-12 col-lg-6">
                        <section class="card-premium p-4 h-100">
                            <h3 class="h6 fw-bold text-on-surface mb-3 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined text-primary">event_available</span>
                                Quy tắc ngày nghỉ
                            </h3>
                            <div class="detail-row">
                                <span class="detail-label">Loại phép năm</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${leaveType.isAnnualLeave}">Có</c:when>
                                        <c:otherwise>Không</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Trừ quỹ phép năm</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${leaveType.requiresBalance}">Có</c:when>
                                        <c:otherwise>Không</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Cách tính ngày</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${leaveType.dayCountMethod == 'CALENDAR_DAY'}">Ngày lịch</c:when>
                                        <c:otherwise>Ngày làm việc</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Số ngày cơ bản</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${not empty leaveType.baseDays}">
                                            <fmt:formatNumber value="${leaveType.baseDays}" minFractionDigits="0" maxFractionDigits="2" groupingUsed="false" />
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Tối đa mỗi đơn</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${not empty leaveType.maxDays}">
                                            <fmt:formatNumber value="${leaveType.maxDays}" minFractionDigits="0" maxFractionDigits="2" groupingUsed="false" />
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </section>
                    </div>

                    <div class="col-12 col-lg-6">
                        <section class="card-premium p-4 h-100">
                            <h3 class="h6 fw-bold text-on-surface mb-3 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined text-primary">workspace_premium</span>
                                Thâm niên
                            </h3>
                            <div class="detail-row">
                                <span class="detail-label">Cộng phép thâm niên</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${leaveType.hasSeniorityBonus}">Có</c:when>
                                        <c:otherwise>Không</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Mỗi số năm</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${leaveType.hasSeniorityBonus}">
                                            <c:out value="${leaveType.seniorityIntervalYears}" /> năm
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Số ngày cộng thêm</span>
                                <span class="detail-value">
                                    <c:choose>
                                        <c:when test="${leaveType.hasSeniorityBonus}">
                                            <fmt:formatNumber value="${leaveType.seniorityBonusDays}" minFractionDigits="0" maxFractionDigits="2" groupingUsed="false" /> ngày
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </section>
                    </div>

                    <div class="col-12 col-lg-6">
                        <section class="card-premium p-4 h-100">
                            <h3 class="h6 fw-bold text-on-surface mb-3 d-flex align-items-center gap-2">
                                <span class="material-symbols-outlined text-primary">database</span>
                                Thông tin hệ thống
                            </h3>
                            <div class="detail-row">
                                <span class="detail-label">ID</span>
                                <span class="detail-value"><c:out value="${leaveType.id}" /></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Ngày tạo</span>
                                <span class="detail-value"><c:out value="${leaveType.createdAt}" /></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Cập nhật lần cuối</span>
                                <span class="detail-value"><c:out value="${leaveType.updatedAt}" /></span>
                            </div>
                        </section>
                    </div>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
