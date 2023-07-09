package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.MysqlPreferences;
import org.broken.arrow.database.library.builders.TableWrapper;
import org.broken.arrow.database.library.log.LogMsg;
import org.broken.arrow.database.library.log.Validate;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.List;

public class MySQL extends Database {

	private final MysqlPreferences mysqlPreference;
	private final String startSQLUrl;
	private final String driver;
	private boolean hasCastException = false;
	private final boolean isHikariAvailable;
	private HikariCP hikari;

	public MySQL(@Nonnull MysqlPreferences mysqlPreference) {
		this(mysqlPreference, "com.zaxxer.hikari.HikariConfig");
	}

	public MySQL(@Nonnull MysqlPreferences mysqlPreference, String hikariClazz) {
		this.mysqlPreference = mysqlPreference;
		this.isHikariAvailable = isHikariAvailable(hikariClazz);
		this.startSQLUrl = "jdbc:mysql://";
		this.driver = "com.mysql.cj.jdbc.Driver";
		createMissingDatabase();
		connect();
	}

	@Override
	public Connection connect() {
		Validate.checkNotNull(mysqlPreference, "You need to set preferences for the database");
		Connection connection = null;
		try {
			if (this.connection == null || this.connection.isClosed()) {
				if (!hasCastException) {
					connection = this.setupConnection();
				}
			}
		} catch (SQLRecoverableException exception) {
			hasCastException = true;
			LogMsg.warn("Could not connect to the database. Check your database connection.", exception);

		} catch (SQLException throwable) {
			throwable.printStackTrace();
		}
		return connection;
	}

	@Override
	protected void batchUpdate(@Nonnull final List<String> batchList, @Nonnull final TableWrapper... tableWrappers) {
		this.batchUpdate(batchList, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
	public Connection setupConnection() throws SQLException {
		Connection connection;

		if (isHikariAvailable) {
			if (this.hikari == null)
				this.hikari = new HikariCP(this.mysqlPreference, this.driver);
			connection = this.hikari.getConnection(startSQLUrl);
		} else {
			String databaseName = mysqlPreference.getDatabaseName();
			String hostAddress = mysqlPreference.getHostAddress();
			String port = mysqlPreference.getPort();
			String user = mysqlPreference.getUser();
			String password = mysqlPreference.getPassword();
			connection = DriverManager.getConnection(startSQLUrl + hostAddress + ":" + port + "/" + databaseName + "?useSSL=false&useUnicode=yes&characterEncoding=UTF-8&autoReconnect=" + true, user, password);
		}

		return connection;
	}

	public void createMissingDatabase() {
		String databaseName = mysqlPreference.getDatabaseName();
		String hostAddress = mysqlPreference.getHostAddress();
		String port = mysqlPreference.getPort();
		String user = mysqlPreference.getUser();
		String password = mysqlPreference.getPassword();

		try {
			Connection createDatabase = DriverManager.getConnection(startSQLUrl + hostAddress + ":" + port + "/?useSSL=false&useUnicode=yes&characterEncoding=UTF-8", user, password);

			PreparedStatement create = createDatabase.prepareStatement("CREATE DATABASE IF NOT EXISTS " + databaseName);
			create.execute();
			close(create);
			createDatabase.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isHasCastException() {
		return hasCastException;
	}
}
