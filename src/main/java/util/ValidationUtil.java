package util;

import java.util.regex.Pattern;

public final class ValidationUtil {

	private ValidationUtil() {
	}

	public static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	public static boolean hasMinLength(String value, int minLength) {
		if (isBlank(value))
			return false;
		return value.trim().length() >= minLength;
	}

	public static boolean matchRegex(String value, String regexPattern) {
		if (isBlank(value) || isBlank(regexPattern))
			return false;
		return Pattern.compile(regexPattern).matcher(value.trim()).matches();
	}
}
