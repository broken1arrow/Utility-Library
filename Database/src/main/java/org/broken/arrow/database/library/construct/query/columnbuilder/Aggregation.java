package org.broken.arrow.database.library.construct.query.columnbuilder;

import org.broken.arrow.database.library.construct.query.utlity.CalcFunc;
import org.broken.arrow.database.library.construct.query.utlity.MathOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Aggregation {

    private final List<CalcFunc> aggregations = new ArrayList<>();
    private final ColumnManger columnManger;
    private final Column.Separator separator;
    private String roundNumber;
    private String roundMode;
    private MathOperation operation = MathOperation.PER_ROUND;

    public Aggregation(final ColumnManger columnManger, final Column.Separator separator) {
        this.columnManger = columnManger;
        this.separator = separator;
    }

    public Aggregation(ColumnManger columnManger, String name, String alias) {
        this.columnManger = columnManger;
        this.separator = new Column.Separator(this, new Column(name, alias));
    }

    public Aggregation colum(String name) {
        return this.colum( name, "") ;
    }

    public Aggregation colum(String name, String alias) {
        return new Aggregation( this.columnManger, new Column.Separator(this, new Column(name, alias)));
    }

    public Column getColumn() {
        return this.separator.getColumn();
    }

    public Column.Separator withAggregation(CalcFunc aggregation) {
        return this.withAggregations((MathOperation) null, aggregation);
    }

    public Column.Separator withAggregation(MathOperation operation, CalcFunc aggregation) {
        return this.withAggregations(operation, aggregation);
    }

    public Column.Separator withAggregations(CalcFunc... aggregations) {
        return this.withAggregations(null, aggregations);
    }

    public Column.Separator withAggregations(MathOperation operation, CalcFunc... aggregations) {
        Collections.addAll(this.aggregations, aggregations);
        if (operation != null)
            this.operation = operation;
        return this.separator;
    }

    public Column.Separator round(Number numberOfDecimalPlaces) {
        return this.round(numberOfDecimalPlaces, null);
    }

    public Column.Separator round(MathOperation operation, Number numberOfDecimalPlaces) {
        return this.round(operation, numberOfDecimalPlaces, null);
    }

    public Column.Separator round(Number numberOfDecimalPlaces, String mode) {
        return this.round(null, numberOfDecimalPlaces, mode);
    }

    public Column.Separator round(MathOperation operation, Number numberOfDecimalPlaces, String mode) {
        this.roundNumber = numberOfDecimalPlaces + "";
        if (operation != null)
            this.operation = operation;
        this.roundMode = mode;
        return this.separator;
    }

    public List<CalcFunc> getAggregations() {
        return aggregations;
    }

    public String getRoundNumber() {
        return roundNumber;
    }

    public String getRoundMode() {
        return roundMode;
    }

    public MathOperation getOperation() {
        return operation;
    }

    public ColumnManger getColumnManger() {
        return this.columnManger;
    }
}
