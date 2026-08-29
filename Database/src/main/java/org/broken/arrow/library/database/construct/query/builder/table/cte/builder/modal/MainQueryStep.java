package org.broken.arrow.library.database.construct.query.builder.table.cte.builder.modal;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.table.cte.WithManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder step responsible for transitioning from the {@code WITH} clause
 * into the main execution queries.
 */
public class MainQueryStep {
    private final WithManager manager;
    private final MainQueryContext mainQueryContext;

    /**
     * Constructs a new {@code MainQueryStep}.
     *
     * @param manager     the parent {@link WithManager} used to route configuration (like the union flag)
     * @param mainQueries the shared list where the configured main queries will be stored
     */
    public MainQueryStep(@Nonnull final WithManager manager,@Nonnull final  List<QueryBuilder> mainQueries) {
        this.manager = manager;
        mainQueryContext = new MainQueryContext(mainQueries);
    }

    /**
     * Adds main queries to be executed after the {@code WITH} clause (defaults to no union).
     *
     * @param consumer the context consumer to add queries
     */
    public void addQueries(@Nonnull final Consumer<MainQueryContext> consumer) {
        this.addQueries(false, consumer);
    }

    /**
     * Adds main queries to be executed after the {@code WITH} clause with an explicit union flag.
     *
     * @param union    {@code true} to combine main queries with {@code UNION ALL}, {@code false} otherwise
     * @param consumer the context consumer to add queries
     */
    public void addQueries(final boolean union, @Nonnull final Consumer<MainQueryContext> consumer) {
        manager.setUnion(union);
        consumer.accept(mainQueryContext);
    }
}
