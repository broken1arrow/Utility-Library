package org.broken.arrow.library.itemcreator.utility.compound;


import org.broken.arrow.library.itemcreator.utility.nms.NBTAdapter;
import org.broken.arrow.library.itemcreator.utility.nms.api.NbtEditor;
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
    private final NbtEditor session;

    /**
     * Creates a new NbtData wrapper.
     *
     * @param itemStack the itemStack to alter the NBT tags on.
     */
    public NbtData(final ItemStack itemStack) {
        this.session = NBTAdapter.session(itemStack);
        Validate.checkNotNull(this.session,"The underlying NBT session could not be loaded.");
    }

    /**
     * something
     * @return Session
     */
    public NbtEditor getSession() {
        return session;
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
     * Checks whether this item contains an {@code NBTTagCompound} with the given name.
     * <p>
     * If the name is empty, the <strong>root compound</strong> is evaluated instead.
     * Both root and nested compounds are considered valid targets.
     *
     * @param name the custom key of the nested compound. To target the root compound,
     *             use an empty string or {@link #hasTag()}.
     * @return {@code true} if the specified (or root) compound exists
     */
    public boolean hasTag(@Nonnull final String name) {
            return this.session.hasTag(name);
    }

    /**
     * Returns the root {@link CompoundTag} of this item, creating one if it does not exist.
     * <p>
     * This method always operates on the root compound. If you want a nested compound,
     * use {@link #getOrCreateCompound(String)} with a specific name.
     *
     * @return the root {@link CompoundTag}, never {@code null} unless reflection failed.
     */
    @Nullable
    public CompoundTag getOrCreateCompound() {
        return this.session.getOrCreateCompound();
    }

    /**
     * Returns a {@link CompoundTag} with the given name, creating it if it does not exist.
     * <p>
     * If {@code name} is empty (""), this will return the root compound, which is equivalent
     * to {@link #getOrCreateCompound()}.
     * <p>
     * Use a non-empty name if you want a nested compound separate from the root.
     *
     * @param name the name of the nested compound, or empty string for root.
     * @return the existing or newly created {@link CompoundTag}, or {@code null} if reflection failed.
     */
    @Nullable
    public CompoundTag getOrCreateCompound(@Nonnull final String name) {
        return this.session.getOrCreateCompound(name);
    }


    /**
     * Returns the root {@link CompoundTag} if present.
     * <p>
     * This method does not create a new compound. Use {@link #getOrCreateCompound()} to
     * create a root compound if it does not exist.
     *
     * @return the root {@link CompoundTag} if present, otherwise {@code null}.
     */
    @Nullable
    public CompoundTag getCompound() {
        return this.session.getCompound();
    }

    /**
     * Returns the {@link CompoundTag} with the given name if present.
     * <p>
     * If {@code name} is empty (""), this returns the root compound.
     * For a nested compound, pass a non-empty name.
     * <p>
     * This method does not create a compound; use {@link #getOrCreateCompound(String)}
     * to create one if it does not exist.
     *
     * @param name the name of the nested compound, or empty string for root.
     * @return the existing {@link CompoundTag} if present, otherwise {@code null}.
     */
    @Nullable
    public CompoundTag getCompound(@Nonnull final String name) {
        return this.session.getCompound(name);
    }

    /**
     * Applies the current CompoundTag to the ItemStack and returns
     * a new Bukkit ItemStack instance.
     *
     * @return Returns the copy of your itemStack with the nbt set.
     */
    @Nullable
    public ItemStack apply() {
        return this.session.finalizeChanges();
    }

}
