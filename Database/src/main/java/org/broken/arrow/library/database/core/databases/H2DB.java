package org.broken.arrow.library.database.core.databases;

import org.broken.arrow.library.database.builders.ConnectionSettings;
import org.broken.arrow.library.database.connection.HikariCP;
import org.broken.arrow.library.database.core.SQLDatabaseQuery;
import org.broken.arrow.library.database.utility.DatabaseCommandConfig;
import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nonnull;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents an H2 database connection handler, supporting both direct JDBC connections
 * and HikariCP connection pooling if available.
 * <p>
 * This class extends {@link SQLDatabaseQuery} to provide H2-specific connection
 * handling, driver loading, and database configuration.
 */
public class H2DB extends SQLDatabaseQuery {
    private final Logging log = new Logging(H2DB.class);

    private final boolean isHikariAvailable;
    private final File dbFile;
    private HikariCP hikari;
    private boolean hasCastException;

    /**
     * Creates a new H2 database handler using the default HikariCP classpath to check for availability.
     *
     * @param parent the parent directory path of the database file.
     * @param child  the child path (database file name).
     */
    public H2DB(@Nonnull final String parent, @Nonnull final String child) {
        this("com.zaxxer.hikari.HikariConfig", parent, child);
    }

    /**
     * Creates a new H2 database handler with a custom HikariCP classpath.
     *
     * @param hikariClazzPath the fully qualified class name of the HikariCP configuration class to check.
     * @param parent          the parent directory path of the database file.
     * @param child           the child path (database file name).
     */
    public H2DB(@Nonnull final String hikariClazzPath, @Nonnull final String parent, @Nonnull final String child) {
        this(hikariClazzPath, new DBPath(parent, child));
    }

    /**
     * Creates a new H2 database handler with a resolved database file path.
     *
     * @param hikariClazzPath the fully qualified class name of the HikariCP configuration class to check.
     * @param dbPath          a {@link DBPath} object containing the database file location.
     */
    public H2DB(@Nonnull final String hikariClazzPath,@Nonnull final DBPath dbPath) {
        super(new ConnectionSettings(dbPath.getDbFile().getPath()));
        this.dbFile = dbPath.getDbFile();

        this.isHikariAvailable = isDriverFound(hikariClazzPath);
        this.loadDriver("org.h2.Driver");
        connect();
    }


    @Override
    public Connection connect() {
        try {
            return setupConnection();
        } catch (SQLException e) {
            this.hasCastException = true;
            log.log(e, () -> "Fail to connect to H2 database. With the file path: " + this.dbFile);
        }
        return null;
    }

    @Override
    public boolean usingHikari() {
        return this.isHikariAvailable;
    }


    @Nonnull
    @Override
    public DatabaseCommandConfig databaseConfig() {
        return new DatabaseCommandConfig(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, (sqlHandler, columnData, whereClause, rowExist) -> {
            if (rowExist)
                return sqlHandler.updateTable(updateBuilder -> updateBuilder.putAll(columnData), whereClause);
            else
                return sqlHandler.insertIntoTable(insertHandler -> insertHandler.addAll(columnData));
        });
    }

    /**
     * Sets up and returns a database connection.
     * <p>
     * If HikariCP is available, it uses the connection pool; otherwise, it falls back to a
     * standard JDBC connection.
     *
     * @return a valid {@link Connection} to the H2 database.
     * @throws SQLException if the connection setup fails.
     */
    public Connection setupConnection() throws SQLException {
        Connection connection;

        if (this.isHikariAvailable) {
            if (this.hikari == null) hikari = new HikariCP(this, "org.h2.Driver");
            connection = this.hikari.getFileConnection("jdbc:h2:");
        } else {
            connection = DriverManager.getConnection("jdbc:h2:" + this.dbFile.getPath());
        }
        hasCastException = false;
        return connection;
    }

    @Override
    public boolean hasConnectionFailed() {
        return this.hasCastException;
    }

    /**
     * Helper class to resolve and store the database file path for the H2 database.
     */
    public static class DBPath {
        private final File dbFile;

        /**
         * Constructs a new database path.
         *
         * @param parent the parent directory path of the database file.
         * @param child  the child path (database file name).
         */
        public DBPath(String parent, String child) {
            if (parent != null && child == null)
                dbFile = new File(parent);
            else
                dbFile = new File(parent, child);
        }

        /**
         * Gets the resolved database file reference.
         *
         * @return the {@link File} object representing the database file.
         */
        public File getDbFile() {
            return dbFile;
        }
    }
}
