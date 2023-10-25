package org.broken.arrow.nbt.library;


import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tr7zw.changeme.nbtapi.utils.MinecraftVersion.getVersion;


public class RegisterNbtAPI {

	private final CompMetadata compMetadata;
	private static boolean hasScoreboardTags = true;
	public RegisterNbtAPI(Plugin plugin, boolean turnOffLogger) {
		Logger logger = Logger.getLogger("NBTAPI");
		if (turnOffLogger)
			logger.setLevel(Level.WARNING);
		getVersion();
		compMetadata = new CompMetadata(plugin);
		checkClassesExist();
	}

	public static void checkClassesExist() {
		try {
			Entity.class.getMethod("getScoreboardTags");
		} catch (Throwable ignore) {
			hasScoreboardTags = false;
		}
	}

	public static boolean isHasScoreboardTags() {
		return hasScoreboardTags;
	}

	/**
	 * work in progress. Will later fix this so you can save data (is optional
	 * method to tr7zw file saving)
	 */
	public void yamlLoad() {
		// work in progress.
	}

	/**
	 * Get methods to easy set metadata. If you want to set up self you can start
	 * with this classes {@link de.tr7zw.changeme.nbtapi.NBTItem} and
	 * {@link de.tr7zw.changeme.nbtapi.NBTEntity}
	 * <p>&nbsp;</p>
	 * <p>
	 * Note: Should use these methods, give you better performance, if you don't use my methods.
	 * </p>
	 * <p>
	 * {@link de.tr7zw.changeme.nbtapi.NBT#get(org.bukkit.inventory.ItemStack, java.util.function.Function)} and {@link de.tr7zw.changeme.nbtapi.NBT#get(org.bukkit.entity.Entity, java.util.function.Function)}
	 * </p>
	 * <p>
	 * {@link de.tr7zw.changeme.nbtapi.NBT#modify(org.bukkit.entity.Entity, java.util.function.Function)} and {@link de.tr7zw.changeme.nbtapi.NBT#modify(org.bukkit.inventory.ItemStack, java.util.function.Function)}
	 * </p>
	 * @return CompMetadata class.
	 */
	public CompMetadata getCompMetadata() {
		return compMetadata;
	}

}
