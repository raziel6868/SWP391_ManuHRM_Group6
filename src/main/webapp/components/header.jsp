<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="user" value="${sessionScope.authUser}" />

<header class="topbar">
    <div class="user-chip">
        <div class="avatar"><c:out value="${fn:substring(user.fullName, 0, 1)}" /></div>
        <div>
            <div class="user-name"><c:out value="${user.fullName}" /></div>
            <div class="user-role">
                <c:out value="${user.roleDisplayName}" /> · <c:out value="${user.jobTitle}" />
            </div>
        </div>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/logout">
        <button class="logout-button" type="submit">Đăng xuất</button>
    </form>
</header>
