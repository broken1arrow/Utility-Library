package org.broken.arrow.library.database.construct.query.builder.table.column;

import org.broken.arrow.library.database.construct.query.builder.table.constraint.referential.ForeignKeyConfig;
import org.broken.arrow.library.database.construct.query.builder.table.constraint.SQLConstraints;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.builder.column.ColumnManager;
import org.broken.arrow.library.database.construct.query.utlity.DataType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * Represents a database table column with a name, data type, and optional SQL constraints.
 * <p>
 * Extends the {@link Column} class and adds type and constraint information,
 * including whether the column is a primary key.
 * </p>
 */
public class TableColumn extends Column {
    private final DataType dataType;
    private final SQLConstraints[] constraints;
    private final boolean isPrimaryKey;
    private final boolean isAutoIncrement;
    private ForeignKeyConfig foreignKeyConfig;

    /**
     * Constructs a new {@code TableColumn} with the specified column manager, name,
     * data type, and optional SQL constraints.
     *
     * @param columnName  the name of the column
     * @param dataType    the data type of the column, must not be null
     * @param constraints zero or more SQL constraints applied to the column
     */
    public TableColumn(@Nonnull final String columnName, @Nonnull final DataType dataType, @Nullable final SQLConstraints... constraints) {
        super(columnName, "");
        this.dataType = dataType;
        this.constraints = constraints;
        this.isPrimaryKey = containsPrimaryKey(constraints);
        this.isAutoIncrement = containsAutoIncrement(constraints);
    }

    /**
     * Returns the SQL constraints applied to this column.
     *
     * @return an array of {@link SQLConstraints} or an empty array if none
     */
    public SQLConstraints[] getConstraints() {
        return constraints;
    }

    /**
     * Returns the data type of this column.
     *
     * @return the {@link DataType} of the column
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Checks if this column is marked as a primary key.
     *
     * @return {@code true} if the column is a primary key, {@code false} otherwise
     */
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * Checks if this column set to auto increment.
     *
     * @return {@code true} if the column set to auto increment, {@code false} otherwise
     */
    public boolean isAutoIncrement() {
        return this.isAutoIncrement;
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
     */
    public void makeForeignKeyReference(@Nonnull final String parentTable, @Nonnull final String parentColumn, @Nonnull final Consumer<ForeignKeyConfig> callBack) {
        ForeignKeyConfig foreignConfig = new ForeignKeyConfig(parentTable, parentColumn);
        callBack.accept(foreignConfig);
        this.foreignKeyConfig = foreignConfig;
    }

    /**
     * Retrieves the foreign key configuration associated with this column, if one exists.
     * <p>
     * <strong>Note:</strong> This method is intended primarily for internal query builder processing
     * when generating SQL DDL statements and does not typically need to be called by end users.
     * </p>
     *
     * @return an {@link Optional} containing the {@link ForeignKeyConfig} if configured,
     * otherwise {@link Optional#empty()}
     */
    public Optional<ForeignKeyConfig> getForeignKeyConfig() {
        return Optional.ofNullable(foreignKeyConfig);
    }

    /**
     * Builds the SQL fragment representing this column's definition,
     * including its name, data type, and constraints.
     *
     * @return the SQL string fragment for the column definition
     */
    public String build() {
        StringJoiner joiner = new StringJoiner(" ");
        if (this.constraints != null)
            for (SQLConstraints constraint : this.constraints) {
                joiner.add(constraint.toString());
            }

        return this.getColumnName() + " " + dataType.getType() + " " + joiner + " ";
    }

    /**
     * Builds the SQL fragment representing this column's definition,
     * excluding any {@link SQLConstraints#primaryKey() primary key} constraints.
     * <p>
     * This method is intended for generating column definitions in contexts
     * where primary keys must be defined separately (e.g., table-level composite keys).
     *
     * @return the SQL string fragment for the column definition without primary key constraints
     */
    public String buildWithoutPrimaryKey() {
        StringJoiner joiner = new StringJoiner(" ");
        if (this.constraints != null) {
            for (SQLConstraints constraint : this.constraints) {
                if (!SQLConstraints.isPrimary(constraint))
                    joiner.add(constraint.toString());
            }
        }
        return this.getColumnName() + " " + dataType.getType() + " " + joiner + " ";
    }

    /**
     * Helper method to check if the provided constraints contain a primary key constraint.
     *
     * @param constraints the SQL constraints to check
     * @return {@code true} if primary key constraint is found, {@code false} otherwise
     */
    private boolean containsPrimaryKey(SQLConstraints... constraints) {
        if (constraints == null) return false;
        for (SQLConstraints constraint : constraints) {
            if (SQLConstraints.isPrimary(constraint)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to check if the provided constraints contain an auto increment constraint.
     *
     * @param constraints the SQL constraints to check
     * @return {@code true} if an auto increment constraint is found, {@code false} otherwise
     */
    private boolean containsAutoIncrement(SQLConstraints... constraints) {
        if (constraints == null) return false;
        for (SQLConstraints constraint : constraints) {
            if (SQLConstraints.isAutoIncrement(constraint)) {
                return true;
            }
        }
        return false;
    }

}
