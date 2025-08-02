package org.broken.arrow.library.itemcreator.meta.map.pixel;

import org.broken.arrow.library.itemcreator.meta.map.font.CharacterSprite;
import org.broken.arrow.library.itemcreator.meta.map.font.MapFontWrapper;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TextOverlay extends MapPixel {

    private final String text;
    private final MapFontWrapper mapFontWrapper = new MapFontWrapper();

    public TextOverlay(final int x, final int y, final String text) {
        super(x, y);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setMapFont(final char ch, @Nonnull final CharacterSprite sprite) {
        mapFontWrapper.setChar(ch, sprite);
    }

    public org.bukkit.map.MapFont getMapFont() {
        return mapFontWrapper.getMapFont();
    }


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

    public static TextOverlay deserialize(Map<String, Object> map) {
        int x = (int) map.get("x");
        int y = (int) map.get("y");
        Object textToSave = map.get("text");
        return new TextOverlay(x, y, textToSave + "");
    }
}