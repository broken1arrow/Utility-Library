package org.broken.arrow.database.library.builders;

import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;

public class DataWrapper {

	private final ConfigurationSerializable configurationSerialize;
	private final Object value;

	public DataWrapper(@Nonnull final Object primaryValue, @Nonnull final ConfigurationSerializable serialize) {
		this.configurationSerialize = serialize;
		this.value = primaryValue;
	}

	public ConfigurationSerializable getConfigurationSerialize() {
		return configurationSerialize;
	}

	public Object getPrimaryValue() {
		return value;
	}
}
