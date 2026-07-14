package org.broken.arrow.library.itemcreator.nbt.nms.modal;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.nbt.nms.api.CompoundEditor;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.NbtCompoundWrapper;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.NbtCompoundAccessor;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.v_21.ModernCompoundWrapperTwentyOne;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.v_21_5.ModernCompoundWrapper;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * A per-NBTTagCompound reflective session. Provides low-level access to manipulate
 * properties on the NMS compound.
 *
 * <p>Should generally be used via {@link CompoundTag} rather than directly.</p>
 */
public class CompoundSession implements CompoundEditor {
    private static final Logging logger = new Logging(CompoundSession.class);
    private final Object handle;
    private final NbtCompoundAccessor compoundAccessor;

    /**
     * Used internally not recommended to create own instance.
     *
     * @param handle the raw {@code NBTTagCompound} instance from NMS or the
     *               {@code CustomData} object in 1.20.5+
     */
    public CompoundSession(@Nonnull final Object handle) {
        Validate.checkNotNull(handle, "CompoundTag handle cannot be null");
        this.handle = handle;
        if (ItemCreator.getVersion().compareTo(21, 5).atLeast())
            this.compoundAccessor = new ModernCompoundWrapper(handle);
        else if (ItemCreator.getVersion().compareTo(21, 0).atLeast()) {
            this.compoundAccessor = new ModernCompoundWrapperTwentyOne(handle);
        } else {
            this.compoundAccessor = new NbtCompoundWrapper(handle);
        }
    }

    /**
     * Checks if it has loaded all reflections.
     *
     * @return true if everything is loaded correctly.
     */
    public boolean isReady() {
        return compoundAccessor.isReady();
    }

    @Override
    @Nonnull
    public Object getHandle() {
        return handle;
    }

    @Override
    public boolean hasKey(@Nonnull final String key) {
        return this.compoundAccessor.hasKey(key);
    }

    @Override
    public void remove(@Nonnull final String key) {
        this.compoundAccessor.remove(key);
    }

    @Override
    public void setInt(@Nonnull final String key, final int value) {
        this.compoundAccessor.setInt(key, value);
    }

    @Override
    public int getInt(@Nonnull final String key) {
        return this.compoundAccessor.getInt(key);
    }

    @Override
    public void setDouble(@NonNull String key, double value) {
        this.compoundAccessor.setDouble(key, value);
    }

    @Override
    public double getDouble(@NonNull String key) {
        return this.compoundAccessor.getDouble(key);
    }

    @Override
    public void setLong(@NonNull String key, long value) {
        this.compoundAccessor.setLong(key, value);
    }

    @Override
    public long getLong(@NonNull String key) {
        return this.compoundAccessor.getLong(key);
    }

    @Override
    public void setString(@Nonnull final String key, final String value) {
        this.compoundAccessor.setString(key, value);
    }

    @Override
    @Nonnull
    public String getString(@Nonnull final String key) {
        return this.compoundAccessor.getString(key);
    }

    @Override
    public void setByte(@Nonnull final String key, final byte value) {
        this.compoundAccessor.setByte(key, value);
    }

    @Override
    public byte getByte(@Nonnull final String key) {
        return this.compoundAccessor.getByte(key);
    }

    @Override
    public void setByteArray(@Nonnull final String key, final byte[] value) {
        this.compoundAccessor.setByteArray(key, value);
    }

    @Override
    public void setIntArray(String key, int[] value) {
        this.compoundAccessor.setIntArray(key, value);
    }

    @Override
    public void setLongArray(String key, long[] value) {
        this.compoundAccessor.setLongArray(key, value);
    }

    @Override
    @Nullable
    public byte[] getByteArray(@Nonnull final String key) {
        return this.compoundAccessor.getByteArray(key);
    }

    @Override
    @NonNull
    public int[] getIntArray(String key) {
        return this.compoundAccessor.getIntArray(key);
    }

    @Override
    @NonNull
    public long[] getLongArray(String key) {
        return this.compoundAccessor.getLongArray(key);
    }


    @Override
    public void setBoolean(@Nonnull final String key, final boolean value) {
        this.compoundAccessor.setBoolean(key, value);
    }

    @Override
    public boolean getBoolean(@Nonnull final String key) {
        return this.compoundAccessor.getBoolean(key);
    }

    @Override
    public void setShort(@Nonnull final String key, final short value) {
        this.compoundAccessor.setShort(key, value);
    }

    @Override
    public short getShort(@Nonnull final String key) {
        return this.compoundAccessor.getShort(key);
    }
}
