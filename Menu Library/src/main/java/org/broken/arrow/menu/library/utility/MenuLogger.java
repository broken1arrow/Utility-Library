package org.brokenarrow.menu.library.utility;


import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuLogger {

	private final Logger LOG;

	public MenuLogger(final Class<?> logg) {
		this.LOG = Logger.getLogger(logg.getName());
	}

	public void sendLOG(Level level, String message) {
		LOG.log(level, message);
	}

	public Logger getLOG() {
		return LOG;
	}
}
