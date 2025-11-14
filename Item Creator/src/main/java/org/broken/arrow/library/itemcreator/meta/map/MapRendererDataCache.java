package org.broken.arrow.library.itemcreator.meta.map;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.color.parser.RenderColors;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapColoredPixel;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapPixel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<Integer, MapRendererData> rendererDataMap = new ConcurrentHashMap<>();
    private boolean applyColorBalance = true;
    private boolean preConvertToPixels = true;
    private boolean applyScaling = true;

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
     * @return the id from the {@link MapRendererData} instance.
     */
    public int processImageAsync(@Nonnull BufferedImage image, @Nullable final Runnable onReady) {
        final MapRendererData data = new MapRendererData();
        return this.processRendererData(data, () -> {
                    final BufferedImage scaled = getBufferedImage(image);
                    if (this.applyColorBalance) {
                        return new ArrayList<>(RenderColors.renderFromImage(scaled));
                    }
                    if (!this.preConvertToPixels) {
                        data.addImage(0, 0, scaled);
                        return new ArrayList<>(data.getPixels());
                    }
                    return this.addPixels(scaled.getHeight(), scaled.getWidth(), scaled);

                }, false,
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
     * @param newImage   the new image to process and apply
     * @param onReady    optional callback executed when finished
     */
    public void updateCachedImage(final int rendererId, @Nonnull final BufferedImage newImage, @Nullable final Runnable onReady) {
        MapRendererData data = get(rendererId);
        if (data != null) {
            this.processRendererData(data, () ->
                            new ArrayList<>(RenderColors.renderFromImage(newImage)),
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
     * @return the id from the {@link MapRendererData} instance.
     */
    public int addPixelsAsync(@Nonnull Collection<MapPixel> pixels, @Nullable final Runnable onReady) {
        final MapRendererData data = new MapRendererData();
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
     * @return the id from the {@link MapRendererData} instance.
     */
    public int addTextAsync(final int x, final int y, @Nonnull final String text, @Nullable final Font font, @Nullable final Runnable onReady) {
        final MapRendererData data = new MapRendererData();
        data.addText(x, y, text, font);
        return this.processRendererData(data, data::getPixels, false, onReady);
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
     * @return the id from the {@link MapRendererData} instance.
     */
    private int processRendererData(@Nonnull final MapRendererData data, final @Nonnull Supplier<List<MapPixel>> pixelSupplier, final boolean update, @Nullable final Runnable onReady) {
        final int id = data.getMapRenderId();
        rendererDataMap.put(id, data);
        CompletableFuture.supplyAsync(pixelSupplier)
                .thenAccept(pixels -> {
                    if (update) data.clear();
                    data.addAll(pixels);
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

    private BufferedImage getBufferedImage(final BufferedImage image) {
        return applyScaling ? ItemCreator.scale(image, 128, 128) :
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
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

}

