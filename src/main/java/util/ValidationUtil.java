package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private ValidationUtil() {}


    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean hasMinLength(String value, int minLength) {
        if (isBlank(value)) return false;
        return value.trim().length() >= minLength;
    }

    public static boolean matchRegex(String value, String regexPattern) {
        if (isBlank(value) || isBlank(regexPattern)) return false;
        return Pattern.compile(regexPattern).matcher(value.trim()).matches();
    }


    public static int getIntOrDefault(String value, int defaultValue) {
        if (isBlank(value)) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double getDoubleOrDefault(String value, double defaultValue) {
        if (isBlank(value)) return defaultValue;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean isBetween(int value, int min, int max) {
        return value >= min && value <= max;
    }


    public static Date parseDate(String value, String format) {
        if (isBlank(value)) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        try {
            return dateFormat.parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }
}