package org.broken.arrow.library.itemcreator.meta.map.font;

import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.AmpersandHexColorParser;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.ColorParser;
import org.broken.arrow.library.itemcreator.meta.map.font.customdraw.RenderState;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
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
    private IntUnaryOperator defaultFontSpacing = (charter) -> 8;
    private ColorParser colorParser;

    /**
     * just a check if the front is malleable
     */
    protected boolean malleable = true;


    /**
     * Sets the sprite for a specific character in this font.
     *
     * @param ch   The character to associate the sprite with.
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
     * Returns the default spacing function for characters.
     * <p>
     * This function is used when a character does not have its own spacing defined.
     * The input is the character code (cast from char to int), and the output is the
     * spacing in pixels to use for that character.
     * <p>
     * You typically don't need to call this method directly, as spacing is handled
     * automatically when a sprite is not provided for a symbol.
     *
     * @return a function mapping a character code to its default spacing in pixels
     */
    @Nonnull
    public IntUnaryOperator getDefaultFontSpacing() {
        return defaultFontSpacing;
    }

    /**
     * Applies the default spacing function to a specific character.
     * <p>
     * You typically don't need to call this method directly, as spacing is handled
     * automatically when a sprite is not provided for a symbol.
     *
     * @param ch the character to determine spacing for
     * @return the spacing in pixels for the given character
     */
    public int applyDefaultFontSpacing(char ch) {
        return defaultFontSpacing.applyAsInt(ch);
    }

    /**
     * Applies the current {@link ColorParser} to the text at the specified index.
     * <p>
     * The parser will attempt to detect a formatting sequence (such as Minecraft-style
     * codes or custom hex codes—the default implementation) starting at {@code index}
     * and update the {@link RenderState} with the corresponding color and/or style.
     * <p>
     * By default, the following codes are supported:
     * <ul>
     *     <li>{@code &0 - &f}: standard color codes</li>
     *     <li>{@code &#fff}: 3-digit hex color</li>
     *     <li>{@code &#ffffff}: 6-digit hex color</li>
     *     <li>{@code &l}: bold text</li>
     *     <li>{@code &o}: shadow effect</li>
     *     <li>{@code &r}: reset color and styles</li>
     * </ul>
     * <p>
     * See {@link RenderState#applyFormattingCode(char)} and
     * {@link RenderState#translateChatColor(char)} for details on the default parser's behavior.
     * <p>
     * You can also provide a custom {@link ColorParser} to define alternative parsing logic
     * or support additional formatting sequences.
     * <p>
     * This method returns the number of characters consumed by the formatting sequence,
     * allowing the caller to advance the cursor appropriately. If no sequence is found,
     * it returns 0. If no parser is set, it returns -1.
     * <p>
     * <strong>Note:</strong> This method is intended for internal use by the API and
     * usually does not need to be invoked directly.
     *
     * @param text        the text to parse
     * @param index       the starting position in the text
     * @param renderState the current render state to update with color/style
     * @return the number of characters consumed by the formatting code, 0 if none found, or -1 if no parser is set
     */
    public int applyColorParser(@Nonnull final String text, final int index,@Nonnull final RenderState renderState) {
        if(colorParser == null)
            return -1;
        return colorParser.tryParse( text,  index, renderState);
    }

    /**
     * Sets the {@link ColorParser} used to interpret color and style codes
     * in this text instance.
     * <p>
     * By default, the API uses the global parser from {@link MapFontWrapper} (typically
     * {@link AmpersandHexColorParser}). This method allows overriding it for this
     * specific text, including using lambda-based or custom parser implementations.
     *
     * @param colorParser the color parser to use for this text
     */
    public void setColorParser(@Nonnull final ColorParser colorParser) {
        this.colorParser = colorParser;
    }

    /**
     * Sets the default spacing function for characters.
     * <p>
     * Note that using this may produce uneven letter spacing if not used carefully,
     * as most symbols often have their own width. This function acts as a fallback
     * for characters without explicit spacing.
     * <p>
     * The input to the operator is the character code (cast from char to int), and
     * the output is the spacing in pixels.
     *
     * @param defaultFontSpacing a function mapping a character code to spacing in pixels
     */
    public void setDefaultFontSpacing(@Nonnull final IntUnaryOperator defaultFontSpacing) {
        this.defaultFontSpacing = defaultFontSpacing;
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
        if (chars.isEmpty()) {
            return new MinecraftFont();
        }
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
        private static BufferedImage workImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        private static Graphics2D graphics = workImage.createGraphics();

        private final int width;
        private final int height;

        private ConvertFontToSprite(final int width, final int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Set the sprite.
         *
         * @param ch   the charter
         * @param font the font to set.
         * @return the sprite character set.
         */
        public static CharacterSprite getSprite(final char ch, final Font font) {
            FontMetrics metrics = initGraphicsForFont(font);

            int fontWidth = metrics.charWidth(ch);
            int baseline = metrics.getAscent();
            int fontHeight = baseline + metrics.getDescent();

            if (fontWidth > workImage.getWidth() || fontHeight > workImage.getHeight()) {
                workImage = new BufferedImage(
                        Math.max(fontWidth, workImage.getWidth()),
                        Math.max(fontHeight, workImage.getHeight()),
                        BufferedImage.TYPE_INT_ARGB
                );
                graphics = workImage.createGraphics();
                metrics = initGraphicsForFont(font);
                fontWidth = metrics.charWidth(ch);
                baseline = metrics.getAscent();
                fontHeight = baseline + metrics.getDescent();
            }
            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(0, 0, workImage.getWidth(), workImage.getHeight());
            graphics.setComposite(AlphaComposite.SrcOver);

            graphics.setColor(Color.WHITE);
            graphics.drawString(String.valueOf(ch), 1, baseline);

            ConvertFontToSprite result = new ConvertFontToSprite(fontWidth, fontHeight);
            return result.extractGlyph();
        }

        private CharacterSprite extractGlyph() {
            int minX = width;
            int maxX = -1;

            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < width; x++) {
                    if ((workImage.getRGB(x, y) & 0xFF000000) != 0) {
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                    }
                }
            }
            return this.toSprite(minX, maxX);
        }

        private CharacterSprite toSprite(final int minX, final int maxX) {
            final boolean[] pixels;
            final int trimmedWidth;
            if (maxX == -1) {
                trimmedWidth = 0;
                pixels = new boolean[0];
                return new CharacterSprite(trimmedWidth, this.height, minX, pixels);
            }

            trimmedWidth = maxX - minX + 1;
            pixels = new boolean[trimmedWidth * this.height];

            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < trimmedWidth; x++) {
                    pixels[y * trimmedWidth + x] =
                            (workImage.getRGB(x + minX, y) & 0xFF000000) != 0;
                }
            }
            return new CharacterSprite(trimmedWidth, this.height, minX, pixels);
        }

        private static FontMetrics initGraphicsForFont(Font font) {
            graphics.setFont(font);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            return graphics.getFontMetrics();
        }

    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final MapFontWrapper that = (MapFontWrapper) o;
        return height == that.height && malleable == that.malleable && Objects.equals(chars, that.chars) && Objects.equals(defaultFontSpacing, that.defaultFontSpacing) && Objects.equals(colorParser, that.colorParser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chars, height, defaultFontSpacing, colorParser, malleable);
    }
}

