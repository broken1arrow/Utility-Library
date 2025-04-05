package org.broken.arrow.database.library.utility;

/**
 * This class serves as a wrapper for different types of database statements,
 * accommodating both traditional SQL databases with PreparedStatement support
 * and NoSQL databases like MongoDB, which lack a direct equivalent.
 *
 * @param <T> the type of data this is associated with.
 */
public class StatementContext<T> {
    private final T statement;

    /**
     * Constructs a PreparedStatementWrapper for use with MongoDB,
     * initializing it with the provided MongoCollection.
     *
     * @param result the result from the type set.
     */
    public StatementContext(T result) {
        this.statement = result;
    }


    /**
     * Retrieves the .
     * This method is intended for use with traditional SQL databases
     * that support the PreparedStatement class.
     *
     * @return the PreparedStatement instance.
     */
    public T getContextResult() {
        return statement;
    }

}
