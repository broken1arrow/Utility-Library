package org.broken.arrow.menu.button.manager.library.utility;


import org.broken.arrow.serialize.library.utility.converters.SpigotSound;
import org.bukkit.Sound;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a template for a menu, containing information about the menu's title, button positions, button data, and sound.
 */
public class MenuTemplate {

	private Logger LOG = Logger.getLogger(MenuTemplate.class.getName());
	private final String menuTitle;
	private final List<Integer> fillSlots;

	private final Map<List<Integer>, MenuButtonData> menuButtons;
	private final int amountOfButtons;
	private final Sound sound;

	/**
	 * Constructs a new MenuTemplate with the specified parameters.
	 *
	 * @param menuTitle   the title of the menu
	 * @param fillSlots   the positions of slots to be filled with empty space in the menu
	 * @param menuButtons the map of button positions to MenuButtonData objects
	 * @param sound       the sound associated with the menu
	 */
	public MenuTemplate(String menuTitle, List<Integer> fillSlots, Map<List<Integer>, MenuButtonData> menuButtons, String sound) {
		this.menuTitle = menuTitle;
		this.fillSlots = fillSlots;
		this.menuButtons = menuButtons;
		this.amountOfButtons = calculateAmountOfButtons(menuButtons);
		this.sound = SpigotSound.getSound(sound);
	}

	/**
	 * Returns the number of buttons in the menu template.
	 *
	 * @return the number of buttons in the menu template
	 */
	public int getAmountOfButtons() {
		return amountOfButtons;
	}

	/**
	 * Calculates and returns the size of the inventory based on the number of buttons in the menu template.
	 * The inventory size will be a multiple of 9 and will not exceed 54 slots.
	 *
	 * @param menu the name of the menu for which to calculate the inventory size
	 * @return the calculated inventory size
	 */
	public int getinvSize(final String menu) {
		int size = this.getAmountOfButtons();
		if (size < 9) return 9;
		if (size < 18) return 18;
		if (size < 27) return 27;
		if (size < 36) return 36;
		if (size < 45) return 45;
		if (size > 54)
			LOG.log(Level.INFO,"This menu " + menu + " has set bigger inventory size an it can handle, your set size " + size + ". will defult to 54.");
		return 54;
	}

	/**
	 * Retrieves the title of the menu.
	 *
	 * @return the title of the menu, or null if not set
	 */
	@Nullable
	public String getMenuTitle() {
		return menuTitle;
	}

	/**
	 * Retrieves the positions of slots to be filled with items in the menu.
	 *
	 * @return the list of fill slots, or null if not set
	 */
	@Nullable
	public List<Integer> getFillSlots() {
		return fillSlots;
	}

	/**
	 * Retrieves the sound associated with the menu.
	 *
	 * @return the sound associated with the menu, or null if not set
	 */
	@Nullable
	public Sound getSound() {
		return sound;
	}

	/**
	 * Retrieves the map of button positions to MenuButtonData objects.
	 *
	 * @return the map of button positions to MenuButtonData objects, or null if not set
	 */
	@Nullable
	public Map<List<Integer>, MenuButtonData> getMenuButtons() {
		if (menuButtons == null) return null;
		return Collections.unmodifiableMap(menuButtons);
	}

	/**
	 * Retrieves the MenuButtonData object associated with the specified slot position in the menu.
	 *
	 * @param slot the slot position
	 * @return the MenuButtonData object associated with the slot position, or null if not found
	 */
	@Nullable
	public MenuButtonData getMenuButton(int slot) {
		for (Entry<List<Integer>, MenuButtonData> slots : menuButtons.entrySet()) {
			for (int menuSlot : slots.getKey())
				if (menuSlot == slot)
					return slots.getValue();
		}
		return null;
	}

	/**
	 * Calculate amount of buttons inside the inventory.
	 *
	 * @param menuButtons the map with all set buttons.
	 * @return amount of slots needed.
	 */
	public int calculateAmountOfButtons(Map<List<Integer>, MenuButtonData> menuButtons) {
		int lastButton = 0;
		for (List<Integer> slots : menuButtons.keySet()) {
			for (final Integer slot : slots) {
				lastButton = Math.max(lastButton, slot);
			}
		}
		return lastButton;
	}

}