package org.broken.arrow.utility.library.utility;

import org.broken.arrow.library.chunk.tracking.ChunkRelevanceTracker;
import org.broken.arrow.library.command.CommandRegister;
import org.broken.arrow.library.command.commandhandler.CommandRegistering;
import org.broken.arrow.library.database.builders.ConnectionSettings;
import org.broken.arrow.library.database.core.databases.MySQL;
import org.broken.arrow.library.database.core.databases.SQLite;
import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.menu.RegisterMenuAPI;
import org.broken.arrow.library.visualization.BlockVisualize;
import org.broken.arrow.utility.library.UtilityLibrary;
import org.broken.arrow.utility.library.chunk.tracker.ChunkRelevanceTrackerWrapper;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Central access point for all services provided by the UtilityLibrary plugin.
 *
 * <p>This class can be obtained through Bukkit's ServicesManager, but is more safely
 * accessed via {@link UtilityLibrary#whenReady(Consumer)}, which guarantees that
 * all services are fully initialized before use.</p>
 */
public class UtilityServices {
    private final UtilityLibrary utilityLibrary;
    private final RegisterMenuAPI menuAPI;
    private final ChunkRelevanceTrackerWrapper chunkRelevanceTracker;

    /**
     * Constructs a new UtilityServices instance.
     *
     * <p>This constructor is intended for internal use by the UtilityLibrary plugin.
     * Consumers should not create instances manually, but instead obtain them through
     * the Bukkit ServicesManager or the provided readiness mechanisms.</p>
     *
     * @param menuAPI the menu API implementation
     * @param chunkRelevanceTracker the chunk relevance tracker implementation
     */
    public UtilityServices( @Nonnull final RegisterMenuAPI menuAPI, @Nonnull final ChunkRelevanceTrackerWrapper chunkRelevanceTracker) {
        this.utilityLibrary = UtilityLibrary.getInstance();
        this.menuAPI = menuAPI;
        this.chunkRelevanceTracker = chunkRelevanceTracker;
    }

    /**
     *  Provides access to the menu API.
     *
     * @return the menu API instance.
     */
    public RegisterMenuAPI getMenuApi() {
        return menuAPI;
    }

    /**
     * Creates a new {@link ItemCreator} instance.
     *
     * <p>Each invocation returns a new instance bound to the current UtilityLibrary context.</p>
     *
     * @return a new ItemCreator instance
     */
    public ItemCreator getItemCreator() {
        return new ItemCreator(utilityLibrary);
    }

    /**
     * Creates a new MySQL connection wrapper using the provided settings.
     *
     * @param mysqlPreference the connection settings
     * @return a new MySQL instance
     */
    public MySQL createMySQLInstance(ConnectionSettings mysqlPreference) {
        return new MySQL(mysqlPreference);
    }

    /**
     * Creates a new SQLite connection wrapper.
     *
     * @param parent the parent directory path
     * @param child the database file name or relative path
     * @return a new SQLite instance
     */
    public SQLite createSQLiteInstance(String parent, String child) {
        return new SQLite(parent, child);
    }

    /**
     * Creates a new command registration helper.
     *
     * @return a new CommandRegistering instance
     */
    public CommandRegistering getCommandRegistry() {
        return new CommandRegister();
    }

    /**
     * Creates a new block visualization helper.
     *
     * @return a new BlockVisualize instance
     */
    public BlockVisualize getVisualizer() {
        return new BlockVisualize(utilityLibrary);
    }

    /**
     * Provides access to the chunk relevance tracker.
     *
     * <p>This tracker allows checking whether chunks are considered relevant,
     * such as having players present or being force-loaded.</p>
     *
     * @return the chunk relevance tracker instance
     */
    public ChunkRelevanceTracker getChunkRelevanceTracker() {
        return chunkRelevanceTracker;
    }
}
