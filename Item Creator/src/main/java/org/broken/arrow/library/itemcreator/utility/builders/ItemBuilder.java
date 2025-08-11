package org.broken.arrow.library.itemcreator.utility.builders;

import org.broken.arrow.library.itemcreator.CreateItemStack;
import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Builder class to create item stacks or arrays of item stacks with custom
 * display names, lore, enchantments, and metadata.
 * <p>
 * Supports creation from various input types like {@link ItemStack}, {@link Material},
 * {@link String}, or arrays of items.
 * </p>
 */
public final class ItemBuilder {

	private final Object item;
	private final ItemCreator itemCreator;
	private final Iterable<?> itemArray;
	private final String displayName;
	private final List<String> lore;

	/**
	 * Creates an ItemBuilder for a single item, using an {@link ItemCreator} and a generic item object.
	 * The item can be a String, Material, or ItemStack.
	 *
	 * @param itemCreator the item creator instance
	 * @param item        the item to create (supports String, Material, or ItemStack)
	 */
	public ItemBuilder(@Nonnull final ItemCreator itemCreator, final Object item) {
		this(itemCreator, null, item, null, null);

	}

	/**
	 * Creates an ItemBuilder for a single {@link ItemStack} with optional display name and lore.
	 *
	 * @param itemCreator the item creator instance
	 * @param itemStack   the ItemStack to create
	 * @param displayName the custom display name of the item
	 * @param lore        the lore for the item, or null if none
	 */
	public ItemBuilder(@Nonnull final ItemCreator itemCreator, final ItemStack itemStack, final String displayName, final List<String> lore) {
		this(itemCreator, null, itemStack, displayName, lore);

	}


	/**
	 * Creates an ItemBuilder for a single {@link Material} with optional display name and lore.
	 *
	 * @param itemCreator the item creator instance
	 * @param material    the material to create
	 * @param displayName the custom display name of the item
	 * @param lore        the lore for the item, or null if none
	 */
	public ItemBuilder(@Nonnull final ItemCreator itemCreator, final Material material, final String displayName, final List<String> lore) {
		this(itemCreator, null, material, displayName, lore);
	}

	/**
	 * Creates an ItemBuilder for a single item represented as a String, with optional display name and lore.
	 *
	 * @param itemCreator the item creator instance
	 * @param stringItem  the string representation of the item to create
	 * @param displayName the custom display name of the item
	 * @param lore        the lore for the item, or null if none
	 */
	public ItemBuilder(@Nonnull final ItemCreator itemCreator, final String stringItem, final String displayName, final List<String> lore) {
		this(itemCreator, null, stringItem, displayName, lore);
	}

	/**
	 * Creates an ItemBuilder for an array (or iterable) of items, with optional display name and lore.
	 *
	 * @param itemCreator the item creator instance
	 * @param itemArray   the iterable collection of items to create
	 * @param displayName the custom display name to apply to all items
	 * @param lore        the lore to apply to all items, or null if none
	 * @param <T>         the type of items in the iterable
	 */
	public <T> ItemBuilder(@Nonnull final ItemCreator itemCreator, final Iterable<T> itemArray, final String displayName, final List<String> lore) {
		this(itemCreator, itemArray, null, displayName, lore);
	}

	/**
	 * Internal constructor for creating an ItemBuilder with all parameters specified.
	 *
	 * @param itemCreator the item creator instance
	 * @param itemArray   the iterable collection of items, or null if single item
	 * @param stringItem  the item to create as an Object (String, Material, or ItemStack)
	 * @param displayName the custom display name
	 * @param lore        the lore list, or null if none
	 * @param <T>         the type of items in the iterable
	 */
	private <T> ItemBuilder(@Nonnull final ItemCreator itemCreator, final Iterable<T> itemArray, final Object stringItem, final String displayName, final List<String> lore) {
		this.itemArray = itemArray;
		this.item = stringItem;
		this.displayName = displayName;
		this.lore = lore;
		this.itemCreator = itemCreator;

	}


	/**
	 * Builds the item or items according to the set parameters.
	 * Calls {@link CreateItemStack#makeItemStack()} or {@link CreateItemStack#makeItemStackArray()}
	 * internally based on whether this builder holds a single item or an array of items.
	 *
	 * @return a {@link CreateItemStack} instance containing the configured data
	 */
	public CreateItemStack build() {
		return new CreateItemStack(this.itemCreator, this);
	}

	/**
	 * Gets the raw item object passed to this builder.
	 *
	 * @return the item object (could be {@link String}, {@link Material}, or {@link ItemStack})
	 */
	public Object getItem() {
		return item;
	}

	/**
	 * Gets the item as an {@link ItemStack} if applicable.
	 *
	 * @return the item as an {@link ItemStack}, or null if not an ItemStack
	 */
	public ItemStack getItemStack() {
		if (item instanceof ItemStack)
			return (ItemStack) item;
		return null;
	}

	/**
	 * Gets the item as a {@link Material} if applicable.
	 *
	 * @return the item as a {@link Material}, or null if not a Material
	 */
	public Material getMaterial() {
		if (item instanceof Material)
			return (Material) item;
		return null;
	}

	/**
	 * Gets the item as a {@link String} if applicable.
	 *
	 * @return the item as a {@link String}, or null if not a String
	 */
	public String getStringItem() {
		if (item instanceof String)
			return (String) item;
		return null;
	}

	/**
	 * Gets the iterable collection of items if this builder was constructed for multiple items.
	 *
	 * @return the iterable of items, or null if not set
	 */
	public Iterable<?> getItemArray() {
		return itemArray;
	}

	/**
	 * Gets the custom display name set for the item(s).
	 *
	 * @return the display name, or null if none set
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Gets the lore list set for the item(s).
	 *
	 * @return the lore list, or null if none set
	 */
	public List<String> getLore() {
		return lore;
	}

	/**
	 * Checks whether the item has been set.
	 *
	 * @return true if an item is set, false otherwise
	 */
	public boolean isItemSet() {
		return item != null;
	}
}