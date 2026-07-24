package org.broken.arrow.library.database.construct.query.columnbuilder.function.strategy;

import javax.annotation.Nonnull;

/**
 * Represents an individual transformation strategy for a SQL column expression segment.
 * <p>
 * Implementations apply a specific SQL strategy (such as function wrapping, binary math,
 * or column aliasing) to an incoming SQL expression context string.
 * </p>
 */
public interface ColumnStrategy {

    /**
     * Applies this transformation strategy to the provided SQL expression context.
     *
     * @param context the current SQL expression string fragment
     * @return the transformed or appended SQL expression segment
     */
    @Nonnull
    String build(@Nonnull String context);

}
