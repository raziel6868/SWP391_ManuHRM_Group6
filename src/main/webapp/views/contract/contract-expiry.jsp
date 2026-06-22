<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>HĐ sắp hết hạn - ManuHRM</title>
    <link href="${ctx}/assets/css/main.css" rel="stylesheet">
    <style>
        .badge-days-ok { background: #d1e7dd; color: #0a3622; }
        .badge-days-warn { background: #fff3cd; color: #664d03; }
        .badge-days-danger { background: #f8d7da; color: #58151c; }
    </style>
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container">
                <jsp:include page="/components/alert.jsp" />

                <nav aria-label="breadcrumb" class="mb-3">
                    <ol class="breadcrumb label-sm mb-0">
                        <li class="breadcrumb-item"><a href="${ctx}/contract-list" class="text-primary text-decoration-none">Hợp đồng</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Sắp hết hạn</li>
                    </ol>
                </nav>

                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-4">
                    <div>
                        <h2 class="h3 text-on-surface fw-bold mb-1">Hợp đồng sắp hết hạn</h2>
                        <p class="body-md text-on-surface-variant mb-0">
                            Danh sách các hợp đồng đang hiệu lực sẽ kết thúc trong vòng <strong>${days}</strong> ngày tới.
                        </p>
                    </div>
                    <form action="${ctx}/contract-expiry" method="GET" class="d-flex align-items-center gap-2">
                        <label for="days" class="label-sm text-on-surface-variant mb-0">Trong khoảng:</label>
                        <select id="days" name="days" class="form-select form-select-sm" style="min-width: 160px;"
                            onchange="this.form.submit()">
                            <option value="7"  ${days == 7  ? 'selected' : ''}>7 ngày tới</option>
                            <option value="15" ${days == 15 ? 'selected' : ''}>15 ngày tới</option>
                            <option value="30" ${days == 30 ? 'selected' : ''}>30 ngày tới (mặc định)</option>
                            <option value="60" ${days == 60 ? 'selected' : ''}>60 ngày tới</option>
                            <option value="90" ${days == 90 ? 'selected' : ''}>90 ngày tới</option>
                        </select>
                    </form>
                </div>

                <div class="card-premium">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 60px;">#</th>
                                    <th>Mã NV</th>
                                    <th>Họ và tên</th>
                                    <th>Phòng ban</th>
                                    <th>Loại HĐ</th>
                                    <th>Ngày kết thúc</th>
                                    <th class="text-center">Còn lại</th>
                                    <th class="text-center">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty items}">
                                        <tr>
                                            <td colspan="8" class="text-center text-on-surface-variant py-5">
                                                Không có hợp đồng nào sắp hết hạn trong khoảng thời gian này.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="c" items="${items}">
                                            <tr>
                                                <td class="text-on-surface-variant">${c.id}</td>
                                                <td class="fw-medium">${c.employeeCode}</td>
                                                <td>${c.fullName}</td>
                                                <td>${c.departmentName}</td>
                                                <td>
                                                    <span class="label-sm">${c.contractTypeName}</span>
                                                </td>
                                                <td>${c.endDate}</td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${c.daysRemaining <= 7}">
                                                            <span class="badge badge-days-danger px-2 py-1">${c.daysRemaining} ngày</span>
                                                        </c:when>
                                                        <c:when test="${c.daysRemaining <= 15}">
                                                            <span class="badge badge-days-warn px-2 py-1">${c.daysRemaining} ngày</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-days-ok px-2 py-1">${c.daysRemaining} ngày</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-center">
                                                    <a href="${ctx}/contract-detail?id=${c.id}" class="btn btn-sm btn-outline-primary">Chi tiết</a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="d-flex gap-3 mt-3 label-sm text-on-surface-variant">
                    <span class="d-inline-flex align-items-center gap-1">
                        <span class="badge badge-days-danger px-2 py-1">&nbsp;</span> ≤ 7 ngày: rất gấp
                    </span>
                    <span class="d-inline-flex align-items-center gap-1">
                        <span class="badge badge-days-warn px-2 py-1">&nbsp;</span> ≤ 15 ngày: sắp tới
                    </span>
                    <span class="d-inline-flex align-items-center gap-1">
                        <span class="badge badge-days-ok px-2 py-1">&nbsp;</span> > 15 ngày: theo dõi
                    </span>
                </div>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>