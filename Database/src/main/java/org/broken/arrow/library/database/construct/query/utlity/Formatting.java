package org.broken.arrow.library.database.construct.query.utlity;

import org.broken.arrow.library.database.construct.query.builder.comparison.ComparisonHandler;
import org.broken.arrow.library.database.construct.query.builder.condition.ConditionQuery;

import java.util.List;
/**
 * Utility class for formatting SQL conditions from a list of ComparisonHandlers.
 * <p>
 * Provides logic to combine multiple conditions with logical operators (AND/OR)
 * and correctly handle parentheses grouping for complex condition expressions.
 * </p>
 * <p>
 * This is used internally by WhereBuilder and HavingBuilder to create the final SQL clause string.
 * </p>
 */
public class Formatting {

    private Formatting() {
    }

    /**
     * Formats a list of conditions into a valid SQL clause segment with proper logical operators
     * and parentheses to ensure correct logical grouping.
     *
     * @param <T> The type of the builder (e.g. WhereBuilder or HavingBuilder) that owns the conditions.
     * @param conditionsList List of ComparisonHandler conditions to format.
     * @return The formatted SQL condition string.
     */
    public static <T> String formatConditions(final List<ComparisonHandler<T>> conditionsList) {
        final StringBuilder whereClause = new StringBuilder();
        boolean openParenthesis = false;

        for (int i = 0; i < conditionsList.size(); i++) {
            final ComparisonHandler<?> comparisonHandler = conditionsList.get(i);
            if (comparisonHandler == null) continue;
            final ConditionQuery<?> current = comparisonHandler.getLogicalOperator().getConditionQuery();
            final ComparisonHandler<?> nextComparisonHandler = (i + 1 < conditionsList.size()) ? conditionsList.get(i + 1) : null;
            final LogicalOperators nextOperator = (nextComparisonHandler != null) ? nextComparisonHandler.getLogicalOperator().getConditionQuery().getLogicalOperator() : null;
            final boolean nextIsOr = nextOperator == LogicalOperators.OR;
            final boolean currentIsOr = current.getLogicalOperator() == LogicalOperators.OR;

            openParenthesis = setOpenParenthesis(whereClause, nextIsOr, openParenthesis);
            whereClause.append(current.getColumn()).append(current.getWhereCondition());
            openParenthesis = setCloseParenthesis(whereClause, nextComparisonHandler, currentIsOr, openParenthesis);

            if (current.getLogicalOperator() != null) {
                whereClause.append(" ").append(current.getLogicalOperator()).append(" ");
            }

            if (currentIsOr && nextOperator != null) {
                whereClause.append("(");
                openParenthesis = true;
            }
        }
        return whereClause.toString();
    }

    private static boolean setOpenParenthesis(final StringBuilder whereClause, final boolean nextIsOr, boolean hasOpenParenthesis) {
        if (nextIsOr && !hasOpenParenthesis) {
            whereClause.append("(");
            hasOpenParenthesis = true;
        }
        return hasOpenParenthesis;
    }

    private static boolean setCloseParenthesis(final StringBuilder whereClause, final ComparisonHandler<?> next, final boolean currentIsOr, boolean hasOpenParenthesis) {
        if (checkIfHasOpenParenthesis(next, currentIsOr, hasOpenParenthesis)) {
            hasOpenParenthesis = appendCloseParenthesis(whereClause);
        }
        return hasOpenParenthesis;
    }

    private static boolean checkIfHasOpenParenthesis(final ComparisonHandler<?> next, final boolean currentIsOr, boolean hasOpenParenthesis) {
        if (currentIsOr && next != null)
            return true;
        return hasOpenParenthesis && (currentIsOr || next == null);
    }

    private static boolean appendCloseParenthesis(StringBuilder whereClause) {
        whereClause.append(")");
        return false;
    }


}

