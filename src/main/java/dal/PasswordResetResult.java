package dal;

public class PasswordResetResult {
	private final boolean success;
	private final String message;
	private final String newPassword;

	public PasswordResetResult(boolean success, String message) {
		this(success, message, null);
	}

	public PasswordResetResult(boolean success, String message, String newPassword) {
		this.success = success;
		this.message = message;
		this.newPassword = newPassword;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public String getNewPassword() {
		return newPassword;
	}
}
