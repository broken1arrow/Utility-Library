package org.broken.arrow.library.database.construct.query.builder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.withbuilder.FromWrapper;
import org.broken.arrow.library.database.construct.query.builder.withbuilder.WithBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for SQL WITH clauses and Common Table Expressions (CTEs).
 * <p>
 * Supports adding multiple WITH queries, optional UNION ALL operation,
 * and builds the final WITH clause string.
 * </p>
 */
public class WithManger {

    private final List<WithBuilder> buildersList = new ArrayList<>();
    private boolean union;

    /**
     * Adds a new WITH query with the specified alias to this WITH manager.
     *
     * @param aliasName the alias name for the WITH query (common table expression)
     * @return the new WithBuilder instance to define the query for the alias
     */
    public WithBuilder as(String aliasName) {
        WithBuilder withBuilder = new WithBuilder(aliasName);
        buildersList.add(withBuilder);
        return withBuilder;
    }

    /**
     * Sets whether the WITH queries should be combined with UNION ALL.
     *
     * @param union true to combine with UNION ALL, false otherwise
     */
    public void setUnion(boolean union) {
        this.union = union;
    }

    /**
     * Builds the complete WITH clause string.
     *
     * @return SQL WITH clause or empty string if none
     */
    public String build() {
        final StringBuilder buildSQLQuery = new StringBuilder();
        final List<String> cteQueries = new ArrayList<>();
        final List<String> finalSelects = new ArrayList<>();
        buildSQLQuery.append("WITH ");
        for (WithBuilder query : buildersList) {
            FromWrapper fromWrapper = query.getFromWrapper();
            if (fromWrapper != null) {
                cteQueries.add(fromWrapper.getWithCommandBuilder() + "");
                finalSelects.add(fromWrapper.getFromClaus() + "");
            }
        }
        buildSQLQuery.append(String.join(", ", cteQueries));

        buildSQLQuery.append(" ");
        if (union) {
            buildSQLQuery.append(String.join(" UNION ALL ", finalSelects));
        } else {
            buildSQLQuery.append(String.join(" ", finalSelects));
        }

        return buildSQLQuery + "";
    }

}
