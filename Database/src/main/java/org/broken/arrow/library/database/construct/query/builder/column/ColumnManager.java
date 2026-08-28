package org.broken.arrow.library.database.construct.query.builder.column;

import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumnRegistry;
import org.broken.arrow.library.database.construct.query.builder.table.column.bulder.TableSeparator;
import org.broken.arrow.library.database.construct.query.builder.table.constraint.SQLConstraints;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumn;
import org.broken.arrow.library.database.construct.query.utlity.DataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of {@link Column} instances being built,
 * providing methods to create columns with optional aliases and constraints,
 * and to collect them for further use in query building or schema definition.
 */
public class ColumnManager {
    private final List<Column> columnsBuilt = new ArrayList<>();

    /**
     * Creates a new instance of {@link ColumnManager}.
     *
     * @return a new ColumnManager instance
     */
    public static ColumnManager of() {
        return new ColumnManager();
    }

    /**
     * Creates a new {@link TableSeparator} with the given parameters,
     * initializing the column manager and table column with constraints.
     *
     * @param columnName the name of the column
     * @param datatype    the data type of the column
     * @param constraints optional SQL constraints applied to the column
     * @return a new TableColumn.Separator instance for further configuration
     */
    public static TableSeparator tableOf(@Nonnull final String columnName, @Nonnull final DataType datatype, @Nullable final SQLConstraints... constraints) {
        final TableColumnRegistry tableColumnBuilder = TableColumnRegistry.make();
        return new TableSeparator(tableColumnBuilder, new TableColumn(columnName, datatype, constraints));
    }

    /**
     * Creates a new {@link Column} column with the specified name and no alias.
     *
     * @param name the column name
     * @return an ColumnManager object for further configuration
     */
    public ColumnManager column(String name) {
        return column(name, "");
    }

    /**
     * Creates a new {@link Column} column with the specified name and alias.
     *
     * @param name  the column name
     * @param alias the alias for the column (can be empty)
     * @return an ColumnManager object for further configuration
     */
    public ColumnManager column(String name, String alias) {
        return add(Column.of(name, alias));
    }

    /**
     * Adds a {@link Column} to the internal list of built columns.
     *
     * @param column the Column to add
     * @return return this class for chaining
     */
    public ColumnManager add(Column column) {
        columnsBuilt.add(column);
        return this;
    }

    /**
     * Adds all columns from the provided list to the internal list of built columns.
     *
     * @param columns the list of columns to add
     * @param <T>     The type of column.
     */
    public <T extends Column> void addAll(List<T> columns) {
        columnsBuilt.addAll(columns);
    }

    /**
     * Returns the list of all columns that have been built and added.
     *
     * @return the list of built columns
     */
    public List<Column> getColumnsBuilt() {
        return columnsBuilt;
    }


}
