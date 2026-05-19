<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Permission Matrix - ManuHRM</title>
        <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet"/>
    <style>
        .custom-checkbox {
            appearance: none;
            background-color: var(--surface-container-lowest);
            margin: 0;
            font: inherit;
            color: currentColor;
            width: 1.15em;
            height: 1.15em;
            border: 2px solid var(--outline-variant);
            border-radius: 4px;
            display: grid;
            place-content: center;
            cursor: pointer;
        }

        .custom-checkbox::before {
            content: "";
            width: 0.65em;
            height: 0.65em;
            transform: scale(0);
            transition: 120ms transform ease-in-out;
            box-shadow: inset 1em 1em white;
            background-color: white;
            transform-origin: center;
            clip-path: polygon(14% 44%, 0 65%, 50% 100%, 100% 16%, 80% 0%, 43% 62%);
        }

        .custom-checkbox:checked {
            background-color: var(--primary);
            border-color: var(--primary);
        }

        .custom-checkbox:checked::before {
            transform: scale(1);
        }
        
        /* Sticky header and first column for table */
        .table-sticky-header th {
            position: sticky;
            top: 0;
            background-color: var(--surface);
            z-index: 10;
        }
        .table-sticky-col {
            position: sticky;
            left: 0;
            background-color: var(--surface-container-lowest);
            z-index: 5;
            box-shadow: inset -1px 0 0 0 var(--outline-variant);
        }
        .table-premium tr:hover .table-sticky-col {
            background-color: var(--surface-container-low);
        }
        .module-header .table-sticky-col {
            background-color: var(--surface-container-low);
        }
    </style>
    <!-- Google Fonts Preconnect & Load -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet">
</head>
<body class="bg-background text-on-surface">

    <div class="layout-wrapper">
        <jsp:include page="/components/sidebar.jsp" />
        
        <div class="main-content d-flex flex-column">
            <jsp:include page="/components/header.jsp" />
            
            <form action="${pageContext.request.contextPath}/role-permission" method="POST" class="page-container flex-grow-1 d-flex flex-column m-0 w-100" style="max-width: 1200px; margin: 0 auto; padding-bottom: 80px;">
                <input type="hidden" name="id" value="${role.id}" />
                
                <!-- Page Header -->
                <header class="mb-4 d-flex justify-content-between align-items-end">
                    <div>
                        <h1 class="h2 text-on-surface fw-bold mb-2">Permission Matrix: ${role.displayName}</h1>
                        <p class="body-md text-on-surface-variant mb-0">Manage role-based access control for this role.</p>
                    </div>
                </header>
                
                <div class="card-premium overflow-hidden flex-grow-1 d-flex flex-column position-relative">
                    <div class="table-responsive flex-grow-1" style="max-height: calc(100vh - 280px); overflow-y: auto;">
                        <table class="table table-premium table-sticky-header mb-0 w-100" style="border-collapse: separate; border-spacing: 0;">
                            <thead>
                                <tr>
                                    <th class="table-sticky-col text-uppercase" style="width: 250px;">Module / Permission</th>
                                    <th class="text-center text-uppercase" style="min-width: 120px;">Granted</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:set var="currentModule" value="" />
                                <c:forEach var="p" items="${allPermissions}">
                                    <c:if test="${p.module != currentModule}">
                                        <tr class="module-header" style="background-color: var(--surface-container-low);">
                                            <td class="table-sticky-col text-primary fw-bold" colspan="2" style="border-top: 1px solid var(--outline-variant);">
                                                ${p.module}
                                            </td>
                                        </tr>
                                        <c:set var="currentModule" value="${p.module}" />
                                    </c:if>
                                    <tr>
                                        <td class="table-sticky-col ps-4">${p.name}</td>
                                        <td class="text-center">
                                            <input type="checkbox" name="permissionIds" value="${p.id}" class="custom-checkbox mx-auto"
                                                   <c:if test="${assignedPermissionIds.contains(p.id)}">checked</c:if> >
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <!-- Sticky Action Bar -->
                <div class="position-fixed bottom-0 end-0 bg-surface border-top border-outline-variant p-3 d-flex justify-content-end shadow-sm" style="width: calc(100% - 280px); z-index: 30;">
                    <div class="d-flex gap-3">
                        <a href="${pageContext.request.contextPath}/role-list" class="btn btn-light px-4 py-2 border text-on-surface-variant fw-bold" style="background-color: var(--surface-container-lowest); border-color: var(--outline-variant) !important;">
                            Discard Changes
                        </a>
                        <button type="submit" class="btn-primary-gradient px-4 py-2 d-flex align-items-center gap-2 shadow-sm">
                            <span class="material-symbols-outlined" style="font-size: 1.125rem;">save</span>
                            Save Matrix
                        </button>
                    </div>
                </div>
            </form>
            
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>


