package org.broken.arrow.library.itemcreator.serialization.typeadapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.BottleEffectMeta;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonReaderHelper;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonWriterHelper;
import org.broken.arrow.library.itemcreator.meta.potion.PotionTypeWrapper;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.IOException;

/**
 * A Gson {@link TypeAdapter} for serializing and deserializing {@link BottleEffectMeta} objects
 * to and from JSON.
 * <p>
 * Handles potion type, bottle color, upgrade/extension flags, and potion effects.
 */
public class BottleEffectMetaAdapter extends TypeAdapter<BottleEffectMeta> {
    private static final Logging logger = new Logging(BottleEffectMetaAdapter.class);

    /**
     * Serializes the {@link BottleEffectMeta} into JSON format.
     *
     * @param out   the JSON writer to output the serialized data
     * @param value the BottleEffectMeta instance to serialize
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final JsonWriter out, final BottleEffectMeta value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        final JsonWriterHelper json = new JsonWriterHelper(out);
        final String potionType = value.getPotionName();

        json.value("potion_type", potionType != null ? potionType : "");
        json.value("is_water_bottle", value.isWaterBottle());
        json.value("is_upgraded", value.isUpgraded());
        json.value("is_extended", value.isExtended());
        json.value("color", value.getColorMeta() == null ? 0 : value.getColorMeta().toRgb());
        json.forEachObject("potion_effects", value.getPotionEffects(), potionEffect -> {
            json.value("type", potionEffect.getType().getName());
            json.value("duration", potionEffect.getDuration());
            json.value("amplifier", potionEffect.getAmplifier());
            json.value("is_ambient", potionEffect.isAmbient());
            json.value("has_particles", potionEffect.hasParticles());
            if(ItemCreator.getVersion().versionNewer(12.2))
                json.value("has_icon", potionEffect.hasIcon());
        });
        json.finish();
    }

    /**
     * Deserializes a {@link BottleEffectMeta} from JSON format.
     *
     * @param in the JSON reader containing the serialized BottleEffectMeta data
     * @return the deserialized BottleEffectMeta instance
     * @throws IOException if an I/O error occurs during reading
     */
    @Override
    public BottleEffectMeta read(final JsonReader in) throws IOException {
        JsonReaderHelper json = new JsonReaderHelper(in);
        BottleEffectMeta meta = new BottleEffectMeta();
        json.forEachObjectField((name, reader) -> {
            switch (name) {
                case "potion_type":
                    final String bukkitPotionType = reader.nextString();
                    if (bukkitPotionType != null && !bukkitPotionType.isEmpty()) {
                        meta.setPotionData(PotionTypeWrapper.findPotionByName(bukkitPotionType));
                    }
                    break;
                case "is_water_bottle":
                    final boolean waterBottle = reader.nextBoolean();
                    meta.setWaterBottle(waterBottle);
                    break;
                case "is_upgraded":
                    final boolean upgraded = reader.nextBoolean();
                    meta.setUpgraded(upgraded);
                    break;
                case "is_extended":
                    final boolean extended = reader.nextBoolean();
                    meta.setExtended(extended);
                    break;
                case "color":
                    final int color = reader.nextInt();
                    if (color > 0)
                        meta.setBottleColor(colorMeta -> colorMeta.setRgb(color));
                    break;
                case "potion_effects":
                    setPotionEffects(reader, meta);
                    break;
                default:
                    reader.skipValue();
            }
        });
        json.endObject();
        return meta;
    }

    private void setPotionEffects(final JsonReaderHelper reader, final BottleEffectMeta meta) throws IOException {
        reader.forEachObjectInArray(potionReader -> {
            final PotionEffectValues effectValues = new PotionEffectValues();

            potionReader.forEachObjectField((fieldName, fieldReader) -> {
                switch (fieldName) {
                    case "type":
                        final String name = fieldReader.nextString();
                        effectValues.type = PotionEffectType.getByName(name);
                        effectValues.typeName = name;
                        break;
                    case "duration":
                        effectValues.duration = fieldReader.nextInt();
                        break;
                    case "amplifier":
                        effectValues.amplifier = fieldReader.nextInt();
                        break;
                    case "is_ambient":
                        effectValues.isAmbient = fieldReader.nextBoolean();
                        break;
                    case "has_particles":
                        effectValues.hasParticles = fieldReader.nextBoolean();
                        break;
                    case "has_icon":
                        effectValues.hasIcon = fieldReader.nextBoolean();
                        break;
                    default:
                        fieldReader.skipValue();
                }
            });
            if (effectValues.isValid()) {
                return meta.addPotionEffect(potion ->
                        potion.add(
                                effectValues.type,
                                effectValues.duration,
                                effectValues.amplifier,
                                effectValues.isAmbient,
                                effectValues.hasParticles,
                                effectValues.hasIcon
                        ));
            } else {
                logger.warn(() -> "The potion effect type is invalid, the current name is: '" + effectValues.typeName);
                return null;
            }
        });
    }


    private static class PotionEffectValues {
        private PotionEffectType type;
        private String typeName;
        private int duration;
        private int amplifier;
        private boolean isAmbient;
        private boolean hasParticles;
        private boolean hasIcon;

        /**
         * Valid if a type is present and duration is either infinite (-1) or positive.
         *
         * @return Returns {@code true} if the type and duration is -1 or more than zero.
         */
        public boolean isValid() {
            return type != null && (duration == -1 || duration > 0);
        }
    }
}
