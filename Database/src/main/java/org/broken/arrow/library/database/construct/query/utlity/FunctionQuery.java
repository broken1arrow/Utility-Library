package org.broken.arrow.library.database.construct.query.utlity;

import org.broken.arrow.library.database.builders.tables.SqlHandler;
import org.broken.arrow.library.database.builders.tables.SqlQueryPair;
import org.broken.arrow.library.database.construct.query.builder.comparison.LogicalOperator;
import org.broken.arrow.library.database.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface FunctionQuery {

    SqlQueryPair apply(final SqlHandler sqlHandler,final Map<Column, Object> columnsMap, final Function<WhereBuilder, LogicalOperator<WhereBuilder>> whereClause, boolean rowExist);
}
