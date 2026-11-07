package org.broken.arrow.library.database.utility.checker;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Read-only view of the composite keys matching rows in a database table.
 *
 */
public interface ExistingRowKeysView {


    /**
     * Checks whether the supplied {@link RowContext} represents an existing row.
     *
     * @param context the context containing column context mappings
     * @return {@code true} if the row exists in the target table, {@code false} otherwise
     */
    boolean contains(@Nonnull final RowContext context);

    /**
     * Checks whether the supplied {@link CompositeKey} key exists in the result.
     *
     * @param compositeKey the row key identifier to check
     * @return {@code true} if the key identifier exists; {@code false} otherwise
     */
    boolean contains(@Nonnull final CompositeKey compositeKey);

    /**
     * Returns an immutable snapshot of all identified database keys.
     *
     * @return an unmodifiable list of matched row key identifiers
     */
    List<CompositeKey> getCompositeKeys();

    /**
     * Returns the total count of matched records found in the database.
     *
     * @return the number of identified row keys
     */
    int size();

    /**
     * Checks whether any matching rows were found.
     *
     * @return {@code true} if no matching rows were found, {@code false} otherwise
     */
    boolean isEmpty();
}