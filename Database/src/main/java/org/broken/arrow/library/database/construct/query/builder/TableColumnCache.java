package org.broken.arrow.library.database.construct.query.builder;


import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;

import java.util.StringJoiner;

/**
 * Cache for table columns, extending the generic ColumnBuilder.
 * <p>
 * Builds a comma-separated list of columns by invoking the build method on each cached TableColumn.
 * </p>
 */
public class TableColumnCache extends ColumnBuilder<Column, Void> {

    /**
     * Default constructor.
     */
    public TableColumnCache() {
        //just empty constructor.
    }

    /**
     * Builds the comma-separated list of column strings.
     *
     * @return concatenated column SQL strings
     */
    @Override
    public String build() {
        StringJoiner joiner = new StringJoiner(", ");
        for(Column column : this.getColumns()){
            joiner.add(((TableColumn)column).build());
        }
        return joiner + "";
    }

    /**
     * Builds a comma-separated string representation of all added columns
     * by invoking their {@code toString()} methods.
     * Returns an empty string if no columns have been added.
     *
     * @return a comma-separated string of columns.
     */
    @Override
    public String buildCampsiteKey() {
        StringJoiner joiner = new StringJoiner(", ");
        for(Column column : this.getColumns()){
            joiner.add(((TableColumn)column).buildCampsiteKey());
        }
        return joiner + "";
    }

}
