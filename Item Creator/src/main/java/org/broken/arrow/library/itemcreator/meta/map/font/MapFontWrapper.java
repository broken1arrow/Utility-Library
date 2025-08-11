package org.broken.arrow.library.itemcreator.meta.map.font;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.map.MapFont;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * Wrapper class for managing a custom font represented by character sprites on a map.
 * <p>
 * This class maintains a mapping from characters to {@link CharacterSprite} instances,
 * which represent the graphical representation of each character.
 * It supports calculating text dimensions, validation of strings against defined characters,
 * and serialization/deserialization for persistence or network transfer.
 * <p>
 * The font can be mutable ("malleable") or static. Once marked as static, no further
 * character sprites can be added.
 */
public class MapFontWrapper {

    private final Map<Character, CharacterSprite> chars = new HashMap<>();
    private int height = 0;
    /** just a check if the front is malleable*/
    protected boolean malleable = true;

    /**
     * Sets the sprite for a specific character in this font.
     *
     * @param ch     The character to associate the sprite with.
     * @param sprite The {@link CharacterSprite} to set for the character.
     * @throws IllegalStateException if the font is static and does not allow modification.
     */
    public void setChar(final char ch, @Nonnull CharacterSprite sprite) {
        if (!malleable) {
            throw new IllegalStateException("this font is not malleable");
        }
        chars.put(ch, sprite);
        if (sprite.getHeight() > height) {
            height = sprite.getHeight();
        }
    }

    /**
     * Get the sprite for a given character.
     *
     * @param ch The character to get the sprite for.
     * @return The CharacterSprite associated with the character, or null if
     * there is none.
     */
    @Nullable
    public CharacterSprite getChar(char ch) {
        return chars.get(ch);
    }

    /**
     * Calculates the total width in pixels of the provided text when rendered
     * with this font. This accounts for individual character widths and
     * inter-character spacing.
     *
     * @param text The text string to measure.
     * @return The width of the text in pixels.
     * @throws IllegalArgumentException if the text contains characters not defined in this font
     *                                  or has malformed color codes.
     */
    public int getWidth(@Nonnull String text) {
        if (!isValid(text)) {
            throw new IllegalArgumentException("text contains invalid characters");
        }

        if (text.isEmpty()) {
            return 0;
        }

        int result = 0;
        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch == ChatColor.COLOR_CHAR) {
                int j = text.indexOf(';', i);
                if (j >= 0) {
                    i = j;
                    continue;
                }
                throw new IllegalArgumentException("Text contains unterminated color string");
            }
            result += chars.get(ch).getWidth();
        }
        result += text.length() - 1; // Account for 1px spacing between characters

        return result;
    }

    /**
     * Get the height of this font.
     *
     * @return The height of the font.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Validates that the given text only contains characters defined in this font,
     * allowing also for Minecraft color codes and newline characters.
     *
     * @param text The string to validate.
     * @return {@code true} if all characters are defined or allowed, {@code false} otherwise.
     */
    public boolean isValid(@Nonnull String text) {
        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch == ChatColor.COLOR_CHAR || ch == '\n') continue;
            if (chars.get(ch) == null) return false;
        }
        return true;
    }

    /**
     * Converts this wrapper into the underlying {@link MapFont} instance,
     * transferring all character sprites.
     *
     * @return A new {@link MapFont} populated with character sprites from this wrapper.
     */
    public MapFont getMapFont() {
        MapFont mapFont = new MapFont();
        chars.forEach((character, characterSprite) -> mapFont.setChar(character, characterSprite.getCharacterSprite()));
        return mapFont;
    }

    /**
     * Serializes this font wrapper into a map structure for storage or transmission.
     *
     * @return A map representing the serialized form of this font wrapper.
     */
    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("chars", chars.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                spriteEntry -> spriteEntry.getValue().serialize())
        ));
        return map;
    }

    /**
     * Deserializes a font wrapper from a map representation.
     *
     * @param map The serialized map containing font data.
     * @return A new {@link MapFontWrapper} instance populated from the map.
     */
    public static MapFontWrapper deserializeData(Map<String, Object> map) {
        MapFontWrapper fontWrapper = new MapFontWrapper();
        Map<?, ?> chars = (Map<?, ?>) map.getOrDefault("chars", new HashMap<>());
        for (Map.Entry<?, ?> font : chars.entrySet()) {
            fontWrapper.chars.put(font.getKey().toString().charAt(0), CharacterSprite.deserializeData((Map<String, Object>) font.getValue()));
        }
        return fontWrapper;
    }
}

