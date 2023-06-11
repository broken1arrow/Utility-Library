package org.broken.arrow.serialize.library.utility.converters.time;

/**
 * Utility class for converting timeFormatter values into formatted timeFormatter strings using a {@link TimeFormatter} instance.
 */
public class TimeFormatterUtil {

	private static final long millisToSeconds = 1000;
	private static final long millisToMinute = millisToSeconds * 60;
	private static final long millisTohours = millisToMinute * 60;
	private static final long millisToDays = millisTohours * 24;
	private final TimeFormatter timeFormatter;

	/**
	 * Constructs a {@code TimeFormatterUtil} instance with the given {@link TimeFormatter} timeFormatter utility.
	 *
	 * @param timeFormatter the converter utility to use for timeFormatter conversion.
	 */
	public TimeFormatterUtil(final TimeFormatter timeFormatter) {
		this.timeFormatter = timeFormatter;
	}

	/**
	 * Converts the given timeFormatter in milliseconds to a formatted timeFormatter string using the configured timeFormatter utility.
	 *
	 * @param milliseconds the timeFormatter in milliseconds to convert.
	 * @return a formatted timeFormatter string representing the given timeFormatter.
	 */
	public String toTimeFromMillis(long milliseconds) {
		return toTime(milliseconds / 1000);
	}

	/**
	 * Converts the given timeFormatter in seconds to a formatted timeFormatter string using the configured timeFormatter utility.
	 *
	 * @param seconds the timeFormatter in seconds to convert.
	 * @return a formatted timeFormatter string representing the given timeFormatter.
	 */
	public String toTime(long seconds) {
		if (timeFormatter == null) return "";

		long time = System.currentTimeMillis() + (1000 * seconds);
		long currentTime = System.currentTimeMillis();
		long second = 0;
		long min = 0;
		long hours;
		long days;

		if (!((time - currentTime) / millisToSeconds % 60 == 0))
			second = (time - currentTime) / millisToSeconds % 60;
		if (!((time - currentTime) / millisToMinute % 60 == 0))
			min = (time - currentTime) / millisToMinute % 60;
		hours = (time - currentTime) / millisTohours % 24;
		days = (time - currentTime) / millisToDays;
		if (min < 0)
			min = 0;
		if (hours < 0)
			hours = 0;
		if (days < 0)
			days = 0;

		return this.timeFormatter.convertTime(days, hours, min, second);
	}

}