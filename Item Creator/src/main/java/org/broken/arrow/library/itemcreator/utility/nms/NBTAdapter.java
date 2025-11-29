package org.broken.arrow.library.itemcreator.utility.nms;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.utility.compound.CompoundTag;
import org.broken.arrow.library.itemcreator.utility.compound.NbtData;
import org.broken.arrow.library.itemcreator.utility.nms.api.CompoundEditor;
import org.broken.arrow.library.itemcreator.utility.nms.api.NbtEditor;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class NBTAdapter {
    private static final Logging logger = new Logging(NBTAdapter.class);
    private static final float serverVersion;

    static {
        serverVersion = ItemCreator.getServerVersion();
    }

    private NBTAdapter() {
    }

    /**
     * Creates a new {@link NbtEditor} for the given Bukkit {@link ItemStack}.
     *
     * <p>This method converts the provided {@link ItemStack} into its internal
     * NMS representation and prepares all required reflective access for
     * reading and writing its underlying {@code NBTTagCompound}.</p>
     *
     * <p>If the required NMS classes or methods could not be resolved during
     * startup, this method will throw an {@link IllegalStateException}. Always
     * verify availability first via {@link NbtEditor#isReady()}, it will have
     * checks so nothing breaks if you miss it.</p>
     *
     * <p>This method is intended to be used internally by {@link NbtData} and
     * other bridge classes. End-user plugin code should prefer {@link NbtData}.</p>
     *
     * @param stack the Bukkit ItemStack to wrap
     * @return a new {@link  NbtEditor} for the given ItemStack
     * @throws IllegalStateException if the NMS bridge is not available
     */
    public static NbtEditor session(@Nonnull final ItemStack stack) {
        if (serverVersion > 13.2) {
            if (!ModernNBT.NmsItemSession.REFLECTION_READY) {
                logger.log(Level.WARNING, () -> "NMS bridge not loaded");
                return null;
            }
            return new ModernNBT.NmsItemSession(stack);
        }
        if (!LegacyNBT.NmsItemSession.REFLECTION_READY) {
            logger.log(Level.WARNING, () -> "NMS bridge not loaded");
            return null;
        }
        return new LegacyNBT.NmsItemSession(stack);
    }

    /**
     * Creates a new {@link CompoundEditor} for the given NBTTagCompound handle.
     *
     * <p>This method is responsible for binding reflective access to the
     * underlying {@code NBTTagCompound} instance. It enables operations such
     * as {@code hasKey}, {@code setBoolean}, and {@code getBoolean}</p>
     *
     * <p>This is a low-level internal factory and is used by {@link CompoundTag}.
     * End-user code should never call this method directly.</p>
     *
     * @param handle the raw NBTTagCompound instance from NMS
     * @return a new {@link CompoundEditor} bound to the provided handle
     * @throws IllegalStateException if the CompoundSession layer is not available
     */
    public static CompoundEditor compoundSession(@Nonnull final Object handle) {
        if (serverVersion > 13.2) {
            if (!ModernNBT.CompoundSession.isReady()) {
                logger.log(Level.WARNING, () -> "CompoundSession not loaded");
                return null;
            }
            return new ModernNBT.CompoundSession(handle);
        }
        if (!LegacyNBT.CompoundSession.isReady()) {
            logger.log(Level.WARNING, () -> "CompoundSession not loaded");
            return null;
        }
        return new LegacyNBT.CompoundSession(handle);
    }


}
