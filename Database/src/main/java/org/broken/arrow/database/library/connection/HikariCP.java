package org.broken.arrow.database.library.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.broken.arrow.database.library.Database;
import org.broken.arrow.database.library.builders.ConnectionSettings;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class manages the HikariCP connection pool if it is available.
 * It consolidates all configurations and operations related to HikariCP,
 * allowing for seamless integration and usage of the default connection pool.
 * <p>
 * If you intend to use the default connection pool provided by HikariCP,
 * encapsulate all HikariCP-related functionality within this class.
 * <p>
 * Note: Ensure that HikariCP is included in your project dependencies
 * for this class to function correctly.
 */
public class HikariCP {
    private HikariDataSource hikari;
    private final Database database;
    private final String driver;

    public HikariCP(@Nonnull Database database, String driver) {
        this.database = database;
        this.driver = driver;
    }

    public Connection getConnection(String driverConnection) throws SQLException {
        final ConnectionSettings connectionSettings = this.database.getConnectionSettings();
        final String databaseName = connectionSettings.getDatabaseName();
        final String hostAddress = connectionSettings.getHostAddress();

        final HikariConfig config = getHikariConfig(driverConnection, hostAddress, databaseName);

        final int poolSize = this.database.getMaximumPoolSize();
        if (poolSize > 0)
            // Default is usually 32
            config.setMaximumPoolSize(poolSize);

        long connectionTimeout = this.database.getConnectionTimeout();
        if (connectionTimeout > 0)
            config.setConnectionTimeout(connectionTimeout);

        long idleTimeout = this.database.getIdleTimeout();
        if (idleTimeout > 0)
            config.setIdleTimeout(idleTimeout);

        int minIdleTimeout = this.database.getMinimumIdle();
        if (idleTimeout > 0)
            config.setMinimumIdle(minIdleTimeout);

        long maxLifeTime = this.database.getMaxLifeTime();
        if (maxLifeTime > 0)
            config.setMaxLifetime(maxLifeTime);

		/*		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");*/
        if (this.hikari == null) {
            synchronized (this) {
                this.hikari = new HikariDataSource(config);
            }
        } else {
            createPoolIfSetDataNotMatch(config);
        }
        turnOfLogs();
        return this.hikari.getConnection();
    }

    private void createPoolIfSetDataNotMatch(HikariConfig config) {

       boolean needRecreate = !this.hikari.getJdbcUrl().equals(config.getJdbcUrl()) ||
                !this.hikari.getUsername().equals(config.getUsername()) ||
                !this.hikari.getPassword().equals(config.getPassword());

        if (needRecreate) {
            synchronized (this) {
                if (needRecreate) {
                    this.hikari.close();
                    this.hikari = new HikariDataSource(config);
                }
            }
        }
    }

    @Nonnull
    private HikariConfig getHikariConfig(String driverConnection, String hostAddress, String databaseName) {
        final ConnectionSettings connectionSettings = this.database.getConnectionSettings();
        String port = connectionSettings.getPort();
        String user = connectionSettings.getUser();
        String password = connectionSettings.getPassword();
        String extra = connectionSettings.getQuery();

        if (extra.isEmpty())
            extra = "?useSSL=false&useUnicode=yes&characterEncoding=UTF-8&autoReconnect=" + true;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(driverConnection + hostAddress + ":" + port + "/" + databaseName + extra);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName(this.driver);
        return config;
    }

    public Connection getFileConnection(String driverConnection) throws SQLException {
        final ConnectionSettings connectionSettings = this.database.getConnectionSettings();
        String hostAddress = connectionSettings.getHostAddress();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(driverConnection + hostAddress);
        config.setDriverClassName(this.driver);

		/*		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");*/
        this.hikari = new HikariDataSource(config);
        turnOfLogs();
        return this.hikari.getConnection();
    }

    public void turnOfLogs() {
        Configurator.setAllLevels("com.zaxxer.hikari.pool.PoolBase", Level.WARN);
        Configurator.setAllLevels("com.zaxxer.hikari.HikariDataSource", Level.WARN);
        Configurator.setAllLevels("com.zaxxer.hikari.pool.HikariPool", Level.WARN);
    }
}
