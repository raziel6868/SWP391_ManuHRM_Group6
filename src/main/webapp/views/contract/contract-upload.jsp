<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Tải lên PDF hợp đồng - ManuHRM</title>
    <link href="${ctx}/assets/css/main.css" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">
    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />

            <div class="page-container" style="max-width: 720px;">
                <jsp:include page="/components/alert.jsp" />

                <nav aria-label="breadcrumb" class="mb-3">
                    <ol class="breadcrumb label-sm mb-0">
                        <li class="breadcrumb-item"><a href="${ctx}/contract-list" class="text-primary text-decoration-none">Hợp đồng</a></li>
                        <li class="breadcrumb-item"><a href="${ctx}/contract-detail?id=${contract.id}" class="text-primary text-decoration-none">Chi tiết #${contract.id}</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Tải PDF</li>
                    </ol>
                </nav>

                <h2 class="h3 text-on-surface fw-bold mb-1">Tải lên file PDF hợp đồng</h2>
                <p class="body-md text-on-surface-variant mb-4">
                    Hợp đồng của <strong>${contract.fullName}</strong> (${contract.employeeCode}) - ${contract.contractTypeName}.
                </p>

                <c:if test="${not empty contract.filePath}">
                    <div class="alert alert-light border d-flex align-items-start gap-2 mb-3" role="alert">
                        <span class="material-symbols-outlined text-primary">info</span>
                        <div class="label-sm">
                            Hợp đồng này đã có file PDF. Tải lên sẽ <strong>ghi đè</strong> file hiện tại:
                            <code class="d-block mt-1">${contract.filePath}</code>
                        </div>
                    </div>
                </c:if>

                <form action="${ctx}/contract-upload" method="POST" enctype="multipart/form-data"
                    class="card-premium p-4">
                    <input type="hidden" name="id" value="${contract.id}" />

                    <div class="mb-3">
                        <label for="contractFile" class="form-label fw-medium">File PDF <span class="text-danger">*</span></label>
                        <input type="file" id="contractFile" name="contractFile" accept="application/pdf,.pdf"
                            class="input-premium w-100" required />
                        <div class="form-text">
                            Chỉ chấp nhận file <strong>PDF</strong>, tối đa <strong>5MB</strong>.
                        </div>
                    </div>

                    <div class="d-flex justify-content-end gap-2">
                        <a href="${ctx}/contract-detail?id=${contract.id}" class="btn btn-light border">Hủy</a>
                        <button type="submit" class="btn-primary-gradient text-decoration-none px-3 py-2 border-0 d-flex align-items-center gap-2">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">upload</span>
                            Tải lên
                        </button>
                    </div>
                </form>
            </div>

            <jsp:include page="/components/footer.jsp" />
        </div>
    </div>

    <jsp:include page="/components/foot.jsp" />
</body>
</html>
