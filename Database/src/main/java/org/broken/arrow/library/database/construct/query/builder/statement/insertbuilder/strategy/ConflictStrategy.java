package org.broken.arrow.library.database.construct.query.builder.statement.insertbuilder.strategy;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.builder.clause.ParameterSupplier;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.builder.column.refernces.LiteralVal;
import org.broken.arrow.library.database.utility.DatabaseType;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Consumer;

/**
 * Orchestrates the generation of SQL collision clauses (e.g., {@code ON CONFLICT} or
 * {@code ON DUPLICATE KEY UPDATE}) based on the active database dialect.
 * <p>
 * This class acts as the bridge between the user's configuration (via {@link ConflictBuilder})
 * and the final SQL query generation in the parent query builder.
 */
public class ConflictStrategy implements ParameterSupplier {
    private final QueryBuilder queryBuilder;
    private final ConflictBuilder conflictBuilder;
    private String[] targetColumns;

    /**
     * Constructs a new ConflictStrategy.
     *
     * @param queryBuilder the parent query builder providing the database context
     */
    public ConflictStrategy(@Nonnull final QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
        this.conflictBuilder = new ConflictBuilder(this);
    }

    /**
     * Specifies the target conflict columns (e.g., primary keys or unique constraints).
     * <p>
     * This is <b>required</b> for PostgreSQL and SQLite. For cross-database portability,
     * it is highly recommended to set this even if you are currently targeting MySQL/MariaDB.
     * </p>
     *
     * @param columns the column names that trigger the conflict resolution
     * @return the {@link ConflictBuilder} to configure update behavior
     */
    public ConflictBuilder target(@Nonnull final String... columns) {
        this.targetColumns = columns;
        return this.conflictBuilder;
    }

    /**
     * Retrieves the builder containing the collision configuration state.
     *
     * @return the conflict builder
     */
    public ConflictBuilder getConflictBuilder() {
        return conflictBuilder;
    }

    /**
     * Retrieves the target columns defined for the conflict.
     *
     * @return an array of target column names, or null if none were set
     */
    public String[] getTargetColumns() {
        return this.targetColumns;
    }

    /**
     * Checks if a specific column has already been configured for an update.
     *
     * @param column the column name to check
     * @return true if the column is already mapped in the update configuration
     */
    public boolean hasUpdate(String column) {
        return getConflictBuilder().containsUpdateColumnsKey(column);
    }

    /**
     * Builds the SQL fragment for the collision resolution based on the database dialect.
     *
     * @return the generated SQL string (e.g., {@code ON CONFLICT ... DO UPDATE SET ...})
     */
    @Nonnull
    public String build() {
        final DatabaseType type = this.queryBuilder.getDatabaseType();
        final ConflictBuilder conflictBuilder = this.getConflictBuilder();
        final boolean isPostgreSqlite = type == DatabaseType.POSTGRESQL || type == DatabaseType.SQLITE;

        if (conflictBuilder.isDoNothing() || conflictBuilder.isUpdateColumnsEmpty()) {
            if (isPostgreSqlite) {
                return "ON CONFLICT DO NOTHING";
            } else {
                return "";
            }
        }
        final StringBuilder sql = new StringBuilder();

        if (isPostgreSqlite) {
            Validate.checkBoolean(targetColumns == null || targetColumns.length == 0, "You must set target conflict columns for database type: " + type.name());
            sql.append("ON CONFLICT (")
                    .append(String.join(", ", targetColumns))
                    .append(") DO UPDATE SET ");

            StringJoiner clause = getClause("EXCLUDED.", "");
            sql.append(clause);
        } else {
            sql.append("ON DUPLICATE KEY UPDATE ");
            StringJoiner clause = getClause("VALUES(", ")");
            sql.append(clause);
        }
        return sql.toString();
    }


    @Override
    @Nonnull
    public List<Object> getRawParameters() {
        final List<Object> parameters = new ArrayList<>();
        final ConflictBuilder conflictBuilder = this.getConflictBuilder();

        conflictBuilder.getUpdateColumns().forEach((s, object) -> {
            if (object instanceof LiteralVal) {
                parameters.add(((LiteralVal) object).value());
            }
        });
        return parameters;
    }

    @Nonnull
    private StringJoiner getClause(String prefix, String suffix) {
        final ConflictBuilder conflictBuilder = this.getConflictBuilder();
        final StringJoiner setClause = new StringJoiner(", ");

        for (Map.Entry<String, Object> col : conflictBuilder.getUpdateColumns().entrySet()) {
            Object value = col.getValue();
            if (value instanceof Column) {
                setClause.add(col.getKey() + " = " + ((Column) value).getColumnName());
            } else if (value instanceof LiteralVal) {
                Object rawValue = ((LiteralVal) value).value();
                if (this.queryBuilder.isGlobalEnableQueryPlaceholders()) {
                    setClause.add(col.getKey() + " = ?");
                } else {
                    if (rawValue instanceof String) {
                        setClause.add(col.getKey() + " = '" + ((String) rawValue).replace("'", "''") + "'");
                    } else {
                        setClause.add(col.getKey() + " = " + rawValue);
                    }
                }
            } else {
                setClause.add(col.getKey() + " = " + prefix + col.getValue() + suffix);
            }
        }
        return setClause;
    }


}