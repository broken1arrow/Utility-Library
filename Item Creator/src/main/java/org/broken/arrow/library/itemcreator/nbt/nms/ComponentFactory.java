package org.broken.arrow.library.itemcreator.nbt.nms;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.nbt.nms.api.CompoundEditor;
import org.broken.arrow.library.itemcreator.nbt.nms.api.NbtEditor;
import org.broken.arrow.library.itemcreator.nbt.nms.modal.ComponentAdapter;
import org.broken.arrow.library.itemcreator.nbt.nms.modal.CompoundSession;
import org.broken.arrow.library.itemcreator.nbt.nms.modal.NBTLegacyAdapter;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.logging.Level;

/**
 * Factory for creating NBT/Component editors for Bukkit {@link org.bukkit.inventory.ItemStack} instances.
 *
 * <p>This factory decides which session implementation to return depending on the
 * server version:</p>
 * <ul>
 *   <li>On modern versions (1.20.5+), it returns a {@link ComponentAdapter} for
 *       manipulating item components (CUSTOM_DATA + optional vanilla components).</li>
 *   <li>On older versions, it returns an {@link NBTLegacyAdapter} for direct manipulation
 *       of legacy NBT compounds.</li>
 * </ul>
 *
 * <p>The factory handles all reflection readiness checks. If the necessary bridge
 * classes failed to initialize, methods will return {@code null} and log a warning.
 * Higher-level APIs, like {@link NbtWrapper}, are expected to handle this safely.</p>
 *
 * <p>Plugin developers should generally <b>not</b> call this class directly. Use
 * {@link NbtWrapper} for version-independent item data operations.</p>
 */
public class ComponentFactory {
    private static final Logging logger = new Logging(ComponentFactory.class);

    private ComponentFactory() {
    }

    /**
     * Creates a new {@link NbtEditor} for the given Bukkit {@link ItemStack}.
     *
     * @param stack the Bukkit ItemStack to wrap
     * @return the appropriate {@link NbtEditor} implementation for the server version,
     * or {@code null} if reflection initialization failed
     */
    public static NbtEditor session(@Nonnull final ItemStack stack) {
        if (ItemCreator.getVersion().compareTo(20,4).newer()) {
            final ComponentAdapter componentItem = new ComponentAdapter(stack);
            if (!componentItem.isReady()) {
                logger.log(Level.WARNING, () -> "NMS bridge not loaded");
                return null;
            }
            return componentItem;
        }
        final NBTLegacyAdapter legacyAdapter = new NBTLegacyAdapter(stack);
        if (!legacyAdapter.isReady()) {
            logger.log(Level.WARNING, () -> "NMS bridge not loaded");
            return null;
        }
        return legacyAdapter;
    }


    /**
     * Creates a new {@link CompoundEditor} for the given NMS {@code NBTTagCompound} handle
     * or a {@code CustomData} object in Minecraft 1.20.5+.
     *
     * <p>This method binds reflective access to the underlying object, enabling
     * operations such as {@code hasKey}, {@code setBoolean}, and {@code getBoolean}.</p>
     *
     * <p>If the reflection layer is not fully initialized or fail the reflection, this method will return
     * {@code null} and log a warning.</p>
     *
     * @param handle the raw {@code NBTTagCompound} instance from NMS or the
     *               {@code CustomData} object in 1.20.5+
     * @return a new {@link CompoundEditor} bound to the provided handle,
     * or {@code null} if the reflection layer is not ready
     */
    public static CompoundEditor compoundSession(@Nonnull final Object handle) {
        CompoundSession compoundSession = new CompoundSession(handle);
        if (!compoundSession.isReady()) {
            logger.log(Level.WARNING, () -> "CompoundSession not loaded");
            return null;
        }
        return compoundSession;
    }

}
