package org.broken.arrow.database.library;

import org.broken.arrow.database.library.builders.tables.SqlHandler;
import org.broken.arrow.database.library.builders.tables.SqlQueryPair;
import org.broken.arrow.database.library.builders.tables.SqlQueryTable;
import org.broken.arrow.database.library.construct.query.builder.insertbuilder.InsertBuilder;
import org.broken.arrow.database.library.construct.query.builder.tablebuilder.SQLConstraints;
import org.broken.arrow.database.library.construct.query.columnbuilder.ColumnManger;
import org.broken.arrow.database.library.construct.query.utlity.CalcFunc;
import org.broken.arrow.database.library.construct.query.utlity.DataType;
import org.broken.arrow.database.library.construct.query.utlity.MathOperation;
import org.broken.arrow.database.library.utility.DatabaseCommandConfig;

import javax.annotation.Nonnull;
import java.sql.Connection;

public class MainTestingSQL {


    public static void main(String[] args) {

        SqlQueryTable table = new SqlQueryTable(queryBuilder ->
                queryBuilder.createTableIfNotExists("table").addColumns(
                        ColumnManger.tableOf("test", DataType.VARCHAR(50), SQLConstraints.PRIMARY_KEY())
                                .column("IS_primary", DataType.VARCHAR(80), SQLConstraints.PRIMARY_KEY())
                                .column("not_null", DataType.VARCHAR(80), SQLConstraints.NOT_NULL())
                                .build()));

        SqlHandler sqlHandler = getSqlHandler(table);

        SqlQueryPair insert = sqlHandler.insertIntoTable(insertHandler -> {
            insertHandler.addAll(
                    InsertBuilder.of("test", 888),
                    InsertBuilder.of("new", 7),
                    InsertBuilder.of("hu", 7859)
            );});

        SqlQueryPair update = sqlHandler.updateTable(updateBuilder -> {
            updateBuilder.put("test", 8);
            updateBuilder.put("something", "testings_");
        }, whereBuilder -> whereBuilder
                .where("test_where").equal("seme").and()
                .where("ho").between(5, 7)
                .or().where("nooo").equal("yes")
                .build());

        SqlQueryPair select = sqlHandler.selectRow(updateBuilder -> {
            updateBuilder.column("first").withAggregation(MathOperation.ADD, CalcFunc.AVG).colum("second_one").getColumn();

        }, whereBuilder -> whereBuilder
                .where("test_where").equal("seme").and()
                .where("ho").between(5, 7)
                .or().where("nooo").equal("yes")
                .build());

        SqlQueryPair select2 = sqlHandler.selectRow(columnManger -> columnManger.addAll(table.getTable().getColumns()),
                table.createWhereClauseFromPrimaryColumns(true, 8, "nooo", "is too much"));
        System.out.println("insert= " + insert);
        System.out.println("update= " + update);
        System.out.println("select= " + select);
        System.out.println("select2= " + select2);
        System.out.println("table.getTable().getColumns()= " + table.getTable().getColumns());
        System.out.println("table PrimaryColumns=" + table.createWhereClauseFromPrimaryColumns(false, 8, "nooo", "is too much").build());
    }

    @Nonnull
    private static SqlHandler getSqlHandler(SqlQueryTable table) {
        return new SqlHandler(table.getTableName(), new Database(null) {
            @Override
            public Connection connect() {
                return null;
            }

            @Override
            public boolean usingHikari() {
                return false;
            }

            @Override
            public boolean isHasCastException() {
                return false;
            }

            @Nonnull
            @Override
            public DatabaseCommandConfig databaseConfig() {
                return new DatabaseCommandConfig(1, 1);
            }
        });
    }
}
