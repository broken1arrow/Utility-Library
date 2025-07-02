package org.broken.arrow.library.itemcreator.utility.builders;

import org.broken.arrow.library.itemcreator.CreateItemStack;
import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public final class ItemBuilder {

	private final Object item;
	private final ItemCreator itemCreator;
	private final Iterable<?> itemArray;
	private final String displayName;
	private final List<String> lore;

	/**
	 * Create one itemStack, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param itemCreator the item creator instrace.
	 * @param item        item you want to create, suports string, matrial or itemstack.
	 */
	public ItemBuilder(@Nonnull final ItemCreator itemCreator, final Object item) {
		this(itemCreator, null, item, null, null);

	}

	/**
	 * Create one itemStack, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param itemCreator the item creator instrace.
	 * @param itemStack   item you want to create.
	 * @param displayName The name on the item.
	 * @param lore        The lore or null if you not want to set lore.
	 */
	public ItemBuilder(@Nonnull final ItemCreator itemCreator, final ItemStack itemStack, final String displayName, final List<String> lore) {
		this(itemCreator, null, itemStack, displayName, lore);

	}

	/**
	 * Create one itemStack, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param itemCreator the item creator instance.
	 * @param material     the material you want to create.
	 * @param displayName The name on the item.
	 * @param lore        The lore or null if you not want to set lore.
	 */
	public ItemBuilder(@Nonnull final ItemCreator itemCreator, final Material material, final String displayName, final List<String> lore) {
		this(itemCreator, null, material, displayName, lore);
	}

	/**
	 * Create one itemStack, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param itemCreator the item creator instance.
	 * @param stringItem  you want to create.
	 * @param displayName The name on the item.
	 * @param lore        The lore or null if you not want to set lore.
	 */
	public ItemBuilder(@Nonnull final ItemCreator itemCreator, final String stringItem, final String displayName, final List<String> lore) {
		this(itemCreator, null, stringItem, displayName, lore);
	}

	/**
	 * Create array of itemStack´s, with name and lore. You can also add enchants and metadata.
	 *
	 * @param itemCreator the item creator instrace.
	 * @param itemArray   The array you want to create.
	 * @param displayName The name on the item.
	 * @param lore        The lore or null if you not want to set lore.
	 * @param <T>         type of items.
	 */
	public <T> ItemBuilder(@Nonnull final ItemCreator itemCreator, final Iterable<T> itemArray, final String displayName, final List<String> lore) {
		this(itemCreator, itemArray, null, displayName, lore);
	}

	/**
	 * Create array of itemStack´s, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param itemCreator the item creator instance.
	 * @param itemArray   The array you want to create.
	 * @param stringItem  The item you want to set the array too.
	 * @param displayName The name on the item.
	 * @param lore        The lore or null if you not want to set lore.
	 * @param <T>         type of items.
	 */
	private <T> ItemBuilder(@Nonnull final ItemCreator itemCreator, final Iterable<T> itemArray, final Object stringItem, final String displayName, final List<String> lore) {
		this.itemArray = itemArray;
		this.item = stringItem;
		this.displayName = displayName;
		this.lore = lore;
		this.itemCreator = itemCreator;

	}

	/**
	 * Build your item. And call {@link CreateItemStack#makeItemStack()} or {@link CreateItemStack#makeItemStackArray()}
	 * depending on if you want to create array of items or ony 1 stack.
	 *
	 * @return CreateItemStack class with data already set in the constructor.
	 */
	public CreateItemStack build() {
		return new CreateItemStack(this.itemCreator, this);
	}

	public Object getItem() {
		return item;
	}

	public ItemStack getItemStack() {
		if (item instanceof ItemStack)
			return (ItemStack) item;
		return null;
	}

	public Material getMaterial() {
		if (item instanceof Material)
			return (Material) item;
		return null;
	}

	public String getStringItem() {
		if (item instanceof String)
			return (String) item;
		return null;
	}

	public Iterable<?> getItemArray() {
		return itemArray;
	}

	public String getDisplayName() {
		return displayName;
	}

	public List<String> getLore() {
		return lore;
	}

	public boolean isItemSet() {
		return item != null;
	}
}