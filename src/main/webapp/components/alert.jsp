<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty error}">
    <div class="alert alert-error"><c:out value="${error}" /></div>
</c:if>

<c:if test="${not empty success}">
    <div class="alert alert-success"><c:out value="${success}" /></div>
</c:if>

<c:if test="${param.loggedOut == '1'}">
    <div class="alert alert-success">Bạn đã đăng xuất khỏi hệ thống.</div>
</c:if>