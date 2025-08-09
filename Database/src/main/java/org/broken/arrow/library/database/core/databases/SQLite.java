package org.broken.arrow.library.database.core.databases;

import org.broken.arrow.library.database.builders.ConnectionSettings;
import org.broken.arrow.library.database.connection.HikariCP;
import org.broken.arrow.library.database.core.SQLDatabaseQuery;
import org.broken.arrow.library.database.utility.DatabaseCommandConfig;
import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a SQLite database connection handler with optional HikariCP connection pooling support.
 * <p>
 * This class extends {@link SQLDatabaseQuery} to provide SQLite-specific connection logic,
 * including the ability to connect directly via {@link DriverManager} or use HikariCP if available.
 * The database file location is resolved from the provided parent and optional child path components.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>
 *     SQLite sqlite = new SQLite("data");
 *     Connection conn = sqlite.connect();
 * </pre>
 */
public class SQLite extends SQLDatabaseQuery {
    private final Logging log = new Logging(SQLite.class);
    private File dbFile;
    private final String parent;
    private final String child;
    private final boolean isHikariAvailable;
    private HikariCP hikari;
    private boolean hasCastException = false;

    /**
     * Creates a new SQLite handler using only the parent path for the database file.
     *
     * @param parent the parent directory or file path for the SQLite database file; must not be {@code null}
     */
    public SQLite(@Nonnull final String parent) {
        this(parent, (String) null);
    }

    /**
     * Creates a new SQLite handler using the specified parent and child path.
     *
     * @param parent the parent directory path; must not be {@code null}
     * @param child  the child filename or subpath; may be {@code null} if {@code parent} is the full file path
     */
    public SQLite(@Nonnull final String parent, @Nullable final String child) {
        this("com.zaxxer.hikari.HikariConfig", parent, child);
    }

    /**
     * Creates a new SQLite handler with an explicit HikariCP configuration classpath.
     * <p>
     * This constructor is useful if you want to control the detection of HikariCP availability.
     * </p>
     *
     * @param hikariClazzPath the fully qualified class name of the HikariCP configuration class
     * @param parent          the parent directory path; must not be {@code null}
     * @param child           the child filename or subpath; may be {@code null}
     */
    public SQLite(@Nonnull final String hikariClazzPath, @Nonnull final String parent, @Nullable final String child) {
        this(hikariClazzPath, new DBPath(parent, child));
    }

    /**
     * Internal constructor that directly accepts a resolved {@link DBPath}.
     *
     * @param hikariClazzPath the fully qualified class name of the HikariCP configuration class
     * @param dbPath          the resolved database file path
     */
    private SQLite(@Nonnull final String hikariClazzPath, DBPath dbPath) {
        super(new ConnectionSettings(dbPath.getDbFile().getPath()));
        this.dbFile = dbPath.getDbFile();
        this.parent = "";
        this.child = "";
        this.isHikariAvailable = isDriverFound(hikariClazzPath);
        this.loadDriver("org.sqlite.JDBC");
        connect();
    }

    @Override
    public Connection connect() {
        try {
            return setupConnection();
        } catch (final SQLException ex) {
            this.hasCastException = true;
            log.log(ex, () -> "Fail to connect to SQLITE database");
        }
        return null;
    }

    @Nonnull
    @Override
    public DatabaseCommandConfig databaseConfig() {
        return new DatabaseCommandConfig(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Establishes a SQLite database connection.
     * <p>
     * If HikariCP is available, it will be used to provide a pooled connection; otherwise,
     * a direct connection via {@link DriverManager} will be created.
     * </p>
     *
     * @return a valid {@link Connection} instance
     * @throws SQLException if a database access error occurs
     */
    public Connection setupConnection() throws SQLException {
        Connection connection;

        if (this.hikari == null)
            hikari = new HikariCP(this, "org.sqlite.JDBC");
        if (this.isHikariAvailable)
            connection = this.hikari.getFileConnection("jdbc:sqlite:");
        else
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
        hasCastException = false;
        return connection;
    }

    @Override
    public boolean hasConnectionFailed() {
        return this.hasCastException;
    }

    @Override
    public boolean usingHikari() {
        return this.isHikariAvailable;
    }

    /**
     * Helper class to resolve the SQLite database file path from parent and child path components.
     */
    private static class DBPath {
        private final File dbFile;

        public DBPath(String parent, String child) {
            if (parent != null && child == null)
                dbFile = new File(parent);
            else
                dbFile = new File(parent, child);
        }
        /**
         * Gets the resolved database file.
         *
         * @return the SQLite database file
         */
        public File getDbFile() {
            return dbFile;
        }
    }

}
 