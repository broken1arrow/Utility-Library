package org.broken.arrow.library.itemcreator.meta.map.pixel;

import org.broken.arrow.library.itemcreator.meta.map.font.CharacterSprite;
import org.broken.arrow.library.itemcreator.meta.map.font.MapFontWrapper;

import javax.annotation.Nonnull;
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
     * @param x the x-coordinate of the text overlay.
     * @param y the y-coordinate of the text overlay.
     * @param text the text to display.
     */
    public TextOverlay(final int x, final int y, final String text) {
        super(x, y);
        this.text = text;
    }

    /**
     * Gets the text content of this overlay.
     *
     * @return the text string.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets a custom map font character for rendering the text.
     *
     * @param ch the character to replace.
     * @param sprite the {@link CharacterSprite} representing the custom character sprite.
     */
    public void setMapFont(final char ch, @Nonnull final CharacterSprite sprite) {
        mapFontWrapper.setChar(ch, sprite);
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