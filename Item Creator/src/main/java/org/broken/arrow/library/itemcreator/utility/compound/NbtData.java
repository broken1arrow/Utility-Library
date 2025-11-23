package org.broken.arrow.library.itemcreator.utility.compound;


import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps an NMS ItemStack and provides access to its NBTTagCompound through
 * the {@link CompoundTag} abstraction.
 *
 * <p>This class is only fully functional in legacy Minecraft versions where
 * certain vanilla item properties are not exposed by the Bukkit API.
 * These values (for example {@code Unbreakable}) are written at a low NMS
 * level and affect the actual behaviour of the item, unlike custom NBT
 * values used only by plugins.</p>
 */
public class NbtData {
    private static final Logging logger = new Logging(NbtData.class);
    LegacyNBT.NmsItemSession session;

    /**
     * Creates a new NMS bridge.
     *
     * <p>Checks for availability of {@link org.bukkit.inventory.meta.ItemMeta#setUnbreakable(boolean)}.
     * If present, reflection loading is skipped.</p>
     *
     * @param itemStack the itemStack to alter the metadata on.
     */
    public NbtData(final ItemStack itemStack) {
        this.session = LegacyNBT.session(itemStack);
        Validate.checkNotNull(this.session,"The session could not be loaded.");
    }

    /**
     * Indicates whether required NMS and NBT classes were successfully
     * resolved through reflection.
     *
     * @return {@code true} if legacy NBT operations are supported
     */
    public boolean isReflectionReady() {
        return this.session.isReady();
    }

    /**
     * Checks if the item has a NBTTagCompound.
     *
     * @return {@code true} if it has an NBTTagCompound.
     */
    public boolean hasTag() {
        return this.session.hasTag();
    }

    /**
     * Returns the existing {@link CompoundTag} if one is present,
     * otherwise creates a new one.
     *
     * @return the existing {@link CompoundTag} or a new instance if none exists.
     * Returns {@code null} only if reflection failed or the underlying
     * NBTTagCompound could not be created.
     */
    @Nullable
    public CompoundTag getOrCreateCompound() {
        return this.session.getOrCreateCompound();
    }

    /**
     * Returns the existing {@link CompoundTag} if one is present.
     *
     * @return the existing {@link CompoundTag} or {@code null} if none exists
     */
    @Nullable
    public CompoundTag getCompound() {
        return this.session.getCompound();
    }

    /**
     * Applies the current CompoundTag to the ItemStack and returns
     * a new Bukkit ItemStack instance.
     *
     * @param tag the {@link CompoundTag} instance that wraps NBTTagCompound.
     * @return Returns the copy of your itemStack with the nbt set.
     */
    @Nullable
    public ItemStack apply(@Nonnull final CompoundTag tag) {
        return this.session.apply(tag);
    }

}
