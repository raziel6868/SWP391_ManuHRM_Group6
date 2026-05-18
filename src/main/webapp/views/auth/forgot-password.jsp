<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Forgot Password - ManuHRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"/>
</head>
<body class="bg-background text-on-surface d-flex align-items-center justify-content-center" style="min-height: 100vh;">

    <div class="card-premium p-4 p-md-5" style="max-width: 450px; width: 100%; margin: 20px; position: relative; overflow: hidden;">
        <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>
        
        <div class="text-center mb-4">
            <h1 class="h3 fw-bold text-primary">Password Recovery</h1>
            <p class="body-md text-muted mt-2">Enter your Employee Code to submit a password reset ticket to HR Administration.</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger border-0 small shadow-sm mb-3">${error}</div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success border-0 small shadow-sm mb-3">${success}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/forgot-password" method="POST">
            <div class="mb-4">
                <label class="form-label fw-bold small text-uppercase text-muted">Employee Code</label>
                <input type="text" class="form-control p-3 bg-light font-monospace" name="employeeCode" required placeholder="e.g. EMP-20481">
            </div>

            <button type="submit" class="btn btn-primary-gradient w-100 py-3 text-white fw-bold border-0 shadow-sm mb-3" style="border-radius: 4px;">
                Submit Ticket Request
            </button>
            
            <div class="text-center">
                <a href="${pageContext.request.contextPath}/views/auth/login.jsp" class="text-decoration-none small fw-bold text-secondary">
                    Back to Login
                </a>
            </div>
        </form>
    </div>

</body>
</html>