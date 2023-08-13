package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.ColumnWrapper;
import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
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
	 *
	 * @param preferences    The set preference information to connect to the database.
	 * @param hikariClazz    If you shade the lib to your plugin, so for this api shall find it you need to set the path.
	 * @param createDatabase If it shall check and create the database if it not created yet.
	 */
	public PostgreSQL(@Nonnull ConnectionSettings preferences, boolean createDatabase, String hikariClazz) {
		this.preferences = preferences;
		this.isHikariAvailable = isHikariAvailable(hikariClazz);
		this.startSQLUrl = "jdbc:postgresql://";

		// This is other solutions I found online
		// and this one seams to work.
		this.loadDriver("org.postgresql.Driver");
		this.driver = "org.postgresql.Driver";
		// This below is the suggested drivers, but they don't work.
		// this.driver = "com.impossibl.postgres.jdbc.PGDataSource";
		// this.driver = "org.postgresql.ds.PGConnectionPoolDataSource";
		// this.driver = "org.postgresql.xa.PGXADataSource";
		//this.driver = "org.postgresql.ds.PGSimpleDataSource";
		if (createDatabase)
			createMissingDatabase();
		connect();
	}

	@Override
	public Connection connect() {
		Validate.checkNotNull(preferences, "You need to set preferences for the database");
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
	protected void batchUpdate(@Nonnull final List<SqlCommandComposer> batchList, @Nonnull final TableWrapper... tableWrappers) {
		this.batchUpdate(batchList, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	@Override
	public boolean isHasCastException() {
		return hasCastException;
	}

	public Connection setupConnection() throws SQLException {
		Connection connection;

		if (isHikariAvailable) {
			if (this.hikari == null)
				this.hikari = new HikariCP(this.preferences, this.driver);
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

		try {
			Connection createDatabase = DriverManager.getConnection(startSQLUrl + hostAddress + ":" + port + "/?useSSL=false&useUnicode=yes&characterEncoding=UTF-8", user, password);
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
			createDatabase.close();
		} catch (
				SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected List<SqlCommandComposer> getSqlsCommand(@Nonnull final List<SqlCommandComposer> listOfCommands, @Nonnull final ColumnWrapper columnWrapper, final boolean shallUpdate) {
		SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(columnWrapper, this);

		if (shallUpdate && this.doRowExist(columnWrapper.getTableWrapper().getTableName(), columnWrapper.getPrimaryKeyValue()))
			sqlCommandComposer.updateTable(columnWrapper.getPrimaryKey());
		else
			sqlCommandComposer.insertIntoTable();

		listOfCommands.add(sqlCommandComposer);
		return listOfCommands;
	}
}
