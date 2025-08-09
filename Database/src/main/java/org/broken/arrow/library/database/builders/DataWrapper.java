package org.broken.arrow.library.database.builders;

import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;

/**
 * Wrapper class holding a primary value and an associated
 * {@link ConfigurationSerializable} object.
 * <p>
 * This class bundles a value with its serializable representation
 * for easy passing and processing in data handling workflows.
 * </p>
 */
public class DataWrapper {

	private final ConfigurationSerializable configurationSerialize;
	private final Object value;

	/**
	 * Creates a new {@code DataWrapper} containing a primary value
	 * and its associated {@link ConfigurationSerializable} instance.
	 *
	 * @param primaryValue the main value to wrap. it is connected to the primary key if set.
	 * @param serialize    the serializable configuration.
	 */
	public DataWrapper(@Nonnull final Object primaryValue, @Nonnull final ConfigurationSerializable serialize) {
		this.configurationSerialize = serialize;
		this.value = primaryValue;
	}

	/**
	 * Returns the associated {@link ConfigurationSerializable} instance.
	 *
	 * @return the serializable configuration object
	 */
	public ConfigurationSerializable getConfigurationSerialize() {
		return configurationSerialize;
	}

	/**
	 * Returns the primary value wrapped by this {@code DataWrapper}.
	 *
	 * @return the primary value object
	 */
	public Object getPrimaryValue() {
		return value;
	}
}
