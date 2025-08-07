package org.broken.arrow.library.itemcreator.serialization.typeadapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.broken.arrow.library.itemcreator.meta.FireworkMeta;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonReaderHelper;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonWriterHelper;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;

import java.io.IOException;
import java.util.List;

public class FireworkMetaAdapter extends TypeAdapter<FireworkMeta> {

    public void write(JsonWriter out, FireworkMeta value) throws IOException {
        JsonWriterHelper json = new JsonWriterHelper(out);

        json.value("fireworkPower", value.getPower());

        json.forEachObject("fireworkEffects", value.getFireworkEffects(), effect -> {
            json.value("type",effect.getType().name());
            json.value("flicker",effect.hasFlicker());
            json.value("trail",effect.hasTrail());
            json.forEach("colors", effect.getColors(), color -> out.value(color.asRGB()));
            json.forEach("fade-colors", effect.getFadeColors(), fadeColor -> out.value(fadeColor.asRGB()));
        });
        json.finish();
    }

    public FireworkMeta read(JsonReader in) throws IOException {
        JsonReaderHelper json = new JsonReaderHelper(in);
        FireworkMeta meta = new FireworkMeta();
        json.forEachObjectField((name, reader) -> {
            switch (name) {
                case "fireworkPower":
                    meta.setPower(reader.nextInt());
                    break;
                case "fireworkEffects":
                    setEffects(reader, meta);
                    break;

                default:
                    reader.skipValue();
            }
        });
        return meta;
    }

    private void setEffects(final JsonReaderHelper json, final FireworkMeta meta) throws IOException {
        List<FireworkEffect> effects = json.forEachObjectInArray(effectReader -> {
            FireworkEffect.Builder builder = FireworkEffect.builder();
            effectReader.forEachObjectField((name, reader) -> {
                switch (name) {
                    case "type":
                        builder.with(FireworkEffect.Type.valueOf(reader.nextString()));
                        break;
                    case "flicker":
                        if (reader.nextBoolean()) builder.flicker(true);
                        break;
                    case "trail":
                        if (reader.nextBoolean()) builder.trail(true);
                        break;
                    case "colors":
                    case "fade-colors":
                        List<Color> colors = reader.forEachInArray(color -> Color.fromRGB(color.nextInt()));
                        colors.forEach(builder::withColor);
                        break;
                    default:
                        reader.skipValue();
                }
            });
            return builder.build();
        });
        meta.setFireworkEffects(effects);
    }
}