package org.broken.arrow.database.library.builders;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;

public class DataWrapper {

	private final ConfigurationSerializable configurationSerialize;
	private final String primaryKey;
	private final Object value;

	public DataWrapper(@Nonnull final String primaryKey, @Nonnull final Object primaryValue, @Nonnull final ConfigurationSerializable serialize) {
		this.configurationSerialize = serialize;
		this.primaryKey = primaryKey;
		this.value = primaryValue;
	}

	public ConfigurationSerializable getConfigurationSerialize() {
		return configurationSerialize;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public Object getValue() {
		return value;
	}
}
