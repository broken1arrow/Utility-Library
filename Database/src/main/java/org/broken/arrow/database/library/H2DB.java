package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.TableWrapper;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.util.List;

public class H2DB extends Database {
 
	@Override
	public Connection connect() {
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
	public boolean isHasCastExeption() {
		return false;
	}
}
