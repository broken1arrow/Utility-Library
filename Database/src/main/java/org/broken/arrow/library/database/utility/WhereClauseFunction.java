package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.construct.query.builder.comparison.ConditionChainer;
import org.broken.arrow.library.database.construct.query.builder.clause.wherebuilder.WhereBuilder;

/**
 * Functional interface for applying conditions to a {@link WhereBuilder}.
 * <p>
 * This interface is typically used to define how a WHERE clause should be constructed
 * based on a provided value. Implementations receive the current {@link WhereBuilder}
 * and a value, then return a {@link ConditionChainer} representing the resulting condition.
 * </p>
 *
 * <p>Common use cases include:</p>
 * <ul>
 *   <li>Filtering rows for deletion or updates based on primary keys or specific column values.</li>
 *   <li>Building custom conditional logic dynamically at runtime.</li>
 * </ul>
 *
 * @see WhereBuilder
 * @see ConditionChainer
 */
@FunctionalInterface
public interface WhereClauseFunction {

    /**
     * Applies a condition to the given {@link WhereBuilder} using the provided value.
     *
     * @param builder the {@link WhereBuilder} to which the condition is applied
     * @return the resulting {@link ConditionChainer} condition
     */
    ConditionChainer<WhereBuilder> apply(WhereBuilder builder);
}