package org.broken.arrow.library.database.construct.query.builder.tablebuilder;

import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.utlity.DataType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * Represents an SQL {@code ALTER TABLE} operation for modifying table structure.
 * <p>
 * This class supports adding and dropping columns, and builds the SQL fragment
 * for the column modifications to be executed as part of an ALTER TABLE statement.
 * </p>
 */
public class AlterTable {

    private final List<String> columns = new ArrayList<>();
    private ModifyConstraints modifyConstraints;
    private String newTableName;

    /**
     * Adds a new column to the ALTER TABLE statement using a name and data type.
     *
     * @param columnName the name of the column
     * @param dataType   the SQL data type of the column
     * @return this instance for chaining
     */
    public AlterTable add(@Nonnull final String columnName, @Nonnull final DataType dataType) {
        TableColumn tableColumn = new TableColumn(null, columnName, dataType);
        this.add(tableColumn);
        return this;
    }

    /**
     * Adds a column to the ALTER TABLE statement.
     * Only columns of type {@link TableColumn} are processed.
     *
     * @param column the column definition to add
     * @return this instance for chaining
     */
    public AlterTable add(Column column) {
        if (!(column instanceof TableColumn)) return this;

        final TableColumn tableColumn = (TableColumn) column;
        columns.add("ADD COLUMN " + tableColumn.build().trim());
        return this;
    }

    /**
     * Drops a column from the ALTER TABLE statement.
     * Only columns of type {@link TableColumn} are processed.
     *
     * @param column the column definition to drop
     * @return this instance for chaining
     */
    public AlterTable drop(Column column) {
        if (!(column instanceof TableColumn)) return this;

        final TableColumn tableColumn = (TableColumn) column;
        columns.add("DROP COLUMN " + tableColumn.getColumnName());
        return this;
    }

    /**
     * Add or remove constraints for the table.
     *
     * @param constraints callback to set your constraints for the table.
     */
    public void setConstraints(@Nonnull final Consumer<ModifyConstraints> constraints) {
        this.modifyConstraints = new ModifyConstraints();
        constraints.accept(this.modifyConstraints);
    }

    /**
     * Rename your table.
     *
     * @param newTableName the new name to set.
     */
    public void rename(final String newTableName) {
        this.newTableName = newTableName;
    }

    /**
     * Retrieve the new name set.
     *
     * @return the name or {@code null} if not set a new name.
     */
    public String getNewTableName() {
        return newTableName;
    }

    /**
     * Builds the SQL part for the ALTER TABLE modifications.
     *
     * @return the SQL string containing all modifications
     */
    public String build() {
        final String tableName = this.newTableName;
        if (tableName != null && !tableName.isEmpty()) {
            return "TO " + tableName;
        }

        if (this.modifyConstraints != null) {
            final StringJoiner build = new StringJoiner(", ");
            final String dropPrimaryKey = this.modifyConstraints.getDropPrimaryKey();
            if (dropPrimaryKey != null)
                build.add(dropPrimaryKey);
            final String addPrimaryKey = this.modifyConstraints.getAddPrimaryKey();
            if (addPrimaryKey != null)
                build.add(addPrimaryKey);
            if (addPrimaryKey == null) {
                final String addUnique = this.modifyConstraints.getAddUnique();
                if (addUnique != null)
                    build.add(addUnique);
            }
            return build + "";
        }
        final StringJoiner build = new StringJoiner(", ");
        for (String column : this.columns) {
            build.add(column);
        }
        return build + "";
    }

}
