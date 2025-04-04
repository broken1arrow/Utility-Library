package org.broken.arrow.database.library.construct.query.utlity;

import org.broken.arrow.database.library.builders.tables.SqlHandler;
import org.broken.arrow.database.library.builders.tables.SqlQueryPair;
import org.broken.arrow.database.library.construct.query.builder.wherebuilder.WhereBuilder;
import org.broken.arrow.database.library.construct.query.columnbuilder.Column;
import org.broken.arrow.database.library.utility.SqlFunction;

import java.util.Map;

@FunctionalInterface
public interface FunctionQuery {

    SqlQueryPair apply(SqlHandler sqlHandler, Map<Column, Object> columnsMap, SqlFunction<WhereBuilder> whereClause, boolean rowExist);
}
