package org.broken.arrow.library.itemcreator.utility;

import org.bukkit.block.banner.PatternType;

import javax.annotation.Nonnull;
import java.util.Locale;

public final class PatternTypeUtil {

    private PatternTypeUtil() {}

    /**
     * Get the string ID for the banner pattern.
     * @param type the type of pattern.
     * @return returns the id.
     */
    public static String toId(@Nonnull final PatternType type) {
        return type.getIdentifier();
    }

    /**
     * Get the type from the string id.
     *
     * @param input the name for the id or the enum name.
     * @return the pattern type or null if it does not find it.
     */
    public static PatternType fromString(final String input) {
        if (input == null) return null;

        PatternType byId = PatternType.getByIdentifier(input.toLowerCase(Locale.ROOT));
        if (byId != null) return byId;
        try {
            return PatternType.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}