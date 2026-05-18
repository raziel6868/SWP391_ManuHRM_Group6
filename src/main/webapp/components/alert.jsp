<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty error}">
    <div class="alert alert-danger d-flex align-items-start gap-2" role="alert">
        <span class="material-symbols-outlined" style="font-size: 1.25rem;">error</span>
        <span><c:out value="${error}" /></span>
    </div>
</c:if>

<c:if test="${not empty success}">
    <div class="alert alert-success d-flex align-items-start gap-2" role="alert">
        <span class="material-symbols-outlined" style="font-size: 1.25rem;">check_circle</span>
        <span><c:out value="${success}" /></span>
    </div>
</c:if>

<c:if test="${param.loggedOut == '1'}">
    <div class="alert alert-success d-flex align-items-start gap-2" role="alert">
        <span class="material-symbols-outlined" style="font-size: 1.25rem;">logout</span>
        <span>Bạn đã đăng xuất khỏi hệ thống.</span>
    </div>
</c:if>
