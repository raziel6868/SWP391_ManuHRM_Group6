<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>500 Server Error - ManuHRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"/>
    <!-- Google Fonts Preconnect & Load -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
</head>
<body class="d-flex align-items-center justify-content-center min-vh-100 bg-background text-on-background">
    <main class="w-100 text-center d-flex flex-column align-items-center" style="max-width: 800px; padding: 2rem;">
        
        <!-- Icon -->
        <div class="mb-4 rounded-circle bg-error-container d-flex align-items-center justify-content-center shadow-sm border" style="width: 96px; height: 96px; border-color: var(--error-container) !important;">
            <span class="material-symbols-outlined text-error" style="font-size: 3rem; font-variation-settings: 'FILL' 1;">engineering</span>
        </div>
        
        <!-- Typography -->
        <h1 class="display-4 fw-bold text-on-background mb-3 d-none d-md-block">500 Internal Server Error</h1>
        <h1 class="h2 fw-bold text-on-background mb-3 d-md-none">500 Error</h1>
        
        <p class="body-lg text-on-surface-variant mb-5" style="max-width: 450px;">
            Something went wrong on our end. Our technical team has been notified and is currently investigating the issue.
        </p>
        
        <!-- Action Area -->
        <div class="d-flex flex-column flex-sm-row align-items-center gap-3">
            <a href="#" class="btn-primary-gradient text-decoration-none w-100 w-sm-auto px-4 py-2 d-flex align-items-center justify-content-center gap-2">
                <span class="material-symbols-outlined" style="font-size: 1.125rem;">support_agent</span>
                Contact IT Admin
            </a>
            <a href="${pageContext.request.contextPath}/" class="btn btn-light border w-100 w-sm-auto px-4 py-2 d-flex align-items-center justify-content-center gap-2 shadow-sm text-on-surface" style="background-color: var(--surface); border-color: var(--outline) !important;">
                <span class="material-symbols-outlined" style="font-size: 1.125rem;">arrow_back</span>
                Return to Dashboard
            </a>
        </div>
    </main>
</body>
</html>


