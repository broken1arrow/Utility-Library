package org.broken.arrow.library.itemcreator.meta.map;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BuildMapView {
    private final MapView mapView;
    private final World world;
    private final Set<MapRendererData> renderers = new HashSet<>();

    private MapView.Scale scale = MapView.Scale.NORMAL;

    private boolean locked;
    private boolean unlimited;
    private boolean trackingPosition;
    private boolean virtual;

    private int mapId;
    private int z;
    private int x;

    public BuildMapView(@Nonnull final World world) {
        this.mapView = Bukkit.createMap(world);
        this.virtual = mapView.isVirtual();
        this.mapId = mapView.getId();
        this.world = mapView.getWorld();
    }

    public BuildMapView(@Nonnull final MapView mapView) {
        this.mapView = mapView;
        this.virtual = mapView.isVirtual();
        this.world = mapView.getWorld(); // nullable, but can be used
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
        return this.scale;
    }

    /**
     * Set the scale of this map.
     *
     * @param scale The scale to set.
     */
    public void setScale(@Nonnull MapView.Scale scale) {
        this.scale = scale;
    }

    /**
     * Get the center X position of this map.
     *
     * @return The center X position.
     */
    public int getCenterX() {
        return this.x;
    }

    /**
     * Get the center Z position of this map.
     *
     * @return The center Z position.
     */
    public int getCenterZ() {
        return this.z;
    }

    /**
     * Set the center X position of this map.
     *
     * @param x The center X position.
     */
    public void setCenterX(int x) {
        this.x = x;
    }

    /**
     * Set the center Z position of this map.
     *
     * @param z The center Z position.
     */
    public void setCenterZ(int z) {
        this.z = z;
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
        return this.world;
    }

    /**
     * Get a list of MapRenderers currently in effect.
     *
     * @return A {@code List<MapRenderer>} containing each map renderer.
     */
    @Nonnull
    public List<MapRenderer> getRenderers() {
        if (renderers.isEmpty())
            return new ArrayList<>();
        return renderers.stream().map(MapRendererData::getMapRenderer).collect(Collectors.toList());
    }

    /**
     * Add a renderer to this map.
     *
     * @param renderer The MapRenderer to add.
     * @return Returns the id set on this instance.
     */
    public MapRendererData addRenderer(@Nonnull final MapRenderer renderer, @Nonnull final Consumer<MapRendererData> dataConsumer) {
        MapRendererData mapRenderer = new MapRendererData(renderer);
        dataConsumer.accept(mapRenderer);
        renderers.add(mapRenderer);
        return mapRenderer;
    }


    /**
     * Add a renderer to this map.
     *
     * @param renderer The MapRenderer to add.
     * @return Returns the id set on this instance.
     */
    public MapRendererData addRenderer(@Nonnull final Consumer<MapRendererData> renderer) {
        final MapRendererData mapRenderer = new MapRendererData();
        renderer.accept(mapRenderer);
        renderers.add(mapRenderer);
        return mapRenderer;
    }

    /**
     * Remove a renderer from this map.
     *
     * @param renderer The MapRenderer to remove.
     */
    public void removeRenderer(@Nonnull final MapRendererData renderer) {
        renderers.removeIf(mapRenderer -> mapRenderer.getMapRenderId() == renderer.getMapRenderId());
    }

    /**
     * Remove a renderer from this map.
     *
     * @param mapRenderId The id of the MapRendererData to remove.
     */
    public void removeRenderer(final int mapRenderId) {
        renderers.removeIf(mapRenderer -> mapRenderer.getMapRenderId() == mapRenderId);
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


    public MapView build() {
        if (world == null)
            throw new IllegalStateException("World must be set before building MapView.");

        mapView.setCenterX(x);
        mapView.setCenterZ(z);
        mapView.setScale(scale);
        mapView.setTrackingPosition(trackingPosition);
        mapView.setUnlimitedTracking(unlimited);
        mapView.setLocked(locked);

        for (MapRenderer renderer : mapView.getRenderers()) {
            mapView.removeRenderer(renderer);
        }

        // Add renderers from your data
        if (!renderers.isEmpty()) {
            for (MapRendererData data : renderers) {
                    mapView.addRenderer(data.getMapRenderer());
            }
        }
        return mapView;
    }
}

