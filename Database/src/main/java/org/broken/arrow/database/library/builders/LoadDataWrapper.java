package org.broken.arrow.database.library.builders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Wrapper class for loading and storing deserialized data along with its primary value.
 *
 * @param <T> The type of the deserialized data.
 */
public class LoadDataWrapper<T> {
	private final T deSerializedData;
	private final Object primaryValue;

	/**
	 * Constructs a LoadDataWrapper instance with the provided primary column name, serialized data from the database,
	 * and the deserialized data.
	 *
	 * @param primaryColumn    The primary column name.
	 * @param dataFromDB       The serialized data obtained from the database.
	 * @param deSerializedData The deserialized data.
	 */
	public LoadDataWrapper(@Nonnull final String primaryColumn, @Nonnull final Map<String, Object> dataFromDB, @Nonnull final T deSerializedData) {
		this.deSerializedData = deSerializedData;
		this.primaryValue = dataFromDB.get(primaryColumn);
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
