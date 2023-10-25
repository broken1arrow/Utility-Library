package org.broken.arrow.serialize.library.utility.converters.time;

import javax.annotation.Nonnull;

/**
 * This interface represents a time utility for converting milliseconds to a time string
 * containing days, hours, minutes, and seconds.
 */
public interface TimeFormatProvider {

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
	default String convertTime(long days, long hours, long minutes, long seconds) {
		StringBuilder builder = new StringBuilder();

		builder.append(formatDay(days));
		if (days > 0 && hours > 0) {
			builder.append(" ");
		}
		builder.append(formatHour(hours));
		if ((days > 0 || hours > 0) && minutes > 0) {
			builder.append(" ");
		}
		builder.append(formatMinute(minutes));
		if ((days > 0 || hours > 0 || minutes > 0) && seconds > 0) {
			builder.append(" ");
		}
		builder.append(formatSecond(seconds));
		return builder.toString();
	}

	/**
	 * Returns the name for the specified number of days.
	 *
	 * @param days the number of days.
	 * @return the name for the specified number of days.
	 */
	default String formatDay(long days) {
		if (days == 0)
			return "";
		if (days <= 1)
			return days + " Day";
		else
			return days + " Days";
	}

	/**
	 * Returns the name for the specified number of hours.
	 *
	 * @param hours the number of hours.
	 * @return the name for the specified number of hours.
	 */
	default String formatHour(long hours) {
		if (hours == 0)
			return "";
		if (hours <= 1)
			return hours + " Hour";
		else
			return hours + " Hours";
	}

	/**
	 * Returns the name for the specified number of minutes.
	 *
	 * @param minutes the number of minutes.
	 * @return the name for the specified number of minutes.
	 */
	default String formatMinute(long minutes) {
		if (minutes == 0)
			return "";
		if (minutes <= 1)
			return minutes + " Minute";
		else
			return minutes + " Minutes";
	}

	/**
	 * Returns the name for the specified number of seconds.
	 *
	 * @param seconds the number of seconds.
	 * @return the name for the specified number of seconds.
	 */
	default String formatSecond(long seconds) {
		if (seconds == 0)
			return "";
		if (seconds <= 1)
			return seconds + " Second";
		else
			return seconds + " Seconds";
	}
}
