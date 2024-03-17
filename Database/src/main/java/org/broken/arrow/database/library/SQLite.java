package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.TableWrapper;
import org.broken.arrow.database.library.connection.HikariCP;
import org.broken.arrow.logging.library.Logging;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.broken.arrow.logging.library.Logging.of;

public class SQLite extends Database {
    private final Logging log = new Logging(SQLite.class);
    private File dbFile;
    private final String parent;
    private final String child;
    private final boolean isHikariAvailable;
    private HikariCP hikari;

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
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }
            return setupConnection();
        } catch (final SQLException ex) {
            log.log(ex, () -> of("Fail to connect to SQLITE database"));
        }
        return null;
    }

    @Override
    protected void batchUpdate(@Nonnull final List<SqlCommandComposer> sqlComposer, @Nonnull final TableWrapper... tableWrappers) {
        this.batchUpdate(sqlComposer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    public Connection setupConnection() throws SQLException {
        Connection connection;

        if (this.hikari == null)
            hikari = new HikariCP(this, "org.sqlite.JDBC");
        if (this.isHikariAvailable)
            connection = this.hikari.getFileConnection("jdbc:sqlite:");
        else
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());

        return connection;
    }

    @Override
    public boolean isHasCastException() {
        return false;
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

}
 