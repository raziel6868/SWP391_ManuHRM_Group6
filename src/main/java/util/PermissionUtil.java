package util;

import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * RBAC helper - Kiểm tra quyền theo hierarchy
 *
 * Hierarchy: SYSADMIN > HR_MANAGER > LINE_MANAGER > EMPLOYEE
 *
 * Ai có rank cao hơn có thể "bắn" (sửa/deactive) người có rank thấp hơn hoặc
 * cùng rank. Không ai được tự bắn mình.
 */
public class PermissionUtil {

	private static final int RANK_EMPLOYEE = 1;
	private static final int RANK_LINE_MANAGER = 2;
	private static final int RANK_HR_MANAGER = 3;
	private static final int RANK_SYSADMIN = 4;

	public static int getRoleRank(String roleName) {
		if (roleName == null)
			return 0;
		switch (roleName) {
			case "SYSADMIN" :
				return RANK_SYSADMIN;
			case "HR_MANAGER" :
				return RANK_HR_MANAGER;
			case "LINE_MANAGER" :
				return RANK_LINE_MANAGER;
			case "EMPLOYEE" :
				return RANK_EMPLOYEE;
			default :
				return 0;
		}
	}

	public static boolean canManageUser(HttpSession session, User targetUser) {
		if (session == null || targetUser == null)
			return false;
		User currentUser = (User) session.getAttribute("authUser");
		if (currentUser == null)
			return false;
		if (currentUser.getId().equals(targetUser.getId()))
			return false;
		return getRoleRank(currentUser.getRoleName()) >= getRoleRank(targetUser.getRoleName());
	}

}
