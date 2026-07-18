package org.broken.arrow.library.itemcreator.nbt.nms.api;

import org.broken.arrow.library.itemcreator.nbt.nms.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.nbt.nms.modal.NBTLegacyAdapter;
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
     * Returns the root {@link CompoundTag}, creating it if it does not exist.
     *
     * @return the root {@link CompoundTag}, never {@code null}
     */
    @Nonnull
    CompoundTag getOrCreateCompound();

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
    CompoundTag getOrCreateCompound(@Nonnull final String name);


    /**
     * Returns the root {@link CompoundTag} if present.
     * <p>
     * This method does not create a compound. Use {@link #getOrCreateCompound()}
     * to create one if it does not exist.
     *
     * @return the root {@link CompoundTag}, or {@code null} if not present
     */
    @Nullable
    CompoundTag getCompound();

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
    CompoundTag getCompound(@Nonnull final String name);

    /**
     * Applies the current NBT data of this item to the underlying {@link ItemStack} and
     * returns a new Bukkit {@link ItemStack} instance.
     * <p>
     * This method always applies the root {@link CompoundTag}, including any nested compounds
     * created via {@link #getOrCreateCompound(String)}. The returned item will contain the
     * full NBT structure currently set in this session.
     * <p>
     * The method checks the {@link NBTLegacyAdapter.CompoundState} before applying changes:
     * <ul>
     *     <li>{@link NBTLegacyAdapter.CompoundState#CREATED}: Compound exists and will be applied.</li>
     *     <li>{@link NBTLegacyAdapter.CompoundState#NULL}: No compound exists, nothing is applied.</li>
     *     <li>{@link NBTLegacyAdapter.CompoundState#ERROR}: Reflection failed or compound initialization failed,
     *     nothing is applied.</li>
     *     <li>{@link NBTLegacyAdapter.CompoundState#NOT_CREATED}: No compound has been created yet.</li>
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
