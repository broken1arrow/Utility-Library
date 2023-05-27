package org.broken.arrow.database.library;

import org.broken.arrow.database.library.log.LogMsg;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
			LogMsg.warn("You need the SQLite JBDC library. Google it. Put it in /lib folder.",ex);
		}
		return null;
	}

	@Override
	protected void batchUpdate(@Nonnull final List<String> batchupdate) {
		final ArrayList<String> sqls = new ArrayList<>(batchupdate);
		if (!openConnection()) return;

		if (sqls.size() == 0)
			return;

		if (!hasStartWriteToDb)
			try {
				hasStartWriteToDb = true;
				final Statement batchStatement = this.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				final int processedCount = sqls.size();

				// Prevent automatically sending db instructions
				this.connection.setAutoCommit(false);

				for (final String sql : sqls)
					batchStatement.addBatch(sql);
				if (processedCount > 10_000)
					LogMsg.warn("Updating your database (" + processedCount + " entries)... PLEASE BE PATIENT THIS WILL TAKE "
							+ (processedCount > 50_000 ? "10-20 MINUTES" : "5-10 MINUTES") + " - If server will print a crash report, ignore it, update will proceed.");

				// Set the flag to start time notifications timer
				batchUpdateGoingOn = true;

				// Notify console that progress still is being made
				new Timer().scheduleAtFixedRate(new TimerTask() {

					@Override
					public void run() {
						if (batchUpdateGoingOn)
							LogMsg.warn("Still executing, DO NOT SHUTDOWN YOUR SERVER.");
						else
							cancel();
					}
				}, 1000 * 30, 1000 * 30);
				// Execute
				batchStatement.executeBatch();

				// This will block the thread
				this.connection.commit();

				LogMsg.info("Updated " + processedCount + " database entries.");

			} catch (final Throwable t) {
				t.printStackTrace();

			} finally {
				try {
					this.connection.setAutoCommit(true);

				} catch (final SQLException ex) {
					ex.printStackTrace();
				}
				hasStartWriteToDb = false;
				// Even in case of failure, cancel
				batchUpdateGoingOn = false;
			}
	}

	@Override
	public boolean isHasCastExeption() {
		return false;
	}

}
 