package org.broken.arrow.library.itemcreator.utility;

import org.bukkit.block.banner.PatternType;

import java.util.Locale;

public final class PatternTypeUtil {

    private PatternTypeUtil() {}

    public static String toId(PatternType type) {
        return type.getIdentifier();
    }

    public static PatternType fromString(String input) {
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