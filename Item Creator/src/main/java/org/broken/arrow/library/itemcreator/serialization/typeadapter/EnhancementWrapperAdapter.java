package org.broken.arrow.library.itemcreator.serialization.typeadapter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.enhancement.EnhancementWrapper;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonReaderHelper;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonWriterHelper;
import org.bukkit.enchantments.Enchantment;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class EnhancementWrapperAdapter extends BaseTypeAdapter<EnhancementWrapper> {

    @Override
    protected void checkedWrite(final JsonWriter out, @Nonnull final EnhancementWrapper value) throws IOException {
        final JsonWriterHelper json = new JsonWriterHelper(out);

        final Enchantment enchantment = value.getEnchantment();
        if (ItemCreator.getServerVersion() > 12.2F) {
            json.value("name", enchantment.getKey().getKey());
        } else {
            json.value("name", enchantment.getName());
        }
        json.value("level", value.getLevel());
        json.value("ignore_level", value.isIgnoreLevelRestriction());

        json.finish();
    }

    @Override
    public EnhancementWrapper read(final JsonReader jsonReader) throws IOException {
        JsonReaderHelper json = new JsonReaderHelper(jsonReader);
        final AtomicReference<EnhancementWrapper> enhancementWrapper = new AtomicReference<>();
        json.forEachObjectField((name, reader) -> {
            EnchantmentData enchantmentData = new EnchantmentData();
            switch (name) {
                case "name":
                    final String enchantmentName = reader.nextString();
                    if (enchantmentName != null) {
                        enchantmentData.setEnhancementName(enchantmentName);
                    }
                    break;
                case "level": {
                    final int level = reader.nextInt();
                    if (level > 0) {
                        enchantmentData.setLevel(level);
                    }
                    break;
                }
                case "ignore_level": {
                    final boolean level = reader.nextBoolean();
                    enchantmentData.setIgnoreLevelRestriction(level);
                    break;
                }
                default:
                    reader.skipValue();
                    break;
            }
            final Enchantment enchantment = enchantmentData.getEnchantment();
            if (enchantment != null) {
                enhancementWrapper.set(new EnhancementWrapper(
                        enchantment,
                        enchantmentData.getLevel(),
                        enchantmentData.isIgnoreLevelRestriction()));
            }
        });
        json.endObject();
        return enhancementWrapper.get();
    }


    private static class EnchantmentData {
        private String enhancementName;
        private int level;
        private boolean ignoreLevelRestriction;

        public void setEnhancementName(@Nonnull final String enhancementName) {
            this.enhancementName = enhancementName;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(final int level) {
            this.level = level;
        }

        public boolean isIgnoreLevelRestriction() {
            return ignoreLevelRestriction;
        }

        public void setIgnoreLevelRestriction(final boolean ignoreLevelRestriction) {
            this.ignoreLevelRestriction = ignoreLevelRestriction;
        }

        public Enchantment getEnchantment() {
            if (enhancementName == null) return null;
            return ItemCreator.getEnchantment(enhancementName);
        }
    }

}
