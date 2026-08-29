package org.broken.arrow.library.database.construct.query.builder.table;


import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumnRegistry;
import org.broken.arrow.library.database.construct.query.builder.table.column.TableColumn;
import org.broken.arrow.library.database.construct.query.builder.table.selector.TableSelector;
import org.broken.arrow.library.database.construct.query.builder.column.Column;
import org.broken.arrow.library.database.construct.query.utlity.SqlExpressionType;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Handles the construction of a {@code CREATE TABLE} SQL statement.
 * <p>
 * This class provides methods for building a table definition from scratch
 * or by copying an existing table. There are three main construction modes:
 * <ul>
 *     <li>{@link #setColumnBuilder(TableColumnRegistry)} – Creates the new table with the
 *         explicitly specified columns. The provided columns are ignored if
 *         {@link #as()} or {@link #like()} is used.</li>
 *     <li>{@link #as()} – Creates the new table using an {@code AS SELECT} statement,
 *         copying both the structure and the data from a query or source table.</li>
 *     <li>{@link #like()} – Creates the new table using a {@code LIKE} statement,
 *         copying only the structure (schema) of the source table without its data.</li>
 * </ul>
 * Additional methods allow retrieving column definitions, filtering primary keys,
 * and building the final SQL output.
 *
 * <p>Example usage:
 * <pre>{@code
 * CreateTableHandler handler = new CreateTableHandler(queryBuilder);
 * handler.as()
 *        .select("*")
 *        .from("existing_table");
 * //optional, as it automatic invoke build in QueryBuilder class.
 * String sql = handler.build();
 * }</pre>
 */
public class CreateTableHandler {
    private final QueryBuilder queryBuilder;
    private SqlExpressionType copyMethod = null;
    private TableSelector tableSelector;
    private TableSelector selector;

    /**
     * Creates a new handler for building {@code CREATE TABLE} statements.
     *
     * @param queryBuilder the parent query builder used for SQL construction
     */
    public CreateTableHandler(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    /**
     * Specifies that the new table should be created using an {@code AS SELECT} statement.
     * <p>
     * This mode creates the table by executing a query and copying the resulting data and structure
     * from the selected source table or query.
     * <p>
     * Works similarly to {@link #like()}, but {@code AS SELECT} copies both the schema and the data,
     * whereas {@code LIKE} copies only the schema.
     *
     * @return a {@link TableSelector} to define the selection query for the table copy
     */
    public TableSelector as() {
        copyMethod = SqlExpressionType.AS;
        selector = new TableSelector(this, this.queryBuilder, TableColumnRegistry.empty());
        return selector;
    }

    /**
     * Specifies that the new table should be created using a {@code LIKE} statement.
     * <p>
     * This mode creates the table by copying the structure of another table, without copying its data.
     * <p>
     * Works similarly to {@link #as()}, but {@code LIKE} does not insert any data,
     * only replicates the schema definition.
     *
     * @return a {@link TableSelector} to select the source table for the structure copy
     */
    public TableSelector like() {
        copyMethod = SqlExpressionType.LIKE;
        selector = new TableSelector(this, this.queryBuilder, TableColumnRegistry.empty());
        return selector;
    }

    /**
     * Adds column definitions to the {@code CREATE TABLE} statement.
     *
     * @param column the {@link TableColumnRegistry} containing the column definitions
     * @return this handler instance for method chaining
     */
    public CreateTableHandler setColumnBuilder(TableColumnRegistry column) {
        tableSelector = new TableSelector(this, this.queryBuilder, column);
        return this;
    }

    /**
     * Adds column definitions to the {@code CREATE TABLE} statement.
     *
     * @param column the list of {@link TableColumn} containing the column definitions for the table
     * @return this handler instance for method chaining
     */
    public CreateTableHandler addAllColumns(List<TableColumn> column) {
        tableSelector = new TableSelector(this, this.queryBuilder, TableColumnRegistry.empty());
        tableSelector.select(columnBuilder -> columnBuilder.addAll(column));
        return this;
    }

    /**
     * Returns all columns currently defined for the table.
     *
     * @return a list of {@link TableColumn} objects, or an empty list if none are defined
     */
    public List<TableColumn> getTableColumns() {
        if (tableSelector != null)
            return tableSelector.getTableSelector().getSelectBuilder().getColumns();
        if (selector != null)
            return selector.getTableSelector().getSelectBuilder().getColumns();
        return new ArrayList<>();
    }

    /**
     * Returns all columns currently defined for the table.
     *
     * @return a list of {@link Column} objects, or an empty list if none are defined
     */
    public List<Column> getColumns() {
        if (tableSelector != null)
            return new ArrayList<>(tableSelector.getTableSelector().getSelectBuilder().getColumns());
        if (selector != null)
            return new ArrayList<>(selector.getTableSelector().getSelectBuilder().getColumns());
        return new ArrayList<>();
    }

    /**
     * Returns all primary key columns currently defined for the new table.
     *
     * @return a list of primary key {@link TableColumn} objects, or an empty list if none are defined
     */
    public List<TableColumn> getPrimaryColumns() {
        if (tableSelector != null) {
            return tableSelector.getTableSelector().getTablesColumnsBuilder().getColumns().stream()
                    .filter(column -> column != null && column.isPrimaryKey())
                    .map(column -> (TableColumn) column).collect(Collectors.toList());
        }
        if (selector != null) {
            return selector.getTableSelector().getSelectBuilder().getColumns().stream()
                    .filter(column -> column != null && column.isPrimaryKey())
                    .map(column -> (TableColumn) column).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Returns the SQL expression type used when copying a table.
     *
     * @return the copy method, or {@code null} if not set
     */
    public SqlExpressionType getCopyMethod() {
        return copyMethod;
    }

    /**
     * Builds and returns the full SQL {@code CREATE TABLE} query string
     * based on the current configuration.
     *
     * @return the generated SQL statement as a string
     */
    public String build() {
        final StringBuilder sql = new StringBuilder();
        final Selector<TableColumnRegistry, TableColumn> selectorData = this.selector != null ? this.selector.getTableSelector() : null;
        final TableSelector wrapper = this.tableSelector;
        final SqlExpressionType copyOption = this.getCopyMethod();

        if (copyOption != null && wrapper == null && selectorData != null) {
            sql.append(" ")
                    .append(copyOption.toString())
                    .append(" ")
                    .append("SELECT")
                    .append(" ")
                    .append(selectorData.getSelectBuilder().build())
                    .append(" ")
                    .append("FROM")
                    .append(" ")
                    .append(selectorData.getTableWithAlias())
                    .append(selectorData.getWhereBuilder().build());

            return sql + "";

        }

        Selector<TableColumnRegistry, TableColumn> selectorDataTable = null;
        if (wrapper != null)
            selectorDataTable = wrapper.getTableSelector();

        if (selectorDataTable != null) {
            sql.append(" (");
            List<TableColumn> primaryColumns = getPrimaryColumns();
            if (primaryColumns.size() > 1) {
                sql.append(selectorDataTable.getSelectBuilder().buildWithoutPrimaryKey());
                sql.append(", ").append(" PRIMARY KEY (").append(buildComposite(primaryColumns)).append(")");
            } else {
                sql.append(selectorDataTable.getSelectBuilder().build());
            }

            setForeignKeys(sql);
            sql.append(")");
        }
        return sql.toString();
    }

    private void setForeignKeys(StringBuilder sql) {
        List<TableColumn> tableColumns = this.getTableColumns();
        if (tableColumns == null) return;

        for (TableColumn col : tableColumns) {
            if (col != null) {
                final TableColumn tableCol = col;
                tableCol.getForeignKeyConfig().ifPresent(fk -> {
                    sql.append(", FOREIGN KEY (").append(tableCol.getFinishColumName()).append(") ")
                            .append("REFERENCES ").append(fk.getParentTable()).append("(").append(fk.getParentColumn()).append(")");

                    if (fk.getRemoveAction() != null) {
                        sql.append(" ON DELETE ").append(fk.getRemoveAction().getAction());
                    }
                    if (fk.getUpdateAction() != null) {
                        sql.append(" ON UPDATE ").append(fk.getUpdateAction().getAction());
                    }
                });
            }
        }
    }


    private String buildComposite(final List<TableColumn> primaryColumns) {
        if (primaryColumns.isEmpty()) return "";
        final StringJoiner joiner = new StringJoiner(", ");

        for (Column column : primaryColumns) {
            joiner.add(column.getColumnName());
        }
        return joiner + "";
    }

    /**
     * Returns the parent {@link QueryBuilder} associated with this handler.
     *
     * @return the parent query builder
     */
    public QueryBuilder getQueryBuilder() {
        return this.queryBuilder;
    }
}
