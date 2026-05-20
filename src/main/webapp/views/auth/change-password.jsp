<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Change Password - ManuHRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"/>
</head>
<body class="bg-background text-on-surface">

    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />
            
            <div class="page-container d-flex flex-column" style="max-width: 600px; margin: 40px auto; width: 100%;">
                <div class="mb-4">
                    <h1 class="h2 text-on-surface fw-bold">Change Password</h1>
                    <p class="body-md text-on-surface-variant mt-1">Ensure your account is using a long, random password to stay secure.</p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger border-0 shadow-sm mb-4">${error}</div>
                </c:if>
                <c:if test="${not empty success}">
                    <div class="alert alert-success border-0 shadow-sm mb-4">${success}</div>
                </c:if>

                <div class="card-premium p-4 p-md-5 position-relative overflow-hidden">
                    <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>
                    
                    <form action="${pageContext.request.contextPath}/auth/change-password" method="POST">
                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Current Password</label>
                            <input type="password" class="form-control p-3 bg-light" name="currentPassword" required placeholder="Enter current password">
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">New Password</label>
                            <input type="password" class="form-control p-3 bg-light" name="newPassword" required placeholder="Minimum 6 characters">
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Confirm New Password</label>
                            <input type="password" class="form-control p-3 bg-light" name="confirmPassword" required placeholder="Repeat new password">
                        </div>

                        <div class="d-flex justify-content-end gap-2 mt-4">
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-secondary px-4 py-2 fw-bold text-decoration-none">Cancel</a>
                            <button type="submit" class="btn-primary-gradient border-0 px-4 py-2 text-white fw-bold" style="border-radius: 4px;">Update Password</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
