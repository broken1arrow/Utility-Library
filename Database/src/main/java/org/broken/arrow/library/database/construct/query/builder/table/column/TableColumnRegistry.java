package org.broken.arrow.library.database.construct.query.builder.table.column;


import org.broken.arrow.library.database.construct.query.builder.table.column.bulder.TableSeparator;
import org.broken.arrow.library.database.construct.query.builder.table.constraint.SQLConstraints;
import org.broken.arrow.library.database.construct.query.builder.column.ColumnRegistry;
import org.broken.arrow.library.database.construct.query.utlity.DataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.StringJoiner;

/**
 * A specialized registry for managing {@link TableColumn} instances.
 * <p>
 * This class builds a comma-separated list of SQL column definitions by invoking
 * the appropriate build methods on each cached {@link TableColumn}.
 * </p>
 */
public class TableColumnRegistry extends ColumnRegistry<TableColumn, TableColumnRegistry> {

    /**
     * Default constructor.
     */
    private TableColumnRegistry() {
    }

    /**
     * Creates a new, empty instance of {@link TableColumnRegistry}.
     *
     * @return a new registry instance
     */
    public static TableColumnRegistry make(){
        return new TableColumnRegistry();
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
        final TableColumnRegistry builder = make();
        return new TableSeparator(builder, new TableColumn(columnName, datatype, constraints));
    }

    /**
     * Builds the comma-separated list of SQL column definitions.
     *
     * @return the concatenated SQL string for all columns
     */
    @Override
    public String build() {
        StringJoiner joiner = new StringJoiner(", ");
        for(TableColumn column : this.getColumns()){
            joiner.add(column.build());
        }
        return joiner.toString();
    }

    /**
     * Builds the SQL fragment representing the column definitions,
     * excluding any {@link SQLConstraints#primaryKey() primary key} constraints.
     * <p>
     * This method is intended for generating column definitions in contexts
     * where primary keys are defined separately (e.g., composite keys or
     * table-level constraints).
     * </p>
     *
     * @return the SQL string fragment for the column definitions without primary key constraints
     */
    public String buildWithoutPrimaryKey() {
        StringJoiner joiner = new StringJoiner(", ");
        for(TableColumn column : this.getColumns()){
            joiner.add(column.buildWithoutPrimaryKey());
        }
        return joiner.toString();
    }

    @Nonnull
    @Override
    protected TableColumnRegistry getContext() {
        return this;
    }

}
