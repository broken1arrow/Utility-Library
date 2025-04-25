package org.broken.arrow.database.library.utility;

import org.broken.arrow.database.library.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;

@FunctionalInterface
public interface WhereClauseApplier {
    LogicalOperator<WhereBuilder> apply(WhereBuilder builder, Object value);
}