package org.broken.arrow.database.library.builders.wrappers;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;

import javax.annotation.Nonnull;

public class DatabaseSettingsSave<V extends ConfigurationSerializable> extends DatabaseSettings {


    public DatabaseSettingsSave(@Nonnull final String tableName) {
        super(tableName);

    }



}
