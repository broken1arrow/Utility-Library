package org.broken.arrow.library.itemcreator.meta.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

@FunctionalInterface
public interface MapRenderHandler {
    boolean render(MapView map, MapCanvas canvas, Player player);
}