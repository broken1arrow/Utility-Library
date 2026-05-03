package org.broken.arrow.utility.library;


import org.broken.arrow.library.chunk.tracking.ChunkRelevanceTracker;
import org.broken.arrow.library.command.CommandRegister;
import org.broken.arrow.library.command.commandhandler.CommandRegistering;
import org.broken.arrow.library.database.core.databases.MySQL;
import org.broken.arrow.library.database.core.databases.SQLite;
import org.broken.arrow.library.database.builders.ConnectionSettings;
import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.menu.RegisterMenuAPI;
import org.broken.arrow.library.title.update.UpdateTitle;
import org.broken.arrow.library.visualization.BlockVisualize;
import org.broken.arrow.utility.library.chunk.tracker.ChunkRelevanceTrackerWrapper;
import org.broken.arrow.utility.library.listner.UtilityListener;
import org.broken.arrow.utility.library.menu.MenuWrapper;
import org.broken.arrow.utility.library.utility.UtilityServices;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * UtilityLibrary is a utility class that provides you with access to all modules.
 * It offers various utility methods and functionalities for accessing all modules
 * when you want to use this library as a standalone plugin, without you needing to
 * compile every module into your plugin.
 */
public final class UtilityLibrary extends JavaPlugin {
    private static UtilityLibrary instance;

    private final List<Consumer<UtilityServices>> callbacks = new ArrayList<>();
    private UtilityServices utilityServices;
    private volatile boolean ready;

    private RegisterMenuAPI menuAPI;
    private ChunkRelevanceTrackerWrapper chunkRelevanceTracker;

    @Override
    public void onLoad() {
        instance = this;
        UpdateTitle.update(null, "");
    }

    @Override
    public void onEnable() {
        this.chunkRelevanceTracker = new ChunkRelevanceTrackerWrapper(this);
        this.menuAPI = new MenuWrapper(this);
        this.utilityServices = new UtilityServices(this.menuAPI, this.chunkRelevanceTracker);
        Bukkit.getPluginManager().registerEvents(new UtilityListener(this.menuAPI,this.chunkRelevanceTracker), this);

        getLogger().log(Level.INFO, "Has started API " + getDescription().getName() + " version= " + getDescription().getVersion());

        Bukkit.getServicesManager().register(UtilityServices.class, this.utilityServices, this, ServicePriority.Normal);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            ready = true;
            callbacks.forEach(callback -> callback.accept(this.utilityServices));
            callbacks.clear();
        }, 20L);
    }

    /**
     * Registers a callback that will be executed once all services are fully initialized.
     *
     * <p>If the UtilityLibrary is already ready, the callback is executed immediately.
     * Otherwise, it will be invoked once initialization has completed.</p>
     *
     * <p>This is the recommended way to access {@link UtilityServices}, as it guarantees
     * that all services are safe to use.</p>
     *
     * @param callback the callback to execute when services are ready.
     */
    public void whenReady(@Nonnull final Consumer<UtilityServices> callback) {
        if (ready) {
            callback.accept(utilityServices);
        } else {
            callbacks.add(callback);
        }
    }

    /**
     * Retrieves the instance of the UtilityLibrary plugin.
     *
     * @return The instance of UtilityLibrary.
     */
    public static UtilityLibrary getInstance() {
        return instance;
    }

    /**
     * Get the RegisterMenuAPI instance for the given plugin.
     *
     * @return The RegisterMenuAPI instance.
     */
    public RegisterMenuAPI getMenuApi() {
        return menuAPI;
    }

    /**
     * Creates a new ItemCreator instance for the given plugin.
     *
     * @return The ItemCreator instance.
     */
    public ItemCreator getItemCreator() {
        return new ItemCreator(this);
    }

    /**
     * Creates a new MySQL instance with the given MySQL preferences.
     *
     * @param mysqlPreference The MySQL preferences.
     * @return The MySQL instance.
     */
    public MySQL createMySQLInstance(ConnectionSettings mysqlPreference) {
        return new MySQL(mysqlPreference);
    }

    /**
     * Creates a new SQLite instance with the given parent and child paths.
     *
     * @param parent The parent path where file is located.
     * @param child  The child path where file is located.
     * @return The SQLite instance.
     */
    public SQLite createSQLiteInstance(String parent, String child) {
        return new SQLite(parent, child);
    }

    /**
     * Retrieves a new CommandRegistering instance.
     *
     * @return The CommandRegistering instance.
     */
    public CommandRegistering getCommandRegistry() {
        return new CommandRegister();
    }

    /**
     * Retrieves a new BlockVisualize instance for the given plugin.
     *
     * @return The BlockVisualize instance.
     */
    public BlockVisualize getVisualizer() {
        return new BlockVisualize(this);
    }

    /**
     * Provides access to the {@link ChunkRelevanceTracker} used by this plugin.
     *
     * <p>This tracker exposes the public API for interacting with chunk relevance,
     * including checking whether a chunk has players present or is force-loaded.</p>
     *
     * @return the {@link ChunkRelevanceTracker} instance
     */
    public ChunkRelevanceTracker getChunkRelevanceTracker() {
        return chunkRelevanceTracker;
    }
}
