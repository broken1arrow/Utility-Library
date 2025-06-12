package org.broken.arrow.menu.library;

public enum MenuMetadataKey {
	/**
	 * When player open a menu some are only connected
	 * to the player self.
	 */
	MENU_OPEN,
	/**
	 * When player open new menu and store the old
	 * if player move back to previous menu. currently have this no use.
	 */
	MENU_OPEN_PREVIOUS,

	/**
	 * open a menu some get stored in cache to allow several players modify
	 * same inventory (used if you want connect a location with a menu).
	 */
	MENU_OPEN_LOCATION,

	/**
	 * Have custom metadata key, you can also set your own ID {{@link #setId(int)}}
	 * to distinguish different modes for your metadata key.
	 */
	CUSTOM;

    private int id = -1;

	/**
	 * Retrieves the unique identifier for this metadata tag.
	 * <p>
	 * The returned value can range from {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}.
	 * </p>
	 *
	 * @return the integer ID representing this metadata tag.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set your own unique ID for your custom tag. Will only be applied if value is more than zero.
	 *
	 * @param id your id for a specific key.
	 */
	public void setId(int id) {
		this.id = id;
	}
}
