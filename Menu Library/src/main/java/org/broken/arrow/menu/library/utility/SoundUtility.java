package org.broken.arrow.menu.library.utility;

import com.google.common.base.Enums;
import org.broken.arrow.logging.library.Logging;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.util.logging.Level;

public class SoundUtility {
    private final Logging logger = new Logging(SoundUtility.class);
    private Sound menuOpenSound;

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

    public Sound getMenuOpenSound() {
        return menuOpenSound;
    }
}
