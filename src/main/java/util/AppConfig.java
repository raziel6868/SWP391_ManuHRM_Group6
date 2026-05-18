package util;

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppConfig {
	private static Dotenv dotenv;

	static {
		try {
			Path envDirectory = findEnvDirectory();

			if (envDirectory != null) {
				dotenv = Dotenv.configure().directory(envDirectory.toString()).ignoreIfMissing().load();
			} else {
				dotenv = Dotenv.configure().ignoreIfMissing().load();
			}
		} catch (Exception e) {
			System.err.println("WARNING: COULD NOT LOAD ENVIRONMENT VARIABLES!");
			e.printStackTrace();
		}
	}

	public static String get(String key) {
		return dotenv != null ? dotenv.get(key) : null;
	}

	public static String get(String key, String defaultValue) {
		return dotenv != null ? dotenv.get(key, defaultValue) : defaultValue;
	}

	private static Path findEnvDirectory() throws Exception {
		Path current = Paths.get(AppConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		if (Files.isRegularFile(current)) {
			current = current.getParent();
		}

		for (int i = 0; current != null && i < 10; i++) {
			if (Files.exists(current.resolve(".env"))) {
				return current;
			}
			current = current.getParent();
		}

		return null;
	}
}
