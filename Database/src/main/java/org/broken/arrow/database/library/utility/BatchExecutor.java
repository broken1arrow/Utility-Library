package org.broken.arrow.database.library.utility;

import org.broken.arrow.database.library.Database;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.logging.library.Logging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.logging.Level;

import static org.broken.arrow.logging.library.Logging.of;

public class BatchExecutor {
    private final Logging log = new Logging(BatchExecutor.class);
    protected final Database database;
    protected final List<SqlCommandComposer> batchList;
    protected final Connection connection;
    protected final int resultSetType;
    protected final int resultSetConcurrency;
    private volatile boolean batchUpdateGoingOn;

    public BatchExecutor(final Database database, final Connection connection, final Consumer<BatchData> data) {
        this.database = database;
        this.connection = connection;
        BatchData batchData = new BatchData();
        data.accept(batchData);
        this.batchList = batchData.batchList;
        this.resultSetType = batchData.resultSetType;
        this.resultSetConcurrency = batchData.resultSetConcurrency;
    }


    public void executeDatabaseTask() {
        batchUpdateGoingOn = true;
        final int processedCount = this.batchList.size();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (batchUpdateGoingOn) log.log(() -> of("Still executing, DO NOT SHUTDOWN YOUR SERVER."));
                else cancel();
            }
        }, 1000 * 30L, 1000 * 30L);
        if (processedCount > 10_000)
            this.printPressesCount(processedCount);
        try {
            this.connection.setAutoCommit(false);
            int batchSize = 1;
            for (SqlCommandComposer sql : batchList) {
                this.setPreparedStatement(sql);

                if (batchSize % 100 == 0)
                    connection.commit();
                batchSize++;
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, e, () -> of("Could not set auto commit to false."));
            this.batchUpdateGoingOn = false;
        } finally {
            try {
                this.connection.commit();
                this.connection.setAutoCommit(true);
            } catch (final SQLException ex) {
                log.log(Level.WARNING, ex, () -> of("Could not set auto commit to true or commit last changes."));
            } finally {
                this.database.closeConnection(connection);
                this.batchUpdateGoingOn = false;
            }
        }
    }

    private void setPreparedStatement(SqlCommandComposer sql) throws SQLException {

        Map<Integer, Object> cachedDataByColumn = sql.getCachedDataByColumn();
        try (PreparedStatement statement = connection.prepareStatement(sql.getPreparedSQLBatch(), resultSetType, resultSetConcurrency)) {
            boolean valuesSet = false;

            if (!cachedDataByColumn.isEmpty()) {
                for (Map.Entry<Integer, Object> column : cachedDataByColumn.entrySet()) {
                    statement.setObject(column.getKey(), column.getValue());
                    valuesSet = true;
                }
            }
            if (valuesSet)
                statement.addBatch();
            statement.executeBatch();
        } catch (SQLException e) {
            log.log(Level.WARNING, () -> of("Could not execute this prepared batch: \"" + sql.getPreparedSQLBatch() + "\""));
            log.log(e, () -> of("Values that could not be executed: '" + cachedDataByColumn.values() + "'"));
            connection.commit();
        }
    }

    public void printPressesCount(int processedCount) {
        if (processedCount > 10_000)
            log.log(() -> of(("Updating your database (" + processedCount + " entries)... PLEASE BE PATIENT THIS WILL TAKE " + (processedCount > 50_000 ? "10-20 MINUTES" : "5-10 MINUTES") + " - If server will print a crash report, ignore it, update will proceed.")));
    }

    public static class BatchData {
        private List<SqlCommandComposer> batchList = new ArrayList<>();
        private int resultSetType;
        private int resultSetConcurrency;

        public void setBatchList(List<SqlCommandComposer> batchList) {
            this.batchList = batchList;
        }

        public void setResultSetType(int resultSetType) {
            this.resultSetType = resultSetType;
        }

        public void setResultSetConcurrency(int resultSetConcurrency) {
            this.resultSetConcurrency = resultSetConcurrency;
        }
    }
}
