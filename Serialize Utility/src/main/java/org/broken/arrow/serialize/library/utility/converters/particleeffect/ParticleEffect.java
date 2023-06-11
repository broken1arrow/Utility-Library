package org.broken.arrow.serialize.library.utility.converters.particleeffect;

import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple wrapper class that provides support for serialization to file or other database.
 * This class makes it easier to work with particle effects in different Minecraft versions,
 * eliminating the need to worry about serializing the Particle class or encountering errors
 * due to missing functions in your Minecraft version.
 */
public final class ParticleEffect implements ConfigurationSerializable {

	private final Particle particle;
	private final Effect effect;
	private final Material material;
	private final int count;
	private final double offsetX;
	private final double offsetY;
	private final double offsetZ;
	private final int data;
	private final Class<?> dataType;
	private final ParticleDustOptions particleDustOptions;
	private final Builder builder;

	private ParticleEffect(final Builder builder) {
		this.particle = builder.particle;
		this.effect = builder.effect;
		this.material = builder.material;
		this.count = builder.count;
		this.offsetX = builder.offsetX;
		this.offsetY = builder.offsetY;
		this.offsetZ = builder.offsetZ;
		this.data = builder.data;
		this.dataType = builder.dataType;
		this.particleDustOptions = builder.dustOptions;
		this.builder = builder;
	}

	/**
	 * Retrieves the Particle associated with this ParticleEffect.
	 *
	 * @return the Particle object, or null if not set.
	 */
	@Nullable
	public Particle getParticle() {
		return particle;
	}

	/**
	 * Retrieves the Effect associated with this ParticleEffect.
	 *
	 * @return the Effect object, or null if not set.
	 */
	@Nullable
	public Effect getEffect() {
		return effect;
	}

	/**
	 * Retrieves the Material associated with this ParticleEffect.
	 *
	 * @return the Material object, or null if not set.
	 */
	@Nullable
	public Material getMaterial() {
		return material;
	}

	public int getCount() {
		return count;
	}

	public double getOffsetX() {
		return offsetX;
	}

	public double getOffsetY() {
		return offsetY;
	}

	public double getOffsetZ() {
		return offsetZ;
	}

	public int getData() {
		return data;
	}

	/**
	 * Retrieves the data set on the effect.
	 *
	 * @return the class type for this effect or particle.
	 */
	@Nonnull
	public Class<?> getDataType() {
		return dataType;
	}

	/**
	 * Retrieves ParticleDustOptions, but this can only be used
	 * if the Minecraft version suport it.
	 *
	 * @return the ParticleDustOptions instance.
	 */
	@Nullable
	public ParticleDustOptions getParticleDustOptions() {
		return particleDustOptions;
	}

	public Builder getBuilder() {
		return builder;
	}

	public static class Builder {
		private final Particle particle;
		private final Effect effect;
		private Material material;
		private int count;
		private double offsetX;
		private double offsetY;
		private double offsetZ;
		private int data;
		private final Class<?> dataType;
		private ParticleDustOptions dustOptions;

		/**
		 * Constructs a new Builder instance for creating a particle effect with the specified effect and data type.
		 * Use this constructor when the particle is not set explicitly.
		 *
		 * @param effect   the effect particle.
		 * @param dataType the type of data this particle belongs to. Only used for certain particles,
		 *                 but still this should not be set to null.
		 */
		public Builder(final Effect effect, @Nonnull final Class<?> dataType) {
			this(null, effect, dataType);

		}


		/**
		 * Constructs a new Builder instance for creating a particle effect with the specified particle and data type.
		 * Use this constructor when the effect is not set explicitly.
		 *
		 * @param particle the particle.
		 * @param dataType the type of data this particle belongs to. Only used for certain particles,
		 *                 but still this should not be set to null.
		 */
		public Builder(final Particle particle, @Nonnull final Class<?> dataType) {
			this(particle, null, dataType);

		}

		/**
		 * Constructs a new Builder instance for creating a particle effect with the specified particle, effect, and data type.
		 *
		 * @param particle the particle.
		 * @param effect   the effect particle.
		 * @param dataType the type of data this particle belongs to. Only used for certain particles,
		 *                 but still this should not be set to null.
		 */
		public Builder(final Particle particle, final Effect effect, @Nonnull final Class<?> dataType) {
			this.particle = particle;
			this.effect = effect;
			this.dataType = dataType;
		}

		/**
		 * The matrial on the item, only works on 1.17+
		 *
		 * @param material the matrial you want to use as effect.
		 * @return this instance.
		 */
		public Builder setMaterial(final Material material) {
			this.material = material;
			return this;
		}

		/**
		 * Amount of particels you want.
		 * <p>
		 * NOTE: this can behave diffrent depening on type of particle.
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
		 * Set data on the particle. On redstone effect this tell the size of the effect.
		 *
		 * @param data the data you want to set.
		 * @return this instance.
		 */
		public Builder setData(final int data) {
			this.data = data;
			return this;
		}

		/**
		 * If the mincraft verfsion support it, you can add doneing to
		 * restone effect and gradients.
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
				", data=" + data +
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
		particleData.put("Data", this.data);
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
		String icon = (String) map.get("Material");
		Material material = null;
		if (icon != null) {
			icon = icon.toUpperCase();
			material = Material.getMaterial(icon);
		}
		final int data = (int) map.get("Data");
		Class<?> dataType = null;
		if (particle != null)
			dataType = particle.getDataType();
		if (effect != null)
			dataType = effect.getData();

		ParticleDustOptions options = (ParticleDustOptions) map.get("DustOptions");

		final Builder builder = new Builder(particle, effect, dataType);
		if (options == null)
			options = (ParticleDustOptions) map.get("Transition");

		return builder
				.setMaterial(material)
				.setDustOptions(options)
				.setData(data)
				.build();
	}
}
