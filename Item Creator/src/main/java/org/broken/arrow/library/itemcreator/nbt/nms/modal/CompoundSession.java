package org.broken.arrow.library.itemcreator.nbt.nms.modal;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.nbt.nms.api.CompoundEditor;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.NbtCompoundWrapper;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.NbtCompoundAccessor;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.modal.v_21.ModernCompoundWrapper;
import org.broken.arrow.library.itemcreator.nbt.nms.utily.NbtPathsUtil;
import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;


/**
 * A per-NBTTagCompound reflective session. Provides low-level access to manipulate
 * properties on the NMS compound.
 *
 * <p>Should generally be used via {@link CompoundTag} rather than directly.</p>
 */
public class CompoundSession implements CompoundEditor {
    private static final Logging logger = new Logging(CompoundSession.class);
    private final Object handle;
    private final NbtCompoundAccessor compoundName;

    /**
     * Used internally not recommended to create own instance.
     *
     * @param handle the raw {@code NBTTagCompound} instance from NMS or the
     *               {@code CustomData} object in 1.20.5+
     */
    public CompoundSession(@Nonnull final Object handle) {
        Validate.checkNotNull(handle, "CompoundTag handle cannot be null");
        this.handle = handle;
        if (ItemCreator.getVersion().compareTo(21, 0).atLeast())
            this.compoundName = new ModernCompoundWrapper(handle);
        else
            this.compoundName = new NbtCompoundWrapper(handle);
    }

    /**
     * Checks if it has loaded all reflections.
     *
     * @return true if everything is loaded correctly.
     */
    public boolean isReady() {
        return compoundName.isReady();
    }

    @Override
    @Nonnull
    public Object getHandle() {
        return handle;
    }

    @Override
    public boolean hasKey(@Nonnull final String key) {
        return this.compoundName.hasKey(key);
    }

    @Override
    public void remove(@Nonnull final String key) {
        this.compoundName.remove(key);
    }

    @Override
    public void setInt(@Nonnull final String key, final int value) {
        this.compoundName.setInt(key, value);
    }

    @Override
    public int getInt(@Nonnull final String key) {
        return this.compoundName.getInt(key);
    }

    @Override
    public void setString(@Nonnull final String key, final String value) {
        this.compoundName.setString(key, value);
    }

    @Override
    @Nonnull
    public String getString(@Nonnull final String key) {
        return this.compoundName.getString(key);
    }

    @Override
    public void setByte(@Nonnull final String key, final byte value) {
        this.compoundName.setByte(key, value);
    }

    @Override
    public byte getByte(@Nonnull final String key) {
        return this.compoundName.getByte(key);
    }

    @Override
    public void setByteArray(@Nonnull final String key, final byte[] value) {
        this.compoundName.setByteArray(key, value);
    }

    @Override
    @Nullable
    public byte[] getByteArray(@Nonnull final String key) {
        return this.compoundName.getByteArray(key);
    }

    @Override
    public void setBoolean(@Nonnull final String key, final boolean value) {
        this.compoundName.setBoolean(key, value);
    }

    @Override
    public boolean getBoolean(@Nonnull final String key) {
        return this.compoundName.getBoolean(key);
    }

    @Override
    public void setShort(@Nonnull final String key, final short value) {
        this.compoundName.setShort(key, value);
    }

    @Override
    public short getShort(@Nonnull final String key) {
        return this.compoundName.getShort(key);
    }
}
