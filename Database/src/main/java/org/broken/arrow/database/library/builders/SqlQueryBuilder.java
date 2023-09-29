package org.broken.arrow.database.library.builders;

import org.broken.arrow.database.library.builders.tables.ColumnWrapper;
import org.broken.arrow.database.library.log.LogMsg;
import org.broken.arrow.database.library.utility.SQLCommandPrefix;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A flexible and versatile SQL query builder for constructing SQL queries in a structured and fluent manner.
 * This class allows you to create SQL queries for various SQL commands, including SELECT, INSERT, UPDATE, and DELETE.
 * You can specify columns, values, conditions, and other query components with ease.
 * <p>&nbsp;</p>
 * Usage Example:
 * <pre>{@code
 * SqlQueryBuilder builder = new SqlQueryBuilder(SQLCommandPrefix.SELECT,"tableName");
 * String query = builder
 *     .selectColumns('*')
 *     .from()
 *     .where("column_name = 'value'")
 *     .build()
 *     .getQuery();
 * //or alternatively like this for example:
 * String query1 = builder
 *     .selectColumns('*')
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
 * - It is essential to set the execution type, such as SELECT, INSERT, etc., using {@link #selectColumns(char)}.
 * </p>
 * <p>
 * - For INSERT and UPDATE queries, ensure you call {@link #setValues(java.util.Map)} or {@link #setParameterizedValues(java.util.Map)}.
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
	private String wildcard;
	private StringBuilder updateBuilder;
	private StringBuilder builtColumnsWithValues;
	private StringBuilder joinClause;
	private StringBuilder orderByClause;
	private Map<Integer, ColumnWrapper> indexCachedWithValue;

	/**
	 * Create the instance for the SQL query, such as SELECT, INSERT, UPDATE, or DELETE.
	 *
	 * @param executionsType The SQL command execution type (e.g., SELECT, INSERT, UPDATE, DELETE).
	 * @param tableName      The table name you want to execute the command inside.
	 */
	public SqlQueryBuilder(final SQLCommandPrefix executionsType, String tableName) {
		this.executionsType = executionsType == null ? null : executionsType.getKey();
		this.tableName = tableName;
		this.fromClause = "";
		this.whereClause = "";
		this.query = "";
		this.wildcard ="";
		this.builtColumnsWithValues = null;
		this.joinClause = null;
		this.orderByClause = null;
		this.indexCachedWithValue = null;
	}

	/**
	 * Create the instance for the SQL query, such as SELECT, INSERT, UPDATE, or DELETE.
	 *
	 * @param executionsType The SQL command execution type (e.g., SELECT, INSERT, UPDATE, DELETE).
	 * @param tableName      The table name you want to execute the command inside.
	 */
	public SqlQueryBuilder(final String executionsType, String tableName) {
		this.executionsType = executionsType;
		this.tableName = tableName;
		this.fromClause = "";
		this.whereClause = "";
		this.query = "";
		this.wildcard ="";
		this.builtColumnsWithValues = null;
		this.joinClause = null;
		this.orderByClause = null;
		this.indexCachedWithValue = null;
	}

	/**
	 * Specify the wildcard character for selecting all columns or type columns you want to get.
	 *
	 * @param wildcard The wildcard character for selecting all columns (e.g., '*') or the name of the
	 *                 column.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder selectColumns(final String wildcard) {
		this.wildcard = wildcard == null? "" : wildcard;
		return this;
	}

	/**
	 * Specify the wildcard character for selecting all columns.
	 *
	 * @param wildcard The wildcard character for selecting all columns (e.g., '*') or the name of the
	 *                 column.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder selectColumns(final char wildcard) {
		this.wildcard = wildcard + "";
		return this;
	}

	/**
	 * Specifies a custom target table for the SQL query, allowing you to set your own for example FROM or INTO clause
	 * in front of the table name.
	 *
	 * @param fromClause The custom FROM or INTO clause to be appended to the table name.
	 *                   If not needed, you can provide an empty string or null.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder withCustomClause(String fromClause) {
		this.fromClause = fromClause == null ? "" : fromClause + this.tableName;
		return this;
	}

	/**
	 * Sets the target table for the SQL query, specifying the table name
	 * and the into statement.
	 * And used for REPLACE_INTO,MERGE_INTO and INSERT_INTO
	 *
	 * <p>&nbsp;</p>
	 * <p>
	 * This method is used to tell where as example below:
	 * "INSERT INTO tableName (column1, column2, column3) VALUES (value1, value2, value3);".
	 * </p>
	 *
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder into() {
		this.fromClause = "INTO " + this.tableName;
		return this;
	}

	/**
	 * Sets the from target table for the SQL query, specifying the table name
	 * and the from statement.
	 * <p>&nbsp;</p>
	 * <p>
	 * This method is used to set the from where as example below:
	 * "DELETE FROM tableName WHERE column1 = value (alternatively a placeholder) ?".
	 * </p>
	 *
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder from() {
		this.fromClause = "FROM " + this.tableName;
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
	 * Sets parameterized values for the SQL query using a map of column-value pairs.
	 * For INSERT and UPDATE queries, this method specifies both columns and values.
	 *
	 * @param columnValueMap A map containing column names as keys and corresponding values.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder setParameterizedValues(Map<String, Object> columnValueMap) {
		if (columnValueMap != null && !columnValueMap.isEmpty()) {
			int index = 1;
			if ("UPDATE".equalsIgnoreCase(this.executionsType) || "update".contains(this.executionsType)) {
				StringBuilder prepareColumnsToUpdate = new StringBuilder();
				prepareColumnsToUpdate.append(" SET ");
				for (Entry<String, Object> entry : columnValueMap.entrySet()) {
					final String columnName = entry.getKey();
					final Object value = entry.getValue();
					prepareColumnsToUpdate.append(columnName).append(" = ?,");
					this.putColumnData(index++, columnName, value);
				}
				prepareColumnsToUpdate.setLength(prepareColumnsToUpdate.length() - 1);
				this.updateBuilder = prepareColumnsToUpdate;
				return this;
			}

			StringBuilder columnBuilder = new StringBuilder();
			columnBuilder.append("(");
			for (Entry<String, Object> column : columnValueMap.entrySet()) {
				columnBuilder.append(column.getKey()).append(",");
				this.putColumnData(index++, column.getKey(), column.getValue());
			}
			columnBuilder.setLength(columnBuilder.length() - 1);
			columnBuilder.append(") ");
			int size = columnValueMap.keySet().size();
			if (size > 0) {
				StringBuilder values = this.merge(true, size);
				columnBuilder.insert(columnBuilder.length(), values);
				this.builtColumnsWithValues = columnBuilder;
			}
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
			if ("UPDATE".equalsIgnoreCase(this.executionsType) || "update".contains(this.executionsType)) {
				StringBuilder columns = new StringBuilder();
				columns.append(" SET ");
				for (Entry<String, Object> entry : columnValueMap.entrySet()) {
					final String columnName = entry.getKey();
					final Object value = entry.getValue();
					columns.append(columnName);
					if (columns.length() == 0) {
						columns.append(" = ").append(value == null ? null : "'" + value + "'");
					} else {
						columns.append(" = ").append(value == null ? null + "," : "'" + value + "', ");
					}
				}
				columns.setLength(columns.length() - 1);
				this.updateBuilder = columns;
				return this;
			}
			this.builtColumnsWithValues = mergeObjectsFromMap(columnValueMap);

		}
		return this;
	}

	/**
	 * Caches the value and column name with an index in a parameterized SQL command.
	 * <p>&nbsp;</p>
	 * <p>
	 * This method is useful when you have a preset command like:
	 * "INSERT INTO tableName (column1, column2, column3) VALUES (?, ?, ?)".
	 * </p>
	 *
	 * @param index      The index of the value where the corresponding column name is set.
	 * @param columnName The name of the column to be set with the given index.
	 * @param value      The value that will be set in the database.
	 * @return The SqlQueryBuilder instance for method chaining.
	 */
	public SqlQueryBuilder putColumnData(int index, String columnName, Object value) {
		if (this.indexCachedWithValue == null)
			this.indexCachedWithValue = new HashMap<>();
		this.indexCachedWithValue.put(index, new ColumnWrapper(columnName, value));
		return this;
	}

	/**
	 * Retrieves the column-value map set by {@link #setParameterizedValues(java.util.Map)}.
	 *
	 * @return The column-value map or empty if the map is not set.
	 */
	public Map<Integer, ColumnWrapper> getIndexCachedWithValue() {
		if (indexCachedWithValue != null)
			return Collections.unmodifiableMap(indexCachedWithValue);
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
		if (!validate())
			return this;
		StringBuilder query = new StringBuilder();

		query.append(executionsType)
				.append(" ");

		if (!wildcard.isEmpty())
			query.append(wildcard)
					.append(" ");
		if ( builtColumnsWithValues != null) {
			query.append(builtColumnsWithValues);
			this.query = query.toString();
			return this;
		}
		if (fromClause == null && whereClause == null) {
			return this;
		}
		query.append(fromClause).append(" ");

		if (updateBuilder != null) {
			query.append(updateBuilder);
		}
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


	/**
	 * Merge the provided objects into a format suitable for a database command.
	 *
	 * @param placeholder    If true, uses '?' as a placeholder instead of the actual values.
	 * @param objectsToMerge The objects to be merged into a StringBuilder.
	 * @return The StringBuilder containing the formatted values, like this: (object1, object2, object3).
	 */
	private StringBuilder mergeObjects(boolean placeholder, Object... objectsToMerge) {
		return merge(placeholder, -1, objectsToMerge);
	}

	/**
	 * Merge the provided objects into a format suitable for a database command.
	 *
	 * @param placeholder    If true, uses '?' as a placeholder instead of the actual values.
	 * @param size           The amount of times to run the loop when using a placeholder. If placeholder is false and
	 *                       not greater than 0 or objects is provided, this parameter is ignored.
	 * @param objectsToMerge The objects to be merged into a StringBuilder.
	 * @return The StringBuilder containing the formatted values, like this: (object1, object2, object3).
	 */
	private StringBuilder merge(boolean placeholder, int size, Object... objectsToMerge) {
		StringBuilder objectBuilder = new StringBuilder();
		objectBuilder.append("(");
		int valuesToSet = placeholder && size > 0 && (objectsToMerge == null || objectsToMerge.length == 0) ? size : objectsToMerge.length;
		for (int i = 0; i < valuesToSet; i++) {
			if (placeholder) {
				objectBuilder.append("?");
			} else {
				objectBuilder.append(objectsToMerge[i]);
			}
			if (i < valuesToSet - 1) {
				objectBuilder.append(",");
			}
		}
		objectBuilder.append(")");
		return objectBuilder;
	}

	/**
	 * Merge the provided objects into a format suitable for a database command.
	 *
	 * @param mapOfObjects The map of objects to be merged into a StringBuilder.
	 * @return The StringBuilder containing the formatted values, like this: (object1, object2, object3).
	 */
	private StringBuilder mergeObjectsFromMap(Map<String, Object> mapOfObjects) {
		StringBuilder columns = new StringBuilder();
		StringBuilder values = new StringBuilder();

		columns.append("(");
		for (Entry<String, Object> entry : mapOfObjects.entrySet()) {
			final String columnName = entry.getKey();
			final Object value = entry.getValue();
			columns.append(columnName).insert(columns.length(), columns.length() == 0 ? "" : ",");
			values.append(value).insert(values.length(), values.length() == 0 ? "" : ",");
		}
		//columns.setLength(columns.length() - 1);
		columns.insert(columns.length(), ") VALUES(" + values + ")");
		return columns;
	}

	private boolean validate() {
		if (executionsType == null || executionsType.isEmpty()) {
			LogMsg.warn("You need to set the executionsType");
			return false;
		}

		if (builtColumnsWithValues == null && fromClause == null && whereClause == null && updateBuilder == null) {
			LogMsg.warn("You need to set the column name or column names and value/values." +
					"\nAlternatively specify from what table and set the where condition or conditions.");
			return false;
		}
		if (builtColumnsWithValues != null && updateBuilder == null)
			return true;

		if (updateBuilder != null) {
			if (whereClause == null) {
				LogMsg.warn("You need to specify where it should update the data");
				return false;
			}
			return true;
		}

		if (fromClause == null && whereClause == null) {
			LogMsg.warn("You need to specify what table to set the data and the condition or conditions where to get the data from.");
			return false;
		}

		return true;
	}
}
