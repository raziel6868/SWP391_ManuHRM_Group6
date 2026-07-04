<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Biểu thuế lũy tiến - ManuHRM</title>
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
                        <h2 class="h3 text-on-surface fw-bold mb-1">Biểu thuế lũy tiến</h2>
                        <p class="body-md text-on-surface-variant mb-0">Quản lý các bậc thuế TNCN theo tháng, lưu trong DB để payroll tính động theo thời gian.</p>
                    </div>
                    <c:if test="${canSetup}">
                        <a href="${pageContext.request.contextPath}/personal-tax-bracket-setup"
                           class="btn-primary-gradient text-decoration-none px-3 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">add</span>
                            Thêm bậc thuế
                        </a>
                    </c:if>
                </div>

                <div class="card-premium overflow-hidden">
                    <div class="table-responsive">
                        <table class="table table-premium mb-0 w-100">
                            <thead>
                                <tr>
                                    <th>Hiệu lực từ</th>
                                    <th>Hiệu lực đến</th>
                                    <th class="text-center">Bậc</th>
                                    <th class="text-end">Thu nhập từ</th>
                                    <th class="text-end">Thu nhập đến</th>
                                    <th class="text-end">Thuế suất</th>
                                    <th class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="bracket" items="${personalTaxBrackets}">
                                    <tr>
                                        <td><fmt:formatDate value="${bracket.effectiveFrom}" pattern="dd/MM/yyyy" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty bracket.effectiveTo}">
                                                    <fmt:formatDate value="${bracket.effectiveTo}" pattern="dd/MM/yyyy" />
                                                </c:when>
                                                <c:otherwise>Đang áp dụng</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-center">${bracket.bracketOrder}</td>
                                        <td class="text-end"><fmt:formatNumber value="${bracket.incomeFrom}" pattern="#,##0" /></td>
                                        <td class="text-end">
                                            <c:choose>
                                                <c:when test="${not empty bracket.incomeTo}">
                                                    <fmt:formatNumber value="${bracket.incomeTo}" pattern="#,##0" />
                                                </c:when>
                                                <c:otherwise>Không giới hạn</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end"><fmt:formatNumber value="${bracket.taxRate}" type="percent" maxFractionDigits="2" /></td>
                                        <td class="text-end">
                                            <c:if test="${canSetup}">
                                                <a href="${pageContext.request.contextPath}/personal-tax-bracket-setup?id=${bracket.id}"
                                                   class="btn btn-sm btn-outline-primary">
                                                    Sửa
                                                </a>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty personalTaxBrackets}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-on-surface-variant">
                                            Chưa có bậc thuế nào.
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

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
