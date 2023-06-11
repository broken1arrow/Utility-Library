package org.broken.arrow.menu.button.manager.library.utility;


import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a data structure that holds information about passive and active buttons for specific button types.
 * The `MenuButtonData` class allows you to define and retrieve menu buttons for different states or permissions.
 */
public class MenuButtonData implements ConfigurationSerializable {

	private final MenuButton passiveButton;
	private final MenuButton activeButton;
	private final String buttonType;

	private MenuButtonData(@Nonnull final MenuButton passiveButton, @Nullable final MenuButton activeButton, @Nullable String buttonType) {
		this.passiveButton = passiveButton;
		this.activeButton = activeButton;
		this.buttonType = buttonType;
	}

	/**
	 * Retrieves the passive button when a player has for example permission to use it.
	 *
	 * @return the passive button associated with this menu button data
	 */
	@Nonnull
	public MenuButton getPassiveButton() {
		return passiveButton;
	}

	/**
	 * Retrieves the active button when a player has for example permission to use it.
	 *
	 * @return the active button associated with this menu button data, or null if not set
	 */
	@Nullable
	public MenuButton getActiveButton() {
		return activeButton;
	}

	/**
	 * Retrieves the type of button. Specifies whether the button performs a specific action
	 * or is a visible button without any function (e.g., "back", "forward").
	 *
	 * @return the type of button represented by this menu button data, or an empty string if not set
	 */
	@Nonnull
	public String getButtonType() {
		return buttonType != null ? buttonType : "";
	}

	@Override
	public String toString() {
		return "MenuButtonData{" +
				"passive=" + passiveButton +
				", active=" + activeButton +
				", buttonType=" + buttonType +
				'}';
	}

	@Nonnull
	@Override
	public Map<String, Object> serialize() {
		final Map<String, Object> map = new LinkedHashMap<>();
		map.put("passive", this.passiveButton);
		map.put("active", this.activeButton);
		if (buttonType != null) map.put("buttonType", buttonType);
		return map;
	}

	public static MenuButtonData deserialize(final Map<String, Object> map) {
		Map<String, Object> activeData = new LinkedHashMap<>();
		Map<String, Object> passiveData = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getKey().startsWith("active.")) {
				String key = entry.getKey().replace("active.", "");
				activeData.put(key, entry.getValue());
			}
			if (entry.getKey().startsWith("passive.")) {
				String key = entry.getKey().replace("passive.", "");
				passiveData.put(key, entry.getValue());
			}
		}
		MenuButton deserializeActiveData = null;
		MenuButton deserializePassiveData;
		if (!activeData.isEmpty())
			deserializeActiveData = MenuButton.deserialize(activeData);
		if (!passiveData.isEmpty())
			deserializePassiveData = MenuButton.deserialize(passiveData);
		else
			deserializePassiveData = MenuButton.deserialize(map);
		String buttonType = (String) map.get("buttonType");
		return new MenuButtonData(deserializePassiveData, deserializeActiveData, buttonType);
	}
}