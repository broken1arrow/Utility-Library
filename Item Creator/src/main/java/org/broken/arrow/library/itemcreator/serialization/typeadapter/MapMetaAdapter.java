package org.broken.arrow.library.itemcreator.serialization.typeadapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.broken.arrow.library.itemcreator.meta.MapWrapperMeta;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonReaderHelper;
import org.broken.arrow.library.itemcreator.serialization.jsonhelper.JsonWriterHelper;
import org.broken.arrow.library.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.map.MapView;

import java.io.IOException;
import java.util.UUID;

/**
 * A Gson {@link TypeAdapter} for serializing and deserializing {@link MapWrapperMeta} objects
 * to and from JSON.
 * <p>
 * Handles map id, world uid, center locations, is locked and more. Beside the pixels
 * is not preserved and that applies mostly to custom maps.
 */
public class MapMetaAdapter extends TypeAdapter<MapWrapperMeta> {
    private static final Logging logger = new Logging(MapMetaAdapter.class);

    /**
     * Serializes the {@link MapWrapperMeta} into JSON format.
     *
     * @param out   the JSON writer to output the serialized data
     * @param value the  MapWrapperMeta instance to serialize
     * @throws IOException if an I/O error occurs during writing
     */
    @Override
    public void write(final JsonWriter out, final MapWrapperMeta value) throws IOException {
        JsonWriterHelper json = new JsonWriterHelper(out);
        final MapView mapView = value.getMapView();
        if (mapView != null) {
            json.value("id", mapView.getId());
            json.value("world", mapView.getWorld().getUID().toString());
            json.value("center_x", mapView.getCenterX());
            json.value("center_z", mapView.getCenterZ());
            json.value("is_locked", mapView.isLocked());

            json.value("is_tracking_position", mapView.isTrackingPosition());
            json.value("is_unlimited_tracking", mapView.isUnlimitedTracking());
            json.value("is_virtual", mapView.isVirtual());
            json.value("scale", mapView.getScale().name());
        }
        json.finish();
    }

    /**
     * Deserializes a {@link  MapWrapperMeta} from JSON format.
     *
     * @param in the JSON reader containing the serialized  MapWrapperMeta data
     * @return the deserialized  MapWrapperMeta instance
     * @throws IOException if an I/O error occurs during reading
     */
    @Override
    public MapWrapperMeta read(final JsonReader in) throws IOException {
        JsonReaderHelper json = new JsonReaderHelper(in);
        MapWrapperMeta meta = new MapWrapperMeta();
        MapViewValues mapViewValues = new MapViewValues();
        json.forEachObjectField((name, reader) -> {
            switch (name) {
                case "id":
                    mapViewValues.id = reader.nextInt();
                    break;
                case "world":
                    String worldName = reader.nextString();
                    mapViewValues.world = Bukkit.getWorld(UUID.fromString(worldName));
                    break;
                case "center_x":
                    mapViewValues.centerX = reader.nextInt();
                    break;
                case "center_z":
                    mapViewValues.centerZ = reader.nextInt();
                    break;
                case "is_locked":
                    mapViewValues.locked = reader.nextBoolean();
                    break;
                case "is_tracking_position":
                    mapViewValues.trackingPosition= reader.nextBoolean();
                    break;
                case "is_unlimited_tracking":
                    mapViewValues.unlimitedTracking = reader.nextBoolean();
                    break;
                case "is_virtual":
                    mapViewValues.virtual = reader.nextBoolean();
                    break;
                case "scale":
                    mapViewValues.scale = MapView.Scale.valueOf(reader.nextString());
                    break;
                default:
                    reader.skipValue();
            }
        });
        MapView mapView = Bukkit.getMap(mapViewValues.id);
        if (mapView == null) mapView = Bukkit.createMap(mapViewValues.world);

        mapView.setWorld(mapViewValues.world);
        mapView.setCenterX(mapViewValues.centerX);
        mapView.setCenterZ(mapViewValues.centerZ);
        mapView.setScale(mapViewValues.scale);

        mapView.setLocked(mapViewValues.locked);
        mapView.setTrackingPosition(mapViewValues.trackingPosition);
        mapView.setUnlimitedTracking(mapViewValues.unlimitedTracking);

        meta.createMapView(mapView);
        json.endObject();
        return meta;
    }


    private static class MapViewValues {
        private int id;
        private World world;
        private MapView.Scale scale;
        private int centerX;
        private int centerZ;
        private boolean locked;
        private boolean trackingPosition;
        private boolean unlimitedTracking;
        private boolean virtual;

        /**
         * Valid if a world is present.
         *
         * @return Returns {@code true} if the world is not null.
         */
        public boolean isValid() {
            return world != null;
        }
    }
}
