package org.broken.arrow.library.itemcreator.meta.map.font;

import org.bukkit.map.MapFont;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the graphics for a single character in a MapFont.
 */
public class CharacterSprite {

    private final int width;
    private final int height;
    private final boolean[] data;

    public CharacterSprite(int width, int height, @Nonnull boolean[] data) {
        this.width = width;
        this.height = height;
        this.data = data;

        if (data.length != width * height) {
            throw new IllegalArgumentException("size of data does not match dimensions");
        }
    }

    /**
     * Get the value of a pixel of the character.
     *
     * @param row The row, in the range [0,8).
     * @param col The column, in the range [0,8).
     * @return True if the pixel is solid, false if transparent.
     */
    public boolean get(int row, int col) {
        if (row < 0 || col < 0 || row >= height || col >= width) return false;
        return data[row * width + col];
    }

    /**
     * Get the width of the character sprite.
     *
     * @return The width of the character.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the character sprite.
     *
     * @return The height of the character.
     */
    public int getHeight() {
        return height;
    }


    public boolean[] getData() {
        return data;
    }

    public MapFont.CharacterSprite getCharacterSprite() {
        return new MapFont.CharacterSprite(width, height, data);
    }

    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("height", height);
        map.put("width", width);
        // Boxed Boolean[] instead of primitive boolean[]
        List<Boolean> boolList = new ArrayList<>(data.length);
        for (boolean b : data) {
            boolList.add(b);
        }
        map.put("data", boolList);
        return map;
    }

    public static CharacterSprite deserializeData(Map<String, Object> map) {
        int height = (int) map.get("height");
        int width = (int) map.get("width");

        List<Boolean> boolList = (List<Boolean>) map.get("data");
        boolean[] data = new boolean[boolList.size()];
        for (int i = 0; i < boolList.size(); i++) {
            data[i] = Boolean.TRUE.equals(boolList.get(i));
        }

        return new CharacterSprite(width, height, data);
    }
}
