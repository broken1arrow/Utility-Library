package org.broken.arrow.library.database.construct.query.builder.table.cte;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.table.cte.builder.modal.CteRegistry;
import org.broken.arrow.library.database.construct.query.builder.table.cte.builder.modal.MainQueryStep;
import org.broken.arrow.library.database.construct.query.builder.table.cte.builder.WithBuilder;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Manager for SQL {@code WITH} clauses and Common Table Expressions (CTEs).
 * <p>
 * Orchestrates the definition of multiple CTEs using a fluent builder syntax,
 * followed by configuring the main execution queries (with optional {@code UNION ALL} support).
 * </p>
 * <p>
 * <b>Usage Example (Fluent Chaining):</b>
 * <pre>{@code
 * // Replace new QueryBuilder() with your actual query instances.
 * with(cteRegistry -> cteRegistry
 *         .as("alias1", builder -> builder.add(Column.of("id")).query(new QueryBuilder()))
 *         .as("alias2", builder -> builder.add(Column.of("id")).query(new QueryBuilder()))
 * ).addQueries(true, mainQueries -> mainQueries // If you only specify one main query, you do not need to set the boolean flag.
 *         .addQuery(new QueryBuilder())
 *         .addQuery(new QueryBuilder())
 * );
 * }</pre>
 * </p>
 * <p>
 * <b>Usage Example (Lambda Blocks):</b>
 * <pre>{@code
 * // Replace new QueryBuilder() with your actual query instances.
 * with(cteRegistry -> {
 *     cteRegistry.as("alias1", builder -> builder.add(Column.of("id")).query(new QueryBuilder()));
 *     cteRegistry.as("alias2", builder -> builder.add(Column.of("id")).query(new QueryBuilder()));
 * }).addQueries(true, mainQueries -> { // If you only specify one main query, you do not need to set the boolean flag.
 *     mainQueries.addQuery(new QueryBuilder());
 *     mainQueries.addQuery(new QueryBuilder());
 * });
 * }</pre>
 * </p>
 */
public class WithManager {
    private final List<WithBuilder> withBuilders = new ArrayList<>();
    private final List<QueryBuilder> mainQueries = new ArrayList<>();
    private final MainQueryStep mainQueryStep = new MainQueryStep(this, mainQueries);
    private boolean unionAll;


    /**
     * Entry point for defining multiple {@code WITH} queries (CTEs) fluently using a lambda block.
     *
     * @param callback the consumer used to configure multiple CTE aliases via {@link CteRegistry}
     * @return the {@link MainQueryStep} to transition into the main query configuration step
     */
    public MainQueryStep with(@Nonnull final Consumer<CteRegistry> callback) {
        CteRegistry cteRegistry = new CteRegistry(withBuilders);
        callback.accept(cteRegistry);
        return mainQueryStep;
    }

    /**
     * Sets whether multiple main queries should be combined using {@code UNION ALL}.
     *
     * @param union {@code true} to combine with {@code UNION ALL}, {@code false} otherwise
     */
    public void setUnion(boolean union) {
        this.unionAll = union;
    }

    /**
     * Builds the complete SQL string combining the {@code WITH} clause and the main execution queries.
     *
     * @return the fully constructed SQL string
     * @throws RuntimeException if validation checks fail (e.g., column mismatches or missing union flags)
     */
    public String build() {
        final StringBuilder buildSQLQuery = new StringBuilder();
        final List<String> cteQueries = new ArrayList<>();
        final List<String> mainQueryStrings = new ArrayList<>();
        buildSQLQuery.append("WITH ");
        for (WithBuilder withBuilder : withBuilders) {
            String fromWrapper = withBuilder.build();
            if (fromWrapper != null) {
                cteQueries.add(fromWrapper);
            }
        }
        buildSQLQuery.append(String.join(", ", cteQueries));

        int columnLength = -2;
        for (QueryBuilder query : mainQueries) {
            if (columnLength != -2) {
                Validate.checkBoolean(columnLength != query.getAmountColumnsSet(),
                        "You must provide an equal amount of columns and ensure the columns have the same data type. Operation blocked.");
            }
            columnLength = query.getAmountColumnsSet();
            mainQueryStrings.add(query.build().replace(";", ""));
        }

        buildSQLQuery.append(" ");
        if (unionAll) {
            buildSQLQuery.append(String.join(" UNION ALL ", mainQueryStrings));
        } else {
            Validate.checkBoolean(mainQueryStrings.size() > 1, "Multiple main queries added but union is false. Operation blocked.");
            buildSQLQuery.append(String.join(" ", mainQueryStrings));
        }
        return buildSQLQuery.toString();
    }

    /**
     * Aggregates and sequentially re-indexes all values from the CTEs and main queries.
     *
     * @return a map of dynamically shifted parameter indexes to their corresponding values.
     */
    public Map<Integer, Object> getValues() {
        final Map<Integer, Object> subMap = new LinkedHashMap<>();
        int runningIndex = 1;

        for (WithBuilder withBuilder : withBuilders) {
            final List<Map.Entry<Integer, Object>> sortedEntries = withBuilder.getValues().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());

            for (Map.Entry<Integer, Object> entry : sortedEntries) {
                subMap.put(runningIndex++, entry.getValue());
            }
        }

        for (QueryBuilder queryBuilder : mainQueries) {
            List<Map.Entry<Integer, Object>> sortedEntries = queryBuilder.getValues().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());
            for (Map.Entry<Integer, Object> entry : sortedEntries) {
                subMap.put(runningIndex++, entry.getValue());
            }
        }
        return subMap;
    }
}
