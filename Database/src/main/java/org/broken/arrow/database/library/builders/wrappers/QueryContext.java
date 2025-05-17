package org.broken.arrow.database.library.builders.wrappers;

import org.broken.arrow.database.library.builders.LoadDataWrapper;
import org.broken.arrow.database.library.core.SQLDatabaseQuery;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class QueryContext<T> {

    @Nonnull
    private final SQLDatabaseQuery sqlDatabaseQuery;
    @Nonnull
    private final String tableName;

    private Consumer<T> context;

    public QueryContext(@Nonnull final SQLDatabaseQuery sqlDatabaseQuery, @Nonnull final String tableName) {
        this.sqlDatabaseQuery = sqlDatabaseQuery;
        this.tableName = tableName;
    }

    @Nonnull
    public SQLDatabaseQuery getSqlDatabaseQuery() {
        return sqlDatabaseQuery;
    }

    @Nonnull
    public String getTableName() {
        return tableName;
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
     * {@link ConfigurationSerializable} implementation by calling {@link SaveRecord#addKeys(String, Object)}.
     * <p>
     * <strong>When used for loading:</strong>
     * </p>
     * Ensure your {@link LoadDataWrapper} instance can provide identifying key/value pairs.
     * You can access selected (or filtered if set) columns via {@link LoadDataWrapper#getFilteredMap()}.
     *
     * @param query a consumer that modifies a {@link SaveRecord} during saving, or uses a {@link LoadDataWrapper} during loading.
     */
    public void forEachQuery(final Consumer<T> query) {
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
