package org.broken.arrow.database.library.construct.query.utlity;

import org.broken.arrow.database.library.builders.tables.SqlHandler;
import org.broken.arrow.database.library.builders.tables.SqlQueryPair;
import org.broken.arrow.database.library.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface FunctionQuery {

    SqlQueryPair apply(final SqlHandler sqlHandler,final Map<Column, Object> columnsMap, final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause, boolean rowExist);
}
