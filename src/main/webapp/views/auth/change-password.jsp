<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Change Password - ManuHRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" />
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"/>
</head>
<body class="bg-background text-on-surface">

    <div class="layout-wrapper">
        <aside class="sidebar">
          <div class="sidebar-header">
            <div class="d-flex align-items-center justify-content-center shadow-sm" style="width: 40px; height: 40px; border-radius: 8px; background: var(--primary-gradient);">
              <span class="material-symbols-outlined text-white" style="font-variation-settings: 'FILL' 1;">factory</span>
            </div>
            <div>
              <h1 class="h3 mb-0 text-primary fw-bolder" style="font-size: 20px;">ManuHRM</h1>
              <p class="label-sm text-muted mb-0 text-uppercase" style="font-size: 10px; letter-spacing: 0.05em;">Manufacturing Ops</p>
            </div>
          </div>
          
          <nav class="sidebar-menu">
            <a class="sidebar-nav-item text-decoration-none" href="${pageContext.request.contextPath}/home/dashboard">
              <span class="material-symbols-outlined">dashboard</span>
              <span>Dashboard</span>
            </a>
            <a class="sidebar-nav-item text-decoration-none" href="${pageContext.request.contextPath}/user/list">
              <span class="material-symbols-outlined">groups</span>
              <span>User List</span>
            </a>
            <a class="sidebar-nav-item text-decoration-none" href="${pageContext.request.contextPath}/role/list">
              <span class="material-symbols-outlined">verified_user</span>
              <span>Role List</span>
            </a>
            <hr class="my-3 border-secondary opacity-25">
            <a class="sidebar-nav-item active text-decoration-none" href="${pageContext.request.contextPath}/profile">
              <span class="material-symbols-outlined">account_circle</span>
              <span>My Profile</span>
            </a>
          </nav>
        </aside>
        
        <div class="main-content">
            <header class="top-header">
              <div class="d-flex align-items-center flex-grow-1" style="max-width: 400px;"></div>
              <div class="d-flex align-items-center gap-2">
                <span class="label-md fw-bold text-on-surface">${sessionScope.user.fullName}</span>
              </div>
            </header>
            
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