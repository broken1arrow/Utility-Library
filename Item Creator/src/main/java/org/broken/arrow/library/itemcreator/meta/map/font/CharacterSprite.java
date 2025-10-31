package org.broken.arrow.library.itemcreator.meta.map.font;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.map.MapFont;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the graphics data for a single character in a {@link MapFont}.
 * <p>
 * Each character is represented by a 2D grid of pixels (width x height),
 * where each pixel is either solid (true) or transparent (false).
 */
public class CharacterSprite {

    private final int width;
    private final int height;
    private final boolean[] data;
    private final int spacing;

    /**
     * Creates a character sprite with an explicit pixel spacing value.
     * <p>
     * Spacing defines how many pixels should appear after this character before drawing
     * the next one. This allows variable-width fonts or compact characters.
     *
     * @param width   the width of the character in pixels
     * @param height  the height of the character in pixels
     * @param spacing the number of pixels to leave after this character when rendering
     * @param data    a boolean array representing pixel solidity; must be {@code width * height} in size
     * @throws IllegalArgumentException if {@code data.length} does not equal {@code width * height}
     */
    public CharacterSprite(final int width, final int height, final int spacing, @Nonnull final boolean[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.spacing = spacing;

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
    public boolean get(final int row, final int col) {
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

    /**
     * Returns the array if what pixel is solid or transparent data for this character.
     * Each element corresponds to a pixel is solid or transparent.
     *
     * @return The solid or transparent pixel data array.
     */
    public boolean[] getData() {
        return data;
    }

    /**
     * Returns the spacing value for this sprite.
     * <p>
     * This is the number of pixels to move forward after drawing the character,
     * before drawing the next one.
     *
     * @return the spacing in pixels
     */
    public int getSpacing() {
        return spacing;
    }

    /**
     * Converts this wrapper into the underlying {@link MapFont.CharacterSprite} instance.
     *
     * @return A new {@link MapFont.CharacterSprite} with the same dimensions and pixel data.
     */
    public MapFont.CharacterSprite getCharacterSprite() {
        return new MapFont.CharacterSprite(width, height, data);
    }

    /**
     * Serializes this character sprite to a map representation.
     * The map contains keys for width, height, and a list of Boolean pixel values.
     *
     * @return A map representing the serialized character sprite.
     */
    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("height", height);
        map.put("width", width);
        map.put("spacing", spacing);
        List<Boolean> boolList = new ArrayList<>(data.length);
        for (boolean b : data) {
            boolList.add(b);
        }
        map.put("data", boolList);
        return map;
    }

    /**
     * Deserializes a CharacterSprite from the given map.
     * Expects keys "width", "height", and "data" (a list of Boolean values).
     *
     * @param map The serialized map.
     * @return A new CharacterSprite instance reconstructed from the map.
     */
    public static CharacterSprite deserializeData(Map<String, Object> map) {
        int height = (int) map.get("height");
        int width = (int) map.get("width");
        int spacing = (int) map.get("spacing");
        List<Boolean> boolList = (List<Boolean>) map.get("data");
        boolean[] data = new boolean[boolList.size()];
        for (int i = 0; i < boolList.size(); i++) {
            data[i] = Boolean.TRUE.equals(boolList.get(i));
        }

        return new CharacterSprite(width, height, spacing, data);
    }

}
