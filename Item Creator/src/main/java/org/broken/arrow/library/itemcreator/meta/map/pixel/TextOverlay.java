package org.broken.arrow.library.itemcreator.meta.map.pixel;

import org.broken.arrow.library.itemcreator.meta.map.color.parser.AmpersandHexColorParser;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.ColorParser;
import org.broken.arrow.library.itemcreator.meta.map.font.CharacterSprite;
import org.broken.arrow.library.itemcreator.meta.map.font.MapFontWrapper;
import org.broken.arrow.library.itemcreator.meta.map.font.customdraw.RenderState;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * A map pixel that displays text at a given position with customizable font characters.
 */
public class TextOverlay extends MapPixel {

    private final String text;
    private final MapFontWrapper mapFontWrapper = new MapFontWrapper();

    /**
     * Constructs a TextOverlay at the specified coordinates with the given text.
     *
     * @param x    the x-coordinate of the text overlay.
     * @param y    the y-coordinate of the text overlay.
     * @param text the text to display.
     */
    public TextOverlay(final int x, final int y,@Nonnull final String text) {
        super(x, y);
        this.text = text;
    }

    /**
     * Gets the text content of this overlay.
     *
     * @return the text string.
     */
    @Nonnull
    public String getText() {
        return text;
    }

    /**
     * Sets a custom map font character for rendering the text.
     *
     * @param ch     the character to replace.
     * @param sprite the {@link CharacterSprite} representing the custom character sprite.
     */
    public void setMapFont(final char ch, @Nonnull final CharacterSprite sprite) {
        mapFontWrapper.setChar(ch, sprite);
    }

    /**
     * Sets a custom map font character for rendering the text. Some fonts not always translates to the map correctly
     * try bigger or smaller font size, that could help with the issue.
     *
     * @param chars     the characters to replace.
     * @param font the {@link Font} representing the custom character sprite.
     */
    public void setMapFont(final char[] chars, @Nonnull final Font font) {
        BufferedImage workImg = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = workImg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        for (char charter : chars) {
            mapFontWrapper.setChar(charter, font);
        }
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
        mapFontWrapper.setColorParser(colorParser);
    }


    /**
     * Gets the {@link org.bukkit.map.MapFont} used to render this text overlay.
     *
     * @return the map font.
     */
    public org.bukkit.map.MapFont getMapFont() {
        return mapFontWrapper.getMapFont();
    }

    /**
     * Gets the {@link MapFontWrapper} wrapper of the front.
     *
     * @return the wrapper of the map font.
     */
    public MapFontWrapper getMapFontWrapper() {
        return mapFontWrapper;
    }


    /**
     * Serializes this TextOverlay into a map representation for saving context.
     *
     * @return a map containing serialized text overlay data.
     */
    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type());
        map.put("x", getX());
        map.put("y", getY());
        String textToSave = text != null ? text : "";
        map.put("text", textToSave);

        return map;
    }

    /**
     * Deserializes a TextOverlay object from a map of saved data.
     *
     * @param map the serialized data map.
     * @return a new TextOverlay instance.
     */
    public static TextOverlay deserialize(Map<String, Object> map) {
        int x = (int) map.get("x");
        int y = (int) map.get("y");
        Object textToSave = map.get("text");
        return new TextOverlay(x, y, textToSave + "");
    }
}