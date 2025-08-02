package org.broken.arrow.library.itemcreator.meta.map.font;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.map.MapFont;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MapFontWrapper {

    private final Map<Character, CharacterSprite> chars = new HashMap<>();
    private int height = 0;
    protected boolean malleable = true;

    /**
     * Set the sprite for a given character.
     *
     * @param ch     The character to set the sprite for.
     * @param sprite The CharacterSprite to set.
     * @throws IllegalStateException if this font is static.
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
     * Get the width of the given text as it would be rendered using this
     * font.
     *
     * @param text The text.
     * @return The width in pixels.
     */
    public int getWidth(@Nonnull String text) {
        if (!isValid(text)) {
            throw new IllegalArgumentException("text contains invalid characters");
        }

        if (text.length() == 0) {
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
     * Check whether the given text is valid.
     *
     * @param text The text.
     * @return True if the string contains only defined characters, false
     * otherwise.
     */
    public boolean isValid(@Nonnull String text) {
        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch == ChatColor.COLOR_CHAR || ch == '\n') continue;
            if (chars.get(ch) == null) return false;
        }
        return true;
    }

    public MapFont getMapFont() {
        MapFont mapFont = new MapFont();
        chars.forEach((character, characterSprite) -> mapFont.setChar(character, characterSprite.getCharacterSprite()));
        return mapFont;
    }


    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("chars", chars.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                spriteEntry -> spriteEntry.getValue().serialize())
        ));
        return map;
    }

    public static MapFontWrapper deserializeData(Map<String, Object> map) {
        MapFontWrapper fontWrapper = new MapFontWrapper();
        Map<?, ?> chars = (Map<?, ?>) map.getOrDefault("chars", new HashMap<>());
        for (Map.Entry<?, ?> font : chars.entrySet()) {
            fontWrapper.chars.put(font.getKey().toString().charAt(0), CharacterSprite.deserializeData((Map<String, Object>) font.getValue()));
        }
        return fontWrapper;
    }
}

