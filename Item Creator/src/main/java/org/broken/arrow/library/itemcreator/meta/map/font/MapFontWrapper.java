package org.broken.arrow.library.itemcreator.meta.map.font;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    /**
     * just a check if the front is malleable
     */
    protected boolean malleable = true;


    /**
     * Sets the sprite for a specific character in this font.
     *
     * @param ch The character to associate the sprite with.
     * @param font the font to set for the character.
     * @throws IllegalStateException if the font is static and does not allow modification.
     */
    public void setChar(final char ch, @Nonnull final Font font) {
        if (!malleable) throw new IllegalStateException("font is not malleable");
        this.setChar(ch, ConvertFontToSprite.getSprite(ch, font));
    }


    /**
     * Sets the sprite for a specific character in this font.
     *
     * @param ch     The character to associate the sprite with.
     * @param sprite The {@link CharacterSprite} to set for the character.
     * @throws IllegalStateException if the font is static and does not allow modification.
     */
    public void setChar(final char ch, @Nonnull final CharacterSprite sprite) {
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
    public CharacterSprite getChar(final char ch) {
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
    public int getWidth(@Nonnull final String text) {
        if (!isValid(text)) {
            throw new IllegalArgumentException("text contains invalid characters");
        }

        if (text.isEmpty()) {
            return 0;
        }
        final FontParser parser = new FontParser(this);

        int result = 0;
        for (int i = 0; i < text.length(); i++) {
            CharResult charResult = parser.handleCharacter(text, i);
            if (charResult.getSkipIndex() != FontParser.NO_SKIP) {
                i = charResult.getSkipIndex();
                continue;
            }
            if (charResult.getWidth() != FontParser.NO_WIDTH)
                result += charResult.getWidth();
        }
/*        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch == ChatColor.COLOR_CHAR) {
                int j = text.indexOf(';', i);
                if (j >= 0) {
                    i = j;
                    continue;
                }
                throw new IllegalArgumentException("Text contains unterminated color string");
            }
            final CharacterSprite characterSprite = chars.get(ch);
            if (characterSprite == null)
                continue;
            result += characterSprite.getWidth();
        }*/
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
        if (chars.isEmpty())
            return MinecraftFont.Font;
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

    private static final class FontParser {
        private static final int NO_WIDTH = -1;
        private static final int NO_SKIP = -1;
        private final Map<Character, CharacterSprite> chars;

        FontParser(@Nonnull final MapFontWrapper mapFontWrapper) {
            chars = mapFontWrapper.chars;
        }

        @Nonnull
        private CharResult handleCharacter(String text, int index) {
            CharResult charResult = new CharResult();
            char ch = text.charAt(index);
            if (ch == ChatColor.COLOR_CHAR) {
                int end = text.indexOf(';', index);
                if (end < 0)
                    throw new IllegalArgumentException("Text contains unterminated color string");
                charResult.setSkipIndex(end);
            } else {
                CharacterSprite sprite = chars.get(ch);
                if (sprite != null) {
                    charResult.setWidth(sprite.getWidth());
                }
            }
            return charResult;
        }
    }

    private static final class CharResult {
        private int width = FontParser.NO_WIDTH;
        private int skipIndex = FontParser.NO_SKIP;

        public int getWidth() {
            return width;
        }

        public void setWidth(final int width) {
            this.width = width;
        }

        public int getSkipIndex() {
            return skipIndex;
        }

        public void setSkipIndex(final int skipIndex) {
            this.skipIndex = skipIndex;
        }
    }

    private static class ConvertFontToSprite {
        private static BufferedImage WORK_IMAGE = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        private static Graphics2D GRAPHICS = WORK_IMAGE.createGraphics();

        private final int width;
        private final int height;

        private ConvertFontToSprite(final int width, final int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Set the sprite.
         *
         * @param ch the charter
         * @param font the font to set.
         * @return the sprite character set.
         */
        public static CharacterSprite getSprite(final char ch, final Font font) {
            FontMetrics metrics = initGraphicsForFont(font);

            int fontWidth = metrics.charWidth(ch);
            int baseline = metrics.getAscent();
            int fontHeight = baseline + metrics.getDescent();

            if (fontWidth > WORK_IMAGE.getWidth() || fontHeight > WORK_IMAGE.getHeight()) {
                WORK_IMAGE = new BufferedImage(
                        Math.max(fontWidth, WORK_IMAGE.getWidth()),
                        Math.max(fontHeight, WORK_IMAGE.getHeight()),
                        BufferedImage.TYPE_INT_ARGB
                );
                GRAPHICS = WORK_IMAGE.createGraphics();
                metrics = initGraphicsForFont(font);
                fontWidth = metrics.charWidth(ch);
                baseline = metrics.getAscent();
                fontHeight = baseline + metrics.getDescent();
            }
            GRAPHICS.setComposite(AlphaComposite.Clear);
            GRAPHICS.fillRect(0, 0, WORK_IMAGE.getWidth(), WORK_IMAGE.getHeight());
            GRAPHICS.setComposite(AlphaComposite.SrcOver);

            GRAPHICS.setColor(Color.WHITE);
            GRAPHICS.drawString(String.valueOf(ch), 0, baseline);

            ConvertFontToSprite result = new ConvertFontToSprite(fontWidth, fontHeight);
            return result.extractGlyph();
        }

        private CharacterSprite extractGlyph() {
            int minX = width, maxX = -1;
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < width; x++) {
                    if ((WORK_IMAGE.getRGB(x, y) & 0xFF000000) != 0) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                    }
                }
            }
            return toSprite(maxX, minX);
        }

        private CharacterSprite toSprite(final int maxX, final int minX) {
            final boolean[] pixels;
            final int trimmedWidth;
            if (maxX == -1) {
                trimmedWidth = 0;
                pixels = new boolean[0];
                return new CharacterSprite(trimmedWidth, this.height, pixels);
            }

            trimmedWidth = maxX - minX + 1;
            pixels = new boolean[trimmedWidth * this.height];

            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < trimmedWidth; x++) {
                    pixels[y * trimmedWidth + x] =
                            (WORK_IMAGE.getRGB(x + minX, y) & 0xFF000000) != 0;
                }
            }
            return new CharacterSprite(trimmedWidth, this.height, pixels);
        }

        private static FontMetrics initGraphicsForFont(Font font) {
            GRAPHICS.setFont(font);
            GRAPHICS.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            return GRAPHICS.getFontMetrics();
        }

    }

}

