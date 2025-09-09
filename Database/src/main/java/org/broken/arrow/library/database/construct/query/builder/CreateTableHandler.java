package org.broken.arrow.library.database.construct.query.builder;


import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.Selector;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.SQLConstraints;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.SelectorWrapper;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableColumn;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableSelector;
import org.broken.arrow.library.database.construct.query.builder.tablebuilder.TableSelectorWrapper;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnManager;
import org.broken.arrow.library.database.construct.query.utlity.SqlExpressionType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the construction of a {@code CREATE TABLE} SQL statement.
 * <p>
 * This class provides methods for building a table definition from scratch
 * or by copying an existing table. There are three main construction modes:
 * <ul>
 *     <li>{@link #addColumns(ColumnManager)} – Creates the new table with the
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
    private TableSelectorWrapper selectorWrapper;
    private SelectorWrapper selector;

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
     * @return a {@link SelectorWrapper} to define the selection query for the table copy
     */
    public SelectorWrapper as() {
        copyMethod = SqlExpressionType.AS;
        selector = new SelectorWrapper(this, this.queryBuilder);
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
     * @return a {@link SelectorWrapper} to select the source table for the structure copy
     */
    public SelectorWrapper like() {
        copyMethod = SqlExpressionType.LIKE;
        selector = new SelectorWrapper(this, this.queryBuilder);
        return selector;
    }

    /**
     * Adds column definitions to the {@code CREATE TABLE} statement.
     *
     * @param column the {@link ColumnManager} containing the column definitions
     * @return this handler instance for method chaining
     */
    public CreateTableHandler addColumns(ColumnManager column) {
        selectorWrapper = new TableSelectorWrapper(this, new TableSelector(this.queryBuilder, new TableColumnCache()));
        selectorWrapper.select(column.getColumnsBuilt());
        return this;
    }

    /**
     * Returns all columns currently defined for the new table.
     *
     * @return a list of {@link Column} objects, or an empty list if none are defined
     */
    public List<Column> getColumns() {
        if (selectorWrapper != null)
            return selectorWrapper.getTableSelector().getSelectBuilder().getColumns();
        if (selector != null)
            return selector.getSelector().getSelectBuilder().getColumns();
        return new ArrayList<>();
    }

    /**
     * Returns all primary key columns currently defined for the new table.
     *
     * @return a list of primary key {@link Column} objects, or an empty list if none are defined
     */
    public List<Column> getPrimaryColumns() {
        if (selectorWrapper != null) {
            return selectorWrapper.getTableSelector().getTablesColumnsBuilder().getColumns().stream()
                    .filter(column -> ((TableColumn) column).isPrimaryKey()).collect(Collectors.toList());
        }
        if (selector != null)
            return selector.getSelector().getSelectBuilder().getColumns().stream()
                    .filter(SQLConstraints::isPrimary).collect(Collectors.toList());
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
        final Selector<?, ?> selectorData = this.selector != null ? this.selector.getSelector() : null;
        final TableSelectorWrapper selectorWrapper = this.selectorWrapper;
        final SqlExpressionType copyOption = this.getCopyMethod();

        if (copyOption != null && selectorWrapper == null && selectorData != null) {
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

        Selector<?, ?> selectorDataTable = null;
        if (selectorWrapper != null)
            selectorDataTable = this.selectorWrapper.getTableSelector();

        if (selectorDataTable != null) {
            sql.append(" (");
            sql.append(selectorDataTable.getSelectBuilder().build());
            sql.append(")");
        }

        return sql + "";
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
