package org.broken.arrow.library.itemcreator.meta.map.cursor;

import org.broken.arrow.library.itemcreator.ItemCreator;
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
public final class MapCursorHandler {
    private final List<MapCursorWrapper> cursors = new ArrayList<>();

    /**
     * Get the amount of cursors in this collection.
     *
     * @return The size of this collection.
     */
    public int size() {
        return cursors.size();
    }

    /**
     * Get a cursor from this collection.
     * <p>
     * Note: if the index is larger than the collection
     * it will try get the last valid index and if the list
     * is empty it will return a new {@link MapCursorWrapper}.
     *
     * @param index The index of the cursor.
     * @return The MapCursor.
     */
    @Nonnull
    public MapCursorWrapper getCursor(int index) {
        if (index > cursors.size()) {
            if (!cursors.isEmpty())
                return cursors.get(index - 1);
            return new MapCursorWrapper((byte) 0, (byte) 0, (byte) 0, MapCursor.Type.BANNER_BLACK,false);
        }
        return cursors.get(index);
    }

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

    public MapCursorWrapper addCursor(@Nonnull final MapCursorWrapper cursorWrapper) {
        cursors.add(cursorWrapper);
        return cursorWrapper;
    }

    public MapCursorCollection getMapCursorCollection() {
        MapCursorCollection mapCursorCollection = new MapCursorCollection();
        if (!cursors.isEmpty())
            cursors.forEach(cursorWrapper -> mapCursorCollection.addCursor(cursorWrapper.build()));
        return mapCursorCollection;
    }

    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("cursor", cursors.stream().map(MapCursorWrapper::serialize).collect(Collectors.toList()));
        return map;
    }

    public static MapCursorHandler deserialize(Map<String, Object> map) {
        MapCursorHandler mapCursor = new MapCursorHandler();
        List<?> cursors = (List<?>) map.getOrDefault("cursor", new ArrayList<>());
        for (Object cursor : cursors) {
            if (cursor instanceof Map<?, ?>) {
                Map<String, Object> cursorMap = ((Map<String, Object>) cursor);
                mapCursor.addCursor(MapCursorWrapper.deserialize(cursorMap));
            }
        }
        return mapCursor;
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
}
