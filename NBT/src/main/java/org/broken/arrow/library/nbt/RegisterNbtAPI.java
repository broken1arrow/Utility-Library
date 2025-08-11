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
/**
 * Utility class for registering and interacting with the NBT API
 * ({@link de.tr7zw.changeme.nbtapi}) in a Bukkit/Spigot/Paper environment.
 * <p>
 * This class provides:
 * <ul>
 *   <li>Initialization and compatibility checks for the NBT API</li>
 *   <li>Optional suppression of the NBT API's internal logger</li>
 *   <li>Serialization and deserialization of {@link ItemStack} arrays to and from byte arrays</li>
 *   <li>Convenience access to {@link CompMetadata} for metadata handling</li>
 * </ul>
 */
public class RegisterNbtAPI {
    private static final Logging logger = new Logging(RegisterNbtAPI.class);
    private final CompMetadata compMetadata;
    private static boolean hasScoreboardTags = true;

    /**
     * Creates a new {@code RegisterNbtAPI} instance.
     *
     * @param plugin         the owning {@link Plugin} instance
     * @param turnOffLogger  if {@code true}, suppresses the default {@code NBTAPI} logger output
     */
    public RegisterNbtAPI(Plugin plugin, boolean turnOffLogger) {
        Logger nbtLogger = Logger.getLogger("NBTAPI");
        if (turnOffLogger)
            nbtLogger.setLevel(Level.WARNING);
        getVersion();
        compMetadata = new CompMetadata(plugin);
        checkClassesExist();
    }

    /**
     * Checks whether the {@link Entity#getScoreboardTags()} method exists,
     * indicating scoreboard tag support in the running server version.
     */
    private static void checkClassesExist() {
        try {
            Entity.class.getMethod("getScoreboardTags");
        } catch (NoSuchMethodException | SecurityException ignore) {
            hasScoreboardTags = false;
        }
    }

    /**
     * Serializes an array of {@link ItemStack}s into a compressed byte array
     * using the NBT API. In the shaded NBT API version (from 2.15.0),
     * this process also automatically applies data fixes via DataFixerUpper
     * when upgrading the server version.
     *
     * @param itemStacks the array of item stacks to serialize; may be {@code null}
     * @return a byte array representation of the items, or an empty array if serialization fails
     */
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

    /**
     * Deserializes a byte array into an array of {@link ItemStack}s using the NBT API.
     * In the shaded NBT API version (from 2.15.0), this process also automatically applies
     * data fixes via DataFixerUpper when upgrading the server version.
     *
     * @param itemStacks the serialized byte array; may be {@code null}
     * @return the deserialized item stacks, or an empty array if deserialization fails
     */
    @Nullable
    public static ItemStack[] deserializeItemStack(final byte[] itemStacks) {
        if (itemStacks == null) return new ItemStack[0];

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(itemStacks)) {
            return NBT.itemStackArrayFromNBT(NBT.readNBT(  byteArrayInputStream));
        } catch (IOException e) {
            logger.log(e,() -> "Could not deserialize the itemStacks.");
        }
        return new ItemStack[0];
    }

    /**
     * Checks whether the current server version supports scoreboard tags.
     *
     * @return {@code true} if scoreboard tags are available; {@code false} otherwise
     */
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
     * Gets a {@link CompMetadata} instance, which provides optimized helper methods
     * for setting and retrieving NBT data from items and entities.
     * <p>
     * <b>Note:</b> While you can directly use {@link de.tr7zw.changeme.nbtapi.NBTItem} and
     * {@link de.tr7zw.changeme.nbtapi.NBTEntity}, using {@code CompMetadata} may be easier
     * to use as you don't have to deal with the compound key. As that needs to be unique or
     * it could write over another plugins data.
     *
     * @return the {@code CompMetadata} helper instance
     */
    public CompMetadata getCompMetadata() {
        return compMetadata;
    }

}
