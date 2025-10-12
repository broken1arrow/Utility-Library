package org.broken.arrow.library.itemcreator.meta.map.cursor;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.bukkit.map.MapCursor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a cursor on a map. This class encapsulates
 * the underlying {@link MapCursor} for easier serialization.
 * <p>
 * Note that instances of this class are mutable; therefore,
 * equality and hash code values may change during its lifecycle.
 */
public final class MapCursorWrapper {
    private static int id;
    private final int cursorId;
    private final MapCursor cursor;
    private final byte rawType;

    /**
     * Initialize the map cursor.
     *
     * @param x         The x coordinate, from -128 to 127.
     * @param y         The y coordinate, from -128 to 127.
     * @param direction The facing of the cursor, from 0 to 15.
     * @param type      The type (color/style) of the map cursor.
     * @param visible   Whether the cursor is visible by default.
     */
    public MapCursorWrapper(final byte x, final byte y, final byte direction, final @Nonnull MapCursor.Type type, final boolean visible) {
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
    public MapCursorWrapper(final byte x, final byte y, final byte direction, @Nonnull final MapCursor.Type type, final boolean visible, @Nullable final String caption) {
        this.cursorId = id++;
        this.rawType = retrieveRawType(type);
        if (ItemCreator.getServerVersion() < 13.0f) {
            if (ItemCreator.getServerVersion() < 12.0f)
                this.cursor = new MapCursor(x, y, direction, rawType, visible, caption);
            else
                this.cursor = new MapCursor(x, y, direction, rawType, visible);
        }
        else
            this.cursor = new MapCursor(x, y, direction, type, visible, caption);
        setDirection(direction);
    }

    /**
     * Returns the current identifier for this cursor instance.
     * <p>
     * This ID is intended to help retrieve the correct data from a collection.
     * Since this class is mutable, note that the equality and hash code
     * may change over time, so this ID reflects the current state.
     *
     * @return the current cursor ID
     */
    public int getCursorId() {
        return cursorId;
    }

    /**
     * Get the X position of this cursor.
     *
     * @return The X coordinate.
     */
    public byte getX() {
        return cursor.getX();
    }

    /**
     * Set the X position of this cursor.
     *
     * @param x The X coordinate.
     */
    public void setX(final byte x) {
        cursor.setX(x);
    }

    /**
     * Get the Y position of this cursor.
     *
     * @return The Y coordinate.
     */
    public byte getY() {
        return cursor.getY();
    }

    /**
     * Set the Y position of this cursor.
     *
     * @param y The Y coordinate.
     */
    public void setY(final byte y) {
        cursor.setY(y);
    }

    /**
     * Get the direction of this cursor.
     *
     * @return The facing of the cursor, from 0 to 15.
     */
    public byte getDirection() {
        return cursor.getDirection();
    }

    /**
     * Get the type of this cursor.
     *
     * @return The type (color/style) of the map cursor.
     */
    @Nonnull
    public MapCursor.Type getType() {
        return cursor.getType();
    }

    /**
     * Get the type of this cursor.
     *
     * @return The type (color/style) of the map cursor.
     */
    public byte getRawType() {
        return rawType;
    }

    /**
     * Get the visibility status of this cursor.
     *
     * @return True if visible, false otherwise.
     */
    public boolean isVisible() {
        return this.cursor.isVisible();
    }


    /**
     * Set the direction of this cursor.
     *
     * @param direction The facing of the cursor, from 0 to 15.
     */
    public void setDirection(final byte direction) {
        this.cursor.setDirection(direction);
    }

    /**
     * Set the visibility status of this cursor.
     *
     * @param visible True if visible.
     */
    public void setVisible(boolean visible) {
        this.cursor.setVisible(visible);
    }

    /**
     * Gets the caption on this cursor.
     *
     * @return caption
     */
    @Nullable
    public String getCaption() {
        return this.cursor.getCaption();
    }

    /**
     * Sets the caption on this cursor.
     *
     * @param caption new caption
     */
    public void setCaption(@Nullable String caption) {
        this.cursor.setCaption(caption);
    }

    /**
     * Retrieve the cursor for the map
     *
     * @return the cursor.
     */
    @Nonnull
    public MapCursor getCursor() {
        return this.cursor;
    }

    /**
     * Serialize the MapCursor data.
     *
     * @return map with data serialized.
     */
    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("cursor_type", getType() + "");
        map.put("x", getX());
        map.put("y", getY());
        map.put("direction", getDirection());
        map.put("visible", isVisible() + "");
        map.put("caption", getCaption());
        return map;
    }

    /**
     * Deserialize from an map the values set.
     *
     * @param map the map to retrieve the data from.
     * @return a instance of the {@link MapCursorWrapper} class.
     */
    public static MapCursorWrapper deserialize(Map<String, Object> map) {
        String type = (String) map.get("cursor_type");
        byte x = ((Number) map.get("x")).byteValue();
        byte y = ((Number) map.get("y")).byteValue();
        byte direction = ((Number) map.get("direction")).byteValue();
        boolean visible = Boolean.parseBoolean(map.get("visible") + "");
        String caption = (String) map.get("caption");

        return new MapCursorWrapper(x, y, direction, byName(type), visible, caption);
    }

    /**
     * Retrieve the pointer type from string.
     *
     * @param name the name of the pointer case-insensitive.
     * @return the type or {@link MapCursor.Type#WHITE_POINTER} if name is null or
     * could not be found.
     */
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

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final MapCursorWrapper that = (MapCursorWrapper) o;
        return cursorId == that.cursorId && rawType == that.rawType && Objects.equals(cursor, that.cursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cursorId, cursor, rawType);
    }

    private byte retrieveRawType(final MapCursor.Type type) {
        if (type == null)
            return 0;
        return type.getValue();
    }

}

