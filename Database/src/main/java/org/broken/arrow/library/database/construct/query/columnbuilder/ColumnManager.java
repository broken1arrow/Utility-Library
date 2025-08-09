package org.broken.arrow.library.database.construct.query.columnbuilder;

import org.broken.arrow.library.database.construct.query.builder.tablebuilder.SQLConstraints;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.library.database.construct.query.utlity.DataType;

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
     * Creates a new {@link TableColumn.Separator} with the given parameters,
     * initializing the column manager and table column with constraints.
     *
     * @param communeName the name of the column
     * @param datatype the data type of the column
     * @param constraints optional SQL constraints applied to the column
     * @return a new TableColumn.Separator instance for further configuration
     */
    public static TableColumn.Separator tableOf(final String communeName, final DataType datatype, final SQLConstraints... constraints) {
        final ColumnManager columnManger = new ColumnManager();
        return new TableColumn.Separator(new TableColumn(columnManger, communeName, datatype, constraints));
    }

    /**
     * Creates a new {@link Aggregation} column with the specified name and no alias.
     *
     * @param name the column name
     * @return an Aggregation object for further configuration
     */
    public Aggregation column(String name) {
        return column(name, "");
    }

    /**
     * Creates a new {@link Aggregation} column with the specified name and alias.
     *
     * @param name the column name
     * @param alias the alias for the column (can be empty)
     * @return an Aggregation object for further configuration
     */
    public Aggregation column(String name, String alias) {
        return new Aggregation(this, name, alias);
    }

    /**
     * Adds a {@link Column} to the internal list of built columns.
     *
     * @param column the Column to add
     */
    public void add(Column column) {
        columnsBuilt.add(column);
    }

    /**
     * Adds all columns from the provided list to the internal list of built columns.
     *
     * @param columns the list of columns to add
     */
    public void addAll(List<Column> columns) {
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
