package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.MysqlPreferences;
import org.broken.arrow.database.library.builders.tables.TableWrapper;
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

public class PostgreSQL extends Database {

	private final MysqlPreferences mysqlPreference;
	private final boolean isHikariAvailable;
	private final String startSQLUrl;
	private final String driver;
	private HikariCP hikari;
	private boolean hasCastException;

	/**
	 * Creates a new PostgreSQL instance with the given MySQL preferences. This
	 * constructor will not check if the database is created or not.
	 *
	 * @param mysqlPreference The set preference information to connect to the database.
	 */
	public PostgreSQL(final MysqlPreferences mysqlPreference) {
		this(mysqlPreference, false, "com.zaxxer.hikari.HikariConfig");
	}

	/**
	 * Creates a new PostgreSQL instance with the given MySQL preferences.
	 *
	 * @param mysqlPreference The set preference information to connect to the database.
	 * @param createDatabase  If it shall check and create the database if it not created yet.
	 */
	public PostgreSQL(@Nonnull MysqlPreferences mysqlPreference, boolean createDatabase) {
		this(mysqlPreference, createDatabase, "com.zaxxer.hikari.HikariConfig");
	}

	/**
	 * Creates a new PostgreSQL instance with the given MySQL preferences.
	 *
	 * @param mysqlPreference The set preference information to connect to the database.
	 * @param hikariClazz     If you shade the lib to your plugin, so for this api shall find it you need to set the path.
	 * @param createDatabase  If it shall check and create the database if it not created yet.
	 */
	public PostgreSQL(@Nonnull MysqlPreferences mysqlPreference, boolean createDatabase, String hikariClazz) {
		this.mysqlPreference = mysqlPreference;
		this.isHikariAvailable = isHikariAvailable(hikariClazz);
		this.startSQLUrl = "jdbc:postgresql://";
		// don't know if this is need or what driver to use.
		//this.driver = "com.impossibl.postgres.jdbc.PGDataSource";
		// This is other solutions I found online.
		this.driver = "org.postgresql.Driver";
		// this.driver = "org.postgresql.ds.PGConnectionPoolDataSource";
		// this.driver = "org.postgresql.xa.PGXADataSource";
		//this.driver = "org.postgresql.ds.PGSimpleDataSource";
		if (createDatabase)
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
		this.batchUpdate(batchList, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	@Override
	public boolean isHasCastException() {
		return hasCastException;
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

	private void createMissingDatabase() {
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
}
