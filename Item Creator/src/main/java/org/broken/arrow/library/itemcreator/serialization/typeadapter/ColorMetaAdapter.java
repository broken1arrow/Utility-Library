package org.broken.arrow.library.itemcreator.serialization.typeadapter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.broken.arrow.library.itemcreator.meta.ColorMeta;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonReaderHelper;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonWriterHelper;


import javax.annotation.Nonnull;
import java.io.IOException;

public class ColorMetaAdapter extends BaseTypeAdapter<ColorMeta> {

    @Override
    protected void checkedWrite(final JsonWriter out, @Nonnull final ColorMeta value) throws IOException {
        final JsonWriterHelper json = new JsonWriterHelper(out);
        json.value("color", value.toRgb());
    }

    @Override
    public ColorMeta read(final JsonReader jsonReader) throws IOException {
        JsonReaderHelper json = new JsonReaderHelper(jsonReader);
        ColorMeta colorMeta = new ColorMeta();
        json.forEachObjectField((name, reader) -> {
            if (name.equals("color")) {
                final int baseColor = reader.nextInt();
                if (baseColor > 0) {
                    colorMeta.setRgb(baseColor);
                }
            } else {
                reader.skipValue();
            }
        });
        json.endObject();
        return colorMeta;
    }
}
