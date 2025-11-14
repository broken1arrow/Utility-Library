package org.broken.arrow.library.itemcreator.meta.map;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.RenderColors;
import org.broken.arrow.library.itemcreator.meta.map.font.Characters;
import org.broken.arrow.library.itemcreator.meta.map.pixel.ImageOverlay;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapColoredPixel;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapPixel;
import org.broken.arrow.library.itemcreator.meta.map.pixel.TextOverlay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    private static final int MAX_PIXEL_SIZE = 200;
    private final Map<Integer, PixelCacheEntry> rendererDataMap = new ConcurrentHashMap<>();
    private boolean applyColorBalance = true;
    private boolean preConvertToPixels = true;
    private boolean applyScaling = true;

    /**
     * Processes an image asynchronously and stores it as a new renderer entry.
     * <p>
     * The image is scaled to 128×128 and converted into a list of {@link MapPixel} entries.
     * Once processed, the resulting {@link PixelCacheEntry} is cached and the {@code onReady}
     * callback is invoked.
     * </p>
     * <p>
     * The callback normally runs on the main thread. If the plugin instance is unavailable,
     * it runs immediately on the current thread.
     * </p>
     *
     * @param image   the image to process
     * @param onReady optional callback executed when finished
     * @return the id from the {@link PixelCacheEntry} instance.
     */
    public int processImageAsync(@Nonnull BufferedImage image, @Nullable final Runnable onReady) {
        PixelCacheEntry pixelCacheEntry = new PixelCacheEntry();
        return this.processRendererData(pixelCacheEntry, () -> getMapPixels(image), false,
                onReady);
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
     * @param image      the new image to process and apply
     * @param onReady    optional callback executed when finished
     */
    public void updateCachedImage(final int rendererId, @Nonnull final BufferedImage image, @Nullable final Runnable onReady) {
        PixelCacheEntry data = get(rendererId);
        if (data != null) {
            this.processRendererData(data, () -> getMapPixels(image),
                    true,
                    onReady);
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
     * @return the id from the {@link PixelCacheEntry} instance.
     */
    public int addPixelsAsync(@Nonnull Collection<MapPixel> pixels, @Nullable final Runnable onReady) {
        final PixelCacheEntry data = new PixelCacheEntry();
        return this.processRendererData(data, () -> new ArrayList<>(pixels), false, onReady);
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
     * @return the id from the {@link PixelCacheEntry} instance.
     */
    public int addTextAsync(final int x, final int y, @Nonnull final String text, @Nullable final Font font, @Nullable final Runnable onReady) {
        final PixelCacheEntry data = new PixelCacheEntry();
        return this.processRendererData(data, () -> {
                    TextOverlay textOverlay = new TextOverlay(x, y, text);
                    if (font != null) {
                        textOverlay.setMapFont(Characters.getFontCharsArray(), font);
                    }
                    return Collections.singletonList(textOverlay);
                },
                false,
                onReady
        );
    }

    /**
     * Checks whether the map renderer should attempt to rebalance colors.
     * <p>
     * Useful because Minecraft maps have a limited color palette. When enabled,
     * the renderer will adjust image colors to better match the palette.
     * </p>
     *
     * @return {@code true} if color balancing is enabled, {@code false} otherwise.
     */
    public boolean isApplyColorBalance() {
        return applyColorBalance;
    }

    /**
     * Enables or disables automatic color balancing.
     *
     * @param applyColorBalance {@code true} to rebalance colors for better map representation,
     *                          {@code false} to leave the original image colors.
     */
    public void setApplyColorBalance(final boolean applyColorBalance) {
        this.applyColorBalance = applyColorBalance;
    }

    /**
     * Checks whether the image should be converted directly to pixels,
     * bypassing Bukkit's automatic color transformation.
     * <p>
     * This can improve performance by skipping Bukkit's internal conversion.
     * It only applies if color balancing is disabled ({@link #isApplyColorBalance()} returns {@code false}).
     * </p>
     *
     * @return {@code true} if direct pixel conversion is enabled, {@code false} otherwise.
     */
    public boolean isPreConvertToPixels() {
        return preConvertToPixels;
    }

    /**
     * Enables or disables direct pixel conversion of the image, bypassing
     * Bukkit's internal transformation.
     *
     * @param preConvertToPixels {@code true} to convert the image to pixels directly,
     *                           {@code false} to let Bukkit handle conversion automatically.
     */
    public void setPreConvertToPixels(final boolean preConvertToPixels) {
        this.preConvertToPixels = preConvertToPixels;
    }

    /**
     * Checks whether the image should be automatically scaled to fit the map.
     *
     * @return {@code true} if automatic scaling is enabled, {@code false} otherwise.
     */
    public boolean isApplyScaling() {
        return applyScaling;
    }

    /**
     * Enables or disables automatic scaling of the image to fit the map size.
     *
     * <p>When enabled, the image will be scaled to 128×128 to fit the map automatically.
     * When disabled, the image retains its original size, which may be larger than the map
     * and cause pixels outside the 0–127 range to be ignored. Scaling large images may be
     * costly, so use this option judiciously.</p>
     *
     * @param applyScaling {@code true} to scale the image automatically, {@code false} to keep its original size.
     */
    public void setApplyScaling(final boolean applyScaling) {
        this.applyScaling = applyScaling;
    }

    /**
     * Retrieves all cached {@link PixelCacheEntry} instances currently held by this cache.
     *
     * @return a live view of all cached renderer data
     */
    public Collection<PixelCacheEntry> getAllRendererData() {
        return rendererDataMap.values();
    }

    /**
     * Retrieves a cached {@link PixelCacheEntry} instance by its renderer ID.
     *
     * @param mapRenderId the renderer ID
     * @return the cached {@link PixelCacheEntry}, or {@code null} if not found
     */
    @Nullable
    public PixelCacheEntry get(final int mapRenderId) {
        return rendererDataMap.get(mapRenderId);
    }

    /**
     * Removes a cached {@link PixelCacheEntry} by its ID.
     * <p>
     * This is useful when you want to discard preprocessed image or pixel data
     * that is no longer needed, such as when unloading a map, replacing an asset,
     * or cleaning up memory during plugin shutdown.
     * </p>
     *
     * @param id the ID of the cached renderer entry to remove
     * @return {@code true} if an entry with the given ID existed and was removed,
     *         {@code false} if no entry with that ID was present in the cache
     */
    public boolean remove(int id) {
        return rendererDataMap.remove(id) != null;
    }

    /**
     * Handles asynchronous rendering and data injection for {@link MapRendererData}.
     * <p>
     * The heavy pixel processing runs asynchronously, while the final update is safely
     * applied on the main thread to maintain Bukkit thread safety.
     * </p>
     *
     * @param data          the {@link MapRendererData} instance it will apply the data.
     * @param pixelSupplier Provide a list of set pixels.
     * @param update        if it shall update current set pixels, it will then clear the old pixels set.
     * @param onReady       consumer that runs after data is processed.
     * @return the id from the {@link PixelCacheEntry} instance.
     */
    private int processRendererData(@Nonnull final PixelCacheEntry data, final @Nonnull Supplier<List<MapPixel>> pixelSupplier, final boolean update, @Nullable final Runnable onReady) {
        final int id = data.getId();
        rendererDataMap.put(id, data);
        CompletableFuture.supplyAsync(pixelSupplier)
                .thenAccept(pixels -> {
                    if (update)
                        data.setPixels(pixels);
                    else
                        data.appendPixels(pixels);

                    if (onReady != null) runSync(onReady);
                });
        return id;
    }

    /**
     * Runs a task on the main server thread, or immediately if no plugin context exists.
     * <p>
     * Ensures safe Bukkit API usage even in non-standard contexts
     * (e.g., during testing or before plugin initialization).
     * </p>
     *
     * @param runnable the task to run.
     */
    private void runSync(@Nonnull final Runnable runnable) {
        ItemCreator.runSync(runnable);
    }

    private BufferedImage getBufferedImage(BufferedImage img) {
        if (!applyScaling)
            return deepCopy(img);
        return ItemCreator.scale(img, 128, 128);
    }

    private static BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
                source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    private List<MapPixel> getMapPixels(final BufferedImage image) {
        BufferedImage scaled = getBufferedImage(image);
        if (applyColorBalance)
            return RenderColors.renderFromImage(scaled);
        if (!preConvertToPixels)
            return Collections.singletonList(new ImageOverlay(0, 0, scaled));
        return addPixels(scaled.getHeight(), scaled.getWidth(), scaled);
    }

    private List<MapPixel> addPixels(final int height, final int width, @Nonnull final BufferedImage filtered) {
        List<MapPixel> mapColoredPixels = new ArrayList<>();
        int adjustedWidth = Math.min(width, MAX_PIXEL_SIZE);
        int adjustedHeight = Math.min(height, MAX_PIXEL_SIZE);

        for (int y = 0; y < adjustedHeight; y++) {
            for (int x = 0; x < adjustedWidth; x++) {
                mapColoredPixels.add(new MapColoredPixel(x, y, new Color(filtered.getRGB(x, y))));
            }
        }
        return mapColoredPixels;
    }

    /**
     * Represents a cache entry for storing a list of {@link MapPixel} objects.
     * Each entry has a unique ID and supports thread-safe updates to its pixel list.
     */
    public static final class PixelCacheEntry {
        private static final AtomicInteger COUNTER = new AtomicInteger();
        private final int id = COUNTER.getAndIncrement();
        private List<MapPixel> pixels = Collections.emptyList();

        /**
         * Returns the unique ID of this cache entry.
         *
         * @return the unique ID
         */
        public int getId() {
            return id;
        }

        /**
         * Replaces the current list of pixels with a new list.
         * The new list is stored as an unmodifiable copy to prevent external modification.
         *
         * @param newPixels the new list of pixels; must not be null
         */
        public synchronized void setPixels(@Nonnull final List<MapPixel> newPixels) {
            this.pixels = Collections.unmodifiableList(new ArrayList<>(newPixels));
        }

        /**
         * Appends additional pixels to the current list.
         * The combined list is stored as an unmodifiable copy to prevent external modification.
         *
         * @param extraPixels the pixels to append; must not be null
         */
        public synchronized void appendPixels(@Nonnull final List<MapPixel> extraPixels) {
            List<MapPixel> combined = new ArrayList<>(this.pixels.size() + extraPixels.size());
            combined.addAll(this.pixels);
            combined.addAll(extraPixels);
            this.pixels = Collections.unmodifiableList(combined);
        }

        /**
         * Returns the current list of pixels.
         * The returned list is unmodifiable.
         *
         * @return the list of pixels
         */
        public List<MapPixel> getPixels() {
            return pixels;
        }
    }
}

