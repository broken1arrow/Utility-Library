package org.broken.arrow.menu.library;

public enum MenuMetadataKey {
	/**
	 * When player open a menu some are only conected
	 * to the player self.
	 */
	MENU_OPEN,
	/**
	 * When player open new menu and store the old
	 * if player move back to previus menu. currently have this no use.
	 */
	MENU_OPEN_PREVIOUS,

	/**
	 * open a menu some get stored in cache to allow several players modify
	 * same inventory (used if you want connect a location with a menu).
	 */
	MENU_OPEN_LOCATION
}
