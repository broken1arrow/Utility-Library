package org.broken.arrow.library.itemcreator.meta.map.pixel;

import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.bukkit.map.MapCanvas;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a pixel element on a map with a fixed x and y coordinate.
 * Subclasses define specific pixel types such as colored pixels, text overlays, or images.
 */
public abstract class MapPixel {
    private final int x;
    private final int y;

    /**
     * Constructs a map pixel at the specified coordinates.
     *
     * @param x  the x-coordinate of the pixel
     * @param y  the y-coordinate of the pixel
     */
    protected MapPixel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Renders this pixel to the given canvas.
     *
     * @param mapRendererData the global data set for the map
     * @param canvas          the map canvas where this pixel's color, text, or image will be drawn
     */
    abstract public void render(@Nonnull final MapRendererData mapRendererData, @Nonnull final MapCanvas canvas);


    /**
     * Gets the x-coordinate of this pixel.
     *
     * @return the x-coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of this pixel.
     *
     * @return the y-coordinate.
     */
    public int getY() {
        return y;
    }


    /**
     * Serialize the data in your class to a format that
     * can be saved to a database.
     *
     * @return the serialized data set.
     */
    @Nonnull
    public abstract Map<String, Object> serialize();

    /**
     * Returns the simple class name representing the pixel type.
     *
     * @return the pixel type name.
     */
    public String type() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "MapPixel{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final MapPixel mapPixel = (MapPixel) o;
        return x == mapPixel.x && y == mapPixel.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
