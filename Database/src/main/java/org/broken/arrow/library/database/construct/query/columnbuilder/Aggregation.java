package org.broken.arrow.library.database.construct.query.columnbuilder;

import org.broken.arrow.library.database.construct.query.utlity.CalcFunc;
import org.broken.arrow.library.database.construct.query.utlity.MathOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents aggregation functions applied to database columns.
 * <p>
 * Supports applying one or multiple aggregation functions (e.g., COUNT, SUM, AVG)
 * to columns with configurable separators, rounding options, and math operations.
 * </p>
 */
public class Aggregation {

    private final List<CalcFunc> aggregations = new ArrayList<>();
    private final ColumnManager columnManger;
    private final Column.Separator separator;
    private String roundNumber;
    private String roundMode;
    private MathOperation operation = MathOperation.PER_ROUND;

    /**
     * Constructs an Aggregation instance with the given column manager and separator.
     *
     * @param columnManger manages columns for aggregation context
     * @param separator    separator to associate aggregation with a specific column or clause
     */
    public Aggregation(final ColumnManager columnManger, final Column.Separator separator) {
        this.columnManger = columnManger;
        this.separator = separator;
    }

    /**
     * Constructs an Aggregation from a column name and alias.
     *
     * @param columnManger manager handling the columns
     * @param name the column name
     * @param alias alias for the column
     */
    public Aggregation(ColumnManager columnManger, String name, String alias) {
        this.columnManger = columnManger;
        this.separator = new Column.Separator(this, new Column(name, alias));
    }

    /**
     * Creates a new Aggregation for the specified column name.
     *
     * @param name the column name
     * @return a new Aggregation instance for the column
     */
    public Aggregation column(String name) {
        return this.column( name, "") ;
    }

    /**
     * Creates a new Aggregation for the specified column and alias.
     *
     * @param name the column name
     * @param alias the alias for the column
     * @return a new Aggregation instance for the column and alias
     */
    public Aggregation column(String name, String alias) {
        return new Aggregation( this.columnManger, new Column.Separator(this, new Column(name, alias)));
    }

    /**
     * Gets the column associated with this aggregation.
     *
     * @return the column object
     */
    public Column getColumn() {
        return this.separator.getColumn();
    }

    /**
     * Adds a single aggregation function with default math operation.
     *
     * @param aggregation aggregation function to apply
     * @return the separator to chain further calls
     */
    public Column.Separator withAggregation(CalcFunc aggregation) {
        return this.withAggregations((MathOperation) null, aggregation);
    }

    /**
     * Adds a single aggregation function with a specific math operation.
     *
     * @param operation math operation to apply
     * @param aggregation aggregation function to apply
     * @return the separator to chain further calls
     */
    public Column.Separator withAggregation(MathOperation operation, CalcFunc aggregation) {
        return this.withAggregations(operation, aggregation);
    }

    /**
     * Adds multiple aggregation functions with default math operation.
     *
     * @param aggregations aggregation functions to apply
     * @return the separator to chain further calls
     */
    public Column.Separator withAggregations(CalcFunc... aggregations) {
        return this.withAggregations(null, aggregations);
    }

    /**
     * Adds multiple aggregation functions with an optional math operation.
     *
     * @param operation math operation to apply (can be null)
     * @param aggregations aggregation functions to apply
     * @return the separator to chain further calls
     */
    public Column.Separator withAggregations(MathOperation operation, CalcFunc... aggregations) {
        Collections.addAll(this.aggregations, aggregations);
        if (operation != null)
            this.operation = operation;
        return this.separator;
    }

    /**
     * Sets rounding precision (decimal places) with default math operation.
     *
     * @param numberOfDecimalPlaces number of decimals to round to
     * @return the separator to chain further calls
     */
    public Column.Separator round(Number numberOfDecimalPlaces) {
        return this.round(numberOfDecimalPlaces, null);
    }

    /**
     * Sets rounding precision with specified math operation.
     *
     * @param operation math operation to apply
     * @param numberOfDecimalPlaces number of decimals to round to
     * @return the separator to chain further calls
     */
    public Column.Separator round(MathOperation operation, Number numberOfDecimalPlaces) {
        return this.round(operation, numberOfDecimalPlaces, null);
    }

    /**
     * Sets rounding precision and mode.
     *
     * @param numberOfDecimalPlaces number of decimals to round to
     * @param mode rounding mode (e.g., "HALF_UP", optional)
     * @return the separator to chain further calls
     */
    public Column.Separator round(Number numberOfDecimalPlaces, String mode) {
        return this.round(null, numberOfDecimalPlaces, mode);
    }

    /**
     * Sets rounding precision, math operation, and mode.
     *
     * @param operation math operation to apply
     * @param numberOfDecimalPlaces number of decimals to round to
     * @param mode rounding mode (e.g., "HALF_UP", optional)
     * @return the separator to chain further calls
     */
    public Column.Separator round(MathOperation operation, Number numberOfDecimalPlaces, String mode) {
        this.roundNumber = numberOfDecimalPlaces + "";
        if (operation != null)
            this.operation = operation;
        this.roundMode = mode;
        return this.separator;
    }

    /**
     * Gets all aggregation functions applied.
     *
     * @return list of aggregation functions
     */
    public List<CalcFunc> getAggregations() {
        return aggregations;
    }

    /**
     * Gets the number of decimal places for rounding.
     *
     * @return decimal places as string, or null if not set
     */
    public String getRoundNumber() {
        return roundNumber;
    }

    /**
     * Gets the rounding mode.
     *
     * @return rounding mode as string, or null if not set
     */
    public String getRoundMode() {
        return roundMode;
    }

    /**
     * Gets the math operation associated with this aggregation.
     *
     * @return the math operation
     */
    public MathOperation getOperation() {
        return operation;
    }

    /**
     * Gets the column separator associated with this aggregation.
     *
     * @return the column separator
     */
    public Column.Separator getSeparator() {
        return separator;
    }

    /**
     * Completes the aggregation and returns the column manager.
     *
     * @return the column manager instance
     */
    public ColumnManager finish() {
        return this.columnManger;
    }
}
