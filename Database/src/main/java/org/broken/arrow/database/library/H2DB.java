package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.builders.RowWrapper;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.TableWrapper;
import org.broken.arrow.database.library.connection.HikariCP;
import org.broken.arrow.logging.library.Logging;

import javax.annotation.Nonnull;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.broken.arrow.logging.library.Logging.of;

public class H2DB extends Database {
    private final Logging log = new Logging(H2DB.class);
    private final String parent;
    private final String child;
    private final boolean isHikariAvailable;
    private final File dbFile;
    private HikariCP hikari;
    private boolean hasCastException;

    public H2DB(@Nonnull final String parent, @Nonnull final String child) {
        this("com.zaxxer.hikari.HikariConfig", parent, child);
    }

    public H2DB(@Nonnull final String hikariClazzPath, @Nonnull final String parent, @Nonnull final String child) {
        this(hikariClazzPath, new DBPath(parent, child));
    }

    public H2DB(String hikariClazzPath, DBPath dbPath) {
        super(new ConnectionSettings(dbPath.getDbFile().getPath()));
        this.dbFile = dbPath.getDbFile();
        this.parent = "";
        this.child = "";
        this.isHikariAvailable = isHikariAvailable(hikariClazzPath);
        this.loadDriver("org.h2.Driver");
        connect();
    }

    @Override
    public Connection connect() {
        try {
            return setupConnection();
        } catch (SQLException e) {
            this.hasCastException = true;
            log.log(e, () -> of("Fail to connect to H2 database. With the file path: " + this.dbFile));
        }
        return null;
    }

    @Override
    protected void batchUpdate(@Nonnull final List<SqlCommandComposer> batchList, @Nonnull final TableWrapper... tableWrappers) {
        this.batchUpdate(batchList, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    @Override
    protected SqlCommandComposer getCommandComposer(@Nonnull final RowWrapper rowWrapper, final boolean shallUpdate, String... columns) {
        SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(rowWrapper, this);
        boolean columnsIsEmpty = columns == null || columns.length == 0;
        sqlCommandComposer.setColumnsToUpdate(columns);

        if ((!columnsIsEmpty || shallUpdate) && this.doRowExist(rowWrapper.getTableWrapper().getTableName(), rowWrapper.getPrimaryKeyValue()))
            sqlCommandComposer.updateTable(rowWrapper.getPrimaryKeyValue());
        else
            sqlCommandComposer.mergeIntoTable();
        return sqlCommandComposer;
    }

    public Connection setupConnection() throws SQLException {
        Connection connection;

        if (this.isHikariAvailable) {
            if (this.hikari == null) hikari = new HikariCP(this, "org.h2.Driver");
            connection = this.hikari.getFileConnection("jdbc:h2:");
        } else {
            connection = DriverManager.getConnection("jdbc:h2:" + this.dbFile.getPath());
        }

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
}
