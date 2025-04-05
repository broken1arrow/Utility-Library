package org.broken.arrow.database.library.utility;

import org.broken.arrow.database.library.core.Database;
import org.broken.arrow.database.library.builders.DataWrapper;
import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;
import org.broken.arrow.database.library.builders.tables.SqlQueryPair;
import org.broken.arrow.logging.library.Logging;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import static org.broken.arrow.logging.library.Logging.of;

public class BatchExecutorUnsafe extends BatchExecutor {
    private final Logging log = new Logging(BatchExecutorUnsafe.class);
    private volatile boolean batchUpdateGoingOn;
    private volatile boolean hasStartWriteToDb;

    public BatchExecutorUnsafe(final Database database, final Connection connection, @Nonnull final List<DataWrapper> dataWrapperList) {
        super(database, connection, dataWrapperList);
    }

    @Override
    protected void executeDatabaseTasks(List<SqlQueryPair> composerList) {
        this.batchUpdateGoingOn = true;
        Connection connection = this.connection;
        if (!this.hasStartWriteToDb)
            try (final Statement statement = connection.createStatement(this.resultSetType, this.resultSetConcurrency)) {
                this.hasStartWriteToDb = true;
                final int processedCount = dataWrapperList.size();

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
                            log.log(Level.WARNING, () -> of("Still executing, DO NOT SHUTDOWN YOUR SERVER."));
                        else cancel();
                    }
                }, 1000 * 30L, 1000 * 30L);
                // Execute
                statement.executeBatch();

                // This will block the thread
                connection.commit();
            } catch (final Exception t) {
                this.log.log(t, () -> of("Could not execute one or several batches."));
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (final SQLException ex) {
                    this.log.log(ex, () -> of("Could not set auto commit back to true."));
                } finally {
                    this.database.closeConnection(connection);
                }
                this.hasStartWriteToDb = false;
                // Even in case of failure, cancel
                this.batchUpdateGoingOn = false;
            }
    }

    @Override
    public void executeDatabaseTask(List<SqlCommandComposer> composerList) {
        this.batchUpdateGoingOn = true;
        Connection connection = this.connection;
        if (!this.hasStartWriteToDb)
            try (final Statement statement = connection.createStatement(this.resultSetType, this.resultSetConcurrency)) {
                this.hasStartWriteToDb = true;
                final int processedCount = dataWrapperList.size();

                // Prevent automatically sending db instructions
                this.connection.setAutoCommit(false);

                for (final SqlCommandComposer sql : composerList)
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
                connection.commit();
            } catch (final Exception t) {
                this.log.log(t, () -> of("Could not execute one or several batches."));
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (final SQLException ex) {
                    this.log.log(ex, () -> of("Could not set auto commit back to true."));
                } finally {
                    this.database.closeConnection(connection);
                }
                this.hasStartWriteToDb = false;
                // Even in case of failure, cancel
                this.batchUpdateGoingOn = false;
            }
    }

}
