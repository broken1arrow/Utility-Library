package org.broken.arrow.library.itemcreator.meta.map;

import org.broken.arrow.library.itemcreator.meta.map.color.parser.AmpersandHexColorParser;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.ColorParser;
import org.broken.arrow.library.itemcreator.meta.map.cursor.MapCursorAdapter;
import org.broken.arrow.library.itemcreator.meta.map.cursor.MapCursorWrapper;
import org.broken.arrow.library.itemcreator.meta.map.font.Characters;
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
 *
 * <p>This class manages a unique map render ID, a custom {@link MapRenderer},
 * a collection of {@link MapCursorWrapper} instances via {@link MapCursorAdapter},
 * and a set of overlay elements such as {@link MapPixel}, text, and images.
 * It also supports a dynamic renderer handler for injecting custom render logic.
 * </p>
 *
 * <p>The class provides methods to add pixels, text overlays, images, and cursors,
 * and can serialize or deserialize its state to and from a map for persistence
 * or network transport.
 * </p>
 *
 * <strong>Overlay Layering and Render Order</strong>
 * <p>
 * All overlays are placed into numbered rendering layers. Layers determine draw
 * priority, overlays in lower-numbered layers are rendered first, and overlays in
 * higher-numbered layers are rendered on top of them.
 * </p>
 *
 * <p>
 * This layering system applies uniformly to all overlay types, including
 * {@link MapColoredPixel}, text overlays, image overlays, and custom pixel types.
 * If a layer does not yet exist when adding an element, it is created automatically.
 * </p>
 */
public class MapRendererData {
    private static int id;
    private final int mapRenderId;
    private final MapRenderer mapRenderer;
    private final Map<Integer, List<MapPixel>> layers = new HashMap<>();
    private final List<MapPixel> pixels = new ArrayList<>();
    private MapCursorAdapter mapCursors = new MapCursorAdapter();

    private MapRenderHandler dynamicRenderer;

    private char[] fontChars = Characters.getFontCharsArray();
    private ColorParser colorParser = new AmpersandHexColorParser();
    private boolean contextual;

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
     * Adds a colored pixel to the specified rendering layer at the given coordinates.
     *
     * <p>Layers determine rendering order: pixels in higher-numbered layers are drawn
     * after (and therefore on top of) pixels in lower-numbered layers. Layers that
     * have not been used before will be created automatically.</p>
     *
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
     *
     * @param layer the rendering layer index; higher values render above lower values
     * @param x     the x-coordinate of the pixel (0–127)
     * @param y     the y-coordinate of the pixel (0–127)
     * @param color the color of the pixel to add
     */
    public void addPixel(int layer, int x, int y, Color color) {
        this.addPixel(layer, new MapColoredPixel(x, y, color));
    }

    /**
     * Adds a colored pixel overlay to the map.
     *
     * <p>See {@link MapRendererData} documentation for details on overlay layering.</p>
     *
     * @param layer           the rendering layer index, higher values render above lower values.
     * @param mapColoredPixel The {@link MapColoredPixel} to add.
     */
    public void addPixel(int layer, @Nonnull final MapColoredPixel mapColoredPixel) {
        this.addMapPixel(layer, mapColoredPixel);
    }

    /**
     * Adds a text overlay to the map without a custom font character sprite.
     *
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
     *
     * @param layer the rendering layer index, higher values render above lower values.
     * @param x     The x-coordinate of the text.
     * @param y     The y-coordinate of the text.
     * @param text  The text to display.
     * @return returns the newly created text overlay, so you could set some of the options after.
     */
    public TextOverlay addText(int layer, final int x, int y, @Nonnull final String text) {
        return this.addText(layer, x, y, text, null, null);
    }

    /**
     * Adds a text overlay to the map with a custom font character sprite. Will use the default set
     * of characters instead.
     *
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
     *
     * @param layer the rendering layer index, higher values render above lower values.
     * @param x     The x-coordinate of the text.
     * @param y     The y-coordinate of the text.
     * @param text  The text to display.
     * @param font  The  font for the character.
     * @return returns the newly created text overlay, so you could set some of the options after.
     */
    public TextOverlay addText(int layer, final int x, int y, @Nonnull final String text, @Nullable final Font font) {
        return this.addText(layer, x, y, text, null, font);
    }

    /**
     * Adds a text overlay to the map with a custom font character sprite.
     *
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
     *
     * @param layer     the rendering layer index, higher values render above lower values.
     * @param x         The x-coordinate of the text.
     * @param y         The y-coordinate of the text.
     * @param text      The text to display.
     * @param fontChars Set the characters you want to replace in your text with the font.
     * @param font      The  font for the character
     * @return returns the newly created text overlay, so you could set some of the options after.
     */
    public TextOverlay addText(final int layer, final int x, int y, @Nonnull final String text, @Nullable final char[] fontChars, @Nullable final Font font) {
        TextOverlay textOverlay = new TextOverlay(x, y, text);
        if (font != null) {
            if (fontChars != null && fontChars.length > 0) {
                this.fontChars = fontChars;
            }
            textOverlay.setMapFont(this.fontChars, font);
        }
        this.addText(layer, textOverlay);
        return textOverlay;
    }

    /**
     * Adds a text overlay to the map.
     *
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
     *
     * @param layer       the rendering layer index, higher values render above lower values.
     * @param textOverlay The {@link TextOverlay} instance to add.
     */
    public void addText(final int layer, @Nonnull final TextOverlay textOverlay) {
        this.addMapPixel(layer, textOverlay);
    }


    /**
     * Adds an image overlay to the renderer at the specified map coordinates.
     *
     * <p>This method creates a new {@link ImageOverlay} using the supplied image
     * and inserts it into the pixel list. Only basic scaling is performed at this
     * stage. Advanced preprocessing—such as color balancing, palette matching,
     * or pixel extraction—is applied only when using {@link MapRendererDataCache}
     * together with {@link BuildMapView#loadCachedLayer(int, MapRendererDataCache)}.
     * </p>
     *
     * <p>When using the cache, all images are preprocessed asynchronously and then
     * registered automatically.
     * </p>
     *
     * <p><strong>Note:</strong> Adding raw images directly may introduce a performance
     * penalty during rendering, because preprocessing must be performed on the main
     * thread during the live map render.
     * </p>
     *
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
     *
     * @param layer the rendering layer index, higher values render above lower values.
     * @param x     the top-left X position on the map (0–127)
     * @param y     the top-left Y position on the map (0–127)
     * @param image the image to draw at the given position
     */
    public void addImage(final int layer, final int x, final int y, @Nonnull final Image image) {
        this.addImage(layer, new ImageOverlay(x, y, image));
    }

    /**
     * Adds an existing {@link ImageOverlay} instance to the renderer.
     *
     * <p>The overlay is inserted exactly as provided. Only basic scaling
     * occurs inside {@link ImageOverlay}. More advanced preprocessing—such
     * as color rebalancing or pixel conversion—is performed only when using
     * {@link MapRendererDataCache} instead of adding images directly.
     * </p>
     *
     * <p><strong>Note:</strong> Adding raw images directly may introduce a performance
     * penalty during rendering, because preprocessing is performed on the main thread
     * instead of being handled asynchronously by the cache.
     * </p>
     *
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
     *
     * @param layer        the rendering layer index, higher values render above lower values.
     * @param imageOverlay the preconfigured {@link ImageOverlay} to add
     */
    public void addImage(final int layer, @Nonnull final ImageOverlay imageOverlay) {
        this.addMapPixel(layer, imageOverlay);
    }

    /**
     * Adds a cursor overlay to the map.
     *
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
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
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
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
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
     *
     * @param cursorWrapper The {@link MapCursorWrapper} to add.
     * @return The same cursor wrapper instance.
     */
    public MapCursorWrapper addCursor(@Nonnull final MapCursorWrapper cursorWrapper) {
        mapCursors.addCursor(cursorWrapper);
        return cursorWrapper;
    }

    /**
     * Sets a collection of map overlays (pixels, text, or images) to this renderer.
     * <p>
     * This method appends the provided overlays and replace the existing pixel list.
     * It can be used to add multiple overlay types at once or to batch-apply
     * preprocessed render data.
     * </p>
     *
     * <p><strong>Note:</strong> For most use cases, prefer the more specific
     * methods such as {@link #addPixel(int, int, int, Color)} ,{@link #addText(int, int, int, String)}
     * or {@link #addImage(int, int, int, Image)}} for clarity.</p>
     *
     * <p>See {@link MapRendererData} for a detailed description of the layering system.</p>
     *
     * @param layer the rendering layer index, higher values render above lower values.
     * @param pixels the list of {@link MapPixel} instances to replace the layer with.
     */
    public void replaceLayer(final int layer, @Nonnull final List<MapPixel> pixels) {
        layers.put(layer, new ArrayList<>(pixels));
    }

    /**
     * Clear the map of set pixels.
     */
    public void clear() {
        this.layers.clear();
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map of pixels contains no mappings.
     */
    public boolean isPixelsEmpty() {
        return this.layers.isEmpty();
    }

    /**
     * Returns the collection of pixels assigned to the specified layer.
     *
     * @param layer the index of the layer
     * @return the pixel list for this layer, or {@code null} if the layer has
     * not been initialized
     */
    @Nullable
    public List<MapPixel> getLayerPixels(final int layer) {
        return this.getLayers().get(layer);
    }

    /**
     * Returns the current collection of map pixels.
     *
     * @return The {@link Map} that contains a list of map pixels set for every layer.
     */
    public Map<Integer, List<MapPixel>> getLayers() {
        return layers;
    }

    /**
     * Returns the current collection of map pixels.
     *
     * @return The {@link Map} that contains a list of map pixels set for every layer.
     */
    public List<MapPixel> getPixels() {
        List<MapPixel> all = new ArrayList<>();

        layers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> all.addAll(e.getValue()));

        return all;
    }

    /**
     * Applies the global {@link ColorParser} to the text at the specified index.
     * <p>
     * This works like the per-text parser, but uses the global parser set for all text
     * instances that do not have a per-instance override.
     *
     * @param text        the text to parse
     * @param index       the starting position in the text
     * @param renderState the current render state to update with color/style
     * @return the number of characters consumed by the formatting code, or 0 if none
     * @see org.broken.arrow.library.itemcreator.meta.map.font.MapFontWrapper#applyColorParser(String, int, RenderState)
     */
    public int applyGlobalColorParser(@Nonnull final String text, final int index, @Nonnull final RenderState renderState) {
        return colorParser.tryParse(text, index, renderState);
    }

    /**
     * Sets the global {@link ColorParser} used for all text instances that do not
     * have a per-text parser set.
     *
     * @param colorParser the global color parser to use
     * @see org.broken.arrow.library.itemcreator.meta.map.font.MapFontWrapper#applyColorParser(String, int, RenderState)
     */
    public void setGlobalColorParser(@Nonnull final ColorParser colorParser) {
        this.colorParser = colorParser;
    }

    /**
     * Set whether the renderer is contextual, i.e. has different canvases for
     * different players.
     *
     * @param contextual Whether the renderer is contextual. See {@link
     *                   #isContextual()}.
     */
    public void setContextual(boolean contextual) {
        this.contextual = contextual;
    }

    /**
     * Get whether the renderer is contextual, i.e. has different canvases for
     * different players.
     *
     * @return True if contextual, false otherwise.
     */
    public final boolean isContextual() {
        return contextual;
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
     * Returns the associated {@link MapRenderer}, or a default one which
     * renders the pixels and cursors and supports a dynamic renderer if set.
     *
     * @return The {@link MapRenderer} instance.
     */
    public MapRenderer getMapRenderer() {
        if (this.mapRenderer != null) {
            return mapRenderer;
        }
        return new MapRenderer(this.contextual) {
            @Override
            public void render(@Nonnull MapView map, @Nonnull MapCanvas canvas, @Nonnull Player player) {
                if (dynamicRenderer != null && dynamicRenderer.render(map, canvas, player))
                    return;
                canvas.setCursors(mapCursors.getMapCursorCollection());

                if (!isPixelsEmpty()) {
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
        map.put("pixels", this.layers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, mapPixels -> mapPixels.getValue().stream().map(MapPixel::serialize))));
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
                for (Map.Entry<String, Object> mapPixels : pixelMap.entrySet()) {
                    Map<String, Object> mapPixelsValue = (Map<String, Object>) mapPixels.getValue();
                    String type = (String) mapPixelsValue.get("type");
                    int layer;
                    try {
                        layer = Integer.parseInt(mapPixels.getKey());
                    } catch (NumberFormatException ignore) {
                        layer = 0;
                    }
                    if (type.equals("MapColoredPixel"))
                        mapRendererData.addPixel(layer, MapColoredPixel.deserialize(mapPixelsValue));
                    if (type.equals("TextOverlay"))
                        mapRendererData.addText(layer, TextOverlay.deserialize(mapPixelsValue));
                    if (type.equals("ImageOverlay"))
                        mapRendererData.addImage(layer, ImageOverlay.deserialize(mapPixelsValue));
                }
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

    private void addMapPixel(final int layer, final MapPixel mapPixel) {
        layers.computeIfAbsent(layer, k -> new ArrayList<>()).add(mapPixel);
    }

    private void setPixels(@Nonnull final MapCanvas canvas) {
        getPixels().forEach(mapPixel -> mapPixel.render(this, canvas));
    }


}