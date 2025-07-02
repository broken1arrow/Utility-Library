package org.broken.arrow.library.database.utility;

import org.broken.arrow.library.database.builders.tables.SqlCommandComposer;
@FunctionalInterface
public interface ConfigConsumer {

    void apply(final SqlCommandComposer commandComposer, final Object primaryKeyValue, final boolean rowExist);

}
