package org.broken.arrow.library.database.construct.query.builder.clause;

import javax.annotation.Nonnull;
import java.util.List;

public interface ParameterSupplier {
    /**
     * Get the raw parameters set in the order is put in.
     *
     * @return An ordered list of raw parameter values for this specific clause.
     */
    @Nonnull
    List<Object> getRawParameters();
}