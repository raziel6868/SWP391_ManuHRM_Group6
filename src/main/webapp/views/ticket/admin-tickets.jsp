<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <title>Manage Password Tickets - ManuHRM</title>
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

                <div class="page-container p-4" style="max-width: 1100px; margin: 0 auto; width: 100%;">
                    <div class="mb-4">
                        <h1 class="h2 text-on-surface fw-bold">Password Reset Tickets</h1>
                        <p class="body-md text-muted">Review, approve, or reject password recovery requests from staff members.</p>
                    </div>

                    <c:if test="${not empty success}">
                        <div class="alert alert-success border-0 shadow-sm mb-4">${success}</div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger border-0 shadow-sm mb-4">${error}</div>
                    </c:if>

                    <div class="card-premium p-4">
                        <div class="table-responsive">
                            <table class="table align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>Ticket ID</th>
                                        <th>Employee Code</th>
                                        <th>Employee Name</th>
                                        <th>Requested Time</th>
                                        <th>Status</th>
                                        <th class="text-center" style="width: 220px;">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty ticketList}">
                                            <tr>
                                                <td colspan="6" class="text-center py-4 text-muted">No manageable password reset requests found.</td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="ticket" items="${ticketList}">
                                                <tr>
                                                    <td class="fw-bold">#${ticket.id}</td>
                                                    <td><span class="badge bg-secondary font-monospace">${ticket.employeeCode}</span></td>
                                                    <td class="fw-medium">${ticket.fullName}</td>
                                                    <td><fmt:formatDate value="${ticket.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${ticket.status == 'PENDING'}">
                                                                <span class="badge bg-warning text-dark">PENDING</span>
                                                            </c:when>
                                                            <c:when test="${ticket.status == 'REJECTED'}">
                                                                <span class="badge bg-danger">REJECTED</span>
                                                            </c:when>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-center">
                                                        <div class="d-flex justify-content-center gap-2">
                                                            <c:choose>
                                                                <%-- NẾU LÀ PENDING: HIỂN THỊ CẢ NÚT APPROVE VÀ REJECT --%>
                                                                <c:when test="${ticket.status == 'PENDING'}">
                                                                    <form action="${pageContext.request.contextPath}/admin/tickets" method="POST" style="margin:0;">
                                                                        <input type="hidden" name="ticketId" value="${ticket.id}">
                                                                        <input type="hidden" name="action" value="APPROVE">
                                                                        <button type="submit" class="btn btn-success btn-sm px-2 fw-bold border-0" style="background-color: #10b981;">
                                                                            Approve
                                                                        </button>
                                                                    </form>
                                                                    <form action="${pageContext.request.contextPath}/admin/tickets" method="POST" style="margin:0;">
                                                                        <input type="hidden" name="ticketId" value="${ticket.id}">
                                                                        <input type="hidden" name="action" value="REJECT">
                                                                        <button type="submit" class="btn btn-outline-danger btn-sm px-2 fw-bold">
                                                                            Reject
                                                                        </button>
                                                                    </form>
                                                                </c:when>

                                                                <%-- NẾU LÀ REJECTED: CHỈ HIỂN THỊ NÚT RE-APPROVE --%>
                                                                <c:when test="${ticket.status == 'REJECTED'}">
                                                                    <form action="${pageContext.request.contextPath}/admin/tickets" method="POST" style="margin:0; width: 100%;">
                                                                        <input type="hidden" name="ticketId" value="${ticket.id}">
                                                                        <input type="hidden" name="action" value="APPROVE">
                                                                        <button type="submit" class="btn btn-primary btn-sm w-100 fw-bold border-0" style="background-color: #3b82f6;">
                                                                            <span class="material-symbols-outlined align-middle fs-6">refresh</span> Re-approve
                                                                        </button>
                                                                    </form>
                                                                </c:when>
                                                            </c:choose>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
