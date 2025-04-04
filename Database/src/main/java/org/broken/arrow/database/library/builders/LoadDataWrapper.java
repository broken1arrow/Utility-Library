package org.broken.arrow.database.library.builders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for loading and storing deserialized data along with its primary value.
 *
 * @param <T> The type of the deserialized data.
 */
public class LoadDataWrapper<T> {
	private final T deSerializedData;
	private final Object primaryValue;
	private final List<Object> primaryValues;

	/**
	 * Constructs a LoadDataWrapper instance with the provided primary column value, deserialized the values from the database.
	 *
	 * @param primaryKeyValue  The primary column value.
	 * @param deSerializedData The deserialized data.
	 */
	public LoadDataWrapper(@Nullable final Object primaryKeyValue, @Nonnull final T deSerializedData) {
		this.deSerializedData = deSerializedData;
		this.primaryValue = primaryKeyValue;
		this.primaryValues = new ArrayList<>();
	}

	public LoadDataWrapper(@Nullable final List<Object> primaryKeyValues, @Nonnull final T deSerializedData) {
		this.deSerializedData = deSerializedData;
		this.primaryValues = primaryKeyValues == null ? new ArrayList<>(): primaryKeyValues;
		this.primaryValue = null;
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
	 * This will be replaced with {@link #getPrimaryValues()}, as you now have the option
	 * return more than 1 primary key from the sql query.
	 *
	 * @return The primary value for the primaryColumn or null if not this value is set in the database.
	 */
	@Nullable
	@Deprecated
	public Object getPrimaryValue() {
		return primaryValue;
	}

	@Nonnull
	public List<Object> getPrimaryValues() {
		return this.primaryValues;
	}
}
