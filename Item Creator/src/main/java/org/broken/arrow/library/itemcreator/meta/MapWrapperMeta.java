package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.itemcreator.meta.map.BuildMapView;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MapWrapperMeta {
    private BuildMapView mapView;

    @Nullable
    public MapView getMapView() {
        return this.mapView.build();
    }

    public void setMapView(BuildMapView mapView) {
        this.mapView = mapView;
    }

    public void applyMapMeta(@Nonnull final ItemMeta itemMeta) {
        if (!(itemMeta instanceof MapMeta)) return;
        final MapMeta mapMeta = (MapMeta) itemMeta;
        if (mapView != null) {
            MapView builtMap = mapView.build();
            mapMeta.setMapView(builtMap);
        }
    }
}
