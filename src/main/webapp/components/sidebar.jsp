<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="user" value="${sessionScope.authUser}" />
<c:set var="currentPath" value="${pageContext.request.servletPath}" />

<c:set var="hasUserView" value="${false}" />
<c:set var="hasRoleView" value="${false}" />
<c:set var="hasDepartmentView" value="${false}" />
<c:set var="hasJobTitleView" value="${false}" />
<c:set var="hasHolidayView" value="${false}" />

<c:set var="hasContractTypeView" value="${false}" />
<c:set var="hasContractView" value="${false}" />
<c:set var="hasContractMyView" value="${false}" />

<c:set var="hasLeaveTypeView" value="${false}" />
<c:set var="hasLeaveBalanceView" value="${false}" />
<c:set var="hasLeaveMyView" value="${false}" />
<c:set var="hasLeaveRequestView" value="${false}" />
<c:set var="hasLeaveRequestApproveL1" value="${false}" />

<c:set var="hasAttendanceView" value="${false}" />
<c:set var="hasAttendanceMyView" value="${false}" />
<c:set var="hasAttendanceCorrectionView" value="${false}" />
<c:set var="hasOtView" value="${false}" />
<c:set var="hasOtMyView" value="${false}" />

<c:set var="hasMonthlySheetView" value="${false}" />
<c:set var="hasMonthlySheetSupervisorView" value="${false}" />
<c:set var="hasSalaryBaseSetup" value="${false}" />
<c:set var="hasPayrollView" value="${false}" />
<c:set var="hasPayslipView" value="${false}" />
<c:set var="hasAllowanceTypeView" value="${false}" />
<c:set var="hasEmployeeAllowanceView" value="${false}" />
<c:set var="hasInsuranceRateView" value="${false}" />
<c:set var="hasPersonalTaxSettingView" value="${false}" />
<c:set var="hasPersonalTaxBracketView" value="${false}" />
<c:set var="hasEmployeeDependentView" value="${false}" />
<c:set var="hasPayrollSettingView" value="${false}" />

<c:set var="hasReportAttendance" value="${false}" />
<c:set var="hasReportLeave" value="${false}" />
<c:set var="hasReportHeadcount" value="${false}" />
<c:set var="hasReportContract" value="${false}" />
<c:set var="hasReportPayroll" value="${false}" />
<c:set var="hasReportOt" value="${false}" />

<c:forEach var="permission" items="${sessionScope.permissions}">
    <c:choose>
        <c:when test="${permission.code == 'USER_VIEW'}">
            <c:set var="hasUserView" value="${true}" />
            <c:set var="userViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'ROLE_VIEW'}">
            <c:set var="hasRoleView" value="${true}" />
            <c:set var="roleViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'DEPARTMENT_VIEW'}">
            <c:set var="hasDepartmentView" value="${true}" />
            <c:set var="departmentViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'JOB_TITLE_VIEW'}">
            <c:set var="hasJobTitleView" value="${true}" />
            <c:set var="jobTitleViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'HOLIDAY_VIEW'}">
            <c:set var="hasHolidayView" value="${true}" />
            <c:set var="holidayViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'CONTRACT_TYPE_VIEW'}">
            <c:set var="hasContractTypeView" value="${true}" />
            <c:set var="contractTypeViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'CONTRACT_VIEW'}">
            <c:set var="hasContractView" value="${true}" />
            <c:set var="contractViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'CONTRACT_MY_VIEW'}">
            <c:set var="hasContractMyView" value="${true}" />
            <c:set var="contractMyViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'LEAVE_TYPE_VIEW'}">
            <c:set var="hasLeaveTypeView" value="${true}" />
            <c:set var="leaveTypeViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'LEAVE_BALANCE_VIEW'}">
            <c:set var="hasLeaveBalanceView" value="${true}" />
            <c:set var="leaveBalanceViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'LEAVE_MY_VIEW'}">
            <c:set var="hasLeaveMyView" value="${true}" />
            <c:set var="leaveMyViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'LEAVE_REQUEST_VIEW'}">
            <c:set var="hasLeaveRequestView" value="${true}" />
            <c:set var="leaveRequestViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'LEAVE_REQUEST_APPROVE_L1'}">
            <c:set var="hasLeaveRequestApproveL1" value="${true}" />
        </c:when>
        <c:when test="${permission.code == 'ATTENDANCE_VIEW'}">
            <c:set var="hasAttendanceView" value="${true}" />
            <c:set var="attendanceViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'ATTENDANCE_MY_VIEW'}">
            <c:set var="hasAttendanceMyView" value="${true}" />
            <c:set var="attendanceMyViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'ATTENDANCE_CORRECTION_VIEW'}">
            <c:set var="hasAttendanceCorrectionView" value="${true}" />
            <c:set var="attendanceCorrectionViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'OT_VIEW'}">
            <c:set var="hasOtView" value="${true}" />
            <c:set var="otViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'OT_MY_VIEW'}">
            <c:set var="hasOtMyView" value="${true}" />
            <c:set var="otMyViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'MONTHLY_SHEET_VIEW'}">
            <c:set var="hasMonthlySheetView" value="${true}" />
            <c:set var="monthlySheetViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'MONTHLY_SHEET_SUPERVISOR_VIEW'}">
            <c:set var="hasMonthlySheetSupervisorView" value="${true}" />
            <c:set var="monthlySheetSupervisorViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'SALARY_BASE_SETUP'}">
            <c:set var="hasSalaryBaseSetup" value="${true}" />
            <c:set var="salaryBaseSetupUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'PAYROLL_VIEW'}">
            <c:set var="hasPayrollView" value="${true}" />
            <c:set var="payrollViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'PAYSLIP_VIEW'}">
            <c:set var="hasPayslipView" value="${true}" />
            <c:set var="payslipViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'ALLOWANCE_TYPE_VIEW'}">
            <c:set var="hasAllowanceTypeView" value="${true}" />
            <c:set var="allowanceTypeViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'EMPLOYEE_ALLOWANCE_VIEW'}">
            <c:set var="hasEmployeeAllowanceView" value="${true}" />
            <c:set var="employeeAllowanceViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'INSURANCE_RATE_VIEW'}">
            <c:set var="hasInsuranceRateView" value="${true}" />
            <c:set var="insuranceRateViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'PERSONAL_TAX_SETTING_VIEW'}">
            <c:set var="hasPersonalTaxSettingView" value="${true}" />
            <c:set var="personalTaxSettingViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'PERSONAL_TAX_BRACKET_VIEW'}">
            <c:set var="hasPersonalTaxBracketView" value="${true}" />
            <c:set var="personalTaxBracketViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'EMPLOYEE_DEPENDENT_VIEW'}">
            <c:set var="hasEmployeeDependentView" value="${true}" />
            <c:set var="employeeDependentViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'PAYROLL_SETTING_VIEW'}">
            <c:set var="hasPayrollSettingView" value="${true}" />
            <c:set var="payrollSettingViewUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'REPORT_ATTENDANCE'}">
            <c:set var="hasReportAttendance" value="${true}" />
            <c:set var="reportAttendanceUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'REPORT_LEAVE'}">
            <c:set var="hasReportLeave" value="${true}" />
            <c:set var="reportLeaveUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'REPORT_HEADCOUNT'}">
            <c:set var="hasReportHeadcount" value="${true}" />
            <c:set var="reportHeadcountUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'REPORT_CONTRACT'}">
            <c:set var="hasReportContract" value="${true}" />
            <c:set var="reportContractUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'REPORT_PAYROLL'}">
            <c:set var="hasReportPayroll" value="${true}" />
            <c:set var="reportPayrollUrl" value="${permission.urlPattern}" />
        </c:when>
        <c:when test="${permission.code == 'REPORT_OT'}">
            <c:set var="hasReportOt" value="${true}" />
            <c:set var="reportOtUrl" value="${permission.urlPattern}" />
        </c:when>
    </c:choose>
</c:forEach>

<c:set var="hasSystemMenu" value="${hasUserView or hasRoleView or hasDepartmentView or hasJobTitleView or hasHolidayView}" />
<c:set var="hasContractMenu" value="${hasContractTypeView or hasContractView}" />
<c:set var="hasLeaveMenu" value="${hasLeaveTypeView or hasLeaveBalanceView or hasLeaveMyView or hasLeaveRequestView or hasLeaveRequestApproveL1}" />
<c:set var="hasAttendanceMenu" value="${hasAttendanceView or hasAttendanceMyView or hasAttendanceCorrectionView or hasOtView or hasOtMyView}" /><c:set var="hasPayrollMenu" value="${hasMonthlySheetView or hasMonthlySheetSupervisorView or hasSalaryBaseSetup or hasPayrollView or hasPayslipView or hasAllowanceTypeView or hasEmployeeAllowanceView or hasInsuranceRateView or hasPersonalTaxSettingView or hasPersonalTaxBracketView or hasEmployeeDependentView or hasPayrollSettingView}" />
<c:set var="hasReportMenu" value="${hasReportAttendance or hasReportLeave or hasReportHeadcount or hasReportContract or hasReportPayroll or hasReportOt}" />
<c:set var="hasMenuPermission" value="${hasSystemMenu or hasContractMenu or hasLeaveMenu or hasAttendanceMenu or hasPayrollMenu or hasReportMenu}" />

<c:set var="systemMenuOpen" value="${currentPath == '/user-list' or currentPath == '/user-create' or currentPath == '/user-update' or currentPath == '/user-detail' or currentPath == '/role-list' or currentPath == '/role-create' or currentPath == '/role-update' or currentPath == '/role-permission' or currentPath == '/department-list' or currentPath == '/department-create' or currentPath == '/department-update' or currentPath == '/job-title-list' or currentPath == '/job-title-create' or currentPath == '/job-title-update' or currentPath == '/holiday-list' or currentPath == '/holiday-create' or currentPath == '/holiday-update'}" />
<c:set var="contractMenuOpen" value="${currentPath == '/contract-type-list' or currentPath == '/contract-type-create' or currentPath == '/contract-type-update' or currentPath == '/contract-list' or currentPath == '/my-contract' or currentPath == '/contract-create' or currentPath == '/contract-detail' or currentPath == '/contract-update' or currentPath == '/contract-upload' or currentPath == '/contract-renew' or currentPath == '/contract-terminate' or currentPath == '/contract-expiry'}" />
<c:set var="leaveMenuOpen" value="${currentPath == '/leave-type-list' or currentPath == '/leave-type-detail' or currentPath == '/leave-type-update' or currentPath == '/leave-balance-list' or currentPath == '/leave-balance-setup' or currentPath == '/leave-request-my' or currentPath == '/leave-request-create' or currentPath == '/leave-request-list'}" />
<c:set var="attendanceMenuOpen" value="${currentPath == '/attendance-list' or currentPath == '/attendance-import' or currentPath == '/attendance-my' or currentPath == '/attendance-correction-list' or currentPath == '/overtime-list' or currentPath == '/overtime-request'}" />
<c:set var="payrollMenuOpen" value="${currentPath == '/monthly-sheet-list' or currentPath == '/monthly-sheet-supervisor' or currentPath == '/salary-base-list' or currentPath == '/salary-base-setup' or currentPath == '/payroll-preview' or currentPath == '/payslip-view' or currentPath == '/allowance-type-list' or currentPath == '/allowance-type-create' or currentPath == '/allowance-type-update' or currentPath == '/employee-allowance-list' or currentPath == '/employee-allowance-setup' or currentPath == '/insurance-rate-list' or currentPath == '/insurance-rate-setup' or currentPath == '/personal-tax-setting-list' or currentPath == '/personal-tax-setting-setup' or currentPath == '/personal-tax-bracket-list' or currentPath == '/personal-tax-bracket-setup' or currentPath == '/employee-dependent-list' or currentPath == '/employee-dependent-setup' or currentPath == '/payroll-setting-list' or currentPath == '/payroll-setting-setup'}" />
<c:set var="reportMenuOpen" value="${currentPath == '/report-attendance' or currentPath == '/report-leave' or currentPath == '/report-headcount' or currentPath == '/report-contract' or currentPath == '/report-payroll' or currentPath == '/report-overtime'}" />

<aside class="sidebar">
    <div class="sidebar-header">
        <div class="d-flex align-items-center justify-content-center shadow-sm"
             style="width: 40px; height: 40px; border-radius: 8px; background: var(--primary-gradient);">
            <span class="material-symbols-outlined text-white" style="font-variation-settings: 'FILL' 1;">factory</span>
        </div>
        <div>
            <h1 class="h3 mb-0 text-primary fw-bolder" style="font-size: 20px;">ManuHRM</h1>
            <p class="label-sm text-muted mb-0 text-uppercase" style="font-size: 10px; letter-spacing: 0.05em;">
                Manufacturing Ops
            </p>
        </div>
    </div>

    <nav class="sidebar-menu">
        <c:url var="homeUrl" value="/home" />
        <a class="${currentPath == '/home' ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
           href="${homeUrl}">
            <span class="material-symbols-outlined">dashboard</span>
            <span>Bảng điều khiển</span>
        </a>

        <c:if test="${hasSystemMenu}">
            <button class="${systemMenuOpen ? 'sidebar-nav-item sidebar-parent active' : 'sidebar-nav-item sidebar-parent'}"
                    type="button" data-bs-toggle="collapse" data-bs-target="#systemMenu"
                    aria-expanded="${systemMenuOpen}" aria-controls="systemMenu">
                <span class="material-symbols-outlined">admin_panel_settings</span>
                <span>Quản trị hệ thống</span>
                <span class="material-symbols-outlined sidebar-chevron">expand_more</span>
            </button>
            <div id="systemMenu" class="collapse ${systemMenuOpen ? 'show' : ''}">
                <div class="sidebar-submenu">
                    <c:if test="${hasUserView}">
                        <c:url var="menuUrl" value="${userViewUrl}" />
                        <a class="${currentPath == userViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Nhân sự</a>
                    </c:if>
                    <c:if test="${hasRoleView}">
                        <c:url var="menuUrl" value="${roleViewUrl}" />
                        <a class="${currentPath == roleViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Vai trò</a>
                    </c:if>
                    <c:if test="${hasDepartmentView}">
                        <c:url var="menuUrl" value="${departmentViewUrl}" />
                        <a class="${currentPath == departmentViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Phòng ban</a>
                    </c:if>
                    <c:if test="${hasJobTitleView}">
                        <c:url var="menuUrl" value="${jobTitleViewUrl}" />
                        <a class="${currentPath == jobTitleViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Chức danh</a>
                    </c:if>
                    <c:if test="${hasHolidayView}">
                        <c:url var="menuUrl" value="${holidayViewUrl}" />
                        <a class="${currentPath == holidayViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Ngày lễ</a>
                    </c:if>
                </div>
            </div>
        </c:if>

        <c:if test="${hasContractMenu}">
            <button class="${contractMenuOpen ? 'sidebar-nav-item sidebar-parent active' : 'sidebar-nav-item sidebar-parent'}"
                    type="button" data-bs-toggle="collapse" data-bs-target="#contractMenu"
                    aria-expanded="${contractMenuOpen}" aria-controls="contractMenu">
                <span class="material-symbols-outlined">assignment</span>
                <span>Quản lý hợp đồng</span>
                <span class="material-symbols-outlined sidebar-chevron">expand_more</span>
            </button>
            <div id="contractMenu" class="collapse ${contractMenuOpen ? 'show' : ''}">
                <div class="sidebar-submenu">
                    <c:if test="${hasContractView}">
                        <c:url var="menuUrl" value="${contractViewUrl}" />
                        <a class="${currentPath == contractViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Hợp đồng</a>
                    </c:if>
                    <c:if test="${hasContractMyView}">
                        <c:url var="menuUrl" value="${contractMyViewUrl}" />
                        <a class="${currentPath == contractMyViewUrl or currentPath == '/contract-detail' ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Hợp đồng cá nhân</a>
                    </c:if>
                    <c:if test="${hasContractTypeView}">
                        <c:url var="menuUrl" value="${contractTypeViewUrl}" />
                        <a class="${currentPath == contractTypeViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Loại hợp đồng</a>
                    </c:if>
                </div>
            </div>
        </c:if>

        <c:if test="${hasLeaveMenu}">
            <button class="${leaveMenuOpen ? 'sidebar-nav-item sidebar-parent active' : 'sidebar-nav-item sidebar-parent'}"
                    type="button" data-bs-toggle="collapse" data-bs-target="#leaveMenu"
                    aria-expanded="${leaveMenuOpen}" aria-controls="leaveMenu">
                <span class="material-symbols-outlined">event_available</span>
                <span>Quản lý nghỉ phép</span>
                <span class="material-symbols-outlined sidebar-chevron">expand_more</span>
            </button>
            <div id="leaveMenu" class="collapse ${leaveMenuOpen ? 'show' : ''}">
                <div class="sidebar-submenu">
                    <c:if test="${hasLeaveMyView}">
                        <c:url var="menuUrl" value="${leaveMyViewUrl}" />
                        <a class="${currentPath == leaveMyViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Yêu cầu nghỉ phép</a>
                    </c:if>
                    <c:if test="${hasLeaveRequestView}">
                        <c:url var="menuUrl" value="${leaveRequestViewUrl}" />
                        <a class="${currentPath == leaveRequestViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Duyệt nghỉ phép</a>
                    </c:if>
                    <c:if test="${hasLeaveRequestApproveL1 and not hasLeaveRequestView}">
                        <c:url var="menuUrl" value="/leave-request-list" />
                        <a class="${currentPath == menuUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Duyệt nghỉ phép</a>
                    </c:if>
                    <c:if test="${hasLeaveBalanceView}">
                        <c:url var="menuUrl" value="${leaveBalanceViewUrl}" />
                        <a class="${currentPath == leaveBalanceViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Hạn mức phép năm</a>
                    </c:if>
                    <c:if test="${hasLeaveTypeView}">
                        <c:url var="menuUrl" value="${leaveTypeViewUrl}" />
                        <a class="${currentPath == leaveTypeViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Loại nghỉ phép</a>
                    </c:if>
                </div>
            </div>
        </c:if>

        <c:if test="${hasAttendanceMenu}">
            <button class="${attendanceMenuOpen ? 'sidebar-nav-item sidebar-parent active' : 'sidebar-nav-item sidebar-parent'}"
                    type="button" data-bs-toggle="collapse" data-bs-target="#attendanceMenu"
                    aria-expanded="${attendanceMenuOpen}" aria-controls="attendanceMenu">
                <span class="material-symbols-outlined">access_time</span>
                <span>Chấm công & OT</span>
                <span class="material-symbols-outlined sidebar-chevron">expand_more</span>
            </button>
            <div id="attendanceMenu" class="collapse ${attendanceMenuOpen ? 'show' : ''}">
                <div class="sidebar-submenu">
                    <c:if test="${hasAttendanceMyView}">
                        <c:url var="menuUrl" value="${attendanceMyViewUrl}" />
                        <a class="${currentPath == attendanceMyViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Chấm công của tôi</a>
                    </c:if>
                    <c:if test="${hasAttendanceView}">
                        <c:url var="menuUrl" value="${attendanceViewUrl}" />
                        <a class="${currentPath == attendanceViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Quản lý chấm công</a>
                    </c:if>
                    <c:if test="${hasAttendanceCorrectionView}">
                        <c:url var="menuUrl" value="${attendanceCorrectionViewUrl}" />
                        <a class="${currentPath == attendanceCorrectionViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Duyệt sửa công</a>
                    </c:if>
                    <c:if test="${hasOtView}">
                        <c:url var="menuUrl" value="${otViewUrl}" />
                        <a class="${currentPath == otViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Tăng ca</a>
                    </c:if>
                    <c:if test="${hasOtMyView}">
                        <c:url var="menuUrl" value="${otMyViewUrl}" />
                        <a class="${currentPath == otMyViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Tăng ca của tôi</a>
                    </c:if>
                </div>
            </div>
        </c:if>

        <c:if test="${hasPayrollMenu}">
            <button class="${payrollMenuOpen ? 'sidebar-nav-item sidebar-parent active' : 'sidebar-nav-item sidebar-parent'}"
                    type="button" data-bs-toggle="collapse" data-bs-target="#payrollMenu"
                    aria-expanded="${payrollMenuOpen}" aria-controls="payrollMenu">
                <span class="material-symbols-outlined">payments</span>
                <span>Lương</span>
                <span class="material-symbols-outlined sidebar-chevron">expand_more</span>
            </button>
            <div id="payrollMenu" class="collapse ${payrollMenuOpen ? 'show' : ''}">
                <div class="sidebar-submenu">
                    <c:if test="${hasMonthlySheetView}">
                        <c:url var="menuUrl" value="${monthlySheetViewUrl}" />
                        <a class="${currentPath == monthlySheetViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Bảng công tháng</a>
                    </c:if>
                    <c:if test="${not hasMonthlySheetView and hasMonthlySheetSupervisorView}">
                        <c:url var="menuUrl" value="${monthlySheetSupervisorViewUrl}" />
                        <a class="${currentPath == monthlySheetSupervisorViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Bảng công tháng</a>
                    </c:if>
                    <c:if test="${hasSalaryBaseSetup}">
                        <c:url var="menuUrl" value="${salaryBaseSetupUrl}" />
                        <a class="${currentPath == salaryBaseSetupUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Thiết lập lương</a>
                    </c:if>
                    <c:if test="${hasAllowanceTypeView}">
                        <c:url var="menuUrl" value="${allowanceTypeViewUrl}" />
                        <a class="${currentPath == allowanceTypeViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Loại phụ cấp</a>
                    </c:if>
                    <c:if test="${hasEmployeeAllowanceView}">
                        <c:url var="menuUrl" value="${employeeAllowanceViewUrl}" />
                        <a class="${currentPath == employeeAllowanceViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Phụ cấp nhân viên</a>
                    </c:if>
                    <c:if test="${hasInsuranceRateView and false}">
                        <c:url var="menuUrl" value="${insuranceRateViewUrl}" />
                        <a class="${currentPath == insuranceRateViewUrl or currentPath == '/insurance-rate-setup' ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Má»©c Ä‘Ã³ng báº£o hiá»ƒm</a>
                    </c:if>
                    <c:if test="${hasInsuranceRateView}">
                        <c:url var="menuUrl" value="${insuranceRateViewUrl}" />
                        <a class="${currentPath == insuranceRateViewUrl or currentPath == '/insurance-rate-setup' ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Mức đóng bảo hiểm</a>
                    </c:if>
                    <c:if test="${hasPersonalTaxSettingView}">
                        <c:url var="menuUrl" value="${personalTaxSettingViewUrl}" />
                        <a class="${currentPath == personalTaxSettingViewUrl or currentPath == '/personal-tax-setting-setup' ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Thiết lập giảm trừ</a>
                    </c:if>
                    <c:if test="${hasPersonalTaxBracketView}">
                        <c:url var="menuUrl" value="${personalTaxBracketViewUrl}" />
                        <a class="${currentPath == personalTaxBracketViewUrl or currentPath == '/personal-tax-bracket-setup' ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Biểu thuế lũy tiến</a>
                    </c:if>
                    <c:if test="${hasEmployeeDependentView}">
                        <c:url var="menuUrl" value="${employeeDependentViewUrl}" />
                        <a class="${currentPath == employeeDependentViewUrl or currentPath == '/employee-dependent-setup' ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Người phụ thuộc</a>
                    </c:if>
                    <c:if test="${hasPayrollView}">
                        <c:url var="menuUrl" value="${payrollViewUrl}" />
                        <a class="${currentPath == payrollViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Bảng lương</a>
                    </c:if>
                    <c:if test="${hasPayslipView}">
                        <c:url var="menuUrl" value="${payslipViewUrl}" />
                        <a class="${currentPath == payslipViewUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Phiếu lương</a>
                    </c:if>
                    <c:if test="${hasPayrollSettingView}">
                        <c:url var="menuUrl" value="${payrollSettingViewUrl}" />
                        <a class="${currentPath == payrollSettingViewUrl or currentPath == '/payroll-setting-setup' ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Cấu hình payroll</a>
                    </c:if>
                </div>
            </div>
        </c:if>

        <c:if test="${hasReportMenu}">
            <button class="${reportMenuOpen ? 'sidebar-nav-item sidebar-parent active' : 'sidebar-nav-item sidebar-parent'}"
                    type="button" data-bs-toggle="collapse" data-bs-target="#reportMenu"
                    aria-expanded="${reportMenuOpen}" aria-controls="reportMenu">
                <span class="material-symbols-outlined">bar_chart</span>
                <span>Báo cáo</span>
                <span class="material-symbols-outlined sidebar-chevron">expand_more</span>
            </button>
            <div id="reportMenu" class="collapse ${reportMenuOpen ? 'show' : ''}">
                <div class="sidebar-submenu">
                    <c:if test="${hasReportAttendance}">
                        <c:url var="menuUrl" value="${reportAttendanceUrl}" />
                        <a class="${currentPath == reportAttendanceUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Chấm công</a>
                    </c:if>
                    <c:if test="${hasReportLeave}">
                        <c:url var="menuUrl" value="${reportLeaveUrl}" />
                        <a class="${currentPath == reportLeaveUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Nghỉ phép</a>
                    </c:if>
                    <c:if test="${hasReportHeadcount}">
                        <c:url var="menuUrl" value="${reportHeadcountUrl}" />
                        <a class="${currentPath == reportHeadcountUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Nhân sự</a>
                    </c:if>
                    <c:if test="${hasReportContract}">
                        <c:url var="menuUrl" value="${reportContractUrl}" />
                        <a class="${currentPath == reportContractUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Hợp đồng</a>
                    </c:if>
                    <c:if test="${hasReportPayroll}">
                        <c:url var="menuUrl" value="${reportPayrollUrl}" />
                        <a class="${currentPath == reportPayrollUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Lương</a>
                    </c:if>
                    <c:if test="${hasReportOt}">
                        <c:url var="menuUrl" value="${reportOtUrl}" />
                        <a class="${currentPath == reportOtUrl ? 'sidebar-subitem active' : 'sidebar-subitem'}" href="${menuUrl}">Tăng ca</a>
                    </c:if>
                </div>
            </div>
        </c:if>

        <hr class="my-3 border-secondary opacity-25">

        <c:url var="profileUrl" value="/profile" />
        <a class="${currentPath == '/profile' ? 'sidebar-nav-item active text-decoration-none' : 'sidebar-nav-item text-decoration-none'}"
           href="${profileUrl}">
            <span class="material-symbols-outlined">account_circle</span>
            <span>Hồ sơ của tôi</span>
        </a>

        <c:if test="${not hasMenuPermission}">
            <div class="text-on-surface-variant body-sm px-3 py-2">
                Role hiện tại chưa có quyền menu nghiệp vụ.
            </div>
        </c:if>
    </nav>

    <div class="sidebar-footer">
        <form method="post" action="${pageContext.request.contextPath}/logout" class="mb-3">
            <button type="submit" class="sidebar-nav-item text-danger text-decoration-none border-0 bg-transparent w-100">
                <span class="material-symbols-outlined">logout</span>
                <span>Đăng xuất</span>
            </button>
        </form>

        <div class="d-flex align-items-center gap-3 px-2">
            <div class="rounded-circle d-flex align-items-center justify-content-center text-white fw-bold"
                 style="width: 40px; height: 40px; background: var(--primary-gradient); border: 2px solid var(--primary-fixed-dim);">
                <c:out value="${fn:substring(user.fullName, 0, 1)}" />
            </div>
            <div>
                <p class="label-sm fw-bold mb-0 text-on-surface"><c:out value="${user.fullName}" /></p>
                <p class="label-sm text-muted mb-0" style="font-size: 11px;">
                    <c:out value="${user.roleDisplayName}" />
                </p>
            </div>
        </div>
    </div>
</aside>
