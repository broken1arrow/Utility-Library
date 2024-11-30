package org.broken.arrow.database.library.utility;

import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;

public class DatabaseCommandConfig {
    protected final int resultSetType;
    protected final int resultSetConcurrency;
    private final ConfigConsumer config;

    public DatabaseCommandConfig(int resultSetType, int resultSetConcurrency) {
        this(resultSetType, resultSetConcurrency, null);
    }

    public DatabaseCommandConfig(final int resultSetType,final int resultSetConcurrency,final ConfigConsumer config) {
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.config = config;
    }

    public int getResultSetType() {
        return resultSetType;
    }

    public int getResultSetConcurrency() {
        return resultSetConcurrency;
    }

    /**
     * Handles the execution of a database command based on the existence of a row.
     * This method provides support for SQL databases that do not use `REPLACE INTO`
     * when modifying a row. It determines whether to update an existing row or
     * replace it entirely, based on the provided parameters.
     *
     * @param commandComposer the composer responsible for constructing and executing
     *                        the appropriate SQL command during database operations.
     * @param primaryKeyValue the primary key value of the row to be modified.
     *                        This value is used to locate the specific row in the database.
     * @param rowExist        a flag indicating whether the row exists in the database.
     *                        If {@code false}, the method will use a `REPLACE INTO`
     *                        command to insert or replace the row.
     */
    public final void applyDatabaseCommand(final SqlCommandComposer commandComposer, final Object primaryKeyValue, final boolean rowExist) {
        if (this.config != null) {
            this.config.apply(commandComposer, primaryKeyValue, rowExist);
            return;
        }
        if (rowExist)
            commandComposer.updateTable(primaryKeyValue);
        else commandComposer.replaceIntoTable();
    }
}
