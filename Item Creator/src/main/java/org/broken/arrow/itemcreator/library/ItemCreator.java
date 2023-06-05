package org.broken.arrow.itemcreator.library;


import org.broken.arrow.itemcreator.library.utility.ServerVersion;
import org.broken.arrow.itemcreator.library.utility.builders.ItemBuilder;
import org.broken.arrow.nbt.library.RegisterNbtAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class ItemCreator {

	private final RegisterNbtAPI nbtApi;

	public ItemCreator(Plugin plugin) {
		this(plugin, false);
	}

	public ItemCreator(Plugin plugin, boolean turnOffLogger) {
		nbtApi = new RegisterNbtAPI(plugin, turnOffLogger);
		ServerVersion.setServerVersion(plugin);
	}

	public RegisterNbtAPI getNbtApi() {
		return nbtApi;
	}


	/**
	 * Starts the creation of an item using an existing or new ItemBuilder instance. You can set all the values you want
	 * with the builder. Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
	 *
	 * @param itemBuilder The ItemBuilder instance for creating the item.
	 * @return An instance of the CreateItemStack class.
	 */
	public CreateItemStack of(ItemBuilder itemBuilder) {
		return itemBuilder.build();
	}

	/**
	 * Starts the creation of a simple item.The item will not have a display name or lore.
	 * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
	 *
	 * @param item The name, Material, or ItemStack of the item.
	 * @return An instance of the CreateItemStack class.
	 */
	public CreateItemStack of(Object item) {
		ItemBuilder itemBuilder = new ItemBuilder(this, item);
		return itemBuilder.build();
	}

	/**
	 * Starts the creation of an item with a display name and lore.
	 * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
	 *
	 * @param item        The name, Material, or ItemStack of the item.
	 * @param displayName The display name of the item.
	 * @param lore        The lore of the item.
	 * @return An instance of the CreateItemStack class.
	 */
	public CreateItemStack of(Object item, String displayName, String... lore) {
		return of(item, displayName, lore != null ? Arrays.asList(lore) : null);
	}

	/**
	 * Starts the creation of an item with a display name and lore.
	 * Complete the creation by calling {@link CreateItemStack#makeItemStack()}.
	 *
	 * @param item        The name, Material, or ItemStack of the item.
	 * @param displayName The display name of the item.
	 * @param lore        The lore of the item.
	 * @return An instance of the CreateItemStack class.
	 */
	public CreateItemStack of(Object item, String displayName, List<String> lore) {
		ItemBuilder itemBuilder;
		if (item instanceof ItemStack)
			itemBuilder = new ItemBuilder(this, (ItemStack) item, displayName, lore);
		else if (item instanceof Material)
			itemBuilder = new ItemBuilder(this, (Material) item, displayName, lore);
		else
			itemBuilder = new ItemBuilder(this, item + "", displayName, lore);
		return itemBuilder.build();
	}

	/**
	 * Starts the creation of an item from an iterable of items.
	 * Complete the creation by calling {@link CreateItemStack#makeItemStackArray()}.
	 *
	 * @param itemArray The iterable of items to convert to ItemStacks.
	 * @return An instance of the CreateItemStack class.
	 */
	public <T> CreateItemStack of(Iterable<T> itemArray) {
		ItemBuilder itemBuilder = new ItemBuilder(this, itemArray);
		return itemBuilder.build();
	}

	public static ItemStack createItemStackAsOne(final Material material) {
		ItemStack itemstack = null;
		if (material != null)
			itemstack = new ItemStack(material);

		return createItemStackAsOne(itemstack != null ? itemstack : new ItemStack(Material.AIR));
	}

	public static ItemStack createItemStackAsOne(final ItemStack itemstacks) {
		ItemStack itemstack = null;
		if (itemstacks != null && !itemstacks.getType().equals(Material.AIR)) {
			itemstack = itemstacks.clone();
			final ItemMeta meta = itemstack.getItemMeta();
			itemstack.setItemMeta(meta);
			itemstack.setAmount(1);
		}
		return itemstack != null ? itemstack : new ItemStack(Material.AIR);
	}

	public static ItemStack[] createItemStackAsOne(final ItemStack[] itemstacks) {
		ItemStack itemstack = null;
		if (itemstacks != null) {
			for (final ItemStack item : itemstacks)
				if (!(item.getType() == Material.AIR)) {
					itemstack = item.clone();
					final ItemMeta meta = itemstack.getItemMeta();
					itemstack.setItemMeta(meta);
					itemstack.setAmount(1);
					return new ItemStack[]{itemstack};
				}
		}
		return new ItemStack[]{new ItemStack(Material.AIR)};
	}

	public static ItemStack createItemStackWhitAmount(final Material matrial, final int amount) {
		ItemStack itemstack = null;
		if (matrial != null) {
			itemstack = new ItemStack(matrial);
			itemstack.setAmount(amount);
		}
		return itemstack != null ? itemstack : new ItemStack(Material.AIR);
	}

}
