package org.broken.arrow.library.itemcreator.utility.compound;

import org.broken.arrow.library.itemcreator.utility.nms.ComponentAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a wrapper for interacting with <b>vanilla item component data</b>
 * in modern Minecraft versions.
 *
 * <p>This class behaves similarly to {@link CompoundTag}, but instead of
 * editing legacy NBT structures (e.g., NBTTagCompound), it operates on the
 * modern <b>vanilla component system</b> which controls raw item properties
 * such as {@code minecraft:damage}, {@code minecraft:unbreakable}, and other
 * core item behaviours exposed through Minecraft’s data-driven component API.</p>
 *
 * <p>All operations on this tag are forwarded to a
 * {@link ComponentAdapter.VanillaComponentSession}, which provides the
 * reflection-based access necessary to manipulate these vanilla components.</p>
 *
 * <p><strong>Reflection loading:</strong><br>
 * Vanilla component support is loaded lazily. Reflection for vanilla
 * components is only initialized when
 * {@link ComponentAdapter#enableVanillaTagEditor()} is explicitly invoked.</p>
 *
 * <p>This wrapper is intended <b>only</b> for editing vanilla-defined item
 * properties. Plugin-specific or user-defined custom data should instead be
 * written to the CUSTOM_DATA component via {@link ComponentAdapter}.
 * </p>
 *
 * <p><strong>Valid keys:</strong>
 * The keys passed to this tag must correspond to actual vanilla item component
 * identifiers, such as {@code minecraft:damage}, {@code minecraft:unbreakable},
 * {@code minecraft:custom_model_data}, etc. The accepted set of keys varies
 * between Minecraft versions.</p>
 */
public final class VanillaComponentTag extends CompoundTag {

    private final ComponentAdapter.VanillaComponentSession vanillaSession;

    /**
     * Constructs a new wrapper for editing vanilla component data.
     *
     * @param base           the underlying component root object (not used directly)
     * @param vanillaSession the vanilla component session that performs all read/write operations
     */
    public VanillaComponentTag(@Nonnull final Object base, @Nonnull final ComponentAdapter.VanillaComponentSession vanillaSession) {
        super(base);
        this.vanillaSession = vanillaSession;
    }

    /**
     * Removes the specified vanilla component.
     *
     *
     * @param key the vanilla component key to remove
     */
    @Override
    public void remove(@Nonnull final String key) {
        vanillaSession.remove(key);
    }

    /**
     * Checks whether the given vanilla component exists.
     *
     * @param key the key to check
     * @return {@code true} if the component exists, otherwise {@code false}
     */
    @Override
    public boolean hasKey(@Nonnull final String key) {
        return vanillaSession.hasKey(key);
    }

    /**
     * Sets an integer-based vanilla component.
     *
     * @param key   the component key
     * @param value the integer value to assign
     */
    @Override
    public void setInt(@Nonnull String key, int value) {
        this.vanillaSession.setInt(key, value);
    }

    /**
     * Sets a string-based vanilla component.
     *
     * @param key   the component key
     * @param value the string value to assign
     */
    @Override
    public void setString(@Nonnull String key, String value) {
        this.vanillaSession.setString(key, value);
    }

    /**
     * Sets a boolean-based vanilla component.
     *
     * @param key   the component key, for example {@code minecraft:unbreakable}
     * @param value the boolean value to assign
     */
    @Override
    public void setBoolean(@Nonnull final String key, final boolean value) {
        this.vanillaSession.setBoolean(key, value);
    }

    /**
     * Sets a byte-based vanilla component.
     *
     * @param key   the component key
     * @param value the byte value to assign
     */
    @Override
    public void setByte(@Nonnull final String key, final byte value) {
        this.vanillaSession.setByte(key, value);
    }

    /**
     * Sets a byte array as a vanilla component.
     *
     * @param key   the component key
     * @param value the byte array value to assign
     */
    @Override
    public void setByteArray(@Nonnull final String key, final byte[] value) {
        this.vanillaSession.setByteArray(key, value);
    }

    /**
     * Sets a short-based vanilla component.
     *
     * @param key   the component key
     * @param value the short value to assign
     */
    @Override
    public void setShort(@Nonnull final String key, final short value) {
        this.vanillaSession.setShort(key, value);
    }

    /**
     * Retrieves an integer-based vanilla component.
     *
     * @param key the component key
     * @return the stored integer value
     */
    @Override
    public int getInt(@Nonnull final String key) {
        return this.vanillaSession.getInt(key);
    }

    /**
     * Retrieves a string-based vanilla component.
     *
     * @param key the component key
     * @return the stored string value
     */
    @Override
    public String getString(@Nonnull final String key) {
        return this.vanillaSession.getString(key);
    }

    /**
     * Retrieves a boolean-based vanilla component.
     *
     * @param key the component key
     * @return the stored boolean value
     */
    @Override
    public boolean getBoolean(@Nonnull final String key) {
        return this.vanillaSession.getBoolean(key);
    }

    /**
     * Retrieves a byte-based vanilla component.
     *
     * @param key the component key
     * @return the stored byte value
     */
    @Override
    public byte getByte(@Nonnull final String key) {
        return this.vanillaSession.getByte(key);
    }

    /**
     * Retrieves a byte array–based vanilla component.
     *
     * @param key the component key
     * @return the stored byte array, or {@code null} if absent
     */
    @Nullable
    @Override
    public byte[] getByteArray(@Nonnull final String key) {
        return this.vanillaSession.getByteArray(key);
    }

    /**
     * Retrieves a short-based vanilla component.
     *
     * @param key the component key
     * @return the stored short value
     */
    @Override
    public short getShort(@Nonnull final String key) {
        return this.vanillaSession.getShort(key);
    }
}