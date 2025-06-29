package org.broken.arrow.database.library.builders.tables;

import org.broken.arrow.database.library.builders.SqlQueryBuilder;
import org.broken.arrow.database.library.core.Database;
import org.broken.arrow.library.logging.Logging;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class help you to wrap your command.
 */
public final class SqlCommandComposer {
	private final Logging log = new Logging(SqlCommandComposer.class);

	private final Database database;
	private SqlQueryBuilder queryBuilder;
	private final Map<Integer, Object> cachedDataByColumn = new HashMap<>();
	private Set<String> columnsToUpdate;
	private final StringBuilder preparedSQLBatch = new StringBuilder();
	private String queryCommand = "";
	private final char quote;

	public SqlCommandComposer( Database database) {

		this.database = database;
		this.quote = database.getQuote();
	}




	/**
	 * Retrieves the cached column names and their corresponding values.
	 *
	 * @return An unmodifiable map with the column name as the key and the associated data as the value.
	 */
	public Map<Integer, Object> getCachedDataByColumn() {
		return Collections.unmodifiableMap(cachedDataByColumn);
	}

	public String getPreparedSQLBatch() {
		return preparedSQLBatch.toString();
	}

	/**
	 * Set the columns name you only want to update.
	 *
	 * @param columnsToUpdate the columns you want to update.
	 */
	public void setColumnsToUpdate(final String... columnsToUpdate) {
		this.columnsToUpdate = new HashSet<>(Arrays.asList(columnsToUpdate));
	}

	/**
	 * Retrieves the SQL command that has been constructed for execution in the database.
	 * <p>&nbsp;</p>
	 * <p>
	 * Note that the returned string has not been checked for potential SQL injection or
	 * other security vulnerabilities. It is recommended to use {@link #getPreparedSQLBatch()}
	 * and {@link #getCachedDataByColumn()} in conjunction with the preparedStatement method to ensure
	 * safer query execution.
	 * </p>
	 *
	 * @return The SQL command that has been constructed.
	 */
	public String getQueryCommand() {
		return queryCommand;
	}

}
