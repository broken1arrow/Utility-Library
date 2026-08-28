package org.broken.arrow.library.database.construct.query.builder.table.column;


import org.broken.arrow.library.database.construct.query.builder.table.column.bulder.TableSeparator;
import org.broken.arrow.library.database.construct.query.builder.table.constraint.SQLConstraints;
import org.broken.arrow.library.database.construct.query.builder.column.ColumnBuilder;
import org.broken.arrow.library.database.construct.query.utlity.DataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.StringJoiner;

/**
 * Cache for table columns, extending the generic ColumnBuilder.
 * <p>
 * Builds a comma-separated list of columns by invoking the build method on each cached TableColumn.
 * </p>
 */
public class TableColumnBuilder extends ColumnBuilder<TableColumn, TableColumnBuilder> {

    /**
     * Default constructor.
     */
    private TableColumnBuilder() {
        this.clazzType = this;
    }

    /**
     * Build your columns with this builder pattern.
     *
     * @return new instance of the builder cache.
     */
    public static TableColumnBuilder make(){
        return new TableColumnBuilder();
    }

    /**
     * Starts building table columns fluently using a {@link TableSeparator}.
     *
     * @param columnName  the name of the initial column
     * @param datatype    the data type of the initial column
     * @param constraints optional SQL constraints applied to the initial column
     * @return a {@link TableSeparator} instance to chain additional columns or foreign keys
     */
    public static TableSeparator start(@Nonnull final String columnName, @Nonnull final DataType datatype, @Nullable final SQLConstraints... constraints) {
        final TableColumnBuilder builder = make();
        return new TableSeparator(builder, new TableColumn(columnName, datatype, constraints));
    }

    /**
     * Builds the comma-separated list of column strings.
     *
     * @return concatenated column SQL strings
     */
    @Override
    public String build() {
        StringJoiner joiner = new StringJoiner(", ");
        for(TableColumn column : this.getColumns()){
            joiner.add(column.build());
        }
        return joiner + "";
    }

    /**
     * Builds the SQL fragment representing this column's definition,
     * excluding any {@link SQLConstraints#primaryKey() primary key} constraints.
     * <p>
     * This method is intended for generating column definitions in contexts
     * where primary keys are defined separately (e.g. composite keys or
     * table-level constraints).
     *
     * @return the SQL string fragment for the column definition without
     *         primary key constraints
     */
    public String buildWithoutPrimaryKey() {
        StringJoiner joiner = new StringJoiner(", ");
        for(TableColumn column : this.getColumns()){
            joiner.add(column.buildWithoutPrimaryKey());
        }
        return joiner.toString();
    }

}
