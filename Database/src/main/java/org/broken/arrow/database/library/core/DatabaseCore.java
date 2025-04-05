package org.broken.arrow.database.library.core;

import org.broken.arrow.database.library.builders.DataWrapper;

import javax.annotation.Nonnull;
import java.util.List;

public interface  DatabaseCore {


    void saveAll(@Nonnull final String tableName, @Nonnull final List<DataWrapper> dataWrapperList, final boolean shallUpdate, String... columns);

}
