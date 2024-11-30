package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.connection.HikariCP;
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

public class MySQL extends Database {
    private final Logging log = new Logging(MySQL.class);
    private final ConnectionSettings mysqlPreference;
    private final String startSQLUrl;
    private final String driver;
    private boolean hasCastException = false;
    private final boolean isHikariAvailable;
    private final HikariCP hikari;

    /**
     * Creates a new MySQL instance with the given MySQL preferences.This
     * constructor will not check if the database is created or not.
     *
     * @param mysqlPreference The set preference information to connect to the database.
     */
    public MySQL(@Nonnull ConnectionSettings mysqlPreference) {
        this(mysqlPreference, "com.zaxxer.hikari.HikariConfig");
    }

    /**
     * Creates a new MySQL instance with the given MySQL preferences.
     *
     * @param mysqlPreference The set preference information to connect to the database.
     * @param createDatabase  If it shall check and create the database if it not created yet.
     */
    public MySQL(@Nonnull ConnectionSettings mysqlPreference, boolean createDatabase) {
        this(mysqlPreference, createDatabase, "com.zaxxer.hikari.HikariConfig");
    }

    /**
     * Creates a new MySQL instance with the given MySQL preferences.
     *
     * @param mysqlPreference The set preference information to connect to the database.
     * @param hikariClazz     If you shade the lib to your plugin, so for this api shall find it you need to set the path.
     */
    public MySQL(@Nonnull ConnectionSettings mysqlPreference, String hikariClazz) {
        this(mysqlPreference, true, hikariClazz);
    }

    /**
     * Creates a new MySQL instance with the given MySQL preferences.
     *
     * @param mysqlPreference The set preference information to connect to the database.
     * @param hikariClazz     If you shade the lib to your plugin, so for this api shall find it you need to set the path.
     * @param createDatabase  If it shall check and create the database if it not created yet.
     */
    public MySQL(@Nonnull ConnectionSettings mysqlPreference, boolean createDatabase, String hikariClazz) {
        super(mysqlPreference);
        this.mysqlPreference = mysqlPreference;
        this.isHikariAvailable = isHikariAvailable(hikariClazz);
        this.setCharacterSet("DEFAULT CHARSET=utf8mb4");
        this.startSQLUrl = "jdbc:mysql://";
        this.driver = "com.mysql.cj.jdbc.Driver";
        if (createDatabase) createMissingDatabase();
        if (isHikariAvailable) {
            this.hikari = new HikariCP(this, this.driver);
        } else this.hikari = null;

        connect();
    }

    @Override
    public Connection connect() {
        Validate.checkNotNull(mysqlPreference, "You need to set preferences for the database");
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
        return new DatabaseCommandConfig(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }


    public Connection setupConnection() throws SQLException {
        Connection connection;

        if (isHikariAvailable && this.hikari != null) {
            connection = this.hikari.getConnection(startSQLUrl);
        } else {
            String databaseName = mysqlPreference.getDatabaseName();
            String hostAddress = mysqlPreference.getHostAddress();
            String port = mysqlPreference.getPort();
            String user = mysqlPreference.getUser();
            String password = mysqlPreference.getPassword();
            String extra = mysqlPreference.getQuery();
            if (extra.isEmpty()) extra = "?useSSL=false&useUnicode=yes&characterEncoding=UTF-8&autoReconnect=" + true;
            connection = DriverManager.getConnection(startSQLUrl + hostAddress + ":" + port + "/" + databaseName + extra, user, password);
        }

        return connection;
    }

    public void createMissingDatabase() {
        String databaseName = mysqlPreference.getDatabaseName();
        String hostAddress = mysqlPreference.getHostAddress();
        String port = mysqlPreference.getPort();
        String user = mysqlPreference.getUser();
        String password = mysqlPreference.getPassword();

        try (Connection createDatabase = DriverManager.getConnection(startSQLUrl + hostAddress + ":" + port + "/?useSSL=false&useUnicode=yes&characterEncoding=UTF-8", user, password)) {
            try (PreparedStatement create = createDatabase.prepareStatement("CREATE DATABASE IF NOT EXISTS " + databaseName)) {
                create.execute();
                close(create);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean usingHikari() {
        return this.isHikariAvailable;
    }

    @Override
    public boolean isHasCastException() {
        return hasCastException;
    }
}
