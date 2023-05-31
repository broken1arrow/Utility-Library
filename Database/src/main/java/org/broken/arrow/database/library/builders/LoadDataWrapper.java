package org.broken.arrow.database.library.builders;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class LoadDataWrapper<T> {

	private final T deSerializedData;
	private final Object primaryValue;

	public LoadDataWrapper(@Nonnull final String primaryColumn, @Nonnull final Map<String, Object> data, @Nonnull final T deSerializedData) {
		this.deSerializedData = deSerializedData;
		this.primaryValue = data.get(primaryColumn);
	}

	@Nonnull
	public T getDeSerializedData() {
		return deSerializedData;
	}

	@Nullable
	public Object getPrimaryValue() {
		return primaryValue;
	}

}
