package org.broken.arrow.library.database.construct.query.builder.withbuilder;

import org.broken.arrow.library.database.construct.query.QueryBuilder;
import org.broken.arrow.library.database.construct.query.columnbuilder.Column;
import org.broken.arrow.library.database.construct.query.columnbuilder.ColumnBuilder;

/**
 * Specialized column builder used within a WITH clause context.
 * <p>
 * Extends the generic {@link ColumnBuilder} to build columns for
 * a common table expression (CTE) and provides a {@link FromWrapper}
 * to continue building the query from the WITH clause.
 * </p>
 */
public class WithColumnBuilder extends ColumnBuilder<Column, FromWrapper> {

    private final FromWrapper fromWrapper;

    /**
     * Constructs a WithColumnBuilder associated with the specified WITH builder and main query builder.
     *
     * @param withBuilder the WITH clause builder that manages this column builder
     * @param query the main query builder used in conjunction with this WITH clause
     */
    public WithColumnBuilder(WithBuilder withBuilder, QueryBuilder query) {
        this.fromWrapper = new FromWrapper(this, withBuilder, query);
    }

    /**
     * Adds a column to this builder and returns the associated {@link FromWrapper}
     * to continue building the query.
     *
     * @param column the column to add
     * @return the {@link FromWrapper} associated with this builder
     */
    @Override
    public FromWrapper add(Column column) {
        super.add(column);
        return this.fromWrapper;
    }

    /**
     * Returns the {@link FromWrapper} associated with this builder.
     *
     * @return returns the from wrapper.
     */
    public FromWrapper getFromWrapper() {
        return this.fromWrapper;
    }

}