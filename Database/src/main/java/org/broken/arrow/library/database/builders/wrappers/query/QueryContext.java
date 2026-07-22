package org.broken.arrow.library.database.builders.wrappers.query;

import org.broken.arrow.library.database.builders.LoadDataWrapper;
import org.broken.arrow.library.database.builders.wrappers.SaveRecord;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;

import java.util.function.Consumer;
/**
 * Encapsulates the context for running a SQL query related to a specific table,
 * including a consumer to process query results or prepare query data.
 *
 * @param <T> the type of query result or data wrapper to be handled
 */
public class QueryContext<T> {

    private Consumer<T> context;

    /**
     * Creates a new QueryContext instance.
     */
    public QueryContext() {
    }

    /**
     * Sets the logic to prepare or handle query data for each entry in the cache.
     * <p>
     * This method is used by both saving and loading processes, and its behavior depends
     * on the surrounding context (i.e., the subclass using it).
     * </p>
     * <p>
     * <strong>When used for saving:</strong>
     * </p>
     * You should define primary key(s) or other identifying values not included in your
     * {@link ConfigurationSerializable} implementation by calling {@link SaveRecord#addKey(String, Object)}.
     * <p>
     * <strong>When used for loading:</strong>
     * </p>
     * Ensure your {@link LoadDataWrapper} instance can provide identifying key/value pairs.
     * You can access selected (or filtered if set) columns via {@link LoadDataWrapper#getFilteredMap()}.
     *
     * @param query a consumer that modifies a {@link SaveRecord} during saving, or uses a {@link LoadDataWrapper} during loading.
     */
    public void forEachMapEntity(final Consumer<T> query) {
        this.context = query;
    }


    /**
     * Applies the query preparation logic for a given save context if present.
     *
     * @param queryResult the result for this query, can be either loading or saving action.
     * @return the {@code SaveRecord} instance you put in.
     */
    public T applyQuery(final T queryResult) {
        if (this.context != null)
            context.accept(queryResult);
        return queryResult;
    }

}
