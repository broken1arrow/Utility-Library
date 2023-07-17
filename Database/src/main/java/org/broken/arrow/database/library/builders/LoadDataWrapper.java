package org.broken.arrow.database.library.builders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wrapper class for loading and storing deserialized data along with its primary value.
 *
 * @param <T> The type of the deserialized data.
 */
public class LoadDataWrapper<T> {
	private final T deSerializedData;
	private final Object primaryValue;

	/**
	 * Constructs a LoadDataWrapper instance with the provided primary column value, deserialized the values from the database.
	 *
	 * @param primaryKeyValue  The primary column value.
	 * @param deSerializedData The deserialized data.
	 */
	public LoadDataWrapper(@Nullable final Object primaryKeyValue, @Nonnull final T deSerializedData) {
		this.deSerializedData = deSerializedData;
		this.primaryValue = primaryKeyValue;
	}

	/**
	 * Retrieves the deserialized data stored in this wrapper.
	 *
	 * @return The deserialized data.
	 */
	@Nonnull
	public T getDeSerializedData() {
		return deSerializedData;
	}

	/**
	 * Retrieves the primary value associated with the data stored in this wrapper.
	 *
	 * @return The primary value for the primaryColumn.
	 */
	@Nullable
	public Object getPrimaryValue() {
		return primaryValue;
	}

}
