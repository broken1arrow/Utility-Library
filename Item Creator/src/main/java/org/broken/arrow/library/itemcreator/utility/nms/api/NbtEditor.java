package org.broken.arrow.library.itemcreator.utility.nms.api;

import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.nms.NBTAdapter;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A transient interface for modifying an Item's NBT data.
 */
public interface NbtEditor {

    /**
     * Checks if it has loaded all reflections.
     *
     * @return true if everything is loaded correctly.
     */
    boolean isReady();

    /**
     * Checks if the item has a NBTTagCompound.
     *
     * @return {@code true} if it has an NBTTagCompound.
     */
    boolean hasTag();

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
    boolean hasTag(@Nonnull final String name);

    /**
     * Returns the root {@link CompoundTag} of this item, creating one if it does not exist.
     * <p>
     * This method always operates on the root compound. If you want a nested compound,
     * use {@link #getOrCreateCompound(String)} with a specific name.
     *
     * @return the root {@link CompoundTag}, never {@code null} unless reflection failed.
     */
    @Nullable
    CompoundTag getOrCreateCompound();

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
    CompoundTag getOrCreateCompound(@Nonnull final String name);


    /**
     * Returns the root {@link CompoundTag} if present.
     * <p>
     * This method does not create a new compound. Use {@link #getOrCreateCompound()} to
     * create a root compound if it does not exist.
     *
     * @return the root {@link CompoundTag} if present, otherwise {@code null}.
     */
    @Nullable
    CompoundTag getCompound();

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
    CompoundTag getCompound(@Nonnull final String name);

    /**
     * Applies the current NBT data of this item to the underlying {@link ItemStack} and
     * returns a new Bukkit {@link ItemStack} instance.
     * <p>
     * This method always applies the root {@link CompoundTag}, including any nested compounds
     * created via {@link #getOrCreateCompound(String)}. The returned item will contain the
     * full NBT structure currently set in this session.
     * <p>
     * The method checks the {@link NBTAdapter.CompoundState} before applying changes:
     * <ul>
     *     <li>{@link NBTAdapter.CompoundState#CREATED}: Compound exists and will be applied.</li>
     *     <li>{@link NBTAdapter.CompoundState#NULL}: No compound exists, nothing is applied.</li>
     *     <li>{@link NBTAdapter.CompoundState#ERROR}: Reflection failed or compound initialization failed,
     *     nothing is applied.</li>
     *     <li>{@link NBTAdapter.CompoundState#NOT_CREATED}: No compound has been created yet.</li>
     * </ul>
     * <p>
     * Use {@link #getOrCreateCompound()} or {@link #getOrCreateCompound(String)} to ensure a
     * compound exists before calling this method.
     *
     * @return a new {@link ItemStack} containing the applied NBT, or the original {@link ItemStack}
     * if the compound was not created or an error occurred.
     */
    @Nonnull
    ItemStack finalizeChanges();

}
