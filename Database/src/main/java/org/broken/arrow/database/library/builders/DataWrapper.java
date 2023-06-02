package org.broken.arrow.database.library.builders;

import org.broken.arrow.convert.library.utility.serialize.ConfigurationSerializable;

public class DataWrapper {

	private final ConfigurationSerializable configurationSerialize;
	private final String primaryKey;
	private final Object value;

	public DataWrapper(final ConfigurationSerializable serialize, final String primaryKey, final Object value) {
		this.configurationSerialize = serialize;
		this.primaryKey = primaryKey;
		this.value = value;
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
