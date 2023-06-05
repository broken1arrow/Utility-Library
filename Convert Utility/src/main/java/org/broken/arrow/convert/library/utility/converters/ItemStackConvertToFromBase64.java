package org.broken.arrow.convert.library.utility.converters;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class is used to convert from and too Base64 string.
 */

public class ItemStackConvertToFromBase64 {
	/**
	 * A method to serialize an {@link org.bukkit.inventory.ItemStack} array to Base64 String.
	 * Based of {@link #itemStackArrayFromBase64(String)}.
	 *
	 * @param items to turn into a Base64 String.
	 * @return Base64 string of the items.
	 */

	public static String itemStackArrayToBase64(@Nonnull final ItemStack[] items) {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			// Write the size of the inventory
			dataOutput.writeInt(items.length);

			// Save every element in the list
			for (int i = 0; i < items.length; i++) {
				dataOutput.writeObject(items[i]);
			}

			// Serialize that array
			dataOutput.close();
		} catch (final IOException exception) {
			exception.printStackTrace();
		}
		return Base64Coder.encodeLines(outputStream.toByteArray());
	}

	/**
	 * Gets an array of ItemStacks from Base64 string.
	 * Base of {@link #itemStackArrayToBase64(org.bukkit.inventory.ItemStack[])}}.
	 *
	 * @param data Base64 string to convert to ItemStack array.
	 * @return ItemStack array created from the Base64 string.
	 */

	public static ItemStack[] itemStackArrayFromBase64(@Nonnull final String data) {
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
		ItemStack[] items = new ItemStack[0];
		try {
			final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
			items = new ItemStack[dataInput.readInt()];
			// Read the serialized inventory
			for (int i = 0; i < items.length; i++) {
				items[i] = (ItemStack) dataInput.readObject();
			}
			dataInput.close();
		} catch (final IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return items;
	}

}
