package org.broken.arrow.library.itemcreator.serialization.typeadapter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.broken.arrow.library.itemcreator.meta.BannerMeta;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonReaderHelper;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonWriterHelper;
import org.broken.arrow.library.itemcreator.utility.PatternTypeUtil;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class BannerMetaAdapter extends BaseTypeAdapter<BannerMeta> {
    private static final Logging logger = new Logging(BannerMetaAdapter.class);

    @Override
    protected void checkedWrite(final JsonWriter out, @Nonnull final BannerMeta value) throws IOException {
        final JsonWriterHelper json = new JsonWriterHelper(out);
        final List<Pattern> patterns = value.getPatterns();
        final DyeColor bannerBaseColor = value.getBannerBaseColor();

        if (bannerBaseColor != null)
            json.value("banner_base_color", bannerBaseColor.getColor().asRGB());

        json.forEachObject("patterns", patterns, pattern -> {
            json.value("color", pattern.getColor().getColor().asRGB());
            json.value("pattern", PatternTypeUtil.toId(pattern.getPattern()));
        });
        json.finish();
    }

    @Override
    public BannerMeta read(final JsonReader jsonReader) throws IOException {
        JsonReaderHelper json = new JsonReaderHelper(jsonReader);
        BannerMeta meta = new BannerMeta();
        json.forEachObjectField((name, reader) -> {
            switch (name) {
                case "banner_base_color":
                    final int baseColor = reader.nextInt();
                    if (baseColor > 0) {
                        meta.setBannerBaseColor(DyeColor.getByColor(Color.fromRGB(baseColor)));
                    }
                    break;
                case "patterns":
                    setBannerPatterns(reader, meta);
                    break;
                default:
                    reader.skipValue();
            }
        });
        json.endObject();
        return meta;
    }

    private void setBannerPatterns(final JsonReaderHelper reader, final BannerMeta meta) throws IOException {
        reader.forEachObjectInArray(potionReader -> {
            PatternWarper patternWarper = new PatternWarper();
            potionReader.forEachObjectField((fieldName, fieldReader) -> {
                switch (fieldName) {
                    case "color":
                        final int color = reader.nextInt();
                        if (color > 0) {
                            patternWarper.setColor(color);
                        }
                        break;
                    case "pattern":
                        final String pattern = reader.nextString();
                        patternWarper.setPattern(pattern);
                        break;
                    default:
                        fieldReader.skipValue();
                }
            });
            if (patternWarper.isValid()) {
                return meta.addPatterns(patternWarper.getPattern());
            } else {
                logger.warn(() -> "The pattern or color is invalid, the current pattern is: '" + patternWarper +  "'.");
                return null;
            }
        });
    }


    private static class PatternWarper {
        private DyeColor color;
        private PatternType pattern;

        public void setColor(final int color) {
            this.color = DyeColor.getByColor(Color.fromRGB(color));
        }

        public void setPattern(final String pattern) {
            this.pattern = PatternTypeUtil.fromString(pattern);
        }

        public Pattern getPattern() {
            return new Pattern(color, pattern);
        }

        public boolean isValid() {
            return color != null && pattern != null;
        }

        @Override
        public String toString() {
            return "PatternWarper{" +
                    "color=" + color +
                    ", pattern=" + pattern +
                    '}';
        }
    }
}
