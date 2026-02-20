package org.broken.arrow.library.menu.button.manager.utility;


import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a data structure that holds information about passive and active buttons for specific button types.
 * The `MenuButtonData` class allows you to define and retrieve menu buttons for different states or permissions.
 */
public class MenuButtonData implements ConfigurationSerializable {

    private final MenuButton passiveButton;
    private final MenuButton activeButton;
    private final Map<String, MenuButton> resolveCustomButtons;
    private final String actionType;
    private final List<String> extra;

    private MenuButtonData(@Nonnull final MenuButton passiveButton, @Nullable final MenuButton activeButton,@Nonnull final Map<String, MenuButton> resolveCustomButtons, @Nullable String actionType, @Nullable List<String> extra) {
        this.passiveButton = passiveButton;
        this.activeButton = activeButton;
        this.resolveCustomButtons = resolveCustomButtons;
        this.actionType = actionType;
        this.extra = extra;
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
     * Returns an unmodifiable view of all registered custom buttons.
     *
     * @return unmodifiable map containing all custom buttons
     */
    public Map<String, MenuButton> getCustomButtons() {
        return Collections.unmodifiableMap(resolveCustomButtons);
    }

    /**
     * Retrieve own set custom button.
     *
     * @param name The identifier of the button
     * @return Returns your set button or null if not find it.
     */
    @Nullable
    public  MenuButton getCustomButton(final String name) {
        return resolveCustomButtons.get(name);
    }

    /**
     * Returns the custom button registered under the given name.
     * <p>
     * If no custom button exists, the passive button is returned instead.
     * This method never returns {@code null}.
     *
     * @param name the identifier of the button
     * @return the matching button, or the passive button if none is registered
     */
    @Nonnull
    public MenuButton resolveCustomButton(final String name) {
        MenuButton menuButton = resolveCustomButtons.get(name);
        if(menuButton != null)
            return menuButton;
        return this.getPassiveButton();
    }

    /**
     * Retrieves the type of button. Specifies whether the button performs a specific action
     * or is a visible button without any function (e.g., "back", "forward").
     *
     * @return The type of button represented by this menu button data, or an empty string if not set
     */
    @Nonnull
    public String getActionType() {
        return actionType != null ? actionType : "";
    }

    /**
     * Sets extra data for a button. If you want to attach commands or other options to your button.
     *
     * @return The extra data as string list.
     */
    @Nullable
    public List<String> getExtra() {
        return extra;
    }

    /**
     * Checks if the button type of this menu button data is equal to the provided button type,
     * ignoring the case.
     *
     * @param actionType the button type to compare against.
     * @return true if the provided button type is the same as the button type of this menu button data,
     * false if either the provided button type or the button type of this menu button data is null.
     */
    public boolean isActionTypeEqual(String actionType) {
        if (actionType == null) return false;
        return this.actionType != null && this.actionType.equalsIgnoreCase(actionType);
    }

    @Override
    public String toString() {
        return "MenuButtonData{" +
                "passive=" + passiveButton +
                ", active=" + activeButton +
                ", actionType=" + actionType +
                '}';
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("passive", this.passiveButton);
        map.put("active", this.activeButton);
        if (actionType != null) map.put("action_type", actionType);
        return map;
    }

    /**
     * Deserializes a MenuButtonData object from a map of key-value pairs.
     *
     * @param map the map containing the menu button data
     * @return the deserialized MenuButtonData object
     */
    public static MenuButtonData deserialize(final Map<String, Object> map) {
        Map<String, Object> activeData = new LinkedHashMap<>();
        Map<String, Object> passiveData = new LinkedHashMap<>();
        Map<String, Map<String, Object>> custom = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final String entryKey = entry.getKey();
            if (entryKey.startsWith("active.")) {
                String key = entryKey.replace("active.", "");
                activeData.put(key, entry.getValue());
            } else if (entryKey.startsWith("passive.")) {
                String key = entryKey.replace("passive.", "");
                passiveData.put(key, entry.getValue());
            } else {
                int index = entryKey.indexOf(".");
                if(index > 0) {
                    String key = entryKey.substring(index + 1);
                    final String substring = entryKey.substring(0, index);
                    Map<String, Object> stringObjectMap = custom.get(substring);
                    if (stringObjectMap == null)
                        stringObjectMap = new HashMap<>();
                    stringObjectMap.put(key, entry.getValue());
                    custom.put(substring, stringObjectMap);
                }
            }
        }

        Map<String, MenuButton> resolveCustomButton = new HashMap<>();
        if(!custom.isEmpty()){
            custom.forEach((key, value) -> resolveCustomButton.put(key, MenuButton.deserialize(value)));
        }
        System.out.println("custom= " + resolveCustomButton );
        MenuButton deserializeActiveData = null;
        MenuButton deserializePassiveData;
        if (!activeData.isEmpty())
            deserializeActiveData = MenuButton.deserialize(activeData);
        if (!passiveData.isEmpty())
            deserializePassiveData = MenuButton.deserialize(passiveData);
        else
            deserializePassiveData = MenuButton.deserialize(map);
        String actionType = (String) map.get("action_type");
        Object extra = map.get("extra");
        List<String> extras;
        if (extra instanceof List) {
            extras = (List<String>) extra;
        } else {
            extras = Collections.singletonList(extra + "");
        }
        return new MenuButtonData(deserializePassiveData, deserializeActiveData,resolveCustomButton, actionType, extras);
    }
}