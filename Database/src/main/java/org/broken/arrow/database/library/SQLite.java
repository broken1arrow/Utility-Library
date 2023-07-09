package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.MysqlPreferences;
import org.broken.arrow.database.library.builders.TableWrapper;
import org.broken.arrow.database.library.log.LogMsg;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLite extends Database {

	private final String parent;
	private final String child;
	private final boolean isHikariAvailable;
	private HikariCP hikari;

	public SQLite(@Nonnull final String parent) {
		this(parent, null);
	}

	public SQLite(@Nonnull final String parent, @Nullable final String child) {
		this("com.zaxxer.hikari.HikariConfig", parent, child);
	}

	public SQLite(@Nonnull final String hikariClazzPath, @Nonnull final String parent, @Nullable final String child) {
		this.parent = parent;
		this.child = child;
		this.isHikariAvailable = isHikariAvailable(hikariClazzPath);
		connect();
	}

	@Override
	public Connection connect() {
		try {
			if (this.connection != null && !this.connection.isClosed()) {
				return this.connection;
			}
			//Class.forName("org.sqlite.JDBC");
			return setupConnection();
			//return DriverManager.getConnection("jdbc:sqlite:" + dbFile);

		} catch (final SQLException ex) {
			LogMsg.warn("SQLite exception on initialize", ex);
		} /*catch (final ClassNotFoundException ex) {
			LogMsg.warn("You need the SQLite JBDC library. Google it. Put it in /lib folder.", ex);
		}*/
		return null;
	}

	@Override
	protected void batchUpdate(@Nonnull final List<String> batchList, @Nonnull final TableWrapper... tableWrappers) {
		this.batchUpdate(batchList, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	public Connection setupConnection() throws SQLException {
		Connection connection;
		File dbFile;
		if (this.parent != null && this.child == null)
			dbFile = new File(parent);
		else
			dbFile = new File(this.parent, this.child);

		if (this.hikari == null)
			hikari = new HikariCP(new MysqlPreferences(dbFile.getPath()), "org.sqlite.JDBC");
		if (this.isHikariAvailable)
			connection = this.hikari.getFileConnection("jdbc:sqlite:");
		else
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());

		return connection;
	}

	@Override
	public boolean isHasCastException() {
		return false;
	}

}
 