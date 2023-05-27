package org.broken.arrow.database.library.builders;

import org.broken.arrow.database.library.utility.serialize.ConfigurationSerializeUtility;

public class DataWrapper {

	private final ConfigurationSerializeUtility configurationSerialize;
	private final String primaryKey;
	private final Object value;

	public DataWrapper(final ConfigurationSerializeUtility serialize, final String primaryKey, final Object value) {
		this.configurationSerialize = serialize;
		this.primaryKey = primaryKey;
		this.value = value;
	}

	public ConfigurationSerializeUtility getConfigurationSerialize() {
		return configurationSerialize;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public Object getValue() {
		return value;
	}
}
