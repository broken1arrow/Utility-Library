package org.broken.arrow.library.itemcreator.serialization.typeadapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

import javax.annotation.Nonnull;
import java.io.IOException;

public abstract class BaseTypeAdapter<T> extends TypeAdapter<T> {

    @Override
    public final void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            out.nullValue(); // central null handling
            return;
        }
        checkedWrite(out, value);
    }

    /**
     * Adapter implementations override this method instead of write().
     * The null check is already handled.
     */
    protected abstract void checkedWrite(JsonWriter out,@Nonnull T value) throws IOException;
}