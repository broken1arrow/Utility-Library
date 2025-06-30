package org.broken.arrow.library.nbt;


import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tr7zw.changeme.nbtapi.utils.MinecraftVersion.getVersion;


public class RegisterNbtAPI {
    private static final Logging logger = new Logging(RegisterNbtAPI.class);
    private final CompMetadata compMetadata;
    private static boolean hasScoreboardTags = true;

    public RegisterNbtAPI(Plugin plugin, boolean turnOffLogger) {
        Logger nbtLogger = Logger.getLogger("NBTAPI");
        if (turnOffLogger)
            nbtLogger.setLevel(Level.WARNING);
        getVersion();
        compMetadata = new CompMetadata(plugin);
        checkClassesExist();
    }

    private static void checkClassesExist() {
        try {
            Entity.class.getMethod("getScoreboardTags");
        } catch (NoSuchMethodException | SecurityException ignore) {
            hasScoreboardTags = false;
        }
    }

    @Nonnull
    public static byte[] serializeItemStack(final ItemStack[] itemStacks) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            NBT.itemStackArrayToNBT((itemStacks == null ? new ItemStack[0] : itemStacks)).writeCompound(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            logger.log(e,() -> "Could not serialize the itemStacks.");
        }
        return new byte[0];
    }

    @Nullable
    public static ItemStack[] deserializeItemStack(final byte[] itemStacks) {
        if (itemStacks == null) return new ItemStack[0];

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(itemStacks)) {
            return NBT.itemStackArrayFromNBT(  new NBTContainer(byteArrayInputStream));
        } catch (IOException e) {
            logger.log(e,() -> "Could not deserialize the itemStacks.");
        }
        return new ItemStack[0];
    }

    public static boolean isHasScoreboardTags() {
        return hasScoreboardTags;
    }

    /**
     * work in progress. Will later fix this so you can save data (is optional
     * method to tr7zw file saving)
     */
    public void yamlLoad() {
        // work in progress.
    }

    /**
     * Get methods to easy set metadata. If you want to set up self you can start
     * with this classes {@link de.tr7zw.changeme.nbtapi.NBTItem} and
     * {@link de.tr7zw.changeme.nbtapi.NBTEntity}
     * <p>&nbsp;</p>
     * <p>
     * Note: Should use these methods, give you better performance, if you don't use my methods.
     * </p>
     * <p>
     * {@link de.tr7zw.changeme.nbtapi.NBT#get(org.bukkit.inventory.ItemStack, java.util.function.Function)} and {@link de.tr7zw.changeme.nbtapi.NBT#get(org.bukkit.entity.Entity, java.util.function.Function)}
     * </p>
     * <p>
     * {@link de.tr7zw.changeme.nbtapi.NBT#modify(org.bukkit.entity.Entity, java.util.function.Function)} and {@link de.tr7zw.changeme.nbtapi.NBT#modify(org.bukkit.inventory.ItemStack, java.util.function.Function)}
     * </p>
     *
     * @return CompMetadata class.
     */
    public CompMetadata getCompMetadata() {
        return compMetadata;
    }

}
