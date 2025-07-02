package org.broken.arrow.library.menu.utility.metadata;


import javax.annotation.Nonnull;

/**
 * Represents a predefined metadata key for menus.
 * <p>
 * For custom metadata with dynamic identifiers, use {@link MenuMetaKeyIdentifier}.
 * This allows extending predefined keys with additional ID-based context, such as different modes or purposes.
 * </p>
 */
public enum MenuMetadataKey implements MetadataKey {

    /**
     * Used when the menu is opened and is only relevant to the specific player.
     */
    MENU_OPEN,

    /**
     * Used when tracking the previously opened menu for potential backward navigation.
     * <p>
     * Currently unused in the API.
     * </p>
     */
    MENU_OPEN_PREVIOUS,

    /**
     * Used when a menu is tied to a specific location and may be modified by multiple players in same location.
     */
    MENU_OPEN_LOCATION,

    /**
     * A reserved key for user-defined purposes. This is not used internally
     * by the API and can be extended via {@link MenuMetaKeyIdentifier}.
     */
    CUSTOM;


    @Nonnull
    @Override
    public MenuMetadataKey getBaseKey() {
        return this;
    }

    /**
     * Retrieves the unique identifier for this metadata tag.
     * <p>
     * The returned value can range from {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}.
     * </p>
     *
     * @return the integer ID representing this metadata tag.
     */
    @Override
    public int getId() {
        return -1;
    }

}
