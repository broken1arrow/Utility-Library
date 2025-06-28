package org.broken.arrow.library.serialize.utility.converters.particleeffect;


import org.broken.arrow.library.serialize.utility.converters.SpigotBlockFace;
import org.broken.arrow.library.serialize.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.logging.library.Validate;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A simple wrapper class that provides support for serialization to file or other database.
 * This class makes it easier to work with particle effects in different Minecraft versions,
 * eliminating the need to worry about serializing the Particle class or encountering errors
 * due to missing functions in your Minecraft version.
 */
public final class ParticleEffect implements ConfigurationSerializable, ParticleEffectAccessor {

	private static final Logging logger = new Logging(ParticleEffect.class);
	private final Particle particle;
	private final Effect effect;
	private final Material material;
	private final Class<? extends MaterialData> materialData;
	private final BlockData materialBlockData;
	private final BlockFace blockFace;
	private final PotionsData potion;
	private final int count;
	private final double offsetX;
	private final double offsetY;
	private final double offsetZ;
	private final double extra;
	@Nonnull
	private final Class<?> dataType;
	private final ParticleDustOptions particleDustOptions;
	private final Builder builder;

	private ParticleEffect(final Builder builder) {
		this.particle = builder.particle;
		this.effect = builder.effect;
		this.material = builder.material;
		this.materialData = builder.materialData;
		this.materialBlockData = builder.materialBlockData;
		this.blockFace = builder.blockFace;
		this.potion = builder.potion;
		this.count = builder.count;
		this.offsetX = builder.offsetX;
		this.offsetY = builder.offsetY;
		this.offsetZ = builder.offsetZ;
		this.extra = builder.extra;
		this.dataType = builder.dataType;
		this.particleDustOptions = builder.dustOptions;
		this.builder = builder;
	}

	/**
	 * Retrieves the Particle associated with this ParticleEffect.
	 *
	 * @return the Particle object, or null if not set.
	 */
	@Override
	@Nullable
	public Particle getParticle() {
		return particle;
	}

	/**
	 * Retrieves the Effect associated with this ParticleEffect.
	 *
	 * @return the Effect object, or null if not set.
	 */
	@Override
	@Nullable
	public Effect getEffect() {
		return effect;
	}

	/**
	 * Retrieves the Material associated with this ParticleEffect.
	 *
	 * @return the Material object, or null if not set.
	 */
	@Override
	@Nullable
	public Material getMaterial() {
		return material;
	}

	/**
	 * Retrieves the Material data associated with this ParticleEffect.
	 *
	 * @return the Material data object, or null if not set.
	 */
	@Override
	@Nullable
	public Class<? extends MaterialData> getMaterialData() {
		return materialData;
	}

	/**
	 * Retrieves the Material BlockData associated with this ParticleEffect.
	 *
	 * @return the Material BlockData object, or null if not set.
	 */
	@Override
	@Nullable
	public BlockData getMaterialBlockData() {
		return materialBlockData;
	}

	/**
	 * Retrieves the block face associated with this ParticleEffect.
	 *
	 * @return the block face, or null if not set.
	 */
	@Override
	@Nullable
	public BlockFace getBlockFace() {
		return blockFace;
	}

	/**
	 * Retrieves the potion associated with this ParticleEffect.
	 *
	 * @return the Material object, or null if not set.
	 */
	@Override
	@Nullable
	public PotionsData getPotion() {
		return potion;
	}

	/**
	 * Retrieves the amount of particles associated with this ParticleEffect.
	 *
	 * @return amount of particles that should spawn at the same time.
	 */
	@Override
	public int getCount() {
		return count;
	}

	/**
	 * Retrieves the X offset
	 *
	 * @return the offset.
	 */
	@Override
	public double getOffsetX() {
		return offsetX;
	}

	/**
	 * Retrieves the X offset
	 *
	 * @return the offset.
	 */
	@Override
	public double getOffsetY() {
		return offsetY;
	}

	/**
	 * Retrieves the X offset
	 *
	 * @return the offset.
	 */
	@Override
	public double getOffsetZ() {
		return offsetZ;
	}

	/**
	 * Retrieves the data on the Particle effect.
	 *
	 * @return the data.
	 */
	@Override
	public double getExtra() {
		return extra;
	}

	/**
	 * Retrieves the data set on the effect.
	 *
	 * @return the class type for this effect or particle.
	 */
	@Override
	@Nonnull
	public Class<?> getDataType() {
		return dataType;
	}

	/**
	 * Retrieves ParticleDustOptions, but this can only be used
	 * if the Minecraft version support it.
	 *
	 * @return the ParticleDustOptions instance.
	 */
	@Override
	@Nullable
	public ParticleDustOptions getParticleDustOptions() {
		return particleDustOptions;
	}

	@Override
	public Builder getBuilder() {
		return builder;
	}

	public static class Builder {
		private final Particle particle;
		private final Effect effect;
		private Material material;
		private Class<? extends MaterialData> materialData;
		private BlockData materialBlockData;
		private BlockFace blockFace;
		private PotionsData potion;
		private int count;
		private double offsetX;
		private double offsetY;
		private double offsetZ;
		private double extra;
		@Nonnull
		private final Class<?> dataType;
		private ParticleDustOptions dustOptions;


		/**
		 * Constructs a new Builder instance for creating a particle effect with the specified effect and data type.
		 * Use this constructor when the particle is not set explicitly.
		 *
		 * @param effect   the effect particle.
		 * @param dataType the type of data this particle belongs to. Only used for certain particles,
		 *                 if the particle data is null it will be set to to Void.class.
		 */
		public Builder(final Effect effect, @Nullable final Class<?> dataType) {
			this(null, effect, dataType);
		}


		/**
		 * Constructs a new Builder instance for creating a particle effect with the specified particle and data type.
		 * Use this constructor when the effect is not set explicitly.
		 *
		 * @param particle the particle.
		 * @param dataType the type of data this particle belongs to. Only used for certain particles,
		 *                 if the particle data is null it will be set to to Void.class.
		 */
		public Builder(final Particle particle, @Nullable final Class<?> dataType) {
			this(particle, null, dataType);

		}

		/**
		 * Constructs a new Builder instance for creating a particle effect with the specified particle, effect, and data type.
		 *
		 * @param particle the particle.
		 * @param effect   the effect particle.
		 * @param dataType the type of data this particle belongs to. Only used for certain particles,
		 *                 if the particle data is null it will be set to to Void.class.
		 */
		public Builder(final Particle particle, final Effect effect, @Nullable final Class<?> dataType) {
			if (particle != null)
				Validate.checkBoolean(particle.getClass() == dataType, "You can't set dataType to same class as the particle or effect, should be '" + particle.getDataType() + "'");
			if (effect != null)
				Validate.checkBoolean(effect.getClass() == dataType, "You can't set dataType to same class as the particle or effect, should be '" + (effect.getData() == null ? "Void.class" : effect.getData()) + "'");

			this.particle = particle;
			this.effect = effect;
			this.dataType = dataType == null ? Void.class : dataType;

		}

		/**
		 * The material on the effect.
		 *
		 * @param material the material you need to set for this effect.
		 * @return this instance.
		 */
		public Builder setMaterial(final Material material) {
			this.material = material;
			return this;
		}

		/**
		 * The material data on the effect, this is used on
		 * legacy versions below 1.13.
		 *
		 * @param materialData the material data you need to set for this effect.
		 * @return this instance.
		 */
		public Builder setMaterialData(final Class<? extends MaterialData> materialData) {
			this.materialData = materialData;
			return this;
		}

		/**
		 * The material data on the effect, not used on legacy.
		 *
		 * @param materialData the block bata from the material you need to set for this effect.
		 * @return this instance.
		 */
		public Builder setMaterialBlockData(final BlockData materialData) {
			this.materialBlockData = materialData;
			return this;
		}

		/**
		 * The block face on the effect, this is used on
		 * legacy versions below 1.13.
		 *
		 * @param blockFace the block face set to this effect.
		 * @return this instance.
		 */
		public Builder setBlockFace(final BlockFace blockFace) {
			this.blockFace = blockFace;
			return this;
		}

		/**
		 * The potion on the effect, this is used on
		 * legacy versions below 1.13.
		 *
		 * @param potion the potion set to this effect.
		 * @return this instance.
		 */

		public Builder setPotion(final PotionsData potion) {
			this.potion = potion;
			return this;
		}

		/**
		 * Amount of particles you want.
		 * <p>
		 * NOTE: this can behave different depending on type of particle.
		 *
		 * @param count the amount
		 * @return this instance.
		 */
		public Builder setCount(final int count) {
			this.count = count;
			return this;
		}

		/**
		 * Set the random offset in x.
		 *
		 * @param offsetX the amount of random offset.
		 * @return this instance.
		 */
		public Builder setOffsetX(final double offsetX) {
			this.offsetX = offsetX;
			return this;
		}

		/**
		 * Set the random offset in y.
		 *
		 * @param offsetY the amount of random offset.
		 * @return this instance.
		 */
		public Builder setOffsetY(final double offsetY) {
			this.offsetY = offsetY;
			return this;
		}

		/**
		 * Set the random offset in z.
		 *
		 * @param offsetZ the amount of random offset.
		 * @return this instance.
		 */
		public Builder setOffsetZ(final double offsetZ) {
			this.offsetZ = offsetZ;
			return this;
		}

		/**
		 * Set extra data on the particle. On redstone effect this tell the size of the effect
		 * and other effects could it be the speed or something else. It also depends on the
		 * Minecraft version.
		 *
		 * @param extra the extra data you want to set.
		 * @return this instance.
		 */
		public Builder setExtra(final double extra) {
			this.extra = extra;
			return this;
		}

		/**
		 * If the Minecraft version support it, you can add doing to
		 * redstone effect and gradients.
		 *
		 * @param dustOptions the dustOptions class.
		 * @return this instance.
		 */
		public Builder setDustOptions(final ParticleDustOptions dustOptions) {
			this.dustOptions = dustOptions;
			return this;
		}


		public ParticleEffect build() {
			return new ParticleEffect(this);
		}
	}

	@Override
	public String toString() {
		return "ParticleEffect{" +
				"particle=" + particle +
				", effect=" + effect +
				", material=" + material +
				", materialData=" + materialData +
				", materialBlockData=" + materialBlockData +
				", blockFace=" + blockFace +
				", potion=" + potion +
				", count=" + count +
				", offsetX=" + offsetX +
				", offsetY=" + offsetY +
				", offsetZ=" + offsetZ +
				", extra=" + extra +
				", dataType=" + dataType +
				", particleDustOptions=" + particleDustOptions +
				", builder=" + builder +
				'}';
	}

	/**
	 * Converts the ParticleEffect object into a serialized Map representation.
	 *
	 * @return a Map containing the serialized data.
	 */
	@Nonnull
	@Override
	public Map<String, Object> serialize() {
		final Map<String, Object> particleData = new LinkedHashMap<>();
		particleData.put("Particle", this.particle + "");
		particleData.put("Effect", this.effect + "");
		particleData.put("Material", this.material + "");
		particleData.put("Block_face", this.blockFace + "");
		particleData.put("Potion", this.potion + "");
		particleData.put("Data", this.extra);
		final ParticleDustOptions dustOptions = this.particleDustOptions;
		if (dustOptions != null) {
			if (dustOptions.getToColor() != null) {
				particleData.put("Transition", new ParticleDustOptions(dustOptions.getFromColor(), dustOptions.getToColor(), dustOptions.getSize()));
			} else {
				particleData.put("DustOptions", new ParticleDustOptions(dustOptions.getFromColor(), dustOptions.getSize()));
			}

		}
		return particleData;
	}

	/**
	 * Deserializes a ParticleEffect object from the given Map representation.
	 *
	 * @param map the Map containing the serialized data.
	 * @return a deserialized ParticleEffect object.
	 */
	public static ParticleEffect deserialize(final Map<String, Object> map) {

		final Particle particle = ConvertParticlesUtility.getParticle((String) map.get("Particle"));
		final Effect effect = ConvertParticlesUtility.getEffect((String) map.get("Effect"));
		String materialString = (String) map.get("Material");
		String blockFace = (String) map.get("Block_face");
		int potion = (int) map.getOrDefault("Potion_number", -1);
		Material material = null;
		if (materialString != null) {
			materialString = materialString.toUpperCase();
			material = Material.getMaterial(materialString);
		}
		final int data = (int) map.get("Data");
		Class<?> dataType = null;
		if (particle != null)
			dataType = particle.getDataType();
		if (effect != null)
			dataType = effect.getData();
		if (dataType == null)
			dataType = Void.class;

		ParticleDustOptions options = (ParticleDustOptions) map.get("DustOptions");

		final Builder builder = new Builder(particle, effect, dataType);
		if (options == null)
			options = (ParticleDustOptions) map.get("Transition");
		setMaterial(material, dataType, builder);

		final Class<?> finalDataType = dataType;
		if (material == null && dataType.isInstance(Material.class)) {
			logger.log(Level.WARNING, () -> "you have to set the material for this particle effect, this particle use this class '" + finalDataType + "' .");
		}
		if (blockFace == null && dataType.isInstance(BlockFace.class)) {
			logger.log(Level.WARNING, () ->"you have to set the block face for this particle effect, from this class '" + finalDataType + "' .");
		}
		PotionsData potionsData = new PotionsData(null,potion);
		if (potion == -1 && potionsData.checkDataType(dataType)) {
			logger.log(Level.WARNING, () ->"you have to set the potion number for this particle effect, from this class '" + finalDataType + "' .");
		}
		BlockFace facing = SpigotBlockFace.getBlockFace(blockFace);
		if (potion >= 0) {
			builder.setPotion(potionsData);
		}

		builder.setBlockFace(facing)
				.setDustOptions(options)
				.setExtra(data)
				.build();

		return builder.build();

	}

	private static void setMaterial(Material material, Class<?> dataType, Builder builder) {
		if (material == null)
			return;
		if (dataType.isInstance(material))
			builder.setMaterial(material);
		if (dataType.isInstance(material))
			builder.setMaterialData(material.getData());
		if (dataType.isInstance(material))
			builder.setMaterialBlockData(material.createBlockData());

	}
}
