package org.broken.arrow.library.itemcreator.meta.map.cursor;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.map.MapCursor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class MapCursorWrapper {
    private static int id;
    private final int cursorId;
    private byte x, y;
    private byte direction;
    private final byte rawType;
    private final MapCursor.Type type;
    private boolean visible;
    private String caption;

    /**
     * Initialize the map cursor.
     *
     * @param x         The x coordinate, from -128 to 127.
     * @param y         The y coordinate, from -128 to 127.
     * @param direction The facing of the cursor, from 0 to 15.
     * @param type      The type (color/style) of the map cursor.
     * @param visible   Whether the cursor is visible by default.
     */
    public MapCursorWrapper(final byte x,final byte y,final byte direction,final @Nonnull MapCursor.Type type,final boolean visible) {
        this(x, y, direction, type, visible, null);
    }


    /**
     * Initialize the map cursor.
     *
     * @param x         The x coordinate, from -128 to 127.
     * @param y         The y coordinate, from -128 to 127.
     * @param direction The facing of the cursor, from 0 to 15.
     * @param type      The type (color/style) of the map cursor.
     * @param visible   Whether the cursor is visible by default.
     * @param caption   cursor caption
     */
    public MapCursorWrapper(final byte x,final byte y,final byte direction, @Nonnull final MapCursor.Type type,final boolean visible, @Nullable final String caption) {
        this.x = x;
        this.y = y;
        this.cursorId = id++;
        this.type = type;
        this.rawType = retrieveRawType(type);
        setDirection(direction);
        this.visible = visible;
        this.caption = caption;
    }

    public int getCursorId() {
        return cursorId;
    }

    /**
     * Get the X position of this cursor.
     *
     * @return The X coordinate.
     */
    public byte getX() {
        return x;
    }

    /**
     * Set the X position of this cursor.
     *
     * @param x The X coordinate.
     */
    public void setX(final byte x) {
        this.x = x;
    }

    /**
     * Get the Y position of this cursor.
     *
     * @return The Y coordinate.
     */
    public byte getY() {
        return y;
    }

    /**
     * Set the Y position of this cursor.
     *
     * @param y The Y coordinate.
     */
    public void setY(final byte y) {
        this.y = y;
    }

    /**
     * Get the direction of this cursor.
     *
     * @return The facing of the cursor, from 0 to 15.
     */
    public byte getDirection() {
        return direction;
    }

    /**
     * Get the type of this cursor.
     *
     * @return The type (color/style) of the map cursor.
     */
    @Nonnull
    public MapCursor.Type getType() {
        if(type == null)
            return MapCursor.Type.WHITE_POINTER;
        return type;
    }

    /**
     * Get the type of this cursor.
     *
     * @return The type (color/style) of the map cursor.
     * @deprecated Magic value
     */
    @Deprecated
    public byte getRawType() {
        if(type == null)
            return MapCursor.Type.WHITE_POINTER.getValue();
        return rawType;
    }

    /**
     * Get the visibility status of this cursor.
     *
     * @return True if visible, false otherwise.
     */
    public boolean isVisible() {
        return visible;
    }


    /**
     * Set the direction of this cursor.
     *
     * @param direction The facing of the cursor, from 0 to 15.
     */
    public void setDirection(byte direction) {
        if (direction < 0 || direction > 15) {
            throw new IllegalArgumentException("Direction must be in the range 0-15");
        }
        this.direction = direction;
    }

    /**
     * Set the visibility status of this cursor.
     *
     * @param visible True if visible.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Gets the caption on this cursor.
     *
     * @return caption
     */
    @Nullable
    public String getCaption() {
        return caption;
    }

    /**
     * Sets the caption on this cursor.
     *
     * @param caption new caption
     */
    public void setCaption(@Nullable String caption) {
        this.caption = caption;
    }

    public MapCursor build() {
        if (ItemCreator.getServerVersion() < 12.1f)
            return new MapCursor(x, y, direction, rawType, visible, caption);
        return new MapCursor(x, y, direction, type, visible, caption);
    }

    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("cursor_type", getType() + "");
        map.put("x", x);
        map.put("y", y);
        map.put("direction", getDirection());
        map.put("visible", isVisible() + "");
        map.put("caption", getCaption());
        return map;
    }

    public static MapCursorWrapper deserialize(Map<String, Object> map) {
        String type = (String) map.get("cursor_type");
        byte x = (byte) (int) map.get("x");
        byte y = (byte) (int) map.get("y");
        byte direction = (byte) (int) map.get("direction");
        boolean visible = Boolean.parseBoolean(map.get("visible") + "");
        String caption = (String) map.get("caption");

        return new MapCursorWrapper(x, y, direction, byName(type), visible, caption);
    }

    @Nonnull
    public static MapCursor.Type byName(final String name) {
        if (name == null)
            return MapCursor.Type.WHITE_POINTER;
        String nameFormatted = name.toUpperCase();
        for (MapCursor.Type t : MapCursor.Type.values()) {
            if (t.name().equals(nameFormatted)) return t;
        }
        return MapCursor.Type.WHITE_POINTER;
    }

    private byte retrieveRawType(final MapCursor.Type type) {
        if(type == null)
            return 0;
        return type.getValue();
    }

}

