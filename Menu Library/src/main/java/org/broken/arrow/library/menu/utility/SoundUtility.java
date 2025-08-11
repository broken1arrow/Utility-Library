package org.broken.arrow.library.menu.utility;

import com.google.common.base.Enums;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.util.logging.Level;

/**
 * Utility class to manage sounds used in menus, adapting to different Minecraft server versions.
 * <p>
 * This class attempts to load a default "menu open" sound based on the server version.
 * For newer versions (1.20.1 and above), it uses the {@link Registry} to fetch the sound by namespaced key.
 * For older versions, it falls back to legacy sound enum names, handling possible missing enums gracefully.
 * </p>
 * <p>
 * Any failure to load the sound due to class cast issues will be logged as a warning.
 * </p>
 */
public class SoundUtility {
    private final Logging logger = new Logging(SoundUtility.class);
    private Sound menuOpenSound;

    /**
     * Constructs a SoundUtility instance and initializes the default menu open sound
     * based on the current server version.
     * <p>
     * Uses {@link ServerVersion} to determine which sound API to use:
     * <ul>
     *   <li>For Minecraft 1.20.1 and above, retrieves sound from {@link Registry} using namespaced key</li>
     *   <li>For older versions, uses legacy {@link Sound} enum names with fallback checks</li>
     * </ul>
     * </p>
     * If the sound cannot be loaded due to a {@link ClassCastException}, logs a warning message.
     */
    public SoundUtility() {
        try {
            if (ServerVersion.atLeast(ServerVersion.V1_20_1)) {
                this.menuOpenSound = Registry.SOUNDS.get(NamespacedKey.fromString("block.note_block.basedrum"));
            } else {
                this.menuOpenSound = Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BLOCK_BASEDRUM").orNull() == null ? Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BASEDRUM").orNull() : Enums.getIfPresent(Sound.class, "BLOCK_NOTE_BLOCK_BASEDRUM").orNull();
            }
        } catch (ClassCastException exception) {
            logger.log(Level.WARNING, () -> "Could not load default sound for this menu.");
        }
    }

    /**
     * Gets the sound used when opening a menu.
     *
     * @return the {@link Sound} instance representing the menu open sound,
     *         or null if the sound could not be loaded.
     */
    public Sound getMenuOpenSound() {
        return menuOpenSound;
    }
}
