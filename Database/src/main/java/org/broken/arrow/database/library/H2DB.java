package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.ConnectionSettings;
import org.broken.arrow.database.library.builders.RowWrapper;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.TableWrapper;

import javax.annotation.Nonnull;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class H2DB extends Database<PreparedStatement> {

	private final String parent;
	private final String child;
	private final boolean isHikariAvailable;
	private HikariCP hikari;

	public H2DB(@Nonnull final String parent, @Nonnull final String child) {
		this("com.zaxxer.hikari.HikariConfig", parent, child);
	}

	public H2DB(@Nonnull final String hikariClazzPath, @Nonnull final String parent, @Nonnull final String child) {
		this.parent = parent;
		this.child = child;
		this.loadDriver("org.h2.Driver");
		this.isHikariAvailable = this.isHikariAvailable(hikariClazzPath);
		connect();
	}

	@Override
	public Connection connect() {
		try {
			return setupConnection();
		} catch (SQLException e) {
			LogMsg.warn("File write error: " + parent, e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void batchUpdate(@Nonnull final List<SqlCommandComposer> batchList, @Nonnull final TableWrapper... tableWrappers) {
		this.batchUpdate(batchList, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	}

	@Override
	protected SqlCommandComposer getCommandComposer(@Nonnull final RowWrapper rowWrapper, final boolean shallUpdate, String... columns) {
		SqlCommandComposer sqlCommandComposer = new SqlCommandComposer(rowWrapper, this);
		boolean columnsIsEmpty = columns == null || columns.length == 0;
		sqlCommandComposer.setColumnsToUpdate(columns);
		
		if ((!columnsIsEmpty || shallUpdate) && this.doRowExist(rowWrapper.getTableWrapper().getTableName(), rowWrapper.getPrimaryKeyValue()))
			sqlCommandComposer.updateTable(rowWrapper.getPrimaryKeyValue());
		else
			sqlCommandComposer.mergeIntoTable();
		return sqlCommandComposer;
	}

	public Connection setupConnection() throws SQLException {
		Connection connection;
		File dbFile;
		if (this.parent != null && this.child == null) dbFile = new File(parent);
		else dbFile = new File(this.parent, this.child);
		if (this.isHikariAvailable) {
			if (this.hikari == null) hikari = new HikariCP(new ConnectionSettings(dbFile.getPath()), "org.h2.Driver");
			connection = this.hikari.getFileConnection("jdbc:h2:");
		} else {
			connection = DriverManager.getConnection("jdbc:h2:" + dbFile.getPath());
		}

		return connection;
	}

	@Override
	public boolean isHasCastException() {
		return false;
	}
}
