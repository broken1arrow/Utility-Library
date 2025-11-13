package org.broken.arrow.library.itemcreator.meta.map;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.RenderColors;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapPixel;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * A universal cache for asynchronously processing and storing {@link MapRendererData} instances.
 * <p>
 * Supports multiple input types such as images, text overlays, and raw pixel collections.
 * Heavy operations (e.g., image scaling or color mapping) are done off the main thread,
 * while final updates to {@link MapRendererData} are safely applied on the Bukkit main thread.
 * </p>
 * <p>
 * This allows large map updates to be processed without blocking the server tick loop,
 * while maintaining thread-safe integration when applying the results.
 * </p>
 */
public class MapRendererDataCache {
    private final Map<Integer, MapRendererData> rendererDataMap = new ConcurrentHashMap<>();

    /**
     * Processes an image asynchronously and stores it as a new renderer entry.
     * <p>
     * The image is scaled to 128×128 and converted into a list of {@link MapPixel} entries.
     * Once processed, the resulting {@link MapRendererData} is cached and the {@code onReady}
     * callback is invoked.
     * </p>
     * <p>
     * The callback normally runs on the main thread. If the plugin instance is unavailable,
     * it runs immediately on the current thread.
     * </p>
     *
     * @param image   the image to process
     * @param onReady optional callback executed when finished
     */
    public void processImageAsync(@Nonnull BufferedImage image, @Nullable final Consumer<MapRendererData> onReady) {
        final MapRendererData data = new MapRendererData();
        this.processRendererData(data, () -> {
            final BufferedImage scaled = scale(image, 128, 128);
            return new ArrayList<>(RenderColors.renderFromImage(scaled));
        }, false, onReady);
    }

    /**
     * Updates an existing cached renderer with a new image asynchronously.
     * <p>
     * If a renderer with the specified ID exists, its pixel data is replaced
     * after the image is processed. Once complete, the {@code onReady} callback
     * is invoked.
     * </p>
     * <p>
     * The callback normally runs on the main thread. If the plugin instance is unavailable,
     * it runs immediately on the current thread.
     * </p>
     *
     * @param rendererId the ID of the renderer to update
     * @param newImage   the new image to process and apply
     * @param onReady    optional callback executed when finished
     */
    public void updateCachedImage(final int rendererId, @Nonnull final BufferedImage newImage, @Nullable final Consumer<MapRendererData> onReady) {
        MapRendererData data = get(rendererId);
        if (data != null) {
            this.processRendererData(data, () -> new ArrayList<>(RenderColors.renderFromImage(newImage)), true, onReady);

        }
    }

    /**
     * Adds a new renderer entry asynchronously using prebuilt pixel data.
     * <p>
     * Useful for queuing and caching custom pixel structures (e.g., dynamic elements)
     * without blocking the main thread. The provided pixels are applied directly to
     * a new {@link MapRendererData} instance.
     * </p>
     * <p>
     * The callback normally runs on the main thread. If the plugin instance is unavailable,
     * it runs immediately on the current thread.
     * </p>
     *
     * @param pixels  the pixel collection to render
     * @param onReady optional callback executed when finished
     */
    public void addPixelsAsync(@Nonnull Collection<MapPixel> pixels, @Nullable Consumer<MapRendererData> onReady) {
        final MapRendererData data = new MapRendererData();
        this.processRendererData(data, () -> new ArrayList<>(pixels), false, onReady);
    }

    /**
     * Adds a text overlay asynchronously using an optional custom font.
     * <p>
     * If no font is provided, the default map font is used.
     * Supports Minecraft formatting codes:
     * <ul>
     *     <li>{@code &0 - &f} color codes</li>
     *     <li>{@code &l} bold</li>
     *     <li>{@code &o} shadow</li>
     *     <li>{@code &r} reset</li>
     * </ul>
     * </p>
     * <p>
     * The callback normally runs on the main thread. If the plugin instance is unavailable,
     * it runs immediately on the current thread.
     * </p>
     *
     * @param x       the X-coordinate of the text
     * @param y       the Y-coordinate of the text
     * @param text    the text to display
     * @param font    the optional {@link Font} used for rendering, or {@code null} for the default font
     * @param onReady optional callback executed when finished
     */
    public void addTextAsync(final int x, final int y, @Nonnull final String text, @Nullable final Font font, @Nullable final Consumer<MapRendererData> onReady) {
        final MapRendererData data = new MapRendererData();
        data.addText(x, y, text, font);
        this.processRendererData(data, data::getPixels, false, onReady);
    }

    /**
     * Retrieves all cached {@link MapRendererData} instances currently held by this cache.
     *
     * @return a live view of all cached renderer data
     */
    public Collection<MapRendererData> getAllRendererData() {
        return rendererDataMap.values();
    }

    /**
     * Retrieves a cached {@link MapRendererData} instance by its renderer ID.
     *
     * @param mapRenderId the renderer ID
     * @return the cached {@link MapRendererData}, or {@code null} if not found
     */
    public MapRendererData get(final int mapRenderId) {
        return rendererDataMap.get(mapRenderId);
    }

    /**
     * Internal helper for scaling images to map dimensions (e.g. 128×128).
     *
     * @param src the image to scale.
     * @param width the width of the image.
     * @param height the height of the image.
     * @return an copy of your image with new dimensions.
     */
    private BufferedImage scale(@Nonnull final BufferedImage src,final int width,final int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }


    /**
     * Handles asynchronous rendering and data injection for {@link MapRendererData}.
     * <p>
     * The heavy pixel processing runs asynchronously, while the final update is safely
     * applied on the main thread to maintain Bukkit thread safety.
     * </p>
     *
     *
     * @param data the {@link MapRendererData} instance it will apply the data.
     * @param pixelSupplier Provide a list of set pixels.
     * @param update if it shall update current set pixels, it will then clear the old pixels set.
     * @param onReady consumer that runs after data is processed.
     */
    private void processRendererData(@Nonnull final MapRendererData data, final @Nonnull Supplier<List<MapPixel>> pixelSupplier, final boolean update, @Nullable final Consumer<MapRendererData> onReady) {
        final int id = data.getMapRenderId();
        rendererDataMap.put(id, data);
        CompletableFuture.supplyAsync(pixelSupplier)
                .thenAccept(pixels -> {
                    if (update) data.clear();
                    data.addAll(pixels);
                    if (onReady != null) runSync(() -> onReady.accept(data));
                });
    }

    /**
     * Runs a task on the main server thread, or immediately if no plugin context exists.
     * <p>
     * Ensures safe Bukkit API usage even in non-standard contexts
     * (e.g., during testing or before plugin initialization).
     * </p>
     * @param runnable the task to run.
     */
    private void runSync(@Nonnull final Runnable runnable) {
        ItemCreator.runSync(runnable);
    }

}

