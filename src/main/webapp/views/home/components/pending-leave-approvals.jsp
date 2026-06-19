<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:if test="${canApproveLeaveLevel1}">
    <article class="card-premium p-4 mb-4">
        <div class="d-flex align-items-center justify-content-between mb-3">
            <h2 class="h4 text-on-surface fw-bold mb-0">Don nghi cho duyet</h2>
            <span class="material-symbols-outlined text-on-surface-variant">approval</span>
        </div>

        <c:choose>
            <c:when test="${not empty pendingSupervisorLeaveRequests}">
                <div class="d-flex flex-column gap-3">
                    <c:forEach var="leaveRequest" items="${pendingSupervisorLeaveRequests}">
                        <div class="border border-outline-variant rounded-3 p-3 bg-surface">
                            <div class="d-flex justify-content-between align-items-start gap-3 mb-2">
                                <div>
                                    <div class="fw-semibold text-on-surface">
                                        <c:out value="${leaveRequest.employeeName}" />
                                    </div>
                                    <div class="body-sm text-on-surface-variant">
                                        <c:out value="${leaveRequest.employeeCode}" />
                                        <c:if test="${not empty leaveRequest.departmentName}">
                                            - <c:out value="${leaveRequest.departmentName}" />
                                        </c:if>
                                    </div>
                                </div>
                                <span class="badge" style="background-color: var(--primary-fixed); color: var(--on-primary-fixed-variant);">
                                    <c:out value="${leaveRequest.leaveTypeCode}" />
                                </span>
                            </div>

                            <div class="body-sm text-on-surface-variant mb-3">
                                <c:out value="${leaveRequest.startDate}" />
                                -
                                <c:out value="${leaveRequest.endDate}" />
                                -
                                <fmt:formatNumber value="${leaveRequest.days}" minFractionDigits="0" maxFractionDigits="2" />
                                ngay
                            </div>

                            <div class="d-flex gap-2 flex-wrap">
                                <form action="${pageContext.request.contextPath}/leave-request-approve" method="POST" class="d-inline">
                                    <input type="hidden" name="id" value="${leaveRequest.id}" />
                                    <input type="hidden" name="returnUrl" value="${pageContext.request.contextPath}/home" />
                                    <button type="submit"
                                            class="btn btn-sm btn-primary d-inline-flex align-items-center gap-1"
                                            onclick="return confirm('Xac nhan duyet cap 1 don nghi nay?');">
                                        <span class="material-symbols-outlined" style="font-size: 1rem;">task_alt</span>
                                        Duyet
                                    </button>
                                </form>

                                <c:if test="${canRejectLeaveRequest}">
                                    <form action="${pageContext.request.contextPath}/leave-request-reject" method="POST" class="d-inline">
                                        <input type="hidden" name="id" value="${leaveRequest.id}" />
                                        <input type="hidden" name="returnUrl" value="${pageContext.request.contextPath}/home" />
                                        <button type="submit"
                                                class="btn btn-sm btn-light border text-danger d-inline-flex align-items-center gap-1"
                                                onclick="return confirm('Xac nhan tu choi don nghi nay?');">
                                            <span class="material-symbols-outlined" style="font-size: 1rem;">block</span>
                                            Tu choi
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-on-surface-variant body-sm">
                    Khong co don nghi nao dang cho duyet.
                </div>
            </c:otherwise>
        </c:choose>
    </article>
</c:if>
