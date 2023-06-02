package org.broken.arrow.itemcreator.library.utility.builders;

import org.broken.arrow.itemcreator.library.CreateItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class ItemBuilder {

	private Object item;
	private Material matrial;
	private String stringItem;
	private final Iterable<?> itemArray;
	private final String displayName;
	private final List<String> lore;

	/**
	 * Create one itemStack, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param item item you want to create, suports string, matrial or itemstack.
	 */
	public ItemBuilder(final Object item) {
		this(null, item, null, null);

	}

	/**
	 * Create one itemStack, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param itemStack   item you want to create.
	 * @param displayName name onb item.
	 * @param lore        lore on item.
	 */
	public ItemBuilder(final ItemStack itemStack, final String displayName, final List<String> lore) {
		this(null, itemStack, displayName, lore);

	}

	/**
	 * Create one itemStack, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param matrial     you want to create.
	 * @param displayName name onb item.
	 * @param lore        lore on item.
	 */
	public ItemBuilder(final Material matrial, final String displayName, final List<String> lore) {
		this(null, matrial, displayName, lore);
	}

	/**
	 * Create one itemStack, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param stringItem  you want to create.
	 * @param displayName name onb item.
	 * @param lore        lore on item.
	 */
	public ItemBuilder(final String stringItem, final String displayName, final List<String> lore) {
		this(null, stringItem, displayName, lore);
	}

	/**
	 * Create array of itemStack´s, with name and lore. You can also add more
	 * like enchants and metadata.
	 *
	 * @param itemArray you want to create.
	 */
	public <T> ItemBuilder(final Iterable<T> itemArray) {
		this(itemArray, null, null, null);
	}

	private <T> ItemBuilder(final Iterable<T> itemArray, final Object stringItem, final String displayName, final List<String> lore) {
		this.itemArray = itemArray;
		this.item = stringItem;
		this.displayName = displayName;
		this.lore = lore;
	}

	/**
	 * Build your item. And call {@link CreateItemStack#makeItemStack()} or {@link CreateItemStack#makeItemStackArray()}
	 * depending on if you want to create array of items or ony 1 stack.
	 *
	 * @return CreateItemUtily class with your data you have set.
	 */
	public CreateItemStack build() {
		return new CreateItemStack(this);
	}

	public Object getItem() {
		return item;
	}

	public ItemStack getItemStack() {
		if (item instanceof ItemStack)
			return (ItemStack) item;
		return null;
	}

	public Material getMatrial() {
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
}