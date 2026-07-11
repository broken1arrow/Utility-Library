package org.broken.arrow.utility.library.listner;

import org.broken.arrow.library.chunk.tracking.chunk.PlayerChunkTracker;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.broken.arrow.library.menu.MenuUtility;
import org.broken.arrow.library.menu.RegisterMenuAPI;
import org.broken.arrow.library.menu.cache.MenuCache;
import org.broken.arrow.library.menu.cache.MenuCacheKey;
import org.broken.arrow.library.menu.utility.Action;
import org.broken.arrow.library.menu.utility.MetadataPlayer;
import org.broken.arrow.library.menu.utility.ServerVersion;
import org.broken.arrow.library.menu.utility.metadata.MenuMetadataKey;
import org.broken.arrow.library.serialize.utility.converters.world.ChunkKey;
import org.broken.arrow.utility.library.chunk.tracker.ChunkRelevanceTrackerWrapper;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central listener that delegates events to module-specific bukkit events.
 */
public class UtilityListener implements Listener {
    private final Map<UUID, SwapData> cacheData = new HashMap<>();
    private final ChunkRelevanceTrackerWrapper chunkRelevanceTracker;
    private final PlayerChunkTracker playerChunkTracker;
    private final RegisterMenuAPI menuAPI;
    private final MenuCache menuCache;

    /**
     * Creates a new listener instance.
     *
     * @param menuAPI               the menu api handler
     * @param chunkRelevanceTracker main chunk relevance handler
     */
    public UtilityListener(@Nonnull final RegisterMenuAPI menuAPI, @Nonnull final ChunkRelevanceTrackerWrapper chunkRelevanceTracker) {
        this.chunkRelevanceTracker = chunkRelevanceTracker;
        this.playerChunkTracker = chunkRelevanceTracker.getPlayerChunkTracker();
        this.menuAPI = menuAPI;
        this.menuCache = menuAPI.getMenuCache();
    }

    /**
     * Event called when chunk unloading.
     *
     * @param event the unload event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void chunkUnLoad(final ChunkUnloadEvent event) {
        final Chunk chunk = event.getChunk();
        this.chunkRelevanceTracker.processChunkState(ChunkKey.of(chunk), chunk, ChunkStatus.UNLOADED, cacheEntry -> {
            cacheEntry.setForceLoaded(chunk.isForceLoaded());
        });
    }

    /**
     * Handles chunk load events.
     *
     * @param event the load event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void chunkLoad(final ChunkLoadEvent event) {
        final Chunk chunk = event.getChunk();
        this.chunkRelevanceTracker.processChunkState(ChunkKey.of(chunk), chunk, ChunkStatus.LOADED, cacheEntry -> {
            cacheEntry.setForceLoaded(chunk.isForceLoaded());
            cacheEntry.markSeen();
        });
    }

    /**
     * Event called when player join server.
     *
     * @param e the load event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent e) {
        this.playerChunkTracker.trackPlayer(e.getPlayer());
    }

    /**
     * Updates player chunk on movement.
     *
     * @param e the player move event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onMove(final PlayerMoveEvent e) {
        this.playerChunkTracker.onPlayerChunkChange(
                e.getPlayer(),
                ChunkKey.of(e.getTo())
        );
    }

    /**
     * Untracks player on quit.
     *
     * @param e the player move event.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onQuit(final PlayerQuitEvent e) {
        this.playerChunkTracker.untrackPlayer(e.getPlayer());
    }

    /**
     * Updates player chunk on teleport.
     *
     * @param e the player teleport.
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onTeleport(final PlayerTeleportEvent e) {
        this.playerChunkTracker.onPlayerChunkChange(
                e.getPlayer(),
                ChunkKey.of(e.getTo())
        );
    }

    /**
     * When player clicking the inventory.
     *
     * @param event the player clicking the inventory.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onMenuClicking(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (event.getClickedInventory() == null) return;
        ItemStack clickedItem = event.getCurrentItem();

        final MenuUtility<?> menuUtility = getMenuHolder(player);
        if (menuUtility == null) return;

        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) menuUtility.menuClickOutside(event, menuUtility);

        if (!menuUtility.checkValidMenu(event.getView().getTopInventory(), Action.CLICKED)) {
            return;
        }

        if (menuUtility.getMenuInteractionChecks().whenPlayerClick(event, player, clickedItem)) {
            onOffHandClick(event, player);
        }
    }

    /**
     * When player open the inventory.
     *
     * @param event the player open  the inventory.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onMenuOpen(final InventoryOpenEvent event) {
        final Player player = (Player) event.getPlayer();

        final MenuUtility<?> menuUtility = getMenuHolder(player);
        if (menuUtility == null) return;

        if (!menuUtility.checkValidMenu(event.getView().getTopInventory(), Action.OPEN)) {
            return;
        }
        if (ServerVersion.olderThan(15.0)) return;

        this.cacheData.put(player.getUniqueId(), new SwapData(false, player.getInventory().getItemInOffHand()));
    }

    /**
     * When player close the inventory.
     *
     * @param event the player close the inventory.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onMenuClose(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();

        final MenuUtility<?> menuUtility = getMenuHolder(player);
        if (menuUtility == null) return;

        final SwapData data = cacheData.get(player.getUniqueId());
        if (data != null && data.isPlayerUseSwapOffHand()) {
            if (data.getItemInOfBeforeOpenMenuHand() != null && data.getItemInOfBeforeOpenMenuHand().getType() != Material.AIR) {
                player.getInventory().setItemInOffHand(data.getItemInOfBeforeOpenMenuHand());
            } else {
                player.getInventory().setItemInOffHand(null);
            }
        }
        cacheData.remove(player.getUniqueId());
        if (!menuUtility.checkValidMenu(event.getView().getTopInventory(), Action.CLOSE)) {
            return;
        }
        try {
            menuUtility.menuClose(event);
        } finally {
            menuUtility.unregister(player);
        }
    }

    /**
     * When player drag items inside the inventory.
     *
     * @param event the player drag items inside the inventory.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryDragTop(final InventoryDragEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (event.getView().getType() == InventoryType.PLAYER) return;

        final MenuUtility<?> menuUtility = getMenuHolder(player);
        if (menuUtility == null) return;
        if (menuUtility.getMenu() == null) return;

        if (!menuUtility.isAddedButtonsCacheEmpty()) {
            final int size = event.getView().getTopInventory().getSize();
            menuUtility.getMenuInteractionChecks().whenPlayerDrag(event, size);
        }
    }


    @Nullable
    private MenuUtility<?> getMenuHolder(final Player player) {
        MenuCacheKey menukey = null;
        MetadataPlayer metadataPlayer = this.menuAPI.getPlayerMeta();
        if (metadataPlayer.hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION)) {
            menukey = metadataPlayer.getPlayerMetadata(player, MenuMetadataKey.MENU_OPEN_LOCATION, MenuCacheKey.class);
        }

        final MenuUtility<?> menuUtility;
        if (metadataPlayer.hasPlayerMetadata(player, MenuMetadataKey.MENU_OPEN)) {
            menuUtility = metadataPlayer.getPlayerMenuMetadata(player, MenuMetadataKey.MENU_OPEN);
        } else {
            menuUtility = menuCache.getMenuInCache(menukey, MenuUtility.class);
        }
        return menuUtility;
    }

    private void onOffHandClick(final InventoryClickEvent event, final Player player) {
        if (ServerVersion.newerThan(15.0) && event.getClick() == ClickType.SWAP_OFFHAND) {
            final SwapData data = cacheData.get(player.getUniqueId());
            ItemStack item = null;
            if (data != null) {
                item = data.getItemInOfBeforeOpenMenuHand();
            }
            cacheData.put(player.getUniqueId(), new SwapData(true, item));
        }
    }

    private static class SwapData {

        boolean playerUseSwapOffHand;
        ItemStack itemInOfBeforeOpenMenuHand;

        public SwapData(final boolean playerUseSwapOffHand, final ItemStack itemInOfBeforeOpenMenuHand) {
            this.playerUseSwapOffHand = playerUseSwapOffHand;
            this.itemInOfBeforeOpenMenuHand = itemInOfBeforeOpenMenuHand;
        }

        public boolean isPlayerUseSwapOffHand() {
            return playerUseSwapOffHand;
        }

        public ItemStack getItemInOfBeforeOpenMenuHand() {
            return itemInOfBeforeOpenMenuHand;
        }
    }

}
