package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.TableWrapper;
import org.broken.arrow.database.library.log.LogMsg;

import javax.annotation.Nonnull;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class H2DB extends Database {

	private final String parent;
	private final String child;

	public H2DB(@Nonnull final String parent, @Nonnull final String child) {
		this.parent = parent;
		this.child = child;
		connect();
	}

	@Override
	public Connection connect() {
		File dbFile = new File(this.parent, this.child);
/*		if (!dbFile.exists()) {
			try {
				dbFile.createNewFile();
				//String url = "jdbc:sqlite:" + dbFile.getPath();
			} catch (final IOException ex) {
				LogMsg.warn("File write error: " + dbFile, ex);
			}
		}*/
		try {
			return DriverManager.getConnection("jdbc:h2:" + dbFile);
		} catch (SQLException e) {
			LogMsg.warn("File write error: " + dbFile, e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void batchUpdate(@Nonnull final List<String> batchList, @Nonnull final TableWrapper... tableWrappers) {

	}

	@Override
	protected void remove(@Nonnull final List<String> batchList, @Nonnull final TableWrapper tableWrappers) {

	}

	@Override
	protected void dropTable(@Nonnull final List<String> batchList, @Nonnull final TableWrapper tableWrappers) {

	}

	@Override
	public boolean isHasCastException() {
		return false;
	}
}
