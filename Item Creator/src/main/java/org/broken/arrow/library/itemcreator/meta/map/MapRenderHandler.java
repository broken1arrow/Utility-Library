package org.broken.arrow.library.itemcreator.meta.map;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

/**
 * Functional interface for customizing map rendering dynamically.
 * Implementations can modify the map canvas during rendering.
 */
@FunctionalInterface
public interface MapRenderHandler {

    /**
     * Called to render the map for a specific player on the given canvas.
     *
     * @param map    the {@link MapView} being rendered.
     * @param canvas the {@link MapCanvas} to draw on.
     * @param player the {@link Player} viewing the map.
     * @return {@code false} if the normal pixel rendering should still be applied
     *         after this method, {@code true} if normal rendering should be skipped,
     *         allowing this method to fully control the map appearance.
     */
    boolean render(MapView map, MapCanvas canvas, Player player);
}