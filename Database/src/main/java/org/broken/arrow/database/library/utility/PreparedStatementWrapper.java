package org.broken.arrow.database.library.utility;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.sql.PreparedStatement;

/**
 * This class serves as a wrapper for different types of database statements,
 * accommodating both traditional SQL databases with PreparedStatement support
 * and NoSQL databases like MongoDB, which lack a direct equivalent.
 */
public class PreparedStatementWrapper {

    private PreparedStatement preparedStatement;
    private MongoCollection<Document> mongoCollection;

    /**
     * Constructs a PreparedStatementWrapper for use with MongoDB,
     * initializing it with the provided MongoCollection.
     *
     * @param mongoCollection the MongoDB collection to be wrapped.
     */
    public PreparedStatementWrapper(MongoCollection<Document> mongoCollection) {
        this.mongoCollection = mongoCollection;
    }

    /**
     * Constructs a PreparedStatementWrapper for use with traditional SQL databases,
     * initializing it with the provided PreparedStatement.
     *
     * @param preparedStatement the PreparedStatement to be wrapped.
     */
    public PreparedStatementWrapper(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    /**
     * Retrieves the PreparedStatement instance.
     * This method is intended for use with traditional SQL databases
     * that support the PreparedStatement class.
     *
     * @return the PreparedStatement instance.
     */
    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    /**
     * Retrieves the MongoCollection instance.
     * <p>
     * Use this method specifically for MongoDB; attempting
     * to use it with traditional SQL databases will return null.
     *
     * @return the MongoCollection instance with the type Document.
     */
    public MongoCollection<Document> getMongoCollection() {
        return mongoCollection;
    }
}
