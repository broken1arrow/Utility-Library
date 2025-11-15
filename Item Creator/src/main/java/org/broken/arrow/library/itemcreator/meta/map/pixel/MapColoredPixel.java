package org.broken.arrow.library.itemcreator.meta.map.pixel;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a pixel on a map with a specific color at a given (x, y) position.
 * The color may be null, indicating a transparent or unspecified pixel.
 */
public class MapColoredPixel extends MapPixel {
    private final Color color;

    /**
     * Constructs a colored pixel at the given coordinates with no color (null).
     *
     * @param x the x-coordinate of the pixel.
     * @param y the y-coordinate of the pixel.
     */
    public MapColoredPixel(final int x, final int y) {
        this(x, y,  null);
    }

    /**
     * Constructs a colored pixel at the given coordinates with the specified color.
     *
     * @param x     the x-coordinate of the pixel.
     * @param y     the y-coordinate of the pixel.
     * @param color the color of the pixel, or null if unspecified.
     */
    public MapColoredPixel(final int x, final int y,  @Nullable final Color color) {
        super(x, y);
        this.color = color;
    }


    @Override
    public void render(final @Nonnull MapRendererData mapRendererData, @Nonnull final MapCanvas canvas) {
        final Color pixelColor = this.getColor();
        if (ItemCreator.getServerVersion() < 20.0F) {
            canvas.setPixel(this.getX(), this.getY(), MapPalette.matchColor(pixelColor));
        } else {
            canvas.setPixelColor(this.getX(), this.getY(), pixelColor);
        }
    }


    /**
     * Gets the color of this pixel.
     *
     * @return the color, or null if no color is set.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Serializes this colored pixel to a map containing its type, coordinates, and color (if set).
     *
     * @return a map representing this colored pixel for storage or transmission.
     */
    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type());
        map.put("x", getX());
        map.put("y", getY());
        if (color != null) {
            map.put("color", color.getRGB());
        }
        return map;
    }

    /**
     * Deserializes a colored pixel from the given map representation.
     *
     * @param map the map containing serialized pixel data.
     * @return a new {@code MapColoredPixel} instance.
     */
    public static MapColoredPixel deserialize(Map<String, Object> map) {
        int x = (int) map.get("x");
        int y = (int) map.get("y");
        Object colorObject = map.get("color");
        Color color = colorObject != null ? new Color((int) colorObject) : null;
        return new MapColoredPixel(x, y, color);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final MapColoredPixel that = (MapColoredPixel) o;
        return Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), color);
    }
}