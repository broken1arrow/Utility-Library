package org.broken.arrow.logging.library;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Logging {

	private final Logger log;
	private static final Builder logBuilder = new Builder();

	public Logging(@Nonnull Class<?> clazz) {
		log = Logger.getLogger(clazz.getName());
	}

	/**
	 * will as default only send the message under info tag.
	 * @param msg the message builder.
	 */
	public void log(Supplier<Builder> msg) {
		this.log(Level.INFO, null, msg);
	}

	public void log(Exception exception, Supplier<Builder> msg) {
		this.log(Level.WARNING, exception, msg);
	}

	public void log(Level level, Supplier<Builder> msg) {
		this.log(level, null, msg);
	}

	public void log(Level level, Exception exception, Supplier<Builder> msg) {
		Builder logMessageBuilder = msg.get();
		if (level != null) {
			if (exception != null) log.log(level, logMessageBuilder.getMessage(), exception);

			else log.log(level, logMessageBuilder.getMessage());
		}
		logMessageBuilder.reset();
	}
	public void warn(Supplier<Builder> msg) {
		this. log(Level.WARNING, null,  msg);
	}

	public void logError(Throwable exception, Supplier<Builder> msg) {
		if (msg == null)
			msg = Builder::new;

		Builder logMessageBuilder = msg.get();
		log.log(Level.WARNING , exception , logMessageBuilder::getMessage);
	}

	public static Builder of(final String msg) {
		return logBuilder.setMessage(msg);
	}
	public static Builder of(final String msg, Map<String,String> placeholders) {
		return logBuilder.setMessage(msg).setPlaceholders(placeholders);
	}

	public static final class Builder {
		private String message;
		private String msgCopy;
		private Map<String,String> placeholders;

		private Builder() {
		}

		private Builder setMessage(final String msg) {
			this.message = msg;
			return this;
		}

		private Builder setPlaceholders(final Map<String,String> placeholders) {
			this.placeholders = placeholders;
			return this;
		}

		public String getMessage() {
			if (placeholders == null) {
				return message;
			}
			return setPlaceholders();
		}

		private String setPlaceholders() {
			if (placeholders == null) {
				return message;
			}
			placeholders.forEach((placeholderKey,value) ->
					msgCopy = message.replace(placeholderKey, value != null ? value : ""));
			return msgCopy;
		}

		private void reset() {
			message = null;
			placeholders = null;
		}
	}
}
