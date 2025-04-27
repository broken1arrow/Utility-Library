package org.broken.arrow.database.library.core.databases;

import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.connection.HikariCP;
import org.broken.arrow.database.library.core.SQLDatabaseQuery;
import org.broken.arrow.database.library.utility.DatabaseCommandConfig;
import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.logging.library.Validate;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;

import static org.broken.arrow.logging.library.Logging.of;

public class PostgreSQL extends SQLDatabaseQuery {

    private final Logging log = new Logging(PostgreSQL.class);
    private final ConnectionSettings preferences;
    private final boolean isHikariAvailable;
    private final String startSQLUrl;
    private final String driver;
    private HikariCP hikari;
    private boolean hasCastException;
    private boolean databaseExists;

    /**
     * Creates a new PostgreSQL instance with the given MySQL preferences. This
     * constructor will not check if the database is created or not.
     *
     * @param preferences The set preference information to connect to the database.
     */
    public PostgreSQL(final ConnectionSettings preferences) {
        this(preferences, true, "com.zaxxer.hikari.HikariConfig");
    }

    /**
     * Creates a new PostgreSQL instance with the given MySQL preferences.
     *
     * @param preferences    The set preference information to connect to the database.
     * @param createDatabase If it shall check and create the database if it not created yet.
     */
    public PostgreSQL(@Nonnull ConnectionSettings preferences, boolean createDatabase) {
        this(preferences, createDatabase, "com.zaxxer.hikari.HikariConfig");
    }

    /**
     * Creates a new PostgreSQL instance with the given MySQL preferences.
     * <p>
     * This below is the suggested drivers, but they don't work.
     * this.driver = "com.impossibl.postgres.jdbc.PGDataSource";
     * this.driver = "org.postgresql.ds.PGConnectionPoolDataSource";
     * this.driver = "org.postgresql.xa.PGXADataSource";
     * this.driver = "org.postgresql.ds.PGSimpleDataSource";
     * <p>
     * The working driver is: "org.postgresql.Driver"
     *
     * @param preferences    The set preference information to connect to the database.
     * @param hikariClazz    If you shade the lib to your plugin, so for this api shall find it you need to set the path.
     * @param createDatabase If it shall check and create the database if it not created yet.
     */
    public PostgreSQL(@Nonnull ConnectionSettings preferences, boolean createDatabase, String hikariClazz) {
        super(preferences);
        this.preferences = preferences;
        this.isHikariAvailable = isHikariAvailable(hikariClazz);
        this.startSQLUrl = "jdbc:postgresql://";

        this.loadDriver("org.postgresql.Driver");
        this.driver = "org.postgresql.Driver";
        if (createDatabase)
            createMissingDatabase();
    }

    @Override
    public Connection connect() {
        Validate.checkNotNull(preferences, "You need to set preferences for the database");
        Connection connection = null;
        try {
            if (!hasCastException) {
                connection = this.setupConnection();
            }
            hasCastException = false;
        } catch (SQLRecoverableException exception) {
            hasCastException = true;
            log.log(exception, () -> of("Unable to connect to the database. Please try this action again after re-establishing the " +
                    "connection. This issue might be caused by a temporary connectivity problem or a timeout."));

        } catch (SQLException throwable) {
            hasCastException = true;
            log.log(throwable, () -> of("Could not connect to the database. Check the error message and make sure the database is running."));
        }
        return connection;
    }

    @Nonnull
    @Override
    public DatabaseCommandConfig databaseConfig() {
        return new DatabaseCommandConfig(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, (sqlHandler, columnData, whereClause, rowExist) -> {
            if (rowExist)
                return sqlHandler.updateTable(updateBuilder -> updateBuilder.putAll(columnData), whereClause);
            else
                return sqlHandler.insertIntoTable(insertHandler -> insertHandler.addAll(columnData));
        });
    }

    @Override
    public boolean isHasCastException() {
        return hasCastException;
    }

    public Connection setupConnection() throws SQLException {
        Connection connection;

        if (isHikariAvailable) {
            if (this.hikari == null)
                this.hikari = new HikariCP(this, this.driver);
            connection = this.hikari.getConnection(startSQLUrl);
        } else {
            String databaseName = preferences.getDatabaseName();
            String hostAddress = preferences.getHostAddress();
            String port = preferences.getPort();
            String user = preferences.getUser();
            String password = preferences.getPassword();
            String extra = preferences.getQuery();
            if (extra.isEmpty())
                extra = "?useSSL=false&useUnicode=yes&characterEncoding=UTF-8&autoReconnect=" + true;
            connection = DriverManager.getConnection(startSQLUrl + hostAddress + ":" + port + "/" + databaseName + extra, user, password);
        }

        return connection;
    }

    private void createMissingDatabase() {
        String databaseName = preferences.getDatabaseName();
        String hostAddress = preferences.getHostAddress();
        String port = preferences.getPort();
        String user = preferences.getUser();
        String password = preferences.getPassword();

        try (Connection createDatabase = DriverManager.getConnection(startSQLUrl + hostAddress + ":" + port + "/?useSSL=false&useUnicode=yes&characterEncoding=UTF-8", user, password);) {
            try (PreparedStatement checkStatement = createDatabase.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                checkStatement.setString(1, databaseName);
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    databaseExists = resultSet.next();
                }
            }
            // If the database doesn't exist, create it
            if (!databaseExists) {
                try (PreparedStatement createStatement = createDatabase.prepareStatement("CREATE DATABASE " + databaseName)) {
                    createStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean usingHikari() {
        return this.isHikariAvailable;
    }
}
