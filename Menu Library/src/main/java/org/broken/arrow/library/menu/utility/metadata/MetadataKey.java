package org.broken.arrow.library.menu.utility.metadata;

import javax.annotation.Nonnull;

/**
 * Represents a metadata key, either predefined via {@link MenuMetadataKey} or user-defined
 * via {@link MenuMetaKeyIdentifier}.
 */
public interface MetadataKey {

	/**
	 *  Returns the base {@link MenuMetadataKey} associated with this metadata key.
	 *
	 *  @return the base metadata key
	 */
	@Nonnull
	MenuMetadataKey getBaseKey();

	/**
	 * Retrieves the unique identifier for this metadata tag.
	 * <p>
	 * The returned value can range from {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}.
	 * </p>
	 *
	 * @return the integer ID representing this metadata tag.
	 */
	int getId();
}