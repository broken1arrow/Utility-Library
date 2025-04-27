package org.broken.arrow.database.library.construct.query.builder;

import org.broken.arrow.database.library.construct.query.QueryBuilder;
import org.broken.arrow.database.library.construct.query.builder.joinbuilder.JoinCondition;
import org.broken.arrow.database.library.construct.query.builder.joinbuilder.JoinType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JoinBuilder {
    private final List<JoinCondition> joins = new ArrayList<>();

    public JoinBuilder(QueryBuilder queryBuilder) {

    }

    public void join(JoinType type, String table, String alias, String onCondition) {
        joins.add(new JoinCondition(type, table, alias, onCondition, false));
    }

    public void oldJoin(String table) {
        this.oldJoin(table, null);
    }

    public void oldJoin(String table, String alias) {
        joins.add(new JoinCondition(null, table, alias, null, true));
    }

    public String build() {
        return joins.isEmpty() ? "" : joins.stream().map(JoinCondition::toString).collect(Collectors.joining(" "));
    }

    public boolean hasOldJoins() {
        return joins.stream().anyMatch(JoinCondition::isOldStyle);
    }
}
