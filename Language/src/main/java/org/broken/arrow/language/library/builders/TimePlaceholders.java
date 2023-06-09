package org.broken.arrow.language.library.builders;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

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


	public String getDay() {
		return day;
	}


	public String getHour() {
		return hour;
	}


	public String getMinute() {
		return minute;
	}


	public String getSecond() {
		return second;
	}


	public String getDays() {
		return days;
	}


	public String getHours() {
		return hours;
	}


	public String getMinutes() {
		return minutes;
	}


	public String getSeconds() {
		return seconds;
	}

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

		public Builder setDay(final String day) {
			this.day = day;
			return this;
		}

		public Builder setHour(final String hour) {
			this.hour = hour;
			return this;
		}

		public Builder setMinute(final String minute) {
			this.minute = minute;
			return this;
		}

		public Builder setSecond(final String second) {
			this.second = second;
			return this;
		}

		public Builder setDays(final String days) {
			this.days = days;
			return this;
		}

		public Builder setHours(final String hours) {
			this.hours = hours;
			return this;
		}

		public Builder setMinutes(final String minutes) {
			this.minutes = minutes;
			return this;
		}

		public Builder setSeconds(final String seconds) {
			this.seconds = seconds;
			return this;
		}

		public TimePlaceholders build() {
			return new TimePlaceholders(this);
		}
	}

	@Nonnull
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("DAY", day);
		map.put("HOUR", hour);
		map.put("MINUTE", minute);
		map.put("SECOND", second);
		map.put("DAYS", days);
		map.put("HOURS", hours);
		map.put("MINUTES", minutes);
		map.put("SECONDS", seconds);
		return map;
	}

	public static TimePlaceholders deserialize(Map<String, Object> map) {
		String day = (String) map.getOrDefault("DAY", "");
		String hour = (String) map.getOrDefault("HOUR", "");
		String minute = (String) map.getOrDefault("MINUTE", "");
		String second = (String) map.getOrDefault("SECOND", "");

		String days = (String) map.getOrDefault("DAYS", "");
		String hours = (String) map.getOrDefault("HOURS", "");
		String minutes = (String) map.getOrDefault("MINUTES", "");
		String seconds = (String) map.getOrDefault("SECONDS", "");

		Builder builder = new Builder()
				.setDay(day)
				.setHour(hour)
				.setMinute(minute)
				.setSecond(second)
				.setDays(days)
				.setHours(hours)
				.setMinutes(minutes)
				.setSeconds(seconds);
		return new TimePlaceholders(builder);
	}
}