package org.broken.arrow.library.itemcreator.persistent.data.container;

import org.broken.arrow.library.itemcreator.nbt.NBTValue;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.CompoundTag;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

/**
 * Utility class for working with NMS compounds.
 * <p>
 * This class provides helper methods for storing and retrieving custom
 * data types such as {@link String}, {@link Integer}, {@link Double},
 * {@link Byte}, {@link Long}, {@link Float}, {@link ItemStack}, {@link UUID},
 * and primitive arrays in an item's persistent data container.
 * <p>
 * It also contains custom implementations
 * for {@link UUID} and {@link ItemStack} values to allow complex type storage.
 */
public final class NBTDataUtility {
    private static final Logging LOG = new Logging(NBTDataUtility.class);
    private static final ItemStackTagType ITEM_TAG_TYPE = new ItemStackTagType();
    private static final UUIDItemTagType UUID_TAG_TYPE = new UUIDItemTagType();

    /**
     * Creates a new persistent data utility for the specified plugin.
     *
     */
    public NBTDataUtility(){}


    /**
     * Sets a value in the given {@link PersistentDataContainer} using the appropriate data type.
     * <p>
     * This method supports standard Bukkit types as well as custom types for
     * {@link UUID} and {@link ItemStack}, including array handling for
     * {@code byte[]}, {@code int[]}, and {@code long[]}.
     * <p>
     * If the {@link NBTValue} indicates that the key should be removed,
     * the key is deleted from the container.
     *
     * @param key           the key to store the value under
     * @param nbtValue      the value wrapper containing the actual value and removal flag
     * @param compound the persistent data container to modify
     */
    public void setPersistentData(final CompoundTag compound, final String key, final NBTValue nbtValue) {
        final Object value = nbtValue.getValue();
        final Class<?> targetType = value.getClass();

        if (nbtValue.isRemoveKey()) {
            compound.remove(key);
        }

        if (targetType == String.class) {
            compound.setString(key, (String) value);
            return;
        }
        if (targetType == Integer.class) {
            compound.setInt(key, (Integer) value);
            return;
        }
        if (targetType == Double.class) {
            compound.setDouble(key, (Double) value);
            return;
        }
        if (targetType == Byte.class) {
            compound.setByte(key, (Byte) value);
            return;
        }
        if (targetType == Long.class) {
            compound.setLong(key, (Long) value);
            return;
        }
        if (targetType == Float.class) {
            //dataContainer.setFloat(key, (Float) value);
            //return;
        }
        if (targetType == ItemStack.class) {
            compound.setString(key, ITEM_TAG_TYPE.toPrimitive((ItemStack) value));
            return;
        }
        if (targetType == UUID.class) {
            compound.setByteArray(key, UUID_TAG_TYPE.toPrimitive((UUID) value));
            return;
        }
        if (setArrays(targetType, compound, key, value)) return;

        compound.setString(key, value + "");
    }

    /**
     * Custom data type for storing {@link UUID} values in a {@code byte[]} format.
     * <p>
     * UUIDs are stored in a 16-byte array (two longs).
     */
    public static class UUIDItemTagType {

        @Nonnull
        public byte[] toPrimitive(@Nonnull final UUID uuid) {
            ByteBuffer buffer = ByteBuffer.allocate(16);
            buffer.putLong(uuid.getMostSignificantBits());
            buffer.putLong(uuid.getLeastSignificantBits());
            return buffer.array();
        }

        @Nonnull
        public UUID fromPrimitive(@Nonnull byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            long mostBits = buffer.getLong();
            long leastBits = buffer.getLong();
            return new UUID(mostBits, leastBits);
        }
    }

    /**
     * Custom data type for storing {@link ItemStack} values as {@link String}.
     * <p>
     * ItemStacks are serialized to a {@code Map}, converted to JSON, and Base64-encoded.
     * When reading, they are Base64-decoded, parsed from JSON, and deserialized.
     */
    public static class ItemStackTagType {

        public String toPrimitive(@Nonnull final ItemStack complex) {
            try {
                ByteArrayOutputStream io = new ByteArrayOutputStream();
                BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
                os.writeObject(complex);
                os.flush();
                byte[] serializedObject = io.toByteArray();
                return Base64.getEncoder().encodeToString(serializedObject);
            } catch (Exception e) {
                LOG.log(e, () -> "Could not serialize the itemStack to a compound.");
                return "";
            }
        }

        public ItemStack fromPrimitive(@Nonnull final String primitive) {
            try {
                byte[] serializedObject = Base64.getDecoder().decode(primitive);
                ByteArrayInputStream in = new ByteArrayInputStream(serializedObject);
                BukkitObjectInputStream is = new BukkitObjectInputStream(in);
                return (ItemStack) is.readObject();
            } catch (Exception e) {
                LOG.log(e, () -> "Could not deserialize the itemStack from a compound.");
                return null;
            }
        }
    }

    /**
     * Attempts to store array types in the {@link PersistentDataContainer}.
     * <p>
     * This method supports {@code byte[]}, {@code int[]}, and {@code long[]} arrays.
     *
     * @param targetType the type of the value
     * @param compound   the compound to modify
     * @param key        the key for the vale to set.
     * @param value      the array value to store
     * @return {@code true} if the array type was handled successfully; {@code false} otherwise
     */
    private boolean setArrays(final Class<?> targetType, final CompoundTag compound, final String key, final Object value) {
        if (targetType.isArray()) {
            if (value instanceof byte[]) {
                compound.setByteArray(key, (byte[]) value);
                return true;
            }
           if (value instanceof int[]) {
                compound.setIntArray(key, (int[]) value);
                return true;
            }
            if (value instanceof long[]) {
                compound.setLongArray(key, (long[]) value);
                return true;
            }
        }
        return false;
    }

}
