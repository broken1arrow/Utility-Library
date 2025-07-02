package org.broken.arrow.library.database.utility;

public enum SQLCommandPrefix {

	/**
	 * Used to retrieve data from one or more tables. It is the
	 * fundamental command for querying a database.
	 */
	SELECT("SELECT"),
	/**
	 * Used to modify existing records in a table.
	 */
	UPDATE("UPDATE"),
	/**
	 * Used to remove records from a table.
	 */
	DELETE("DELETE"),
	/**
	 * Used to create a new table in the database.
	 */
	CREATE_TABLE("CREATE TABLE"),
	/**
	 * Used to modify an existing table, such as adding, modifying, or deleting columns.
	 */
	ALTER_TABLE("ALTER TABLE"),
	/**
	 * Used to delete an existing table and all its data.
	 */
	DROP_TABLE("DROP TABLE"),
	/**
	 * Used to remove all records from a table but retains the table structure.
	 */
	TRUNCATE_TABLE("TRUNCATE TABLE"),
	/**
	 * Used to create an index on one or more columns of a table to improve query performance..
	 */
	CREATE_INDEX("CREATE INDEX"),
	/**
	 * Used to remove an index from a table.
	 */
	DROP_INDEX("DROP INDEX"),
	/**
	 * In some database systems like MySQL, this is used to either insert a new row
	 * or update an existing row in a table based on a unique key.
	 */
	REPLACE("REPLACE"),
	/**
	 * This is same as REPLACE INTO for non MySQL databases, this is used to either insert a new row
	 * or update an existing row in a table based on a unique key.
	 */
	MERGE("MERGE"),
	/**
	 * Used to add new records (rows) into a table.
	 */
	INSERT("INSERT"),



	/**
	 * The SET clause is used in an UPDATE statement to specify the new values for columns in
	 * existing rows that meet the WHERE clause's condition.
	 */
	SET("SET"),
	/**
	 * The FROM clause is used in SELECT statements to specify the table or tables from which
	 * to retrieve data. It can also be used in DELETE and UPDATE statements when joining multiple tables.
	 */
	FROM("FROM"),


	/**
	 * The WHERE clause is used in SELECT, UPDATE, and DELETE statements to specify a condition
	 * that filters the rows to be included in the result set (for SELECT) or the rows to be
	 * modified (for UPDATE) or deleted (for DELETE).
	 */
	WHERE("WHERE"),
	/**
	 * The ON clause is used in JOIN operations to specify the join condition that determines
	 * how two or more tables are linked together.
	 */
	ON("ON"),
	/**
	 * The VALUES clause is used primarily in INSERT statements to specify the values
	 * to be inserted into a table's columns.
	 */
	VALUES("VALUES"),
	/**
	 * Used to group rows that have the same values in specified columns.
	 */
	GROUP_BY("GROUP BY"),
	/**
	 * Used in conjunction with GROUP BY to filter grouped rows based on a condition.
	 */
	HAVING("HAVING"),
	/**
	 * Used to sort the result set based on one or more columns.
	 */
	ORDER_BY("ORDER BY"),
	/**
	 * LIMIT (or equivalent keywords like TOP, FETCH): Used to limit the number
	 * of rows returned in a query result.
	 */
	LIMIT("LIMIT"),
	/**
	 * Used to retrieve unique values in a column.
	 */
	DISTINCT("DISTINCT"),
	/**
	 * Used for conditional logic within SQL queries.
	 */
	CASE("CASE"),
	/**
	 * Used to alias column names or table names in query results.
	 */
	AS("AS"),
	/**
	 * JOIN types: Including INNER JOIN, LEFT JOIN (or LEFT OUTER JOIN), RIGHT JOIN
	 * (or RIGHT OUTER JOIN), FULL JOIN (or FULL OUTER JOIN), which specify different
	 * ways of combining data from multiple tables.
	 */
	INNER_JOIN("INNER JOIN"),
	LEFT_JOIN("LEFT JOIN"),
	LEFT_OUTER_JOIN("LEFT OUTER JOIN"),
	RIGHT_JOIN("RIGHT JOIN"),
	RIGHT_OUTER_JOIN("RIGHT OUTER JOIN"),
	FULL_JOIN("FULL JOIN"),
	FULL_OUTER_JOIN("FULL OUTER JOIN"),
	;

	private final String key;

	SQLCommandPrefix(final String key) {
		this.key = key;
	}

	public static boolean contains(Object text) {
		for (SQLCommandPrefix value : values()) {
			if (value.getKey().contains(text + "")) {
				return true;
			}
		}
		return false;
	}

	public String getKey() {
		return key;
	}
}
