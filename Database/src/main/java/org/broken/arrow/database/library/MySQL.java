package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.MysqlPreferences;
import org.broken.arrow.database.library.log.LogMsg;
import org.broken.arrow.database.library.log.Validate;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLRecoverableException;

public class MySQL extends Database {

	private final MysqlPreferences mysqlPreference;
	private Connection connection;
	private boolean hasCastExeption = false;

	public MySQL(@Nonnull MysqlPreferences mysqlPreference) {
		this.mysqlPreference = mysqlPreference;
	}

	@Override
	public Connection connect() {
		Validate.checkNotNull(mysqlPreference, "You need to set preferences for the database");
		try {
			if (this.connection == null || this.connection.isClosed()) {
				String databaseName = mysqlPreference.getDatabaseName();
				String hostAdress = mysqlPreference.getHostAdress();
				String port = mysqlPreference.getPort();
				String user = mysqlPreference.getUser();
				String password = mysqlPreference.getPassword();
				//Class.forName("com.mysql.jdbc.Driver");
				if (!hasCastExeption) {
					String driverConection = "jdbc:mysql://";

					Connection createDatabase = DriverManager.getConnection(driverConection + hostAdress + ":" + port + "/?useSSL=false&useUnicode=yes&characterEncoding=UTF-8", user, password);

					PreparedStatement createdatabase = createDatabase.prepareStatement("CREATE DATABASE IF NOT EXISTS " + databaseName);
					createdatabase.execute();
					close(createdatabase);
					createDatabase.close();

					connection = DriverManager.getConnection(driverConection + hostAdress + ":" + port + "/" + databaseName + "?useSSL=false&useUnicode=yes&characterEncoding=UTF-8&autoReconnect=" + true, user, password);
				}
				return this.connection;
			}
		} catch (SQLRecoverableException throwables) {
			hasCastExeption = true;
			LogMsg.warn("Could not connect to the database. Check your database connection.", throwables);

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					LogMsg.warn("Could not close connection to the database.", e);
				}
			}
		}
		return this.connection;
	}

	@Override
	public boolean isHasCastExeption() {
		return hasCastExeption;
	}
}
