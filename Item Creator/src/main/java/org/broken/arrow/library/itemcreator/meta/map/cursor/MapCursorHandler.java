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

/**
 * Represents all the map cursors on a {@link org.bukkit.map.MapCanvas}. Like MapCanvas, a
 * MapCursorCollection is linked to a specific {@link org.bukkit.map.MapRenderer}.
 */
public final class MapCursorHandler {
  private final List<MapCursor> cursors = new ArrayList<>();

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
   *
   * @param index The index of the cursor.
   * @return The MapCursor.
   */
  @Nonnull
  public MapCursor getCursor(int index) {
    return cursors.get(index);
  }

  public List<MapCursor> getCursors() {
    return cursors;
  }

  /**
   * Remove a cursor from the collection.
   *
   * @param cursor The MapCursor to remove.
   * @return Whether the cursor was removed successfully.
   */
  public boolean removeCursor(@Nonnull MapCursor cursor) {
    return cursors.remove(cursor);
  }

  /**
   * Add a cursor to the collection.
   *
   * @param cursor The MapCursor to add.
   * @return The MapCursor that was passed.
   */
  @Nonnull
  public MapCursor addCursor(@Nonnull MapCursor cursor) {
    cursors.add(cursor);
    return cursor;
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
  public MapCursor addCursor(int x, int y, byte direction) {
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
  public MapCursor addCursor(int x, int y, byte direction, MapCursor.Type type) {
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
  public MapCursor addCursor(int x, int y, byte direction, MapCursor.Type type, boolean visible) {
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
  public MapCursor addCursor(int x, int y, byte direction, MapCursor.Type type, boolean visible, @Nullable String caption) {
    if (ItemCreator.getServerVersion() < 12.1f)
      return addCursor(new MapCursor((byte) x, (byte) y, direction, type.getValue(), visible, caption));
    return addCursor(new MapCursor((byte) x, (byte) y, direction, type, visible, caption));
  }

  public MapCursorCollection getMapCursorCollection() {
    MapCursorCollection mapCursorCollection = new MapCursorCollection();
    if (!cursors.isEmpty())
      cursors.forEach(mapCursorCollection::addCursor);
    return mapCursorCollection;
  }

  @Nonnull
  public Map<String, Object> serialize() {
    Map<String, Object> map = new HashMap<>();
    List<Map<String, Object>> charsList = new ArrayList<>();
    cursors.forEach(cursor -> {
      Map<String, Object> chars = new HashMap<>();
      chars.put("type", cursor.getType() + "");
      chars.put("x", cursor.getX());
      chars.put("y", cursor.getY());
      chars.put("direction", cursor.getDirection());
      chars.put("visible", cursor.isVisible() + "");
      chars.put("caption", cursor.getCaption());
      charsList.add(chars);
    });
    map.put("cursor", charsList);
    return map;
  }

  public static MapCursorHandler deserialize(Map<String, Object> map) {
    MapCursorHandler mapCursor = new MapCursorHandler();
    List<?> cursors = (List<?>) map.getOrDefault("cursor", new ArrayList<>());
    for (Object cursor : cursors) {
      if (cursor instanceof Map<?, ?>) {
        Map<?, Object> cursorMap = ((Map<?, Object>) cursor);
        String type = (String) cursorMap.get("type");
        byte x = (byte) (int) cursorMap.get("x");
        byte y = (byte) (int)  cursorMap.get("y");
        byte direction = (byte) (int)  cursorMap.get("direction");
        boolean visible = Boolean.parseBoolean(cursorMap.get("visible") + "");
        String caption = (String) cursorMap.get("caption");
        mapCursor.addCursor(x, y, direction, byName(type), visible, caption);
      }
    }
    return mapCursor;
  }

  @Nonnull
  public static MapCursor.Type byName(final String name) {
    if(name == null)
      return MapCursor.Type.WHITE_POINTER;
    String nameFormatted = name.toUpperCase();
    for (MapCursor.Type t : MapCursor.Type.values()) {
      if (t.name().equals(nameFormatted)) return t;
    }
    return MapCursor.Type.WHITE_POINTER;
  }
}
