package org.broken.arrow.database.library;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.broken.arrow.database.library.builders.MysqlPreferences;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HikariCP {
	private HikariDataSource hikari;
	private final MysqlPreferences mysqlPreference;
	private final String driver;

	public HikariCP(@Nonnull MysqlPreferences mysqlPreference, String driver) {
		this.mysqlPreference = mysqlPreference;
		this.driver = driver;
		Logger.getLogger("com.zaxxer.hikari.pool.PoolBase").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.pool.HikariPool").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.HikariConfig").setLevel(Level.OFF);
		Logger.getLogger("com.zaxxer.hikari.util.DriverDataSource").setLevel(Level.OFF);
	}

	public Connection getConnection(String driverConnection) throws SQLException {
		String databaseName = mysqlPreference.getDatabaseName();
		String hostAddress = mysqlPreference.getHostAdress();
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
		String databaseName = mysqlPreference.getDatabaseName();
		String hostAddress = mysqlPreference.getHostAdress();
		String port = "";
		String user = "";
		String password = "";

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(driverConnection + hostAddress);
		//config.setUsername(user);
		//config.setPassword(password);
		config.setDriverClassName(this.driver);

		/*		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");*/
		this.hikari = new HikariDataSource(config);

		return this.hikari.getConnection();
	}
}
