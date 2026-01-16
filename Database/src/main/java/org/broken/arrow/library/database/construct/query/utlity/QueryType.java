package org.broken.arrow.library.database.construct.query.utlity;

/**
 * Represents the various types of SQL query commands.
 * <p>
 * This enum is used to identify the type of SQL operation being executed,
 * such as {@code SELECT}, {@code DELETE}, {@code UPDATE}, and so on.
 * It is primarily used to classify and handle queries appropriately
 * during execution or validation.
 * </p>
 */
public enum QueryType {
    /** A {@code SELECT} query, used to retrieve data from one or more tables. */
    SELECT,

    /** A {@code DELETE} query, used to remove rows from a table. */
    DELETE,

    /** A {@code DROP} query, used to remove an entire table, view, or other database object. */
    DROP,

    /** A {@code CREATE} query, used to create a new database object (e.g., table, view). */
    CREATE,

    /** A {@code CREATE IF NOT EXISTS} query, used to create a new object only if it does not already exist. */
    CREATE_IF_NOT_EXISTS,

    /** An {@code UPDATE} query, used to modify existing rows in a table. */
    UPDATE,

    /** A {@code WITH} clause, used for common table expressions (CTEs). */
    WITH,

    /** An {@code INSERT} query, used to add new rows to a table. */
    INSERT,

    /** A {@code REPLACE INTO} query, used to insert or replace rows in a table. */
    REPLACE_INTO,

    /** A {@code MERGE INTO} query, used to combine insert/update logic into one statement. */
    MERGE_INTO,

    /** {@code INSERT OR REPLACE INTO} Will replace current value if exist or insert if it not exist. */
    INSERT_REPLACE,

    /** An {@code ALTER TABLE} query, used to modify the structure of an existing table. */
    ALTER_TABLE,

    /** Indicates that no valid query type was detected. */
    NON,
  ;

    /**
     * Attempts to match a string to an exact {@link QueryType}.
     * <p>
     * The match is case-insensitive and requires the provided string to
     * exactly equal one of the enum constants.
     * </p>
     *
     * @param type the query type as a string, such as {@code "SELECT"} or {@code "UPDATE"}
     * @return the matching {@link QueryType}, or {@link #NON} if no match is found
     */
    public static QueryType getType(final String type) {
      if (type == null) return NON;
      String typeUp = type.toUpperCase();
      for (QueryType queryType : QueryType.values()) {
        if (queryType.toString().equals(typeUp)) {
          return queryType;
        }
      }
      return NON;
    }

    /**
     * Attempts to match a string to a {@link QueryType} based on its starting keyword.
     * <p>
     * This method is case-insensitive and only requires that the provided
     * string begins with a valid query type name. This is useful for parsing
     * full SQL statements.
     * </p>
     *
     * @param type the query string, such as {@code "SELECT * FROM users"} or {@code "UPDATE users SET ..."}
     * @return the {@link QueryType} that matches the starting keyword, or {@link #NON} if no match is found
     */
    public static QueryType getStartsWithType(final String type) {
      if (type == null) return NON;
      String typeUp = type.toUpperCase();
      for (QueryType queryType : QueryType.values()) {
        if (typeUp.startsWith(queryType.toString())) {
          return queryType;
        }
      }
      return NON;
    }

  }
