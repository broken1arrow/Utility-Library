package org.broken.arrow.library.database.utility.checker;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Represents an object that provides a mapping of database column names
 * to their corresponding values.
 *
 * <p>Implementing classes can be utilized by batch processors and database
 * utilities (such as {@link RowExistenceChecker}) to extract key-value
 * contexts for SQL operations.
 */
public interface RowContext {

    /**
     * Retrieves the column-to-value context mapping for this record.
     *
     * @return a non-null map containing column names as keys and their values
     */
    @Nonnull
    Map<String, Object> getColumnContext();
}
