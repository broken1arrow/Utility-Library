package org.broken.arrow.library.database.utility.checker;

import org.broken.arrow.library.database.builders.DataWrapper;
import org.broken.arrow.library.database.builders.WriteContext;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the set of matching composite key identifiers returned from the database check.
 */
public final class ExistingRowKeys implements ExistingRowKeysView {
    private final List<String> matchKeys;
    private final Set<CompositeKey> compositeKeys = new LinkedHashSet<>();

    /**
     * Private constructor to instantiate the identifier holder with matching key column names.
     *
     * @param matchKeys the list of matching column names
     */
    ExistingRowKeys(@Nonnull final List<String> matchKeys) {
        this.matchKeys = Collections.unmodifiableList(new ArrayList<>(matchKeys));
    }

    /**
     * Adds a matched composite row key identifier to the set.
     *
     * @param compositeKey the matched row key identifier
     * @return this instance for method chaining
     */
    ExistingRowKeys add(@Nonnull final CompositeKey compositeKey) {
        compositeKeys.add(compositeKey);
        return this;
    }

    @Override
    public boolean contains(@Nonnull final DataWrapper dataWrapper) {
        Validate.checkNotNull(dataWrapper, "DataWrapper cannot be null.");
        return this.contains(dataWrapper.getWriteContext());
    }

    @Override
    public boolean contains(@Nonnull final WriteContext writeContext) {
        Validate.checkNotNull(writeContext, "WriteContext cannot be null.");
        return this.contains(writeContext.getColumnContext());
    }

    @Override
    public boolean contains(@Nonnull final CompositeKey compositeKey) {
        return compositeKeys.contains(compositeKey);
    }

    @Override
    public List<CompositeKey> getCompositeKeys() {
        return Collections.unmodifiableList(new ArrayList<>(compositeKeys));
    }

    @Override
    public int size() {
        return compositeKeys.size();
    }

    @Override
    public boolean isEmpty() {
        return compositeKeys.isEmpty();
    }

    @Override
    public String toString() {
        return "ExistingRowKeys{" +
                "compositeKeys=" + compositeKeys +
                '}';
    }

    /**
     * Checks whether the supplied column map contains an existing composite key.
     *
     * @param columnContext the map of column names to values
     * @return {@code true} if the composite key matches an existing record; {@code false} otherwise
     * @throws Validate.ValidateExceptions if the supplied column map is empty
     */
    private boolean contains(@Nonnull final Map<String, Object> columnContext) {
        Validate.checkNotNull(columnContext, "You must provide a map instance, not null.");
        if (columnContext.isEmpty()) {
            throw new Validate.ValidateExceptions("Supplied WriteContext/DataWrapper returned an empty columnContext. Match keys " + this.matchKeys + " can't be verified.");
        }

        final Map<String, Object> keyValues = new LinkedHashMap<>(this.matchKeys.size());
        for (String key : matchKeys) {
            if (!columnContext.containsKey(key)) {
                return false;
            }
            keyValues.put(key, columnContext.get(key));
        }
        return this.contains(new CompositeKey(keyValues));
    }
}