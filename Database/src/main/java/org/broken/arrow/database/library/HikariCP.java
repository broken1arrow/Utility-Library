package org.broken.arrow.database.library;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.broken.arrow.database.library.builders.MysqlPreferences;
import org.broken.arrow.database.library.log.LogMsg;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;

public class HikariCP {
	private HikariDataSource hikari;
	private final MysqlPreferences mysqlPreference;
	private final String driver;

	public HikariCP(@Nonnull MysqlPreferences mysqlPreference, String driver) {
		this.mysqlPreference = mysqlPreference;
		this.driver = driver;
		Configurator.setAllLevels("com.zaxxer.hikari.pool.PoolBase", Level.WARN);
		Configurator.setAllLevels("com.zaxxer.hikari.HikariDataSource", Level.WARN);
		Configurator.setAllLevels("com.zaxxer.hikari.pool.HikariPool", Level.WARN);
		LogMsg.info("load the HikariCP api...");
	}

	public Connection getConnection(String driverConnection) throws SQLException {
		String databaseName = mysqlPreference.getDatabaseName();
		String hostAddress = mysqlPreference.getHostAddress();
		String port = mysqlPreference.getPort();
		String user = mysqlPreference.getUser();
		String password = mysqlPreference.getPassword();

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(driverConnection + hostAddress + ":" + port + "/" + databaseName + "?useSSL=false&useUnicode=yes&characterEncoding=UTF-8&autoReconnect=" + true);
		config.setUsername(user);
		config.setPassword(password);
		config.setDriverClassName(this.driver);

		/*		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");*/
		this.hikari = new HikariDataSource(config);

		return this.hikari.getConnection();
	}

	public Connection getFileConnection(String driverConnection) throws SQLException {

		String hostAddress = mysqlPreference.getHostAddress();
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(driverConnection + hostAddress);
		config.setDriverClassName(this.driver);

		/*		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");*/
		this.hikari = new HikariDataSource(config);
		return this.hikari.getConnection();
	}
}
