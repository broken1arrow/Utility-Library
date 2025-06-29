package org.broken.arrow.database.library.core.databases;

import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.connection.HikariCP;
import org.broken.arrow.database.library.core.SQLDatabaseQuery;
import org.broken.arrow.database.library.utility.DatabaseCommandConfig;
import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLite extends SQLDatabaseQuery {
    private final Logging log = new Logging(SQLite.class);
    private File dbFile;
    private final String parent;
    private final String child;
    private final boolean isHikariAvailable;
    private HikariCP hikari;
    private boolean hasCastException = false;

    public SQLite(@Nonnull final String parent) {
        this(parent, (String) null);
    }

    public SQLite(@Nonnull final String parent, @Nullable final String child) {
        this("com.zaxxer.hikari.HikariConfig", parent, child);
    }

    public SQLite(@Nonnull final String hikariClazzPath, @Nonnull final String parent, @Nullable final String child) {
        this(hikariClazzPath, new DBPath(parent, child));
    }

    private SQLite(@Nonnull final String hikariClazzPath, DBPath dbPath) {
        super(new ConnectionSettings(dbPath.getDbFile().getPath()));
        this.dbFile = dbPath.getDbFile();
        this.parent = "";
        this.child = "";
        this.isHikariAvailable = isHikariAvailable(hikariClazzPath);
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
    public boolean isHasCastException() {
        return this.hasCastException;
    }

    private static class DBPath {
        private final File dbFile;

        public DBPath(String parent, String child) {
            if (parent != null && child == null)
                dbFile = new File(parent);
            else
                dbFile = new File(parent, child);
        }

        public File getDbFile() {
            return dbFile;
        }
    }

    @Override
    public boolean usingHikari() {
        return this.isHikariAvailable;
    }


}
 