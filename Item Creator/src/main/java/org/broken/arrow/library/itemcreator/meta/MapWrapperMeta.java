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
        return this.mapView.finilazeMapView();
    }

    /**
     * Returns the {@link BuildMapView} that also populate the {@link MapView}.
     *
     * @return The built {@link BuildMapView}, or null if no {@code BuildMapView} is set.
     */
    @Nullable
    public BuildMapView getMapViewBuilder() {
        this.getMapView();
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
     * Attempts to retrieve an existing map by its ID and wraps it in a {@link BuildMapView}.
     * <p>
     * Unlike {@link #createOrRetrieveMapView(World, int, Consumer)} and {@link #createMapView(World, Consumer)},
     * the difference is that the first method always creates a new map when one cannot be found, while the second
     * always creates a new map regardless. This method, however, does not create a new map if the ID is missing,
     * it simply returns {@code null}.
     * </p>
     *
     * @param id     the map ID to retrieve can't be below zero.
     * @param action a consumer to configure the resulting {@link BuildMapView}, if found.
     * @return the retrieved {@link BuildMapView}, or {@code null} if no map exists for the given ID.
     */
    @Nullable
    public BuildMapView getExistingMapView(final int id, @Nonnull final Consumer<BuildMapView> action) {
        return this.createOrRetrieveMapView(null, id, action);
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
        MapView mapView = null;

        if (id >= 0) {
            mapView = ItemCreator.getMapById(id);
        }
        if (mapView == null) {
            if (world == null) return null;
            mapView = Bukkit.createMap(world);
        }

        this.mapView = new BuildMapView(mapView);
        action.accept(this.mapView);
        return this.mapView;
    }

    /**
     * It is using your crated {@link BuildMapView} instance by wrapping the provided {@link MapView}.
     *
     * @param buildMapView new instance of {@link BuildMapView} to wrap (non-null)
     * @return the newly created {@link BuildMapView} instance wrapping the given map view.
     */
    public BuildMapView createMapView(@Nonnull final BuildMapView buildMapView) {
        this.mapView = buildMapView;
        return buildMapView;
    }

    /**
     * Applies the stored {@link BuildMapView} to the given {@link ItemMeta} if it is a {@link MapMeta}.
     *
     * <p>If the {@code itemMeta} is not an instance of {@link MapMeta}, this method does nothing.
     * Otherwise, it builds the {@link MapView} from the stored {@link BuildMapView} and sets it on the {@link MapMeta}.</p>
     *
     * @param item     the itemStack to apply the data.
     * @param itemMeta The {@link ItemMeta} to apply the map view to.
     */
    public void applyMapMeta(final ItemStack item, @Nonnull final ItemMeta itemMeta) {
        if (!(itemMeta instanceof MapMeta)) return;
        final MapMeta mapMeta = (MapMeta) itemMeta;

        if (ItemCreator.getServerVersion() < 13.0F) {
            final BuildMapView mapViewBuilder = this.getMapViewBuilder();
            short durability = mapViewBuilder == null ? -1 : (short) mapViewBuilder.getId();
            if (durability >= 0) {
                item.setDurability(durability);
            }
            return;
        }

        if (mapView != null) {
            MapView builtMap = mapView.finilazeMapView();
            mapMeta.setMapView(builtMap);
        }
    }

    @Override
    public String toString() {
        return mapView + "";
    }
}
