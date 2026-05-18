package util;

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppConfig {
	private static Dotenv dotenv;

	static {
		try {
			Path envDirectory = null;
			Path current = Paths.get(AppConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			if (Files.isRegularFile(current)) {
				current = current.getParent();
			}

			for (int i = 0; current != null && i < 10; i++) {
				if (Files.exists(current.resolve(".env"))) {
					envDirectory = current;
					break;
				}
				current = current.getParent();
			}

			if (envDirectory != null) {
				dotenv = Dotenv.configure().directory(envDirectory.toString()).ignoreIfMissing().load();
			} else {
				dotenv = Dotenv.configure().ignoreIfMissing().load();
			}
		} catch (Exception e) {
			System.err.println("==========================================================================");
			System.err.println("WARNING: COULD NOT LOAD ENVIRONMENT VARIABLES!");
			System.err.println("Please check your '.env' file.");
			System.err.println("==========================================================================");
			e.printStackTrace();
		}
	}

	public static String get(String key) {
		return dotenv != null ? dotenv.get(key) : null;
	}

	public static String get(String key, String defaultValue) {
		return dotenv != null ? dotenv.get(key, defaultValue) : defaultValue;
	}
}
