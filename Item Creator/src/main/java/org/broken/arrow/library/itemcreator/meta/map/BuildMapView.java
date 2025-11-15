package org.broken.arrow.library.itemcreator.meta.map;


import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.builder.RenderConfigurator;
import org.broken.arrow.library.itemcreator.meta.map.utility.MapRendererBuilder;
import org.broken.arrow.library.itemcreator.meta.map.cache.MapRendererDataCache;
import org.broken.arrow.library.itemcreator.utility.FormatString;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * A builder and wrapper for {@link MapView} instances, providing enhanced
 * control over map properties such as scale, renderers, and tracking settings.
 * <p>
 * This class allows creation of new map views in a specified world or wrapping
 * existing {@link MapView} instances, while managing additional custom state
 * and renderer data.
 * </p>
 */
public class BuildMapView {
    private final MapView mapView;
    private final World world;
    private final MapRendererData renderer;
    private final MapRendererBuilder mapRendererBuilder;

    private final boolean virtual;

    private boolean usingBuilder;
    private boolean locked;
    private boolean unlimited;
    private boolean trackingPosition;

    private final int mapId;

    /**
     * Constructs a new {@code BuildMapView} for the given {@link World},
     * creating a fresh {@link MapView} instance tied to that world.
     *
     * @param world the {@link World} where the map view is created (non-null)
     */
    public BuildMapView(@Nonnull final World world) {
        this(Bukkit.createMap(world));
    }

    /**
     * Wraps an existing {@link MapView} into a {@code BuildMapView},
     * preserving its current properties and renderers.
     *
     * @param mapView the existing {@link MapView} to wrap (non-null)
     */
    public BuildMapView(@Nonnull final MapView mapView) {
        this(null, mapView);
    }

    /**
     * Wraps an existing {@link MapView} into a {@code BuildMapView},
     * preserving its current properties and renderers.
     *
     * @param renderer A new {@link MapRenderer} instance you want to add.
     * @param mapView  the existing {@link MapView} to wrap (non-null)
     */
    public BuildMapView(@Nullable final MapRenderer renderer, @Nonnull final MapView mapView) {
        this.mapView = mapView;
        this.virtual = mapView.isVirtual();
        this.mapId = retrieveMapId(mapView);
        this.world = mapView.getWorld();
        this.renderer = renderer != null ? new MapRendererData(renderer) : new MapRendererData();
        this.mapRendererBuilder = new MapRendererBuilder(this.renderer);
    }


    /**
     * Get the ID of this map item for use with {@link MapMeta}.
     *
     * @return The ID of the map.
     */
    public int getId() {
        return this.mapId;
    }

    /**
     * Check whether this map is virtual. A map is virtual if its lowermost
     * MapRenderer is plugin-provided.
     *
     * @return Whether the map is virtual.
     */
    public boolean isVirtual() {
        return this.virtual;
    }

    /**
     * Get the scale of this map.
     *
     * @return The scale of the map.
     */
    @Nonnull
    public MapView.Scale getScale() {
        final MapView.Scale mapViewScale = mapView.getScale();
        if (mapViewScale == null)
            return MapView.Scale.NORMAL;
        return mapViewScale;
    }

    /**
     * Set the scale of this map.
     *
     * @param scale The scale to set.
     */
    public void setScale(@Nonnull MapView.Scale scale) {
        this.mapView.setScale(scale);
    }

    /**
     * Get the center X position of this map.
     *
     * @return The center X position.
     */
    public int getCenterX() {
        return this.mapView.getCenterX();
    }

    /**
     * Get the center Z position of this map.
     *
     * @return The center Z position.
     */
    public int getCenterZ() {
        return this.mapView.getCenterZ();
    }

    /**
     * Set the center X position of this map.
     *
     * @param x The center X position.
     */
    public void setCenterX(int x) {
        this.mapView.setCenterX(x);
    }

    /**
     * Set the center Z position of this map.
     *
     * @param z The center Z position.
     */
    public void setCenterZ(int z) {
        this.mapView.setCenterZ(z);
    }

    /**
     * Get the world that this map is associated with. Primarily used by the
     * internal renderer, but may be used by external renderers. May return
     * null if the world the map is associated with is not loaded.
     *
     * @return The World this map is associated with.
     */
    @Nullable
    public World getWorld() {
        return this.mapView.getWorld();
    }

    /**
     * Get the set MapRenderer.
     *
     * @return A {@link MapRendererData} containing each pixel set for the renderer.
     */
    @Nonnull
    public MapRendererData getRenderer() {
        return this.renderer;
    }

    /**
     * Get the set MapRenderer.
     *
     * @return A {@code MapRenderer} containing each pixel set for the renderer.
     */
    @Nonnull
    public MapRenderer getMapRenderer() {
        return this.renderer.getMapRenderer();
    }

    /**
     * Provides full access to configure the renderer for your Minecraft map,
     * including setting pixels, adding text or images, loading cached layers,
     * and using the builder for automatic layer assignment.
     *
     * <p>All configuration steps are optional and can be chained. Multiple calls
     * on the same layer index may overwrite previously set data.</p>
     *
     * <p>Example usage - all steps are optional, you only need to use the methods
     * relevant for your case:</p>
     * <pre>{@code
     * final MapRendererDataCache cache = new MapRendererDataCache();
     * configureRenderer(data ->
     *     data.withCachedLayer(0, 0, cache)
     *         .withRenderer(mapRendererData -> {
     *             // configure renderer here, e.g., add pixels, text, or images
     *         })
     *         .withBuilder(mapRendererBuilder -> {
     *             // configure builder here, options are the same as mapRendererData.
     *             // The only difference is that layers are assigned automatically.
     *         });
     * );
     * }</pre>
     *
     * @param dataConsumer a consumer that receives a {@link RenderConfigurator}
     *                     for performing renderer, cached layer, or builder configuration
     */
    public void configureRenderer(@Nonnull final Consumer<RenderConfigurator> dataConsumer) {
        dataConsumer.accept(new RenderConfigurator(this.renderer, this.mapRendererBuilder));
        final MapRendererDataCache cache = new MapRendererDataCache();
    }


    /**
     * Remove the pixels set from this MapRendererData instance.
     *
     */
    public void clearPixels() {
        this.renderer.clear();
    }

    /**
     * Gets whether a position cursor should be shown when the map is near its
     * center.
     *
     * @return tracking status
     */
    public boolean isTrackingPosition() {
        return this.trackingPosition;
    }

    /**
     * Sets whether a position cursor should be shown when the map is near its
     * center.
     *
     * @param trackingPosition tracking status
     */
    public void setTrackingPosition(final boolean trackingPosition) {
        this.trackingPosition = trackingPosition;
    }

    /**
     * Whether the map will show a smaller position cursor (true), or no
     * position cursor (false) when cursor is outside of map's range.
     *
     * @return unlimited tracking state
     */
    public boolean isUnlimitedTracking() {
        return this.unlimited;
    }

    /**
     * Whether the map will show a smaller position cursor (true), or no
     * position cursor (false) when cursor is outside of map's range.
     *
     * @param unlimited tracking state
     */
    public void setUnlimitedTracking(final boolean unlimited) {
        this.unlimited = unlimited;
    }

    /**
     * Gets whether the map is locked or not.
     * <p>
     * A locked map may not be explored further.
     *
     * @return lock status
     */
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * Gets whether the map is locked or not.
     * <p>
     * A locked map may not be explored further.
     *
     * @param locked status
     */
    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    /**
     * Applies all configured settings and renderers to the wrapped MapView.
     * Should be called once all properties are set.
     *
     * @return the wrapped MapView instance, ready for use
     * @throws IllegalStateException if the world is not set
     */
    public MapView finalizeMapView() {
        if (world == null)
            throw new IllegalStateException("World must be set before building MapView.");

        if (mapView.getScale() == null)
            mapView.setScale(this.getScale());

        if (ItemCreator.getServerVersion() > 13.2F) {
            mapView.setTrackingPosition(trackingPosition);
            mapView.setLocked(locked);
        }

        if (ItemCreator.getServerVersion() > 10.2F)
            mapView.setUnlimitedTracking(unlimited);

        if (!renderer.isPixelsEmpty()) {
            for (MapRenderer renderer : mapView.getRenderers()) {
                mapView.removeRenderer(renderer);
            }
            mapView.addRenderer(this.getMapRenderer());
        }
        return mapView;
    }

    /**
     * Retrieves the map ID from a {@link MapView}, handling version differences between
     * legacy (≤ 1.12.2) and modern (≥ 1.13) Spigot APIs.
     * <p>
     * In versions 1.12.2 and below, {@code MapView#getId()} returns a {@code short}.
     * In versions 1.13 and above, it returns an {@code int}. This method ensures compatibility
     * across both by using reflection only when necessary.
     * </p>
     *
     * @param mapView the {@link MapView} instance from which to retrieve the map ID
     * @return the map ID as an {@code int}, or {@code -1} if retrieval failed
     */
    public static int retrieveMapId(@Nonnull final MapView mapView) {
        if (ItemCreator.getServerVersion() > 12.2F)
            return mapView.getId();
        try {
            Method getId = mapView.getClass().getMethod("getId");
            Object result = getId.invoke(mapView);
            return ((Number) result).intValue();
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        FormatString string = new FormatString(new StringBuilder("{"));

        string.appendFieldRecursive("id", getId(), 1);
        string.appendFieldRecursive("world", (world == null ? "" : world.getName()), 1);
        string.appendFieldRecursive("x", getCenterX(), 1);
        string.appendFieldRecursive("z", getCenterZ(), 1);
        string.appendFieldRecursive("renderer", renderer, 1);

        return string.finalizeString() + "";
    }


}

