package org.broken.arrow.library.itemcreator.meta.map.utility;

import org.broken.arrow.library.itemcreator.meta.map.MapRenderHandler;
import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.broken.arrow.library.itemcreator.meta.map.MapRendererDataCache;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.ColorParser;
import org.broken.arrow.library.itemcreator.meta.map.cursor.MapCursorAdapter;
import org.broken.arrow.library.itemcreator.meta.map.cursor.MapCursorWrapper;
import org.broken.arrow.library.itemcreator.meta.map.font.customdraw.RenderState;
import org.broken.arrow.library.itemcreator.meta.map.pixel.ImageOverlay;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapColoredPixel;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapPixel;
import org.broken.arrow.library.itemcreator.meta.map.pixel.TextOverlay;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A builder-style helper for constructing layered map overlays for a
 * {@link MapRendererData} instance.
 *
 * <p>This class provides a high-level API for adding pixels, text, images,
 * and cursors in a defined layer order. Each call to an <code>add*</code>
 * method automatically assigns the overlay to the next available layer,
 * ensuring deterministic rendering from bottom to top.</p>
 *
 * <p>For advanced use cases that require precise control—including manual
 * layer management—refer to {@link MapRendererData} directly.</p>
 */
public class MapRendererBuilder {
    private final AtomicInteger layerCounter = new AtomicInteger();
    private final MapRendererData renderer;

    /**
     * The builder instance.
     * @param renderer the render instance to add the set pixels too.
     */
    public MapRendererBuilder(@Nonnull final MapRendererData renderer) {
        this.renderer = renderer;
    }

    /**
     * Sets a dynamic renderer callback capable of overriding the default
     * rendering process.
     *
     * <p>If the supplied {@link MapRenderHandler} returns {@code true}
     * during rendering, the internal renderer skips its normal pixel and
     * cursor processing.</p>
     *
     * @param handler the dynamic render handler to use
     */
    public void setDynamicRenderer(MapRenderHandler handler) {
        this.renderer.setDynamicRenderer(handler);
    }


    /**
     * Adds a single colored pixel to the next available layer.
     *
     * <p>See {@link MapRendererData} for details on how layering affects
     * final rendering.</p>
     *
     * @param x     the x-coordinate of the pixel
     * @param y     the y-coordinate of the pixel
     * @param color the pixel color
     * @return the assigned layer index
     */
    public int addPixel(int x, int y, Color color) {
        final int currentLayer = ensureFreeLayer();
        this.renderer.addPixel(currentLayer, new MapColoredPixel(x, y, color));
        return currentLayer;
    }

    /**
     * Adds an existing {@link MapColoredPixel} to the next available layer.
     *
     * <p>See {@link MapRendererData} for layering behavior.</p>
     *
     * @param pixel the colored pixel instance to add
     * @return the assigned layer index
     */
    public int addPixel(@Nonnull final MapColoredPixel pixel) {
        final int currentLayer = ensureFreeLayer();
        this.renderer.addPixel(currentLayer, pixel);
        return currentLayer;
    }

    /**
     * Adds a text overlay to the next available layer using the default
     * character set and no custom font.
     *
     * @param x    the x-coordinate of the text
     * @param y    the y-coordinate of the text
     * @param text the raw text to display
     * @return the assigned layer index
     */
    public int addText(final int x, int y, @Nonnull final String text) {
        return this.addText(x, y, text, null, null);
    }


    /**
     * Adds a text overlay using the default character set and a custom font.
     *
     * @param x    the x-coordinate
     * @param y    the y-coordinate
     * @param text the text to display
     * @param font the font to use for rendering, or {@code null} to use default font rendering
     * @return the assigned layer index
     */
    public int addText(final int x, int y, @Nonnull final String text, @Nullable final Font font) {
        return this.addText(x, y, text, null, font);
    }

    /**
     * Adds a text overlay with full customization options.
     *
     * @param x         the x-coordinate
     * @param y         the y-coordinate
     * @param text      the text to display
     * @param fontChars optional character mapping used to replace characters in the text
     * @param font      optional font used for glyph rendering
     * @return the assigned layer index
     */
    public int addText(final int x, int y, @Nonnull final String text, @Nullable final char[] fontChars, @Nullable final Font font) {
        final int currentLayer = ensureFreeLayer();
        this.renderer.addText(currentLayer, x, y, text, fontChars, font);
        return currentLayer;
    }

    /**
     * Adds a preconfigured {@link TextOverlay} to the next available layer.
     *
     * @param textOverlay the text overlay to add
     */
    public void addText(@Nonnull final TextOverlay textOverlay) {
        final int currentLayer = ensureFreeLayer();
        this.renderer.addText(currentLayer, textOverlay);
    }


    /**
     * Adds an image overlay to the next available layer at the specified
     * coordinates.
     *
     * <p>This performs only minimal scaling. Full preprocessing—such as
     * palette matching, color balancing, and pixel extraction—is available
     * through {@link MapRendererDataCache}.</p>
     *
     * @param x     the top-left x-coordinate (0–127)
     * @param y     the top-left y-coordinate (0–127)
     * @param image the image to draw
     * @return the assigned layer index
     */
    public int addImage(final int x, final int y, @Nonnull final Image image) {
        return this.addImage(new ImageOverlay(x, y, image));
    }

    /**
     * Adds a preconfigured {@link ImageOverlay} to the next available layer.
     *
     * @param imageOverlay the image overlay to add
     * @return the assigned layer index
     */
    public int addImage(@Nonnull final ImageOverlay imageOverlay) {
        final int currentLayer = ensureFreeLayer();
        this.renderer.addImage(currentLayer, imageOverlay);
        return currentLayer;
    }

    /**
     * Adds a cursor overlay to the map.
     *
     * <p>See {@link MapRendererData} documentation for details on overlay layering.</p>
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
     * <p>See {@link MapRendererData} documentation for details on overlay layering.</p>
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
     * <p>See {@link MapRendererData} documentation for details on overlay layering.</p>
     *
     * @param cursorWrapper The {@link MapCursorWrapper} to add.
     * @return The same cursor wrapper instance.
     */
    public MapCursorWrapper addCursor(@Nonnull final MapCursorWrapper cursorWrapper) {
        this.renderer.addCursor(cursorWrapper);
        return cursorWrapper;
    }

    /**
     * Replaces the content of a specific layer.
     *
     * <p>This does not affect the automatic layer counter. It is intended
     * for advanced use cases such as merging cached images or recalculating
     * a specific layer.</p>
     *
     * @param layer  the layer index to replace
     * @param pixels the new pixel list, or {@code null} to clear the layer
     */
    public void replaceLayer(final int layer, @Nullable final List<MapPixel> pixels) {
        if (pixels == null) {
            this.renderer.replaceLayer(layer, new ArrayList<>());
            return;
        }
        this.renderer.replaceLayer(layer, new ArrayList<>(pixels));
    }

    /**
     * Clears all layers and resets the automatic layer counter to zero.
     */
    public void clear() {
        this.renderer.clear();
        this.layerCounter.set(0);
    }

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     * <p>
     * @return Returns {@code true} if no pixel layers are present.
     */
    public boolean isPixelsEmpty() {
        return this.renderer.isPixelsEmpty();
    }

    /**
     * Returns a map of all layers and their associated pixel lists.
     *
     * @return The {@link Map} that contains a list of map pixels set for every layer.
     */
    public Map<Integer, List<MapPixel>> getLayers() {
        return this.renderer.getLayers();
    }

    /**
     * Returns a map of all layers and their associated pixel lists.
     *
     * @param layer the index of the layer.
     * @return returns the list of map pixels set for every layer.
     */
    public List<MapPixel> getLayer(int layer) {
        return this.renderer.getLayerPixels(layer);
    }


    /**
     * Returns a flattened list of all pixels across all layers.
     *
     * @return The {@link Map} that contains a list of map pixels set for every layer.
     */
    public List<MapPixel> getPixels() {
        return this.renderer.getPixels();
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
        return this.renderer.applyGlobalColorParser(text, index, renderState);
    }

    /**
     * Sets the global {@link ColorParser} used for all text instances that do not
     * have a per-text parser set.
     *
     * @param colorParser the global color parser to use
     * @see org.broken.arrow.library.itemcreator.meta.map.font.MapFontWrapper#applyColorParser(String, int, RenderState)
     */
    public void setGlobalColorParser(@Nonnull final ColorParser colorParser) {
        this.renderer.setGlobalColorParser(colorParser);
    }

    /**
     * Set whether the renderer is contextual, i.e. has different canvases for
     * different players.
     *
     * @param contextual Whether the renderer is contextual. See {@link
     *                   #isContextual()}.
     */
    public void setContextual(boolean contextual) {
        this.renderer.setContextual(contextual);
    }

    /**
     * Get whether the renderer is contextual, i.e. has different canvases for
     * different players.
     *
     * @return True if contextual, false otherwise.
     */
    public final boolean isContextual() {
        return this.renderer.isContextual();
    }

    /**
     * Returns the unique ID assigned to this map renderer data instance.
     *
     * @return The map render ID.
     */
    public int getMapRenderId() {
        return this.renderer.getMapRenderId();
    }

    /**
     * Returns the {@link MapCursorAdapter} that holds the cursors for this map view.
     *
     * @return the {@code MapCursorAdapter} instance containing the map cursors.
     */
    public MapCursorAdapter getMapCursors() {
        return this.renderer.getMapCursors();
    }


    /**
     * Returns the associated {@link MapRenderer}, or a default one which
     * renders the pixels and cursors and supports a dynamic renderer if set.
     *
     * @return The {@link MapRenderer} instance.
     */
    public MapRenderer getMapRenderer() {
        return this.renderer.getMapRenderer();
    }

    /**
     * Ensures that the next automatically assigned layer is not already
     * occupied (e.g. if the end user inserted their own layers before
     * using the builder).
     *
     * @return the next free layer index
     */
    private int ensureFreeLayer() {
        int layer = layerCounter.get();

        while (this.getLayer(layer) != null) {
            layer++;
        }

        layerCounter.set(layer + 1);
        return layer;
    }

}
