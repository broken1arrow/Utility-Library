package org.broken.arrow.database.library;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.broken.arrow.database.library.builders.MysqlPreferences;

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
	}

	public Connection getConection(String driverConection) throws SQLException {
		String databaseName = mysqlPreference.getDatabaseName();
		String hostAdress = mysqlPreference.getHostAdress();
		String port = mysqlPreference.getPort();
		String user = mysqlPreference.getUser();
		String password = mysqlPreference.getPassword();

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(driverConection + hostAdress + ":" + port + "/" + databaseName + "?useSSL=false&useUnicode=yes&characterEncoding=UTF-8&autoReconnect=" + true);
		config.setUsername(user);
		config.setPassword(password);
		config.setDriverClassName(this.driver);

		/*		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");*/
		this.hikari = new HikariDataSource(config);

		return this.hikari.getConnection();
	}
}
