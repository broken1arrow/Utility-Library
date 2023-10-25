package org.broken.arrow.nbt.library.utility;

import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ConvertObjectType {

	private ConvertObjectType() {
	}

	/**
	 * Sets an NBT value in the ReadWriteNBT compound. The value is cast to the appropriate class when retrieved from an entity or ItemStack.
	 * If a matching NBT supported class is not found, the value will be cast to a string.
	 *
	 * @param compound       The compound to which you want to set the value.
	 * @param key            The key to use for retrieving the value.
	 * @param object         The data you want to set.
	 */
	public static void setNBTValue(@Nonnull ReadWriteNBT compound, @Nonnull String key, @Nonnull Object object) {
		setNBTValue(compound,null,key,object);
	}

	/**
	 * Sets an NBT value in the ReadWriteNBT compound. The value is cast to the appropriate class when retrieved from an entity or ItemStack.
	 * If a matching NBT supported class is not found, the value will be cast to a string.
	 *
	 * @param compound       The compound to which you want to set the value.
	 * @param mergeCompound  If not null, old compound data will also be added to this compound.
	 * @param key            The key to use for retrieving the value.
	 * @param object         The data you want to set.
	 */
	public static void setNBTValue(@Nonnull ReadWriteNBT compound, @Nullable ReadWriteNBT mergeCompound, @Nonnull String key, @Nonnull Object object) {
		if (mergeCompound != null) {
			compound.mergeCompound(mergeCompound);
			return;
		}

		Class<?> targetType = object.getClass();

		if (targetType == String.class) {
			compound.setString(key, object.toString());
			return;
		}
		if (targetType == Integer.class ) {
			compound.setInteger(key, ((Number) object).intValue());
			return;
		}
		if (targetType == Double.class) {
			compound.setDouble(key, ((Number) object).doubleValue());
			return;
		}
		if (targetType == Byte.class) {
			compound.setByte(key, ((Byte) object));
			return;
		}
		if (targetType == Long.class) {
			compound.setLong(key, ((Number) object).longValue());
			return;
		}
		if (targetType == Float.class) {
			compound.setFloat(key, ((Number) object).floatValue());
			return;
		}
		if (targetType == ItemStack.class) {
			compound.setItemStack(key, (ItemStack) object);
			return;
		}
		if (targetType == UUID.class) {
			compound.setUUID(key, (UUID) object);
			return;
		}
		if (targetType.isArray() && object.getClass().isArray()) {
			// Handle array casting here, e.g., for int[] or ItemStack[]
			if (object instanceof byte[]) {
				compound.setByteArray(key, (byte[]) object);
				return;
			}
			if (object instanceof int[]) {
				compound.setIntArray(key, (int[]) object);
				return;
			}
			if (object instanceof ItemStack[]) {
				compound.setItemStackArray(key, (ItemStack[]) object);
				return;
			}
		}
		compound.setString(key, object + "");
	}
}
