package org.broken.arrow.title.update.library.utility;


import java.util.logging.Level;
import java.util.logging.Logger;

public class TitleLogger {

	private final Logger LOG;

	public TitleLogger(final Class<?> logg) {
		this.LOG = Logger.getLogger(logg.getName());
	}

	public void sendLOG(Level level, String message) {
		LOG.log(level, message);
	}

	public void sendLOG(Exception exception, Level level, String message) {
		LOG.log(level, message, exception);
	}

	public Logger getLOG() {
		return LOG;
	}
}
