package org.broken.arrow.localization.library.builders;

import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a data structure that holds placeholders for time-related values used in text manipulation.
 * The `TimePlaceholders` class allows you to define and retrieve placeholders for representing time durations.
 */
public class TimePlaceholders implements ConfigurationSerializable {

	private final String day;
	private final String hour;
	private final String minute;
	private final String second;
	private final String days;
	private final String hours;
	private final String minutes;
	private final String seconds;
	private final Builder builder;

	/**
	 * Constructs a new TimePlaceholders object with the specified builder.
	 *
	 * @param builder the builder used to construct this TimePlaceholders object
	 */
	private TimePlaceholders(Builder builder) {
		this.day = builder.day;
		this.hour = builder.hour;
		this.minute = builder.minute;
		this.second = builder.second;
		this.days = builder.days;
		this.hours = builder.hours;
		this.minutes = builder.minutes;
		this.seconds = builder.seconds;
		this.builder = builder;
	}

	/**
	 * Retrieves the placeholder for representing a single day.
	 *
	 * @return the placeholder for a day
	 */
	public String getDay() {
		return day;
	}

	/**
	 * Retrieves the placeholder for representing a single hour.
	 *
	 * @return the placeholder for an hour
	 */
	public String getHour() {
		return hour;
	}

	/**
	 * Retrieves the placeholder for representing a single minute.
	 *
	 * @return the placeholder for a minute
	 */
	public String getMinute() {
		return minute;
	}

	/**
	 * Retrieves the placeholder for representing a single second.
	 *
	 * @return the placeholder for a second
	 */
	public String getSecond() {
		return second;
	}

	/**
	 * Retrieves the placeholder for representing multiple days.
	 *
	 * @return the placeholder for multiple days
	 */
	public String getDays() {
		return days;
	}

	/**
	 * Retrieves the placeholder for representing multiple hours.
	 *
	 * @return the placeholder for multiple hours
	 */
	public String getHours() {
		return hours;
	}

	/**
	 * Retrieves the placeholder for representing multiple minutes.
	 *
	 * @return the placeholder for multiple minutes
	 */
	public String getMinutes() {
		return minutes;
	}

	/**
	 * Retrieves the placeholder for representing multiple seconds.
	 *
	 * @return the placeholder for multiple seconds
	 */
	public String getSeconds() {
		return seconds;
	}

	/**
	 * Retrieves the builder associated with the TimePlaceholders object.
	 *
	 * @return the builder used to construct this TimePlaceholders object
	 */
	public Builder getBuilder() {
		return builder;
	}

	public static class Builder {
		private String day;
		private String hour;
		private String minute;
		private String second;
		private String days;
		private String hours;
		private String minutes;
		private String seconds;

		/**
		 * Sets the placeholder for representing a single day.
		 *
		 * @param day the placeholder for a day
		 * @return the builder instance
		 */
		public Builder setDay(final String day) {
			this.day = day;
			return this;
		}

		/**
		 * Sets the placeholder for representing a single hour.
		 *
		 * @param hour the placeholder for an hour
		 * @return the builder instance
		 */
		public Builder setHour(final String hour) {
			this.hour = hour;
			return this;
		}

		/**
		 * Sets the placeholder for representing a single minute.
		 *
		 * @param minute the placeholder for a minute
		 * @return the builder instance
		 */
		public Builder setMinute(final String minute) {
			this.minute = minute;
			return this;
		}

		/**
		 * Sets the placeholder for representing a single second.
		 *
		 * @param second the placeholder for a second
		 * @return the builder instance
		 */
		public Builder setSecond(final String second) {
			this.second = second;
			return this;
		}

		/**
		 * Sets the placeholder for representing multiple days.
		 *
		 * @param days the placeholder for multiple days
		 * @return the builder instance
		 */
		public Builder setDays(final String days) {
			this.days = days;
			return this;
		}

		/**
		 * Sets the placeholder for representing multiple hours.
		 *
		 * @param hours the placeholder for multiple hours
		 * @return the builder instance
		 */
		public Builder setHours(final String hours) {
			this.hours = hours;
			return this;
		}

		/**
		 * Sets the placeholder for representing multiple minutes.
		 *
		 * @param minutes the placeholder for multiple minutes
		 * @return the builder instance
		 */
		public Builder setMinutes(final String minutes) {
			this.minutes = minutes;
			return this;
		}

		/**
		 * Sets the placeholder for representing multiple seconds.
		 *
		 * @param seconds the placeholder for multiple seconds
		 * @return the builder instance
		 */
		public Builder setSeconds(final String seconds) {
			this.seconds = seconds;
			return this;
		}

		/**
		 * Constructs a new {@link TimePlaceholders} object using the configured values.
		 *
		 * @return the constructed {@link TimePlaceholders} object
		 */
		public TimePlaceholders build() {
			return new TimePlaceholders(this);
		}
	}

	@Nonnull
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("day", day);
		map.put("hour", hour);
		map.put("minute", minute);
		map.put("second", second);
		map.put("days", days);
		map.put("hours", hours);
		map.put("minutes", minutes);
		map.put("seconds", seconds);
		return map;
	}

	public static TimePlaceholders deserialize(Map<String, Object> map) {
		String day = (String) map.getOrDefault("day", "");
		String hour = (String) map.getOrDefault("hour", "");
		String minute = (String) map.getOrDefault("minute", "");
		String second = (String) map.getOrDefault("second", "");

		String days = (String) map.getOrDefault("days", "");
		String hours = (String) map.getOrDefault("hours", "");
		String minutes = (String) map.getOrDefault("minutes", "");
		String seconds = (String) map.getOrDefault("seconds", "");

		Builder builder = new Builder()
				.setDay(day)
				.setHour(hour)
				.setMinute(minute)
				.setSecond(second)
				.setDays(days)
				.setHours(hours)
				.setMinutes(minutes)
				.setSeconds(seconds);
		return builder.build();
	}
}