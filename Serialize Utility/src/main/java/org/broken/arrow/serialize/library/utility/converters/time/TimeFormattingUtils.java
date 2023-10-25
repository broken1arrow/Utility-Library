package org.broken.arrow.serialize.library.utility.converters.time;

/**
 * Utility class for converting timeFormatProvider values into formatted timeFormatProvider strings using a {@link TimeFormatProvider} instance.
 */
public class TimeFormattingUtils {

	private static final long MILLIS_TO_SECONDS = 1000;
	private static final long MILLIS_TO_MINUTE = MILLIS_TO_SECONDS * 60;
	private static final long MILLIS_TO_HOURS = MILLIS_TO_MINUTE * 60;
	private static final long MILLIS_TO_DAYS = MILLIS_TO_HOURS * 24;
	private final TimeFormatProvider timeFormatProvider;

	/**
	 * Constructs a {@code TimeFormattingUtils} instance with the given {@link TimeFormatProvider} timeFormatProvider utility.
	 *
	 * @param timeFormatProvider the converter utility to use for timeFormatProvider conversion.
	 */
	public TimeFormattingUtils(final TimeFormatProvider timeFormatProvider) {
		this.timeFormatProvider = timeFormatProvider;
	}

	/**
	 * Converts the given timeFormatProvider in milliseconds to a formatted timeFormatProvider string using the configured timeFormatProvider utility.
	 *
	 * @param milliseconds the timeFormatProvider in milliseconds to convert.
	 * @return a formatted timeFormatProvider string representing the given timeFormatProvider.
	 */
	public String toTimeFromMillis(long milliseconds) {
		return toTime(milliseconds / 1000);
	}

	/**
	 * Converts the given timeFormatProvider in seconds to a formatted timeFormatProvider string using the configured timeFormatProvider utility.
	 *
	 * @param seconds the timeFormatProvider in seconds to convert.
	 * @return a formatted timeFormatProvider string representing the given timeFormatProvider.
	 */
	public String toTime(long seconds) {
		if (this.timeFormatProvider == null) return "";

		long time = System.currentTimeMillis() + (1000 * seconds);
		long currentTime = System.currentTimeMillis();
		long second = 0;
		long min = 0;
		long hours;
		long days;
		if (!((time - currentTime) / MILLIS_TO_SECONDS % 60 == 0))
			second = (time - currentTime) / MILLIS_TO_SECONDS % 60;
		if (!((time - currentTime) / MILLIS_TO_MINUTE % 60 == 0))
			min = (time - currentTime) / MILLIS_TO_MINUTE % 60;
		hours = (time - currentTime) / MILLIS_TO_HOURS % 24;
		days = (time - currentTime) / MILLIS_TO_DAYS;
		if (min < 0)
			min = 0;
		if (hours < 0)
			hours = 0;
		if (days < 0)
			days = 0;

		return this.timeFormatProvider.convertTime(days, hours, min, second);
	}

}