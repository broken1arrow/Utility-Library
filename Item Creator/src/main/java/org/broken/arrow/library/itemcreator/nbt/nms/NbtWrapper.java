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
 * <li><strong>1.20.5+ (Modern):</strong> Transparently routes custom values to the
 * {@code minecraft:custom_data} component when a name is provided, and maps vanilla
 * tags directly to native Minecraft Data Components.</li>
 * <li><strong>Legacy Versions:</strong> Directly modifies the item's underlying
 * {@code NBTTagCompound}.</li>
 * </ul>
 *
 * <strong>Usage Example:</strong>
 * <pre>{@code
 * NbtWrapper nbt = NbtWrapper.makeNbt(item);
 *
 * // Always use a unique identifier for your plugin!
 * // Empty strings or generic names risk colliding with vanilla tags or other plugins.
 * CompoundTag customData = nbt.getOrCreateCompound("MyPlugin");
 * customData.putString("key", "value");
 *
 * // To remove a key from your compound:
 * customData.remove("key");
 * // Note: If removing a key leaves the compound empty, it will be automatically
 * // cleared from the item during the apply phase.
 *
 * // You MUST capture the returned item stack!
 * ItemStack updatedItem = nbt.apply();
 * }</pre>
 *
 * <strong>Fallback Behavior:</strong>
 * <p>All {@code getOrCreateCompound} methods are guaranteed to return a non-null
 * {@link CompoundTag}. If the underlying NMS layer fails to initialize, this wrapper
 * gracefully enters a safe fallback mode. In this state, your code will not throw
 * exceptions, but <strong>no modifications will be written back</strong> to the {@link ItemStack}.
 * Use {@link #isReflectionReady()} to verify support.</p>
 */
public class NbtWrapper {
    private final NbtEditor session;

    /**
     * Creates a new {@code NbtWrapper} session for the given item stack.
     *
     * @param itemStack the Bukkit item stack to modify
     * @throws Validate.ValidateExceptions if the underlying NMS session fails to initialize
     */
    public NbtWrapper(final ItemStack itemStack) {
        this.session = ComponentFactory.session(itemStack);
        Validate.checkNotNull(this.session, "The underlying NBT session could not be loaded.");
    }

    /**
     * Creates a new {@code NbtWrapper} for the given item stack.
     *
     * @param itemStack the item stack to modify
     * @return a new {@code NbtWrapper} instance
     */
    public static NbtWrapper makeNbt(final ItemStack itemStack) {
        return new NbtWrapper(itemStack);
    }

    /**
     * Returns the underlying transient session.
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
     * @return {@code true} if NBT/component operations are fully supported
     */
    public boolean isReflectionReady() {
        return this.session.isReady();
    }

    /**
     * Checks whether the item has a root compound.
     *
     * <ul>
     *   <li><strong>1.20.5+</strong>: Evaluates the internal state, including pending
     *   (unapplied) changes. Calling {@link #apply()} will synchronize these changes
     *   with the underlying {@link ItemStack}.</li>
     *   <li><strong>Older versions</strong>: Directly checks whether the item has a root compound.</li>
     * </ul>
     *
     * <p>Note: This method reflects the wrapper's current state, which may include
     * unapplied modifications.</p>
     *
     * @return {@code true} if a root {@code NBTTagCompound} or modern {@code CompoundTag} exists
     */
    public boolean hasTag() {
        return this.session.hasTag();
    }


    /**
     * Checks whether this item contains a compound with the given name.
     * <p>
     * If the name is empty, the <strong>root compound</strong> is evaluated.
     *
     * @param name the name of the compound; use an empty string for the root compound
     * @return {@code true} if the specified compound exists
     */
    public boolean hasTag(@Nonnull final String name) {
        return this.session.hasTag(name);
    }

    /**
     * Returns the root {@link CompoundTag}, creating it if it does not exist.
     *
     * @return the root {@link CompoundTag}, never {@code null}
     */
    @Nonnull
    public CompoundTag getOrCreateCompound() {
        return this.session.getOrCreateCompound();
    }

    /**
     * Returns a {@link CompoundTag} with the given name, creating it if necessary.
     *
     * <ul>
     *   <li><strong>1.20.5+</strong>: Named compounds are stored under the
     *   {@code custom_data} component. It is recommended to use {@link #getOrCreateCompound()}
     *   for the root rather than using an empty string.</li>
     *   <li><strong>Older versions</strong>: Non-empty names create nested compounds under the root.</li>
     * </ul>
     *
     * @param name the name of the compound, or an empty string for the root
     * @return the existing or newly created {@link CompoundTag}, never {@code null}
     */
    @Nonnull
    public CompoundTag getOrCreateCompound(@Nonnull final String name) {
        return this.session.getOrCreateCompound(name);
    }

    /**
     * Returns the root {@link CompoundTag} if present.
     * <p>
     * This method does not create a compound. Use {@link #getOrCreateCompound()}
     * to create one if it does not exist.
     *
     * @return the root {@link CompoundTag}, or {@code null} if not present
     */
    @Nullable
    public CompoundTag getCompound() {
        return this.session.getCompound();
    }

    /**
     * Returns the {@link CompoundTag} with the given name if present.
     * <p>
     * This method does not create a compound, use {@link #getOrCreateCompound(String)}
     * if creation is required.
     *
     * @param name the name of the compound. For the root compound, prefer using {@link #getCompound()}.
     * @return the existing {@link CompoundTag}, or {@code null} if not present
     */
    @Nullable
    public CompoundTag getCompound(@Nonnull final String name) {
        return this.session.getCompound(name);
    }

    /**
     * Applies the current {@link CompoundTag} modifications to the {@link ItemStack}.
     * <p>
     * <strong>Important:</strong> This does not modify the original item in-place.
     * You MUST capture and use the returned {@link ItemStack}.
     *
     * @return a new {@link ItemStack} with applied NBT data, or the original item if an error occurred
     */
    @Nullable
    public ItemStack apply() {
        return this.session.finalizeChanges();
    }

}
