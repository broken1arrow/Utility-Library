package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.TableWrapper;
import org.broken.arrow.database.library.log.LogMsg;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLite extends Database {

	private final String parent;
	private final String child;

	public SQLite(@Nonnull final String parent) {
		this(parent, null);
	}

	public SQLite(@Nonnull final String parent, @Nullable final String child) {
		this.parent = parent;
		this.child = child;
		connect();
	}

	@Override
	public Connection connect() {
		try {
			File dbFile;
			if (this.parent != null && this.child == null)
				dbFile = new File(parent);
			else
				dbFile = new File(this.parent, this.child);

			if (!dbFile.exists()) {
				try {
					dbFile.createNewFile();

					//String url = "jdbc:sqlite:" + dbFile.getPath();
				} catch (final IOException ex) {
					LogMsg.warn("File write error: " + dbFile + ".db", ex);
				}
			}

			if (this.connection != null && !this.connection.isClosed()) {
				return this.connection;
			}
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:" + dbFile);

		} catch (final SQLException ex) {
			LogMsg.warn("SQLite exception on initialize", ex);
		} catch (final ClassNotFoundException ex) {
			LogMsg.warn("You need the SQLite JBDC library. Google it. Put it in /lib folder.", ex);
		}
		return null;
	}

	@Override
	protected void batchUpdate(@Nonnull final List<String> batchList, @Nonnull final TableWrapper... tableWrappers) {
		this.batchUpdate(batchList, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	@Override
	protected void remove(@Nonnull final List<String> batchList, @Nonnull final TableWrapper tableWrappers) {
		this.batchUpdate(batchList, tableWrappers);
	}

	@Override
	protected void dropTable(@Nonnull final List<String> batchList, @Nonnull final TableWrapper tableWrappers) {
		this.batchUpdate(batchList, tableWrappers);
	}


	@Override
	public boolean isHasCastExeption() {
		return false;
	}

}
 