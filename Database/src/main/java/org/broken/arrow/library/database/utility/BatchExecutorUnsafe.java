package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.builders.tables.SqlQueryPair;
import org.broken.arrow.library.database.core.Database;
import org.broken.arrow.library.logging.Logging;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * Handles batch updates and operations on the database, is using
 * unsanitized query strings.
 *
 * @param <T> The type of data to be saved or processed.
 */
public class BatchExecutorUnsafe<T> extends BatchExecutor<T> {
    private final Logging log = new Logging(BatchExecutorUnsafe.class);
    private volatile boolean batchUpdateGoingOn;
    private volatile boolean hasStartWriteToDb;

    /**
     * Creates a new BatchExecutor instance.
     *
     * @param database      the database instance to operate on.
     * @param connection    the database connection to use.
     * @param dataToProcess the list of data to be processed in batch.
     */
    public BatchExecutorUnsafe(final Database database, final Connection connection, @Nonnull final List<T> dataToProcess) {
        super(database, connection, dataToProcess);
    }

    /**
     * Executes a list of SQL queries as batch operations against the database.
     * Commits after every 100 queries and handles rollback on errors.
     *
     * @param composerList the list of SQL query pairs to execute.
     */
    @Override
    protected void executeDatabaseTasks(List<SqlQueryPair> composerList) {
        this.batchUpdateGoingOn = true;
        Connection connection = this.connection;
        if (!this.hasStartWriteToDb)
            try (final Statement statement = connection.createStatement(this.resultSetType, this.resultSetConcurrency)) {
                this.hasStartWriteToDb = true;
                final int processedCount = dataToProcess.size();

                // Prevent automatically sending db instructions
                this.connection.setAutoCommit(false);

                for (final SqlQueryPair sql : composerList)
                    statement.addBatch(sql.getQuery());
                if (processedCount > 10_000)
                    this.printPressesCount(processedCount);

                // Set the flag to start time notifications timer
                this.batchUpdateGoingOn = true;

                // Notify console that progress still is being made
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (batchUpdateGoingOn)
                            log.log(Level.WARNING, () -> "Still executing, DO NOT SHUTDOWN YOUR SERVER.");
                        else cancel();
                    }
                }, 1000 * 30L, 1000 * 30L);
                // Execute
                statement.executeBatch();

                // This will block the thread
                connection.commit();
            } catch (final Exception t) {
                this.log.log(t, () -> "Could not execute one or several batches.");
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (final SQLException ex) {
                    this.log.log(ex, () -> "Could not set auto commit back to true.");
                } finally {
                    this.database.closeConnection(connection);
                }
                this.hasStartWriteToDb = false;
                // Even in case of failure, cancel
                this.batchUpdateGoingOn = false;
            }
    }

}
