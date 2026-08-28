package org.broken.arrow.library.database.construct.query.builder.table.column.bulder;

import org.broken.arrow.library.database.construct.query.builder.column.ColumnManager;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumn;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumnBuilder;
import org.broken.arrow.library.database.construct.query.builder.table.constraint.SQLConstraints;
import org.broken.arrow.library.database.construct.query.builder.table.constraint.referential.ForeignKeyConfig;
import org.broken.arrow.library.database.construct.query.utlity.DataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A builder-style helper class to chain the creation of {@link TableColumn} instances
 * and add them to a {@link ColumnManager}.
 */
public class TableSeparator {
    private final TableColumnBuilder tableColumnBuilder;
    private final TableColumn column;

    /**
     * Creates a new {@code Separator} wrapping the given {@link TableColumn}
     * and registers the column with its manager if not {@code null}.
     *
     * @param tableColumnBuilder the table builder to add columns too
     * @param column the {@link TableColumn} to wrap
     */
    public TableSeparator(@Nonnull final TableColumnBuilder tableColumnBuilder, @Nonnull final TableColumn column) {
        this.tableColumnBuilder = tableColumnBuilder;
        this.column = column;
        tableColumnBuilder.add(column);
    }

    /**
     * Adds a new column with the specified name, data type, and constraints
     * to the same {@link ColumnManager}.
     *
     * @param columnName  the name of the new column
     * @param datatype    the data type of the new column
     * @param constraints zero or more SQL constraints for the new column
     * @return a new {@code Separator} wrapping the newly created column
     */
    public TableSeparator nextColumn(@Nonnull final String columnName, @Nonnull final DataType datatype, @Nullable final SQLConstraints... constraints) {
        return new TableSeparator(this.tableColumnBuilder, new TableColumn(columnName, datatype, constraints));
    }

    /**
     * Configures a foreign key reference for this column pointing to a specified parent table and column.
     * <p>
     * Uses a configuration callback allowing fluent setup of referential actions, such as
     * {@code ON DELETE} or {@code ON UPDATE} rules.
     * </p>
     *
     * @param parentTable  the name of the referenced parent table (must not be {@code null})
     * @param parentColumn the name of the referenced column in the parent table (must not be {@code null})
     * @param callBack     a callback consumer used to configure referential actions via {@link ForeignKeyConfig} (must not be {@code null})
     * @return returns this class for chaining.
     */
    public TableSeparator makeForeignKeyReference(@Nonnull final String parentTable, @Nonnull final String parentColumn, @Nonnull final Consumer<ForeignKeyConfig> callBack) {
        this.column.makeForeignKeyReference(parentTable, parentColumn, callBack);
        return this;
    }

    /**
     * Finishes the column building process and returns the associated {@link TableColumnBuilder}.
     *
     * @return the {@link TableColumnBuilder} managing the columns
     */
    public TableColumnBuilder build() {
        return this.tableColumnBuilder;
    }

}