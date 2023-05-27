package org.broken.arrow.database.library.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogMsg {
	static Logger logger;

	static {
		logger = Logger.getLogger("Database.broken.arrow");
	}

	public static void info(String message) {
		logger.log(Level.INFO, message);
	}

	public static void warn(String message) {
		logger.log(Level.SEVERE, message);
	}

	public static void warn(String message, Exception exception) {
		logger.log(Level.SEVERE, message, exception);
	}

	public static void logMes(Level level, String message) {
		logger.log(level, message);
	}
}