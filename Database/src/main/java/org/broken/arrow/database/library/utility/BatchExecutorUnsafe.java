package org.broken.arrow.database.library.utility;

import org.broken.arrow.database.library.Database;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.logging.library.Logging;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.logging.Level;

import static org.broken.arrow.logging.library.Logging.of;

public class BatchExecutorUnsafe extends BatchExecutor {
    private final Logging log = new Logging(BatchExecutorUnsafe.class);
    private volatile boolean batchUpdateGoingOn;
    private volatile boolean hasStartWriteToDb;

    public BatchExecutorUnsafe(final Database database, final Connection connection, final Consumer<BatchData> data) {
        super(database, connection, data);
    }

    @Override
    public void executeDatabaseTask() {
        this.batchUpdateGoingOn = true;
        if (!this.hasStartWriteToDb)
            try (final Statement statement = this.connection.createStatement(this.resultSetType, this.resultSetConcurrency)) {
                this.hasStartWriteToDb = true;
                final int processedCount = batchList.size();

                // Prevent automatically sending db instructions
                this.connection.setAutoCommit(false);

                for (final SqlCommandComposer sql : batchList)
                    statement.addBatch(sql.getQueryCommand());
                if (processedCount > 10_000)
                    this.printPressesCount(processedCount);

                // Set the flag to start time notifications timer
                this.batchUpdateGoingOn = true;

                // Notify console that progress still is being made
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (batchUpdateGoingOn)
                            log.log(Level.WARNING, () -> of("Still executing, DO NOT SHUTDOWN YOUR SERVER."));
                        else cancel();
                    }
                }, 1000 * 30L, 1000 * 30L);
                // Execute
                statement.executeBatch();

                // This will block the thread
                this.connection.commit();

            } catch (final Exception t) {
                this.log.log(t, () -> of("Could not execute one or several batches."));
            } finally {
                try {
                    this.connection.setAutoCommit(true);
                } catch (final SQLException ex) {
                    this.log.log(ex, () -> of("Could not set auto commit back to true."));
                } finally {
                    this.database.closeConnection(this.connection);
                }
                this.hasStartWriteToDb = false;
                // Even in case of failure, cancel
                this.batchUpdateGoingOn = false;
            }
    }

}
