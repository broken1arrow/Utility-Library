package org.broken.arrow.library.itemcreator.utility.compound;

import org.broken.arrow.library.itemcreator.utility.nms.ComponentItemDataSession;

import javax.annotation.Nonnull;

public final class VanillaCompoundTag extends CompoundTag {

    private final ComponentItemDataSession.VanillaComponentSession vanillaSession;

    /**
     * Create the base Component to set Minecraft data like minecraft:damage.
     *
     * @param base the component object (not used).
     * @param vanillaSession the section of the vanilla wrapper for the data set.
     */
    public VanillaCompoundTag(@Nonnull final Object base,@Nonnull final ComponentItemDataSession.VanillaComponentSession vanillaSession) {
        super(base);
        this.vanillaSession = vanillaSession;
    }

    @Override
    public void setInt(String key, int value) {
        vanillaSession.setInt(key, value);
    }

    @Override
    public void setString(String key, String value) {
        vanillaSession.setValue(key, value);
    }

    @Override
    public void remove(String key) {
        vanillaSession.remove(key);
    }

    @Override
    public boolean hasKey(String key) {
        return vanillaSession.hasKey(key);
    }

    @Override
    public int getInt(String key) {
        Object o = vanillaSession.getRaw(key);
        return (o instanceof Integer) ? (int) o : -1;
    }

    @Override
    public String getString(String key) {
        Object o = vanillaSession.getRaw(key);
        return (o instanceof String) ? o.toString() : "";
    }
}