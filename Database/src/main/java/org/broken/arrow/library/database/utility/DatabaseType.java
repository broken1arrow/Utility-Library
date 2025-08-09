package org.broken.arrow.library.database.utility;

/**
 * Enum representing supported types of databases.
 */
public enum DatabaseType {

	/** SQLite database. */
	SQLITE,

	/** MySQL database. */
	MYSQL,

	/** PostgreSQL database. */
	POSTGRESQL,

	/** H2 in-memory database. */
	H2,

	/** MongoDB NoSQL database. */
	MONGO_DB,

	/** Unknown or unsupported database type. */
	UNKNOWN,
}
