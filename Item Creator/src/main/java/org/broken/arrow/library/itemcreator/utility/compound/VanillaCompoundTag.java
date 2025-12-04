package org.broken.arrow.library.itemcreator.utility.compound;

import org.broken.arrow.library.itemcreator.utility.nms.ComponentItemDataSession;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VanillaCompoundTag extends CompoundTag {

    private final ComponentItemDataSession.VanillaComponentSession vanillaSession;

    /**
     * Create the base Component to set Minecraft data like minecraft:damage.
     *
     * @param base           the component object (not used).
     * @param vanillaSession the section of the vanilla wrapper for the data set.
     */
    public VanillaCompoundTag(@Nonnull final Object base, @Nonnull final ComponentItemDataSession.VanillaComponentSession vanillaSession) {
        super(base);
        this.vanillaSession = vanillaSession;
    }

    @Override
    public void remove(@Nonnull final String key) {
        vanillaSession.remove(key);
    }

    @Override
    public boolean hasKey(@Nonnull final String key) {
        return vanillaSession.hasKey(key);
    }


    @Override
    public void setInt(@Nonnull String key, int value) {
        this.vanillaSession.setInt(key, value);
    }

    @Override
    public void setString(@Nonnull String key, String value) {
        this.vanillaSession.setString(key, value);
    }

    @Override
    public void setBoolean(@Nonnull final String key, final boolean value) {
        this.vanillaSession.setBoolean(key, value);
    }

    @Override
    public void setByte(@Nonnull final String key, final byte value) {
        this.vanillaSession.setByte(key, value);
    }

    @Override
    public void setByteArray(@Nonnull final String key, final byte[] value) {
        this.vanillaSession.setByteArray(key, value);
    }

    @Override
    public void setShort(@Nonnull final String key, final short value) {
        this.vanillaSession.setShort(key, value);
    }

    @Override
    public int getInt(@Nonnull final String key) {
        return this.vanillaSession.getInt(key);
    }

    @Override
    public String getString(@Nonnull final String key) {
        return this.vanillaSession.getString(key);
    }

    @Override
    public boolean getBoolean(@Nonnull final String key) {
        return this.vanillaSession.getBoolean(key);
    }

    @Override
    public byte getByte(@Nonnull final String key) {
        return this.vanillaSession.getByte(key);
    }

    @Nullable
    @Override
    public byte[] getByteArray(@Nonnull final String key) {
        return this.vanillaSession.getByteArray(key);
    }

    @Override
    public short getShort(@Nonnull final String key) {
        return this.vanillaSession.getShort(key);
    }
}