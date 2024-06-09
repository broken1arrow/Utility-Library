package org.broken.arrow.menu.button.manager.library.utility;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;
import org.bukkit.DyeColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a menu button with configurable properties such as
 * material, color, display name, lore, glow effect, and button type.
 */
public class MenuButton implements ConfigurationSerializable {

	private final DyeColor color;
	private final String material;
	private final String displayName;
	private final List<String> lore;
	private final boolean glow;
	private final String actionType;
    private final String extra;

    public MenuButton(final Builder builder) {
		this.color = builder.color;
		this.material = builder.material;
		this.displayName = builder.displayName;
		this.lore = builder.lore;
		this.glow = builder.glow;
		this.actionType = builder.buttonType;
        this. extra = builder. extra;

	}

	/**
	 * Retrieves the name of the material associated with this menu button.
	 *
	 * @return the name of the material as a string. The specific identifier used as an item
	 * depends on how it is defined. For example, if you provide "GLASS" as the material,
	 * you can then get the color using {@link #getColor()}.
	 */
	@Nonnull
	public String getMaterial() {
		return material;
	}

	/**
	 * Retrieves the color of the menu button. Certain materials such as wool, concrete, or glass can have colors.
	 *
	 * @return the associated DyeColor or null if not set.
	 */
	@Nullable
	public DyeColor getColor() {
		return color;
	}

	/**
	 * Retrieves the custom value of the menu button. Certain materials such as heads texture need
     * extra value to be set.
	 *
	 * @return the associated value or null if not set.
	 */
	@Nullable
	public String getExtra() {
		return extra;
	}

	/**
	 * Retrive the name of the item.
	 *
	 * @return the name.
	 */
	@Nullable
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Retrive list of lore set on the item
	 *
	 * @return the list of lore.
	 */
	@Nullable
	public List<String> getLore() {
		return lore;
	}

	/**
	 * Checks if the menu button should have a glow effect.
	 * Note: This effect does not work on chest items.
	 *
	 * @return true if the menu button should have a glow effect, false otherwise.
	 */
	public boolean isGlow() {
		return glow;
	}

	/**
	 * Retrieve type of button, if this is a button that run some code
	 * or only visible button without any function.
	 *
	 * @return the type of button this is.
	 */
	@Nullable
	public String getActionType() {
		return actionType;
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

	public static class Builder {

		private final String material;
        private String extra;
        private DyeColor color;
		private String displayName;
		private List<String> lore;
		private boolean glow;
		private String buttonType;

		/**
		 * Constructs a builder for creating a menu button with the specified material.
		 *
		 * @param material the name of the material as a string, used as an identifier for the item.
		 *                 It should correspond to the material used in Minecraft.
		 *                 For example, "DIAMOND_SWORD" or "STONE". If you want to use a material like "GLASS"
		 *                 and also want to have color options, you can use the {@link #setColor(org.bukkit.DyeColor)}
		 *                 method.
		 */
		public Builder(@Nonnull final String material) {
			this.material = material;
		}

		/**
		 * Sets the color of the menu button. Certain materials such as wool, concrete, or glass can have colors.
		 *
		 * @param color the DyeColor type.
		 * @return the associated DyeColor.
		 */
		public Builder setColor(final DyeColor color) {
			this.color = color;
			return this;
		}

		/**
		 * Sets the display name of the menu button.
		 *
		 * @param displayName the name of the item to be displayed.
		 * @return the updated builder instance.
		 */
		public Builder setDisplayName(final String displayName) {
			this.displayName = displayName;
			return this;
		}

		/**
		 * Sets the lore of the menu button.
		 *
		 * @param lore the lore of the item to be displayed.
		 * @return the updated builder instance.
		 */
		public Builder setLore(final List<String> lore) {
			this.lore = lore;
			return this;
		}

		/**
		 * Sets the glow of the menu button.
		 *
		 * @param glow set this to true if you wants glow effect.
		 * @return the updated builder instance.
		 */
		public Builder setGlow(final boolean glow) {
			this.glow = glow;
			return this;
		}

		/**
		 * Sets the type of button, indicating whether it should trigger a
		 * specific action or simply serve as a visible button without any function.
		 *
		 * @param buttonType the type of button (e.g., "back", "forward").
		 * @return the updated builder instance.
		 */
		public Builder setButtonType(final String buttonType) {
			this.buttonType = buttonType;
			return this;
		}

        /**
         * Extra if you want to add for example custom value like head
         * or similar.
         * @param extra the custom value for non normal data.
         * @return the set string value.
         */
        public Builder setExtra(String extra) {
            this.extra = extra;
            return this;
        }

		public MenuButton build() {
			return new MenuButton(this);
		}


    }

	@Nonnull
	@Override
	public Map<String, Object> serialize() {
		final Map<String, Object> map = new LinkedHashMap<>();
		map.put("color", color);
		map.put("material", material + "");
		map.put("name", displayName);
		map.put("lore", lore);
		map.put("glow", glow);
        map.put("extra", extra);
		if (actionType != null) map.put("action_type", actionType);
		return map;
	}

    public static MenuButton deserialize(final Map<String, Object> map) {
        final String color = (String) map.get("color");
        final String extra = (String) map.get("extra");
        final String material = (String) map.get("material");
        final String displayName = (String) map.get("name");
        final List<String> lore = (List<String>) map.get("lore");
        final boolean glow = (boolean) map.getOrDefault("glow", false);
        final String actionType = (String) map.get("action_type");
        DyeColor dyeColor = dyeColor(color);

        final Builder builder = new Builder(material)
                .setButtonType(actionType)
                .setColor(dyeColor)
                .setDisplayName(displayName)
                .setGlow(glow)
                .setExtra(extra)
                .setLore(lore);
        return builder.build();
    }

    @Override
    public String toString() {
        return "MenuButton{" +
                "color=" + color +
                ", material='" + material + '\'' +
                ", displayName='" + displayName + '\'' +
                ", lore=" + lore +
                ", glow=" + glow +
                ", actionType='" + actionType + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }

    @Nullable
	public static DyeColor dyeColor(final String dyeColor) {
		final DyeColor[] dyeColors = DyeColor.values();

		for (final DyeColor color : dyeColors) {
			if (color.name().equalsIgnoreCase(dyeColor)) {
				return color;
			}
		}
		return null;
	}
}