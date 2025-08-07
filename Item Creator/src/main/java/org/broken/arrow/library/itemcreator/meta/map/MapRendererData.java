package org.broken.arrow.library.itemcreator.meta.map;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.cursor.MapCursorAdapter;
import org.broken.arrow.library.itemcreator.meta.map.cursor.MapCursorWrapper;
import org.broken.arrow.library.itemcreator.meta.map.font.CharacterSprite;
import org.broken.arrow.library.itemcreator.meta.map.pixel.ImageOverlay;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapPixel;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapColoredPixel;
import org.broken.arrow.library.itemcreator.meta.map.pixel.TextOverlay;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MapRendererData {
    private static int id;
    private final int mapRenderId;
    private final MapRenderer mapRenderer;
    private MapCursorAdapter mapCursors = new MapCursorAdapter();
    private final List<MapPixel> pixels = new ArrayList<>();

    private MapRenderHandler dynamicRenderer;

    public MapRendererData() {
        this.mapRenderer = null;
        this.mapRenderId = id++;
    }

    public MapRendererData(final MapRenderer mapRenderer) {
        this.mapRenderer = mapRenderer;
        this.mapRenderId = id++;
    }

    public void setDynamicRenderer(MapRenderHandler handler) {
        this.dynamicRenderer = handler;
    }

    public void addPixel(int x, int y, Color color) {
        pixels.add(new MapColoredPixel(x, y, color));
    }

    public void addPixel(@Nonnull final MapColoredPixel mapColoredPixel) {
        pixels.add(mapColoredPixel);
    }

    public void addText(final int x, int y, final String text) {
        TextOverlay textOverlay = new TextOverlay(x, y, text);
        pixels.add(textOverlay);
    }

    public void addText(final int x, int y, final String text, final char ch, @Nonnull CharacterSprite sprite) {
        TextOverlay textOverlay = new TextOverlay(x, y, text);
        textOverlay.setMapFont(ch, sprite);
        pixels.add(textOverlay);
    }

    public void addText(@Nonnull final TextOverlay textOverlay) {
        pixels.add(textOverlay);
    }

    public void addImage(int x, int y, Image image) {
        pixels.add(new ImageOverlay(x, y, image));
    }

    public void addImage(@Nonnull final ImageOverlay imageOverlay) {
        pixels.add(imageOverlay);
    }

    public MapCursorWrapper addCursor(final byte x, final byte y, final byte direction, @Nonnull final MapCursor.Type type, final boolean visible) {
        return this.addCursor(x, y, direction, type, visible, null);
    }

    public MapCursorWrapper addCursor(final byte x, final byte y, final byte direction, @Nonnull final MapCursor.Type type, final boolean visible, @Nullable final String caption) {
        final MapCursorWrapper cursorWrapper = new MapCursorWrapper(x, y, direction, type, visible, caption);
        return this.addCursor(cursorWrapper);
    }

    public MapCursorWrapper addCursor(@Nonnull final MapCursorWrapper cursorWrapper) {
        mapCursors.addCursor(cursorWrapper);
        return cursorWrapper;
    }

    public int getMapRenderId() {
        return mapRenderId;
    }

    public MapCursorAdapter getMapCursors() {
        mapCursors.getCursor(1);
        return mapCursors;
    }

    public List<MapPixel> getPixels() {
        return pixels;
    }


    public MapRenderer getMapRenderer() {
        if (this.mapRenderer != null)
            return mapRenderer;
        return new MapRenderer() {
            @Override
            public void render(@Nonnull MapView map, @Nonnull MapCanvas canvas, @Nonnull Player player) {
                if (dynamicRenderer != null && dynamicRenderer.render(map, canvas, player))
                    return;
                canvas.setCursors(mapCursors.getMapCursorCollection());
                if (!getPixels().isEmpty()) {
                    getPixels().forEach(mapPixel -> {
                        if (mapPixel instanceof MapColoredPixel) {
                            if (ItemCreator.getServerVersion() < 20.0F)
                                canvas.setPixel(mapPixel.getX(), mapPixel.getY(), MapPalette.matchColor(((MapColoredPixel) mapPixel).getColor()));
                            else
                                canvas.setPixelColor(mapPixel.getX(), mapPixel.getY(), ((MapColoredPixel) mapPixel).getColor());
                        }
                        if (mapPixel instanceof TextOverlay) {
                            TextOverlay textOverlay = (TextOverlay) mapPixel;
                            canvas.drawText(mapPixel.getX(), mapPixel.getY(), textOverlay.getMapFont(), textOverlay.getText());
                        }
                        if (mapPixel instanceof ImageOverlay) {
                            ImageOverlay textOverlay = (ImageOverlay) mapPixel;
                            canvas.drawImage(mapPixel.getX(), mapPixel.getY(), textOverlay.getImage());
                        }
                    });
                }
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MapRendererData that = (MapRendererData) o;
        return mapRenderId == that.mapRenderId && Objects.equals(mapRenderer, that.mapRenderer) && Objects.equals(mapCursors, that.mapCursors) && Objects.equals(pixels, that.pixels) && Objects.equals(dynamicRenderer, that.dynamicRenderer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapRenderId, mapRenderer, mapCursors, pixels, dynamicRenderer);
    }

    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.putAll(mapCursors.serialize());
        map.put("pixels", this.pixels.stream().map(MapPixel::serialize).collect(Collectors.toList()));
        return map;
    }

    public static MapRendererData deserialize(Map<String, Object> map) {
        final Object cursors = map.get("cursors");
        final Object pixels = map.get("pixels");
        final MapRendererData mapRendererData = new MapRendererData();
        if (cursors instanceof List<?>) {
            MapCursorAdapter mapCursorAdapter = new MapCursorAdapter();
            for (Object cursor : (List<?>) cursors) {
                Map<String, Object> pixelMap = (Map<String, Object>) cursor;
                mapCursorAdapter.addCursor(MapCursorWrapper.deserialize(pixelMap));
            }
            mapRendererData.mapCursors = mapCursorAdapter;
        }
        if (pixels instanceof List<?>) {
            for (Object pixel : (List<?>) pixels) {
                Map<String, Object> pixelMap = (Map<String, Object>) pixel;
                String type = (String) pixelMap.get("type");
                if (type.equals("MapColoredPixel"))
                    mapRendererData.addPixel(MapColoredPixel.deserialize(pixelMap));
                if (type.equals("TextOverlay"))
                    mapRendererData.addText(TextOverlay.deserialize(pixelMap));
                if (type.equals("ImageOverlay"))
                    mapRendererData.addImage(ImageOverlay.deserialize(pixelMap));
            }
        }
        return mapRendererData;
    }

    @Override
    public String toString() {
        return "MapRendererData{" +
                "mapRenderId=" + mapRenderId +
                ", mapRenderer=" + mapRenderer +
                ", mapCursors=" + mapCursors +
                ", pixels=" + pixels +
                ", dynamicRenderer=" + dynamicRenderer +
                '}';
    }
}