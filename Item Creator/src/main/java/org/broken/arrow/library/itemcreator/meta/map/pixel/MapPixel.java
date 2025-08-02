package org.broken.arrow.library.itemcreator.meta.map.pixel;

import javax.annotation.Nonnull;
import java.util.Map;

public abstract class MapPixel {
    private final int x, y;

    public MapPixel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

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

    public String type() {
        return this.getClass().getSimpleName();
    }
}
