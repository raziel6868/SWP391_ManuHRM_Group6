<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>My Profile - ManuHRM</title>
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
            <a class="sidebar-nav-item text-decoration-none" href="${pageContext.request.contextPath}/home/dashboard.html">
              <span class="material-symbols-outlined">dashboard</span>
              <span>Dashboard</span>
            </a>
            <a class="sidebar-nav-item text-decoration-none" href="${pageContext.request.contextPath}/user-list.html">
              <span class="material-symbols-outlined">groups</span>
              <span>User List</span>
            </a>
            <a class="sidebar-nav-item text-decoration-none" href="${pageContext.request.contextPath}/role/role-list.html">
              <span class="material-symbols-outlined">verified_user</span>
              <span>Role List</span>
            </a>
            <hr class="my-3 border-secondary opacity-25">
            <a class="sidebar-nav-item active text-decoration-none" href="${pageContext.request.contextPath}/profile">
              <span class="material-symbols-outlined">account_circle</span>
              <span>My Profile</span>
            </a>
          </nav>
          
          <div class="sidebar-footer">
            <a class="sidebar-nav-item text-danger text-decoration-none mb-3" href="${pageContext.request.contextPath}/auth/logout">
              <span class="material-symbols-outlined">logout</span>
              <span>Logout</span>
            </a>
            <div class="d-flex align-items-center gap-3 px-2">
              <img alt="User Avatar" class="rounded-circle" src="https://ui-avatars.com/api/?name=${user.fullName}&background=0D8ABC&color=fff" style="width: 40px; height: 40px; object-fit: cover; border: 2px solid var(--primary-fixed-dim);"/>
              <div>
                <p class="label-sm fw-bold mb-0 text-on-surface">${user.fullName}</p>
                <p class="label-sm text-muted mb-0" style="font-size: 11px;">${user.roleDisplayName}</p>
              </div>
            </div>
          </div>
        </aside>
        
        <div class="main-content">
            <header class="top-header">
              <div class="d-flex align-items-center flex-grow-1" style="max-width: 400px;">
                <div class="position-relative w-100 header-search">
                  <span class="material-symbols-outlined position-absolute top-50 translate-middle-y text-muted" style="left: 12px; font-size: 1.125rem;">search</span>
                  <input class="input-premium w-100" placeholder="Search resources, users, or roles..." type="text" style="padding-left: 2.5rem; border-radius: var(--rounded-full); background-color: var(--surface-container-low); border-color: transparent;"/>
                </div>
              </div>
              
              <div class="d-flex align-items-center gap-2">
                <button class="btn btn-link text-on-surface-variant position-relative p-2" title="Notifications">
                  <span class="material-symbols-outlined">notifications</span>
                  <span class="position-absolute bg-danger rounded-circle" style="width: 8px; height: 8px; top: 6px; right: 6px; border: 2px solid var(--surface-container-lowest);"></span>
                </button>
                <button class="btn btn-link text-on-surface-variant p-2" title="Help">
                  <span class="material-symbols-outlined">help_outline</span>
                </button>
                <div class="vr mx-2 bg-outline-variant"></div>
                <button class="btn btn-light d-flex align-items-center gap-2 rounded-pill px-2 py-1 border-0" style="background: transparent;">
                  <img alt="User profile" class="rounded-circle border border-outline-variant shadow-sm" src="https://ui-avatars.com/api/?name=${user.fullName}&background=0D8ABC&color=fff" style="width: 32px; height: 32px; object-fit: cover;"/>
                  <span class="material-symbols-outlined text-on-surface-variant" style="font-size: 16px;">arrow_drop_down</span>
                </button>
              </div>
            </header>
            
            <div class="page-container d-flex flex-column" style="max-width: 900px; margin: 0 auto; width: 100%;">
                <div class="d-flex justify-content-between align-items-start mb-4">
                    <div>
                        <h1 class="h2 text-on-surface fw-bold">My Profile</h1>
                        <p class="body-md text-on-surface-variant mt-1">Manage your personal information and account security.</p>
                    </div>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/profile/edit" class="btn btn-outline-primary px-4 py-2 d-flex align-items-center gap-2 shadow-sm fw-bold text-decoration-none">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">edit</span>
                            Edit Profile
                        </a>
                        <a href="${pageContext.request.contextPath}/auth/change-password.html" class="btn-primary-gradient text-decoration-none px-4 py-2 d-flex align-items-center gap-2 shadow-sm text-white">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">lock_reset</span>
                            Change Password
                        </a>
                    </div>
                </div>
                
                <div class="card-premium p-4 p-md-5 mb-4 position-relative overflow-hidden d-flex flex-column flex-md-row align-items-center align-items-md-start gap-4">
                    <div class="position-absolute top-0 start-0 w-100" style="height: 4px; background-color: var(--primary);"></div>
                    
                    <div class="position-relative flex-shrink-0">
                        <img alt="${user.fullName}" class="rounded-circle object-fit-cover shadow-sm" src="https://ui-avatars.com/api/?name=${user.fullName}&size=112&background=0D8ABC&color=fff" style="width: 112px; height: 112px; border: 4px solid var(--surface);"/>
                        <div class="position-absolute bottom-0 end-0 rounded-circle d-flex align-items-center justify-content-center shadow-sm" style="width: 24px; height: 24px; background-color: var(--surface); border: 2px solid var(--surface-container-lowest);">
                            <div class="rounded-circle" style="width: 12px; height: 12px; background-color: ${user.isActive ? '#10b981' : '#ef4444'};"></div>
                        </div>
                    </div>
                    
                    <div class="flex-grow-1 text-center text-md-start">
                        <div class="d-flex flex-column flex-md-row align-items-center gap-3 mb-2">
                            <h2 class="h3 text-on-surface fw-bold mb-0">${user.fullName}</h2>
                            <c:choose>
                                <c:when test="${user.isActive == true}">
                                    <span class="badge border" style="background-color: #d1fae5; color: #065f46; border-color: #a7f3d0 !important;">Active Employee</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge border" style="background-color: #fee2e2; color: #991b1b; border-color: #fca5a5 !important;">Inactive</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <p class="body-lg text-on-surface-variant mb-3">
                            <c:out value="${not empty user.jobTitle ? user.jobTitle : 'N/A'}" />
                        </p>
                        
                        <div class="d-flex flex-wrap justify-content-center justify-content-md-start gap-3">
                            <div class="d-flex align-items-center gap-2 text-on-surface-variant">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">badge</span>
                                <span class="label-md font-monospace">${user.employeeCode}</span>
                            </div>
                            <div class="d-flex align-items-center gap-2 text-on-surface-variant">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">domain</span>
                                <span class="body-md">${user.departmentName}</span>
                            </div>
                            <div class="d-flex align-items-center gap-2 text-on-surface-variant">
                                <span class="material-symbols-outlined" style="font-size: 1.125rem;">badge_visibility</span>
                                <span class="body-md">${user.employeeType}</span>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="row g-4 mb-4">
                    <div class="col-md-6">
                        <div class="card-premium h-100 p-4">
                            <h3 class="label-md text-on-surface mb-4 pb-3 border-bottom border-outline-variant d-flex align-items-center gap-2 text-uppercase">
                                <span class="material-symbols-outlined text-primary" style="font-size: 1.125rem;">contact_mail</span>
                                Contact & Personal Info
                            </h3>
                            <div class="d-flex flex-column gap-3">
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Username</div>
                                    <div class="body-md text-on-surface fw-medium">${user.username}</div>
                                </div>
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Phone Number</div>
                                    <div class="body-md text-on-surface fw-medium">${not empty user.phone ? user.phone : 'N/A'}</div>
                                </div>
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Date of Birth</div>
                                    <div class="body-md text-on-surface fw-medium">
                                        <c:choose>
                                            <c:when test="${user.dob != null}">
                                                <fmt:formatDate value="${user.dob}" pattern="dd MMMM, yyyy"/>
                                            </c:when>
                                            <c:otherwise>N/A</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-6">
                        <div class="card-premium h-100 p-4">
                            <h3 class="label-md text-on-surface mb-4 pb-3 border-bottom border-outline-variant d-flex align-items-center gap-2 text-uppercase">
                                <span class="material-symbols-outlined text-primary" style="font-size: 1.125rem;">work</span>
                                Employment Details
                            </h3>
                            <div class="d-flex flex-column gap-3">
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Department</div>
                                    <div class="body-md text-on-surface fw-medium">${user.departmentName}</div>
                                </div>
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Direct Manager</div>
                                    <div class="d-flex align-items-center gap-2">
                                        <div class="rounded-circle bg-surface-container-high d-flex align-items-center justify-content-center text-on-surface fw-bold" style="width: 24px; height: 24px; font-size: 10px;">M</div>
                                        <div class="body-md text-on-surface fw-medium">${not empty user.managerName ? user.managerName : 'No Manager Assigned'}</div>
                                    </div>
                                </div>
                                <div>
                                    <div class="label-sm text-on-surface-variant text-uppercase mb-1">Joined Date</div>
                                    <div class="body-md text-on-surface fw-medium">
                                        <c:choose>
                                            <c:when test="${user.createdAt != null}">
                                                <fmt:formatDate value="${user.createdAt}" pattern="MMMM dd, yyyy"/>
                                            </c:when>
                                            <c:otherwise>N/A</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                
            </div>
            
            <footer class="w-100 py-3 border-top border-outline-variant bg-surface d-flex justify-content-between align-items-center px-4 mt-auto">
              <p class="label-md fw-bold text-on-surface mb-0" style="font-size: 13px;">© 2026 ManuHRM Industrial Solutions. All rights reserved.</p>
              <div class="d-flex gap-4">
                <a class="text-on-surface-variant text-decoration-none label-sm hover-primary" href="#">Privacy Policy</a>
                <a class="text-on-surface-variant text-decoration-none label-sm hover-primary" href="#">Compliance</a>
                <a class="text-on-surface-variant text-decoration-none label-sm hover-primary" href="#">System Status</a>
              </div>
            </footer>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>