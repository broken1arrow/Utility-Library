package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.meta.map.BuildMapView;
import org.bukkit.World;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
        return this.mapView.build();
    }

    /**
     * Creates and sets a new {@link BuildMapView} instance based on the given world.
     *
     * @param world the {@link World} from which to create a new map view (non-null)
     * @return the newly created {@link BuildMapView} instance.
     */
    public BuildMapView createMapView(@Nonnull final World world) {
        this.mapView = new BuildMapView(world);
        return this.mapView;
    }

    /**
     * Creates and sets a new {@link BuildMapView} instance by wrapping the provided {@link MapView}.
     *
     * @param mapView the existing {@link MapView} to wrap (non-null)
     * @return the newly created {@link BuildMapView} instance wrapping the given map view.
     */
    public BuildMapView createMapView(@Nonnull final MapView mapView) {
        this.mapView = new BuildMapView(mapView);
        return this.mapView;
    }

    /**
     * Applies the stored {@link BuildMapView} to the given {@link ItemMeta} if it is a {@link MapMeta}.
     *
     * <p>If the {@code itemMeta} is not an instance of {@link MapMeta}, this method does nothing.
     * Otherwise, it builds the {@link MapView} from the stored {@link BuildMapView} and sets it on the {@link MapMeta}.</p>
     *
     * @param itemMeta The {@link ItemMeta} to apply the map view to.
     */
    public void applyMapMeta(@Nonnull final ItemMeta itemMeta) {
        if (!(itemMeta instanceof MapMeta)) return;
        final MapMeta mapMeta = (MapMeta) itemMeta;
        if (mapView != null) {
            MapView builtMap = mapView.build();
            mapMeta.setMapView(builtMap);
        }
    }
}
