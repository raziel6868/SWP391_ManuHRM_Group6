<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Edit Profile - ManuHRM</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0" />
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"/>
    <!-- Google Fonts Preconnect & Load -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">

    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        
        <div class="main-content">
            <jsp:include page="/components/header.jsp" />
            <div class="page-container d-flex flex-column" style="max-width: 700px; margin: 40px auto; width: 100%;">
                
                <div class="mb-4">
                    <h1 class="h2 text-on-surface fw-bold">Edit Personal Profile</h1>
                    <p class="body-md text-on-surface-variant mt-1">Update your personal data. Work-related info can only be modified by HR Admin.</p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger shadow-sm border-0 mb-4">${error}</div>
                </c:if>

                <div class="card-premium p-4 p-md-5 mb-4 position-relative overflow-hidden">
                    <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>
                    
                    <form action="${pageContext.request.contextPath}/profile/edit" method="POST">
                        
                        <input type="hidden" name="id" value="${user.id}"/>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Full Name</label>
                            <input type="text" class="form-control p-3 bg-light" name="fullName" value="${user.fullName}" required placeholder="Enter your full name">
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Phone Number</label>
                            <input type="text" class="form-control p-3 bg-light" name="phone" value="${user.phone}" placeholder="Enter phone number">
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold small text-uppercase text-muted">Date of Birth</label>
                            <fmt:formatDate var="formattedDob" value="${user.dob}" pattern="yyyy-MM-dd"/>
                            <input type="date" class="form-control p-3 bg-light" name="dob" value="${formattedDob}">
                        </div>

                        <hr class="my-4 border-outline-variant opacity-25">

                        <div class="bg-surface-container-low p-3 rounded mb-4">
                            <p class="small fw-bold text-danger text-uppercase mb-2"><span class="material-symbols-outlined align-middle" style="font-size: 16px;">lock</span> HR Restricted Information</p>
                            <div class="row g-3">
                                <div class="col-6">
                                    <span class="text-muted small">Employee Code:</span> <strong class="font-monospace text-on-surface">${user.employeeCode}</strong>
                                </div>
                                <div class="col-6">
                                    <span class="text-muted small">Username:</span> <strong class="text-on-surface">${user.username}</strong>
                                </div>
                                <div class="col-6">
                                    <span class="text-muted small">Department:</span> <strong class="text-on-surface">${user.departmentName}</strong>
                                </div>
                                <div class="col-6">
                                    <span class="text-muted small">Job Title:</span> <strong class="text-on-surface">${user.jobTitle}</strong>
                                </div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-2 mt-4">
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline-secondary px-4 py-2 fw-bold text-decoration-none">
                                Cancel
                            </a>
                            <button type="submit" class="btn-primary-gradient border-0 px-4 py-2 shadow-sm text-white fw-bold" style="border-radius: 4px;">
                                Save Changes
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
