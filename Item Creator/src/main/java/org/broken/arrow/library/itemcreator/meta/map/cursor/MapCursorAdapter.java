package org.broken.arrow.library.itemcreator.meta.map.cursor;

import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents all the map cursors on a {@link org.bukkit.map.MapCanvas}. Like MapCanvas, a
 * MapCursorCollection is linked to a specific {@link org.bukkit.map.MapRenderer}.
 */
public final class MapCursorAdapter {
    private final List<MapCursorWrapper> cursors = new ArrayList<>();
    private final transient MapCursorCollection mapCursorCollection = new MapCursorCollection();

    /**
     * Get the amount of cursors in this collection.
     *
     * @return The size of this collection.
     */
    public int size() {
        return cursors.size();
    }

    /**
     * Retrieves a cursor from this collection.
     * <p>
     * Note: If the index is larger than the size of the collection,
     * it will attempt to return the last valid cursor. If the list
     * is empty, it returns a new {@link MapCursorWrapper} that
     * is not visible on the map.
     *
     * @param index The index of the cursor.
     * @return The {@link MapCursorWrapper} at the specified index, or a fallback cursor if out of bounds.
     */
    @Nonnull
    public MapCursorWrapper getCursor(int index) {
        if (index > cursors.size()) {
            if (!cursors.isEmpty())
                return cursors.get(index - 1);
            return new MapCursorWrapper((byte) 0, (byte) 0, (byte) 0, MapCursor.Type.BANNER_BLACK, false);
        }
        return cursors.get(index);
    }

    /**
     * Retrieves a cursor from this collection by its ID.
     * <p>
     * Note: If the list is empty or the ID is not found,
     * it returns a new {@link MapCursorWrapper} that
     * is not visible on the map.
     *
     * @param id The ID of the cursor.
     * @return The {@link MapCursorWrapper} with the specified ID, or a fallback cursor if not found.
     */
    @Nonnull
    public MapCursorWrapper getCursorFromID(int id) {
        if (cursors.isEmpty())
            return new MapCursorWrapper((byte) 0, (byte) 0, (byte) 0, MapCursor.Type.BANNER_BLACK, false);
        for (MapCursorWrapper cursor : cursors) {
            if (cursor.getCursorId() == id)
                return cursor;
        }
        return new MapCursorWrapper((byte) 0, (byte) 0, (byte) 0, MapCursor.Type.BANNER_BLACK, false);
    }

    /**
     * Retrieve the set map cursors.
     *
     * @return list of  map cursors from an wrapper class.
     */
    public List<MapCursorWrapper> getCursors() {
        return cursors;
    }

    /**
     * Remove a cursor from the collection.
     *
     * @param cursor The MapCursor to remove.
     * @return Whether the cursor was removed successfully.
     */
    public boolean removeCursor(@Nonnull MapCursorWrapper cursor) {
        return cursors.remove(cursor);
    }

    /**
     * Add a cursor to the collection.
     *
     * @param x         The x coordinate, from -128 to 127.
     * @param y         The y coordinate, from -128 to 127.
     * @param direction The facing of the cursor, from 0 to 15.
     * @return The newly added MapCursor.
     */
    @Nonnull
    public MapCursorWrapper addCursor(int x, int y, byte direction) {
        return addCursor(x, y, direction, MapCursor.Type.WHITE_POINTER, true);
    }

    /**
     * Add a cursor to the collection.
     *
     * @param x         The x coordinate, from -128 to 127.
     * @param y         The y coordinate, from -128 to 127.
     * @param direction The facing of the cursor, from 0 to 15.
     * @param type      The type (color/style) of the map cursor.
     * @return The newly added MapCursor.
     */
    @Nonnull
    public MapCursorWrapper addCursor(int x, int y, byte direction, MapCursor.Type type) {
        return addCursor(x, y, direction, type, true);
    }

    /**
     * Add a cursor to the collection.
     *
     * @param x         The x coordinate, from -128 to 127.
     * @param y         The y coordinate, from -128 to 127.
     * @param direction The facing of the cursor, from 0 to 15.
     * @param type      The type (color/style) of the map cursor.
     * @param visible   Whether the cursor is visible.
     * @return The newly added MapCursor.
     */
    @Nonnull
    public MapCursorWrapper addCursor(int x, int y, byte direction, MapCursor.Type type, boolean visible) {
        return addCursor((byte) x, (byte) y, direction, type, visible, null);
    }

    /**
     * Add a cursor to the collection.
     *
     * @param x         The x coordinate, from -128 to 127.
     * @param y         The y coordinate, from -128 to 127.
     * @param direction The facing of the cursor, from 0 to 15.
     * @param type      The type (color/style) of the map cursor.
     * @param visible   Whether the cursor is visible.
     * @param caption   banner caption
     * @return The newly added MapCursor.
     */
    @Nonnull
    public MapCursorWrapper addCursor(int x, int y, byte direction, MapCursor.Type type, boolean visible, @Nullable String caption) {
        return addCursor(new MapCursorWrapper((byte) x, (byte) y, direction, type, visible, caption));
    }

    /**
     * Add a cursor to the collection.
     *
     * @param cursorWrapper add the created object to the list.
     * @return The newly added MapCursor.
     */
    public MapCursorWrapper addCursor(@Nonnull final MapCursorWrapper cursorWrapper) {
        cursors.add(cursorWrapper);
        mapCursorCollection.addCursor(cursorWrapper.getCursor());
        return cursorWrapper;
    }

    /**
     * Retrieve the collection of cursors from bukkit. Not recommended to attempt
     * to serialize this class.
     *
     * @return the MapCursorCollection instance.
     */
    public MapCursorCollection getMapCursorCollection() {
        return mapCursorCollection;
    }

    /**
     * Serialize list of MapCursors.
     *
     * @return map with data serialized.
     */
    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("cursors", cursors.stream().map(MapCursorWrapper::serialize).collect(Collectors.toList()));
        return map;
    }

}
