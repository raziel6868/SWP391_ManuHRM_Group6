<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>403 Forbidden - ManuHRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"/>
    <!-- Google Fonts Preconnect & Load -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
</head>
<body class="d-flex align-items-center justify-content-center min-vh-100 bg-surface-container-low text-on-surface">
    <main class="w-100 text-center d-flex flex-column align-items-center" style="max-width: 600px; padding: 2rem;">
        
        <!-- Icon -->
        <div class="mb-4 rounded-circle bg-error-container d-flex align-items-center justify-content-center" style="width: 96px; height: 96px; box-shadow: var(--shadow-premium);">
            <span class="material-symbols-outlined text-error" style="font-size: 3rem; font-variation-settings: 'FILL' 1;">block</span>
        </div>
        
        <!-- Typography -->
        <h1 class="display-4 fw-bold text-on-surface mb-3">403 Forbidden</h1>
        <h2 class="h3 text-on-surface-variant mb-4">Access Denied</h2>
        
        <p class="body-lg text-on-surface-variant mb-5" style="max-width: 480px;">
            You do not have the necessary permissions to view this page or perform this action within ManuHRM. Please contact your system administrator if you believe this is an error.
        </p>
        
        <!-- Action -->
        <a href="${pageContext.request.contextPath}/" class="btn-primary-gradient text-decoration-none px-4 py-3">
            <span class="material-symbols-outlined" style="font-size: 1.25rem;">arrow_back</span>
            Return to Dashboard
        </a>
    </main>
</body>
</html>


