package org.broken.arrow.serialize.library.utility.converters.time;

import javax.annotation.Nonnull;

/**
 * This interface represents a time utility for converting milliseconds to a time string
 * containing days, hours, minutes, and seconds.
 */
public interface TimeFormatter {

	/**
	 * Converts the given time components into a formatted time string.
	 * <p>
	 * Note: By default, this method does not include values less than or equal to zero.
	 *
	 * @param days    the number of days.
	 * @param hours   the number of hours.
	 * @param minutes the number of minutes.
	 * @param seconds the number of seconds.
	 * @return a formatted time string representing the given time components.
	 */
	@Nonnull
	default String convertTime(Long days, Long hours, Long minutes, Long seconds) {
		StringBuilder builder = new StringBuilder();

		builder.append(day(days));
		if (days > 0 && hours > 0) {
			builder.append(" ");
		}
		builder.append(hour(hours));
		if ((days > 0 || hours > 0) && minutes > 0) {
			builder.append(" ");
		}
		builder.append(minute(minutes));
		if ((days > 0 || hours > 0 || minutes > 0) && seconds > 0) {
			builder.append(" ");
		}
		builder.append(second(seconds));
		return builder.toString();
	}

	/**
	 * Returns the name for the specified number of days.
	 *
	 * @param amount the number of days.
	 * @return the name for the specified number of days.
	 */
	String day(long amount);

	/**
	 * Returns the name for the specified number of hours.
	 *
	 * @param amount the number of hours.
	 * @return the name for the specified number of hours.
	 */
	String hour(long amount);

	/**
	 * Returns the name for the specified number of minutes.
	 *
	 * @param amount the number of minutes.
	 * @return the name for the specified number of minutes.
	 */
	String minute(long amount);

	/**
	 * Returns the name for the specified number of seconds.
	 *
	 * @param amount the number of seconds.
	 * @return the name for the specified number of seconds.
	 */
	String second(long amount);
}
