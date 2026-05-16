package util;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {
    private static Dotenv dotenv;

    static {
        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            System.err.println("==========================================================================");
            System.err.println("WARNING: COULD NOT LOAD ENVIRONMENT VARIABLES!");
            System.err.println("Please check your '.env' file.");
            System.err.println("==========================================================================");
        }
    }

    public static String get(String key) {
        return dotenv != null ? dotenv.get(key) : null;
    }

    public static String get(String key, String defaultValue) {
        return dotenv != null ? dotenv.get(key, defaultValue) : defaultValue;
    }
}
