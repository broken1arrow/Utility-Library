package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.BuildMapView;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A wrapper class for managing a {@link BuildMapView} and applying it to item metadata.
 *
 * <p>This class holds a {@link BuildMapView} instance and provides methods to get,
 * set, and apply the associated map view to an {@link ItemMeta} instance if it is a {@link MapMeta}.</p>
 */
public class MapWrapperMeta {
    private BuildMapView mapView;

    /**
     * Returns the built {@link MapView} from the stored {@link BuildMapView}.
     *
     * @return The built {@link MapView}, or null if no {@code mapView} is set.
     */
    @Nullable
    public MapView getMapView() {
        if (this.mapView == null)
            return null;
        return this.mapView.finalizeMapView();
    }

    /**
     * Returns the {@link BuildMapView} that also wraps the {@link MapView} logic.
     *
     * @return The built {@link BuildMapView}, or null if no {@code BuildMapView} is set.
     */
    @Nullable
    public BuildMapView getMapViewBuilder() {
        return this.mapView;
    }

    /**
     * Creates and sets a new {@link BuildMapView} instance based on the given world.
     *
     * @param world the {@link World} from which to create a new map view (non-null)
     * @return the newly created {@link BuildMapView} instance.
     */
    public BuildMapView createMapView(@Nonnull final World world) {
        return this.createOrRetrieveMapView(world, -1, (view) -> {
        });
    }

    /**
     * Creates and sets a new {@link BuildMapView} instance, allowing configuration via a lambda.
     * <p>
     * This method always creates a new map using the provided {@link World}.
     * </p>
     *
     * @param world  the world to associate the map with. This does not affect the rendering of the map.
     * @param action a consumer to configure the resulting {@link BuildMapView}.
     * @return the created {@link BuildMapView}.
     */
    public BuildMapView createMapView(@Nonnull final World world, @Nonnull final Consumer<BuildMapView> action) {
        return this.createOrRetrieveMapView(world, -1, action);
    }

    /**
     * Attempts to retrieve and configure an existing {@link BuildMapView} based on the currently assigned instance.
     * <p>
     * This method first checks if a {@link BuildMapView} has already been assigned via {@link #getMapViewBuilder()}.
     * If found, its ID is used to look up the corresponding map view. Unlike
     * {@link #createOrRetrieveMapView(World, int, Consumer)} and {@link #createMapView(World, Consumer)},
     * this method does <strong>not</strong> create a new map if none exists—it simply returns {@code null}.
     * </p>
     *
     * @param action a consumer used to configure the {@link BuildMapView}, if it exists.
     * @return the existing {@link BuildMapView}, or {@code null} if none was found or assigned.
     */
    @Nullable
    public BuildMapView getExistingMapView(@Nonnull final Consumer<BuildMapView> action) {
        final BuildMapView builtMapView = this.getMapViewBuilder();
        if(builtMapView == null) return null;
        final int id = builtMapView.getId();

        action.accept(builtMapView);
        return getExistingMapView(id, action);
    }

    /**
     * Attempts to retrieve and wrap an existing map view by its ID into a {@link BuildMapView}.
     * <p>
     * This method differs from {@link #createOrRetrieveMapView(World, int, Consumer)} and
     * {@link #createMapView(World, Consumer)} in that it never creates a new map.
     * If no map exists with the provided ID, the method returns {@code null}.
     * </p>
     * <p>
     * If a {@link BuildMapView} is not currently assigned, a new wrapper will be created
     * and linked via {@link #assignMapView(BuildMapView)} using the retrieved {@link MapView}.
     * </p>
     *
     * @param id     the map ID to retrieve, must be zero or greater.
     * @param action a consumer used to configure the {@link BuildMapView}, if found.
     * @return the existing {@link BuildMapView}, or {@code null} if no map exists for the given ID.
     */
    @Nullable
    public BuildMapView getExistingMapView(final int id, @Nonnull final Consumer<BuildMapView> action) {
        final MapView mapView = (id >= 0) ? ItemCreator.getMapById(id) : null;
        if (mapView == null)  return null;

        BuildMapView builtMapView = this.getMapViewBuilder();
        if(builtMapView == null) {
            builtMapView = this.assignMapView(new BuildMapView(mapView));
        }
        action.accept(builtMapView);
        return builtMapView;
    }

    /**
     * Creates or retrieves a {@link BuildMapView} instance, allowing configuration via a lambda.
     * <p>
     * If {@code id >= 0}, this method attempts to retrieve an existing map with that ID.
     * If no such map exists or {@code id < 0}, a new map is created using the provided {@link World}.
     * </p>
     *
     * @param world  the world to associate the map with. This does not affect the rendering of the map.
     *               May be {@code null} only when {@code id} is provided and exists.
     * @param id     the map ID to retrieve, or -1 to create a new one.
     * @param action a consumer to configure the resulting {@link BuildMapView}.
     * @return the created or retrieved {@link BuildMapView}, or {@code null} if retrieval failed and no world was provided.
     */
    @Nullable
    public BuildMapView createOrRetrieveMapView(@Nullable final World world, final int id, @Nonnull final Consumer<BuildMapView> action) {
        MapView mapView = (id >= 0) ? ItemCreator.getMapById(id) : null;

        if (mapView == null) {
            if (world == null) return null;
            mapView = Bukkit.createMap(world);
        }
        final BuildMapView builtMapView = this.assignMapView(new BuildMapView(mapView));
        action.accept(builtMapView);
        return builtMapView;
    }

    /**
     * It is using your crated {@link BuildMapView} instance by wrapping the provided {@link MapView}.
     *
     * @param buildMapView new instance of {@link BuildMapView} to wrap (non-null)
     * @return the newly created {@link BuildMapView} instance wrapping the given map view.
     */
    public BuildMapView assignMapView(@Nonnull final BuildMapView buildMapView) {
        this.mapView = buildMapView;
        return buildMapView;
    }

    /**
     * Applies the stored {@link BuildMapView} to the given {@link ItemMeta} if it is a {@link MapMeta}.
     *
     * <p>If the {@code itemMeta} is not an instance of {@link MapMeta}, this method does nothing.
     * Otherwise, it builds the {@link MapView} from the stored {@link BuildMapView} and sets it on the {@link MapMeta}.</p>
     *
     * @param item     the ItemStack to which the data is applied, used only on legacy versions
     *                 safe to set to null on Minecraft 1.13+.
     * @param itemMeta The {@link ItemMeta} to apply the map view to.
     */
    public void applyMapMeta(@Nullable final ItemStack item, @Nonnull final ItemMeta itemMeta) {
        if (!(itemMeta instanceof MapMeta)) return;
        final MapMeta mapMeta = (MapMeta) itemMeta;

        if (ItemCreator.getServerVersion() < 13.0F) {
            final BuildMapView mapViewBuilder = this.getMapViewBuilder();
            if(mapViewBuilder != null)
                mapViewBuilder.finalizeMapView();

            short durability = mapViewBuilder == null ? -1 : (short) mapViewBuilder.getId();
            if (item != null && durability >= 0) {
                item.setDurability(durability);
            }
            return;
        }

        if (mapView != null) {
            MapView builtMap = mapView.finalizeMapView();
            mapMeta.setMapView(builtMap);
        }
    }

    @Override
    public String toString() {
        return mapView + "";
    }
}
