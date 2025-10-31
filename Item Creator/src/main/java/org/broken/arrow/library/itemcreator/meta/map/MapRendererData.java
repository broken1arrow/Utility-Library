package org.broken.arrow.library.itemcreator.meta.map;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.AmpersandHexColorParser;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.ColorParser;
import org.broken.arrow.library.itemcreator.meta.map.cursor.MapCursorAdapter;
import org.broken.arrow.library.itemcreator.meta.map.cursor.MapCursorWrapper;
import org.broken.arrow.library.itemcreator.meta.map.font.customdraw.MapTextRenderer;
import org.broken.arrow.library.itemcreator.meta.map.font.customdraw.RenderState;
import org.broken.arrow.library.itemcreator.meta.map.pixel.ImageOverlay;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapPixel;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapColoredPixel;
import org.broken.arrow.library.itemcreator.meta.map.pixel.TextOverlay;
import org.bukkit.entity.Player;
import org.bukkit.map.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Holds rendering data and state for a Minecraft map renderer.
 * <p>
 * This class manages a unique map render ID, a custom {@link MapRenderer},
 * a collection of {@link MapCursorWrapper} instances via {@link MapCursorAdapter},
 * and a list of {@link MapPixel} overlays including pixels, text, and images.
 * It also supports a dynamic renderer handler for custom render logic.
 * </p>
 * <p>
 * The class provides methods to add pixels, text overlays, images, and cursors,
 * and can serialize/deserialize its state to/from a map for persistence or network transfer.
 * </p>
 */
public class MapRendererData {
    private static int id;
    private final int mapRenderId;
    private final MapRenderer mapRenderer;
    private MapCursorAdapter mapCursors = new MapCursorAdapter();
    private final List<MapPixel> pixels = new ArrayList<>();
    private MapRenderHandler dynamicRenderer;
    private char[] fontChars = Characters.getFontCharsArray();
    private ColorParser colorParser = new AmpersandHexColorParser();

    /**
     * Constructs a new MapRendererData instance with no associated {@link MapRenderer}.
     * A unique map render ID is automatically assigned.
     */
    public MapRendererData() {
        this(null);
    }

    /**
     * Constructs a new MapRendererData instance with the given {@link MapRenderer}.
     * A unique map render ID is automatically assigned.
     *
     * @param mapRenderer The underlying map renderer, or null to use the dynamic renderer.
     */
    public MapRendererData(final MapRenderer mapRenderer) {
        this.mapRenderer = mapRenderer;
        this.mapRenderId = id++;
    }

    /**
     * Sets the dynamic renderer handler that can override rendering behavior.
     *
     * @param handler The dynamic {@link MapRenderHandler} to use during rendering.
     */
    public void setDynamicRenderer(MapRenderHandler handler) {
        this.dynamicRenderer = handler;
    }

    /**
     * Adds a colored pixel overlay to the map at the specified coordinates.
     *
     * @param x     The x-coordinate of the pixel.
     * @param y     The y-coordinate of the pixel.
     * @param color The color of the pixel.
     */
    public void addPixel(int x, int y, Color color) {
        pixels.add(new MapColoredPixel(x, y, color));
    }

    /**
     * Adds a colored pixel overlay to the map.
     *
     * @param mapColoredPixel The {@link MapColoredPixel} to add.
     */
    public void addPixel(@Nonnull final MapColoredPixel mapColoredPixel) {
        pixels.add(mapColoredPixel);
    }

    /**
     * Adds a text overlay to the map without a custom font character sprite.
     *
     * @param x    The x-coordinate of the text.
     * @param y    The y-coordinate of the text.
     * @param text The text to display.
     */
    public void addText(final int x, int y, final String text) {
        this.addText(x, y, text, null, null);
    }

    /**
     * Adds a text overlay to the map with a custom font character sprite. Will use the default set
     * of characters instead.
     *
     * @param x    The x-coordinate of the text.
     * @param y    The y-coordinate of the text.
     * @param text The text to display.
     * @param font The  font for the character.
     */
    public void addText(final int x, int y, final String text, @Nullable final Font font) {
        this.addText(x, y, text, null, font);
    }

    /**
     * Adds a text overlay to the map with a custom font character sprite.
     *
     * @param x         The x-coordinate of the text.
     * @param y         The y-coordinate of the text.
     * @param text      The text to display.
     * @param fontChars Set the characters you want to replace in your text with the font.
     * @param font      The  font for the character
     */
    public void addText(final int x, int y, @Nonnull final String text, @Nullable final char[] fontChars, @Nullable final Font font) {
        TextOverlay textOverlay = new TextOverlay(x, y, text);
        if (font != null) {
            if (fontChars != null && fontChars.length > 0) {
                this.fontChars = fontChars;
            }
            textOverlay.setMapFont(this.fontChars, font);
        }
        this.addText(textOverlay);
    }

    /**
     * Adds a text overlay to the map.
     *
     * @param textOverlay The {@link TextOverlay} instance to add.
     */
    public void addText(@Nonnull final TextOverlay textOverlay) {
        pixels.add(textOverlay);
    }

    /**
     * Applies the global {@link ColorParser} to the text at the specified index.
     * <p>
     * This works like the per-text parser, but uses the global parser set for all text
     * instances that do not have a per-instance override.
     *
     * @see org.broken.arrow.library.itemcreator.meta.map.font.MapFontWrapper#applyColorParser(String, int, RenderState)
     * @param text        the text to parse
     * @param index       the starting position in the text
     * @param renderState the current render state to update with color/style
     * @return the number of characters consumed by the formatting code, or 0 if none
     */
    public int applyGlobalColorParser(@Nonnull final String text, final int index, @Nonnull final RenderState renderState) {
        return colorParser.tryParse(text, index, renderState);
    }

    /**
     * Sets the global {@link ColorParser} used for all text instances that do not
     * have a per-text parser set.
     *
     * @see org.broken.arrow.library.itemcreator.meta.map.font.MapFontWrapper#applyColorParser(String, int, RenderState)
     * @param colorParser the global color parser to use
     */
    public void setGlobalColorParser(@Nonnull final ColorParser colorParser) {
        this.colorParser = colorParser;
    }

    /**
     * Adds an image overlay to the map at the specified coordinates.
     *
     * @param x     The x-coordinate of the image.
     * @param y     The y-coordinate of the image.
     * @param image The image to display.
     */
    public void addImage(final int x, final int y, @Nonnull final Image image) {
        pixels.add(new ImageOverlay(x, y, image));
    }

    /**
     * Adds an image overlay to the map.
     *
     * @param imageOverlay The {@link ImageOverlay} instance to add.
     */
    public void addImage(@Nonnull final ImageOverlay imageOverlay) {
        pixels.add(imageOverlay);
    }

    /**
     * Adds a cursor overlay to the map.
     *
     * @param x         The x-coordinate of the cursor.
     * @param y         The y-coordinate of the cursor.
     * @param direction The direction the cursor points to.
     * @param type      The cursor type.
     * @param visible   Whether the cursor is visible.
     * @return The added {@link MapCursorWrapper}.
     */
    public MapCursorWrapper addCursor(final byte x, final byte y, final byte direction, @Nonnull final MapCursor.Type type, final boolean visible) {
        return this.addCursor(x, y, direction, type, visible, null);
    }

    /**
     * Adds a cursor overlay to the map with an optional caption.
     *
     * @param x         The x-coordinate of the cursor.
     * @param y         The y-coordinate of the cursor.
     * @param direction The direction the cursor points to.
     * @param type      The cursor type.
     * @param visible   Whether the cursor is visible.
     * @param caption   Optional caption for the cursor, or null if none.
     * @return The added {@link MapCursorWrapper}.
     */
    public MapCursorWrapper addCursor(final byte x, final byte y, final byte direction, @Nonnull final MapCursor.Type type, final boolean visible, @Nullable final String caption) {
        final MapCursorWrapper cursorWrapper = new MapCursorWrapper(x, y, direction, type, visible, caption);
        return this.addCursor(cursorWrapper);
    }

    /**
     * Adds a cursor overlay to the map.
     *
     * @param cursorWrapper The {@link MapCursorWrapper} to add.
     * @return The same cursor wrapper instance.
     */
    public MapCursorWrapper addCursor(@Nonnull final MapCursorWrapper cursorWrapper) {
        mapCursors.addCursor(cursorWrapper);
        return cursorWrapper;
    }

    /**
     * Returns the unique ID assigned to this map renderer data instance.
     *
     * @return The map render ID.
     */
    public int getMapRenderId() {
        return mapRenderId;
    }

    /**
     * Returns the {@link MapCursorAdapter} that holds the cursors for this map view.
     *
     * @return the {@code MapCursorAdapter} instance containing the map cursors.
     */
    public MapCursorAdapter getMapCursors() {
        return mapCursors;
    }

    /**
     * Returns the current collection of map pixels.
     *
     * @return The {@link MapCursorAdapter} managing  pixels.
     */
    public List<MapPixel> getPixels() {
        return pixels;
    }

    /**
     * Returns the associated {@link MapRenderer}, or a default one which
     * renders the pixels and cursors and supports a dynamic renderer if set.
     *
     * @return The {@link MapRenderer} instance.
     */
    public MapRenderer getMapRenderer() {
        if (this.mapRenderer != null) {
            return mapRenderer;
        }
        return new MapRenderer() {
            @Override
            public void render(@Nonnull MapView map, @Nonnull MapCanvas canvas, @Nonnull Player player) {
                if (dynamicRenderer != null && dynamicRenderer.render(map, canvas, player))
                    return;
                canvas.setCursors(mapCursors.getMapCursorCollection());
                if (!getPixels().isEmpty()) {
                    setPixels(canvas);
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

    /**
     * Serializes this MapRendererData to a map representation for persistence.
     * The map includes serialized cursors and pixels.
     *
     * @return A map representing this MapRendererData's state.
     */
    @Nonnull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.putAll(mapCursors.serialize());
        map.put("pixels", this.pixels.stream().map(MapPixel::serialize).collect(Collectors.toList()));
        return map;
    }

    /**
     * Deserializes a MapRendererData instance from a map representation.
     *
     * @param map The map containing serialized MapRendererData data.
     * @return A new {@link MapRendererData} instance reconstructed from the map.
     */
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

    private void setPixels(@Nonnull final MapCanvas canvas) {
        getPixels().forEach(mapPixel -> {
            if (mapPixel instanceof MapColoredPixel) {
                final Color color = ((MapColoredPixel) mapPixel).getColor();
                if (ItemCreator.getServerVersion() < 20.0F)
                    canvas.setPixel(mapPixel.getX(), mapPixel.getY(), MapPalette.matchColor(color));
                else
                    canvas.setPixelColor(mapPixel.getX(), mapPixel.getY(), color);
            }
            if (mapPixel instanceof TextOverlay) {
                TextOverlay textOverlay = (TextOverlay) mapPixel;
                final MapFont mapFont = textOverlay.getMapFont();
                if (mapFont instanceof MinecraftFont)
                    canvas.drawText(mapPixel.getX(), mapPixel.getY(), mapFont, textOverlay.getText());
                else {
                    final MapTextRenderer mapTextRenderer = new MapTextRenderer(canvas, this, textOverlay);
                    mapTextRenderer.drawCustomFontText(mapPixel.getX(), mapPixel.getY());
                }
            }
            if (mapPixel instanceof ImageOverlay) {
                ImageOverlay textOverlay = (ImageOverlay) mapPixel;
                final Image image = textOverlay.getImage();
                if (image != null)
                    canvas.drawImage(mapPixel.getX(), mapPixel.getY(), image);
            }
        });
    }
}