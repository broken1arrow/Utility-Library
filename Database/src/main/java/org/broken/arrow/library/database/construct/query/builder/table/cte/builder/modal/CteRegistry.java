package org.broken.arrow.library.database.construct.query.builder.table.cte.builder.modal;

import org.broken.arrow.library.database.construct.query.builder.table.cte.builder.WithBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * Registry context used to define multiple Common Table Expressions (CTEs).
 */
public class CteRegistry {
    private final List<WithBuilder> withBuilders;

    /**
     * Constructs a new {@code CteRegistry}.
     *
     * @param withBuilders the shared reference list where newly instantiated CTE builders are registered
     */
    public CteRegistry(@Nonnull final List<WithBuilder> withBuilders) {
        this.withBuilders = withBuilders;
    }

    /**
     * Defines a new CTE alias and configures its columns and inner query.
     *
     * @param aliasName the alias name for the CTE
     * @param callback  the callback to set the columns and the inner query
     * @return this {@link CteRegistry} instance to allow fluent chaining of multiple CTEs
     */
    public CteRegistry as(String aliasName, Consumer<WithBuilder> callback) {
        WithBuilder withBuilder = new WithBuilder(aliasName);
        callback.accept(withBuilder);
        withBuilders.add(withBuilder);
        return this;
    }
}