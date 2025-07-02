package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;

@FunctionalInterface
public interface WhereClauseApplier {
    LogicalOperator<WhereBuilder> apply(WhereBuilder builder, Object value);
}