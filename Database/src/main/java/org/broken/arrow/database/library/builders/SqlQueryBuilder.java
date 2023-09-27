package org.broken.arrow.database.library.builders;

import org.broken.arrow.database.library.log.LogMsg;
import org.broken.arrow.database.library.utility.SQLCommandPrefix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A flexible and versatile SQL query builder for constructing SQL queries in a structured and fluent manner.
 * This class allows you to create SQL queries for various SQL commands, including SELECT, INSERT, UPDATE, and DELETE.
 * You can specify columns, values, conditions, and other query components with ease.
 * <p>&nbsp;</p>
 * Usage Example:
 * <pre>{@code
 * SqlQueryBuilder builder = new SqlQueryBuilder("tableName");
 * String query = builder
 *     .setExecutionsType(SQLCommandPrefix.SELECT, '*')
 *     .from()
 *     .where("column_name = 'value'")
 *     .build()
 *     .getQuery();
 * //or alternatively like this for example:
 * String query1 = builder
 *     .setExecutionsType(SQLCommandPrefix.SELECT, '*')
 *     .from()
 *     .where("column_name < threshold")
 *     .build()
 *     .getQuery();
 * }</pre>
 * <p>&nbsp;</p>
 * Error Handling:
 * <p>
 * - This class performs basic error checks during query construction and provides warnings if essential components are missing.
 * </p>
 * <p>
 * - It is essential to set the execution type, such as SELECT, INSERT, etc., using {@link #setExecutionsType(SQLCommandPrefix, char)}.
 * </p>
 * <p>
 * - For INSERT and UPDATE queries, ensure you call {@link #setValues(java.util.Map)} or {@link #setParameterizedValues(java.util.Map)},
 * alternatively use this methods directly {@link #columns(String...)} and {@link #values(Object...)}
 * to specify the columns and values.
 * </p>
 * <p>
 * - For SELECT and DELETE queries, use {@link #from()} and optionally {@link #where(String)} to set the table and conditions.
 * </p>
 *
 * @see SQLCommandPrefix
 */
public class SqlQueryBuilder {
	private String executionsType;
	private final String tableName;
	private String fromClause;
	private String whereClause;
	private String query;
	private StringBuilder columnBuilder;
	private StringBuilder valueBuilder;
	private StringBuilder joinClause;
	private StringBuilder orderByClause;
	private Map<String, Object> columnValueMap;


	public SqlQueryBuilder(String tableName) {
		this.executionsType = "";
		this.tableName = tableName;
		this.fromClause = "";
		this.whereClause = "";
		this.query = "";
		this.columnBuilder = null;
		this.valueBuilder = null;
		this.joinClause = null;
		this.orderByClause = null;
		this.columnValueMap = null;
	}

	/**
	 * Sets the execution type for the SQL query, such as SELECT, INSERT, UPDATE, or DELETE,
	 * and specifies the wildcard character for selecting all columns.
	 *
	 * @param executionsType The SQL command execution type (e.g., SELECT, INSERT, UPDATE, DELETE).
	 * @param wildcard       The wildcard character for selecting all columns (e.g., '*') or the name of the
	 *                       column.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder setExecutionsType(final SQLCommandPrefix executionsType, String wildcard) {
		this.executionsType = executionsType.getKey() + wildcard;
		return this;
	}

	/**
	 * Sets the execution type for the SQL query, such as SELECT, INSERT, UPDATE, or DELETE,
	 * and specifies the wildcard character for selecting all columns.
	 *
	 * @param executionsType The SQL command execution type (e.g., SELECT, INSERT, UPDATE, DELETE).
	 * @param wildcard       The wildcard character for selecting all columns (e.g., '*') or the name of the
	 *                       column.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder setExecutionsType(final SQLCommandPrefix executionsType, char wildcard) {
		this.executionsType = executionsType.getKey() + wildcard;
		return this;
	}

	/**
	 * Sets the target table for the SQL query, specifying the table name.
	 *
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder from() {
		this.fromClause = "FROM " + this.tableName;
		return this;
	}

	/**
	 * Sets parameterized values for the SQL query using a map of column-value pairs.
	 * For INSERT and UPDATE queries, this method specifies both columns and values.
	 *
	 * @param columnValueMap A map containing column names as keys and corresponding values.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder setParameterizedValues(Map<String, Object> columnValueMap) {
		if (columnValueMap != null && !columnValueMap.isEmpty()) {
			this.columnValueMap = columnValueMap;
			this.columns(columnValueMap.keySet().toArray(new String[0]));
			this.parameterizedOfValues(columnValueMap.keySet().size());
		}
		return this;
	}

	/**
	 * Sets values for the SQL query using a map of column-value pairs.
	 * For INSERT and UPDATE queries, this method specifies both columns and values.
	 *
	 * @param columnValueMap A map containing column names as keys and corresponding values.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder setValues(Map<String, Object> columnValueMap) {
		if (columnValueMap != null && !columnValueMap.isEmpty()) {
			this.columns(columnValueMap.keySet().toArray(new String[0]));
			this.values(columnValueMap.values().toArray(new Object[0]));
		}
		return this;
	}

	/**
	 * Sets the WHERE clause for the SQL query, specifying a condition.
	 *
	 * @param condition The condition to apply in the WHERE clause.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder where(String condition) {
		if (condition != null && !condition.isEmpty()) {
			this.whereClause = "WHERE " + condition;
		}
		return this;
	}

	/**
	 * Adds a JOIN clause to the SQL query.
	 *
	 * @param tableName The name of the table to join.
	 * @param condition The join condition.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder join(String tableName, String condition) {
		if (joinClause == null) {
			joinClause = new StringBuilder();
		} else {
			joinClause.append(" ");
		}
		joinClause.append("JOIN ").append(tableName).append(" ON ").append(condition);
		return this;
	}

	/**
	 * Adds an ORDER BY clause to the SQL query.
	 *
	 * @param columnName The name of the column by which to order the results.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder orderBy(String columnName) {
		if (orderByClause == null) {
			orderByClause = new StringBuilder();
		} else {
			orderByClause.append(", ");
		}
		orderByClause.append(columnName);
		return this;
	}

	/**
	 * Sets column names for the SQL query, specifying one or more columns.
	 *
	 * @param columns An array of column names.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder columns(String... columns) {
		if (columns != null && columns.length > 0) {
			StringBuilder columnBuilder = new StringBuilder();
			columnBuilder.append("(");
			for (String column : columns)
				columnBuilder.append(column).append(",");
			columnBuilder.setLength(columnBuilder.length() - 1);
			columnBuilder.append(")");
			this.columnBuilder = columnBuilder;
		}
		return this;
	}

	/**
	 * Sets non-parameterized values for the SQL query, specifying one or more values.
	 * For INSERT and UPDATE queries, this method specifies the values corresponding to columns.
	 *
	 * @param values An array of values to insert or update.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder values(Object... values) {
		if (values != null && values.length > 0) {
			StringBuilder valueBuilder = new StringBuilder();
			valueBuilder.append("(");
			for (Object value : values) {
				valueBuilder.append(value).append(",");
			}
			valueBuilder.setLength(valueBuilder.length() - 1);
			valueBuilder.append(")");
			this.valueBuilder = valueBuilder;
		}
		return this;
	}

	/**
	 * Sets parameterized values for the SQL query, specifying one or more values.
	 * For INSERT and UPDATE queries, this method specifies the values corresponding to columns.
	 *
	 * @param valuesToSet Specify the amount of values to set, need to match amount of columns you plan to alter
	 *                    in the database.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	private SqlQueryBuilder parameterizedOfValues(int valuesToSet) {
		if (valuesToSet > 0) {
			StringBuilder valueBuilder = new StringBuilder();
			valueBuilder.append("(");
			for (int i = 0; i < valuesToSet; i++) {
				valueBuilder.append("?");
				if (i < valuesToSet - 1) {
					valueBuilder.append(",");
				}
			}
			valueBuilder.append(")");
			this.valueBuilder = valueBuilder;
		}
		return this;
	}

	/**
	 * Retrieves the column-value map set by {@link #setParameterizedValues(java.util.Map)}.
	 *
	 * @return The column-value map or empty if the map is not set.
	 */
	public Map<String, Object> getColumnValueMap() {
		if (columnValueMap != null)
			return Collections.unmodifiableMap(columnValueMap);
		return Collections.unmodifiableMap(new HashMap<>());
	}

	/**
	 * Retrieves the constructed SQL query as a string.
	 *
	 * @return The SQL query as a string.
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Get the table name.
	 *
	 * @return the table name.
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Builds the SQL query based on the specified components and returns the query as a string.
	 * This method performs error checks and provides warnings if essential components are missing.
	 *
	 * @return The constructed SQL query as a string.
	 */
	public SqlQueryBuilder build() {
		if (executionsType == null || executionsType.isEmpty()) {
			LogMsg.warn("You need to set the executionsType");
			return this;
		}
		if ((fromClause == null && whereClause == null) || (columnBuilder == null && valueBuilder == null)) {
			LogMsg.warn("You need to set the column name or column names and condition or value/values.");
			return this;
		}

		StringBuilder query = new StringBuilder();
		query.append(executionsType).append(" ");
		if (columnBuilder != null && valueBuilder != null) {
			query.append(columnBuilder).append("VALUES").append(valueBuilder);
			this.query = query.toString();
			return this;
		}
		query.append(fromClause).append(" ");
		if (whereClause != null) {
			query.append(whereClause);
		}
		if (joinClause != null) {
			query.append(joinClause);
		}
		if (orderByClause != null) {
			query.append(orderByClause);
		}

		this.query = query.toString();
		return this;
	}
}
