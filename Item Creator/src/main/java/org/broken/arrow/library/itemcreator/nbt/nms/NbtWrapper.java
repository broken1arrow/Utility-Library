package org.broken.arrow.library.itemcreator.nbt.nms;

import org.broken.arrow.library.itemcreator.nbt.nms.api.NbtEditor;
import org.broken.arrow.library.itemcreator.nbt.nms.compound.CompoundTag;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a Bukkit {@link ItemStack} and provides a unified, high-performance
 * interface for modifying its NBT and Data Components.
 *
 * <p>This wrapper automatically adapts to the running Minecraft server version:</p>
 * <ul>
 * <li><strong>1.20.5+ (Modern)</strong>: Transparently routes custom values to the
 * {@code minecraft:custom_data} component, and maps vanilla tags directly to native
 * Minecraft Data Components.</li>
 * <li><strong>Legacy Versions</strong>: Directly modifies the item's raw underlying
 * {@code NBTTagCompound}.</li>
 * </ul>
 */
public class NbtWrapper {
    private final NbtEditor session;

    /**
     * Creates a new NbtWrapper session for the given item stack.
     *
     * @param itemStack the Bukkit item stack to modify
     * @throws Validate.ValidateExceptions if the underlying NMS session fails to initialize
     */
    public NbtWrapper(final ItemStack itemStack) {
        this.session = ComponentFactory.session(itemStack);
        Validate.checkNotNull(this.session, "The underlying NBT session could not be loaded.");
    }

    /**
     * Factory method to create NBT modifier.
     *
     * @param itemStack The itemStack you want to modify.
     * @return Returns instance of the nbt wrapper.
     */
    public static NbtWrapper makeNbt(final ItemStack itemStack) {
        return new NbtWrapper(itemStack);
    }

    /**
     * Returns the underlying transient engine session.
     *
     * @return the active {@link NbtEditor} session
     */
    public NbtEditor getSession() {
        return session;
    }

    /**
     * Indicates whether the required NMS and reflection bindings
     * were successfully mapped for this server version.
     *
     * @return {@code true} if NBT/Component operations are fully supported
     */
    public boolean isReflectionReady() {
        return this.session.isReady();
    }

    /**
     * Checks if the item has a Root Compound.
     * <ul>
     *   <li><strong>1.20.5+</strong>: It will just check if the root is created, as you most provide the tag to check the correct thing.</li>
     *   <li><strong>Older versions</strong>: This method always operates on the root compound and
     *   will check if the stack have a root compound.
     *   </li>
     * </ul>
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
     * <ul>
     *   <li><strong>1.20.5+</strong>: You get the root tag with this method or
     *   when use {@link #getOrCreateCompound(String)} with an empty string.</li>
     *   <li><strong>Older versions</strong>: This method always operates on the root compound.
     *   If you want a nested compound, use {@link #getOrCreateCompound(String)} with a specific name.
     *   </li>
     * </ul>
     * This method always operates on the root compound. If you want a nested compound,
     * use {@link #getOrCreateCompound(String)} with a specific name.
     *
     * @return the root {@link CompoundTag}, never {@code null} unless reflection failed.
     */
    @Nonnull
    public CompoundTag getOrCreateCompound() {
        return this.session.getOrCreateCompound();
    }

    /**
     * Returns a {@link CompoundTag} with the given name, creating it if it does not exist.
     * <p>
     * <ul>
     *   <li><strong>1.20.5+</strong>: If you type root tag it will be created under {@code custom_data} tag,
     *   use the {@link #getOrCreateCompound()} to work under root tag or left variable empty.
     *   </li>
     *   <li><strong>Older versions</strong>: Use a non-empty name if you want a nested compound separate from the root,
     *   same as invoke {@link #getOrCreateCompound()}.
     *   </li>
     * </ul>
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
     * <ul>
     *   <li><strong>1.20.5+</strong>: You get the root tag with this method or
     *   when use {@link #getCompound(String)} with an empty string.</li>
     *   <li><strong>Older versions</strong>: This method always operates on the root compound.
     *   If you want a nested compound, use {@link #getCompound(String)} with a specific name.
     *   </li>
     * </ul>
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
     * This method does not create a compound; use {@link #getOrCreateCompound(String)}
     * to create one if it does not exist.
     *
     * <ul>
     *   <li><strong>1.20.5+</strong>: If you type root tag it will be created under {@code custom_data} tag,
     *   use the {@link #getCompound()} to work under root tag or left variable empty.
     *   </li>
     *   <li><strong>Older versions</strong>: Use a non-empty name if you want a nested compound separate from the root,
     *   same as invoke {@link #getCompound()}.
     *   </li>
     * </ul>
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
