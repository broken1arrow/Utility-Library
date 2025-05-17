package org.broken.arrow.serialize.library.utility.converters;

import org.broken.arrow.logging.library.Logging;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.broken.arrow.logging.library.Logging.of;

/**
 * This class provides methods to convert an array of ItemStacks to and from a Base64 string representation.
 */
public class Base64ItemStackConverter {
	private static final Logging log = new Logging(Base64ItemStackConverter.class);
	private Base64ItemStackConverter() {
	}

	/**
	 * Serializes an array of ItemStacks to a Base64 string representation.
	 * This method is based on {@link #itemStackArrayFromBase64(String)}.
	 *
	 * @param items The ItemStack array to convert into a Base64 string.
	 * @return The Base64 string representation of the ItemStack array.
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
			log.log(exception, () -> of("Failed to convert the items to base64"));
		}
		return Base64Coder.encodeLines(outputStream.toByteArray());
	}

	/**
	 * Deserializes an array of ItemStacks from a Base64 string representation.
	 * This method is based on {@link #itemStackArrayToBase64(ItemStack[])}.
	 *
	 * @param data The Base64 string to convert into an ItemStack array.
	 * @return The ItemStack array created from the Base64 string.
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
			log.log(e, () -> of("Failed to convert the itemsfrom base64"));
		}
		return items;
	}
}
