package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.TableWrapper;
import org.broken.arrow.database.library.log.LogMsg;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLite extends Database {

	private final String filePath;

	public SQLite(final String filePath) {
		this.filePath = filePath;
		connect();
	}

	@Override
	public Connection connect() {
		try {
			final File dbFile = new File(filePath);
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
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
			return this.connection;


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
	public boolean isHasCastExeption() {
		return false;
	}

}
 