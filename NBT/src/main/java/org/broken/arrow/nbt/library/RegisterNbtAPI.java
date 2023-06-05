package org.broken.arrow.nbt.library;


import org.broken.arrow.nbt.library.utility.ServerVersion;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tr7zw.nbtapi.utils.MinecraftVersion.getVersion;

public class RegisterNbtAPI {

	private final CompMetadata compMetadata;

	public RegisterNbtAPI(Plugin plugin, boolean turnOffLogger) {
		Logger logger = Logger.getLogger("NBTAPI");
		if (turnOffLogger)
			logger.setLevel(Level.OFF);

		getVersion();
		compMetadata = new CompMetadata(plugin);
		ServerVersion.setServerVersion(plugin);
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
	 * with this classes {@link de.tr7zw.nbtapi.NBTItem} and
	 * {@link de.tr7zw.nbtapi.NBTEntity}
	 *
	 * @return CompMetadata class.
	 */
	public CompMetadata getCompMetadata() {
		return compMetadata;
	}

}
