package org.broken.arrow.database.library.utility;

import org.broken.arrow.database.library.builders.tables.SqlCommandComposer;

public interface ConfigConsumer {

    void apply(final SqlCommandComposer commandComposer, final Object primaryKeyValue, final boolean rowExist);

}
