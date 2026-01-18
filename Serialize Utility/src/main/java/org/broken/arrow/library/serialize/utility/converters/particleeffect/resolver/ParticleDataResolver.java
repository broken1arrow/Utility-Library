package org.broken.arrow.library.serialize.utility.converters.particleeffect.resolver;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.serialize.utility.converters.SpigotBlockFace;
import org.broken.arrow.library.serialize.utility.converters.particleeffect.PotionsData;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Resolves and stores particle-related data based on a single input object.
 * <p>
 * This class acts as a lightweight data resolver for particle parameters used
 * by the Bukkit/Spigot particle system. It inspects the provided object and
 * extracts any compatible particle data types such as materials, block data,
 * numeric type markers, block faces, or potion-related data.
 * </p>
 *
 * <p>
 * Only the data types that can be resolved from the input will be stored.
 * All unresolved or unsupported types remain {@code null}.
 * </p>
 *
 * <p>
 * This class does not perform validation of particle compatibility; it merely
 * resolves and exposes data in a type-safe way for later use.
 * </p>
 */
public class ParticleDataResolver implements ConfigurationSerializable {
    private static final Logging logger = new Logging(ParticleDataResolver.class);
    private Material material;
    private Class<? extends MaterialData> materialData;
    private BlockData blockData;
    private BlockFace blockFace;
    private Integer integerData;
    private Float floatData;
    private PotionsData potionsData;

    /**
     * Creates a new resolver for the given particle data object.
     *
     * <p>
     * The constructor attempts to resolve the supplied object into one or more
     * supported particle data representations. Supported types include:
     * </p>
     *
     * <ul>
     *     <li>{@link Material}</li>
     *     <li>{@link MaterialData}</li>
     *     <li>{@link BlockData}</li>
     *     <li>{@link BlockFace}</li>
     *     <li>{@link Integer}</li>
     *     <li>{@link Float}</li>
     *     <li>Potion-related data (via {@link PotionsData})</li>
     * </ul>
     *
     * @param particleData the raw particle data object to resolve
     */
    public ParticleDataResolver(final Object particleData) {
        if (particleData instanceof Material)
            this.material = (Material) particleData;
        if (particleData instanceof MaterialData)
            this.materialData = (Class<? extends MaterialData>) particleData;
        if (particleData instanceof BlockData)
            this.blockData = (BlockData) particleData;
        if (particleData instanceof BlockFace)
            this.blockFace = (BlockFace) particleData;

        if (particleData instanceof Number) {
            final Number data = (Number) particleData;
            if (data instanceof Integer)
                this.integerData = (Integer) data;
            if (data instanceof Float)
                this.floatData = (Float) data;
        }

        PotionsData potionsData = new PotionsData(particleData);
        if (potionsData.isPotion())
            this.potionsData = potionsData;
    }

    /**
     * Returns the resolved {@link Material}, if present.
     *
     * @return the material, or {@code null} if not resolved
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns the resolved {@link MaterialData} class, if present.
     *
     * @return the material data class, or {@code null} if not resolved
     */
    public Class<? extends MaterialData> getMaterialData() {
        return materialData;
    }

    /**
     * Returns the resolved {@link BlockData}, if present.
     *
     * @return the block data, or {@code null} if not resolved
     */
    public BlockData getBlockData() {
        return blockData;
    }

    /**
     * Returns the resolved {@link BlockFace}, if present.
     *
     * @return the block face, or {@code null} if not resolved
     */
    public BlockFace getBlockFace() {
        return blockFace;
    }

    /**
     * Returns the resolved integer particle data type.
     * <p>
     * Note: This value acts only as a type indicator for legacy particle handling
     * and does not represent a meaningful numeric value.
     * </p>
     *
     * @return the integer data type, or {@code null} if not applicable
     */
    public Integer getIntegerData() {
        return integerData;
    }

    /**
     * Returns the resolved float particle data type.
     * <p>
     * Note: This value acts only as a type indicator for legacy particle handling
     * and does not represent a meaningful numeric value.
     * </p>
     *
     * @return the float data type, or {@code null} if not applicable
     */
    public Float getFloatData() {
        return floatData;
    }

    /**
     * Returns the resolved potion-related particle data, if present.
     *
     * @return the potion data, or {@code null} if not resolved
     */
    public PotionsData getPotionsData() {
        return potionsData;
    }

    /**
     * Returns the resolved particle data matching the given type.
     *
     * <p>
     * This method allows dynamic lookup of resolved data by class type.
     * If the requested type was not resolved, {@code null} is returned.
     * </p>
     *
     * @param dataType the expected particle data type
     * @return the resolved data matching the type, or {@code null}
     */
    public Object compute(@Nonnull final Class<?> dataType) {
        if (dataType == Material.class)
            return getMaterial();
        if (dataType == MaterialData.class)
            return getMaterialData();
        if (dataType == BlockFace.class)
            return getBlockFace();
        if (dataType == Float.class)
            return getFloatData();
        if (dataType == Integer.class)
            return getIntegerData();

        return null;
    }

    @Nonnull
    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new LinkedHashMap<>();
        if (material != null)
            map.put("material", material.name());

        if (materialData != null)
            map.put("materialData", materialData.getName());

        if (blockData != null)
            map.put("blockData", blockData.getAsString());

        if (blockFace != null)
            map.put("blockFace", blockFace.name());

        if (integerData != null)
            map.put("integer", true);

        if (floatData != null)
            map.put("float", true);

        if (potionsData != null)
            map.put("potion", potionsData.getPotion() + "");

        return map;
    }

    /**
     * Just deserialize the data.
     * 
     * @param map the map of values to be set.
     * @return new instance of ParticleDataResolver.
     */
    public static ParticleDataResolver deserialize(final Map<String, Object> map) {

        Object resolved = null;

        if (map.containsKey("material")) {
            resolved = Material.getMaterial(
                    ((String) map.get("material")).toUpperCase()
            );
        }

        if (map.containsKey("blockFace")) {
            resolved = BlockFace.valueOf(
                    ((String) map.get("blockFace")).toUpperCase()
            );
        }

        if (map.containsKey("materialData")) {
            try {
                resolved = Class.forName((String) map.get("materialData"));
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unknown MaterialData class", e);
            }
        }

        if (map.containsKey("blockData")) {
            resolved = Bukkit.createBlockData((String) map.get("blockData"));
        }

        if (map.containsKey("integer")) {
            resolved = Integer.valueOf(0);
        }

        if (map.containsKey("float")) {
            resolved = Float.valueOf(0f);
        }

        if (map.containsKey("potion")) {
            resolved = new PotionsData(map.get("potion"));
        }

        return new ParticleDataResolver(resolved);
    }

}
