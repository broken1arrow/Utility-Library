package org.broken.arrow.yaml.library;


import org.broken.arrow.serialize.library.DataSerializer;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.serialize.library.utility.serialize.MethodReflectionUtils;
import org.broken.arrow.yaml.library.config.updater.ConfigUpdater;
import org.broken.arrow.yaml.library.utillity.Valid;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Helper class to load and save data from one or several files.
 * Provides methods for serialization and file operations.
 */
public abstract class YamlFileManager {

	private boolean shallGenerateFiles;
	private boolean singelFile;
	private boolean firstLoad = true;
	private final String path;
	private final String fileName;
	private int version;
	private String extension;
	private Set<String> filesFromResource;
	private FileConfiguration customConfig;
	private File customConfigFile;
	private final File dataFolder;
	protected Plugin plugin;
	private ConfigUpdater configUpdater;

	/**
	 * Constructs a new `YamlFileManager` object with the specified plugin and path.
	 *
	 * @param plugin the plugin associated with the file manager
	 * @param path   the path to the file or folder where data will be stored
	 */
	public YamlFileManager(Plugin plugin, final String path) {
		this(plugin, path, true, true);
	}

	/**
	 * Constructs a new `YamlFileManager` object with the specified plugin, path, and file options.
	 *
	 * @param plugin             the plugin associated with the file manager
	 * @param path               the path to the file or folder where data will be stored
	 * @param singleFile         specifies whether the file manager operates on a single file or multiple files
	 * @param shallGenerateFiles specifies whether default files should be generated
	 */
	public YamlFileManager(Plugin plugin, final String path, boolean singleFile, boolean shallGenerateFiles) {
		if (plugin == null)
			throw new RuntimeException("The plugin is null");
		this.singelFile = singleFile;
		this.shallGenerateFiles = shallGenerateFiles;
		this.plugin = plugin;
		this.dataFolder = plugin.getDataFolder();
		if (singleFile)
			this.fileName = this.getNameOfFile(path);
		else
			this.fileName = "";

		this.path = this.setExtensionIfExist(path);
		final File folder = this.dataFolder;
		if (!folder.exists())
			folder.mkdir();
		System.out.println("path " + this.getPath());
		System.out.println("path " + this.getPathWithExtension());
	}

	/**
	 * Subclasses must implement this method to save data to the specified file.
	 * The argument is most useful when you have a list of files to save. For other cases,
	 * you could use {@link #getCustomConfig()} directly.
	 *
	 * @param file the file to which the data should be saved
	 */
	protected abstract void saveDataToFile(final File file);

	/**
	 * Subclasses must implement this method to load settings from the specified YAML file.
	 * The argument is most useful when you have a list of files to load. For other cases,
	 * you could use {@link #getCustomConfig()} directly.
	 *
	 * @param file the YAML file from which to load the settings.
	 */
	protected abstract void loadSettingsFromYaml(final File file);

	/**
	 * Updates the configuration file.
	 */
	public final void update(final String... ignoredSections) {
		this.update(null, null, ignoredSections);
	}

	/**
	 * Updates the configuration file with the specified file and ignored sections.
	 *
	 * @param file            the file to update (if null, the current file is used)
	 * @param ignoredSections the sections to ignore during the update
	 */
	public final void update(@Nullable final File file, final String... ignoredSections) {
		this.update(file, null, ignoredSections);
	}

	/**
	 * Updates the configuration file with the specified file and ignored sections.
	 *
	 * @param file            The file to update. If null, the current file is used.
	 * @param resource        The resource path of the file. Use this if you store your file on the disk in a different path
	 *                        than what you have in the resource folder.
	 * @param ignoredSections The sections to ignore during the update.
	 */

	public final void update(@Nullable File file, final String resource, final String... ignoredSections) {
		if (this.configUpdater == null)
			this.configUpdater = new ConfigUpdater(this.plugin, ignoredSections);
		if (file == null)
			file = new File(this.getFullPath());
		try {
			this.configUpdater.update(getVersion(), resource != null ? resource : this.getPathWithExtension(), file);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			customConfig = YamlConfiguration.loadConfiguration(file);
		}
	}

	/**
	 * Reloads the configuration file.
	 */
	public void reload() {
		try {
			if (this.getCustomConfigFile() == null || this.firstLoad) {
				load(getAllFilesInPluginJar());
			} else {
				load(getFilesInPluginFolder(this.getPath()));
			}
		} catch (final IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves data from the specified path in the configuration and deserializes it into an object of the given class.
	 *
	 * @param path  the path to the data in the configuration
	 * @param clazz the class to deserialize the data into
	 * @param <T>   the type of the deserialized object
	 * @return the deserialized object, or null if the path or class is invalid
	 */
	@Nullable
	public <T extends ConfigurationSerializable> T getData(final String path, final Class<T> clazz) {
		Valid.checkBoolean(path != null, "path can't be null");
		if (clazz == null) return null;

		final Map<String, Object> fileData = new HashMap<>();
		final ConfigurationSection configurationSection = customConfig.getConfigurationSection(path);
		if (configurationSection != null)
			for (final String data : configurationSection.getKeys(true)) {
				final Object object = customConfig.get(path + "." + data);
				if (object instanceof MemorySection) continue;
				fileData.put(data, object);
			}
		Method deserializeMethod = MethodReflectionUtils.getMethod(clazz, "deserialize", Map.class);
		if (deserializeMethod == null)
			deserializeMethod = MethodReflectionUtils.getMethod(clazz, "valueOf", Map.class);

		return MethodReflectionUtils.invokeStaticMethod(clazz, deserializeMethod, fileData);
	}

	/**
	 * Sets the serialized data of the specified ConfigurationSerializable object at the given path in the configuration.
	 *
	 * @param path          the path to set the serialized data at
	 * @param configuration the ConfigurationSerializable object to serialize and set
	 * @throws IllegalArgumentException if the path or configuration object is null, or if the serialization fails
	 */
	public void setData(@Nonnull final String path, @Nonnull final ConfigurationSerializable configuration) {
		Valid.checkBoolean(path != null, "path can't be null");
		Valid.checkBoolean(configuration != null, "Serialize utility can't be null, need provide a class instance some implements ConfigurationSerializeUtility");
		Valid.checkBoolean(configuration.serialize() != null, "Missing serialize method or it is null, can't serialize the class data.");

		this.getCustomConfig().set(path, null);
		for (final Map.Entry<String, Object> key : configuration.serialize().entrySet()) {
			this.getCustomConfig().set(path + "." + key.getKey(), DataSerializer.serialize(key.getValue()));
		}
	}

	/**
	 * Saves the data to the appropriate file(s).
	 */
	public final void save() {
		save(null);
	}

	/**
	 * Saves the data to the specified file.
	 *
	 * @param fileToSave the name of the file to save the data to
	 */
	public final void save(final String fileToSave) {
		final File dataFolder = new File(getFullPath());
		if (!dataFolder.isDirectory()) {
			saveData(dataFolder);
			return;
		}
		final File[] listOfFiles = dataFolder.listFiles();

		if (dataFolder.exists() && listOfFiles != null) {
			if (fileToSave != null) {
				if (!checkFolderExist(fileToSave, listOfFiles)) {
					final File newDataFolder = new File(getFullPath(), fileToSave + "." + this.getExtension());
					try {
						newDataFolder.createNewFile();
					} catch (final IOException e) {
						e.printStackTrace();
					} finally {
						saveData(newDataFolder);
					}
				} else {
					for (final File file : listOfFiles) {
						if (getNameOfFile(file.getName()).equals(fileToSave)) {
							saveData(file);
						}
					}
				}
			} else
				for (final File file : listOfFiles) {
					saveData(file);
				}
		}
	}

	/**
	 * Saves the configuration to the specified file.
	 *
	 * @param file the file to which the configuration should be saved
	 */
	public void saveToFile(final File file) {
		this.saveToFile(file, false);
	}

	/**
	 * Saves the configuration to the specified file and updates the data if requested.
	 *
	 * @param file       the file to which the configuration should be saved
	 * @param updateData specifies whether the data should be updated after saving
	 */
	public void saveToFile(final File file, boolean updateData) {
		try {
			this.customConfig.save(file);
			if (updateData)
				update(file);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Removes the specified file.
	 *
	 * @param fileName the name of the file to be removed
	 * @return true if the file was successfully removed, false otherwise
	 */
	public boolean removeFile(final String fileName) {
		final File dataFolder = new File(this.getPath(), fileName + "." + getExtension());
		return dataFolder.delete();
	}

	/**
	 * Loads data from the specified files.
	 *
	 * @param files the array of files to load data from
	 * @throws IOException                   if an I/O error occurs while loading the files
	 * @throws InvalidConfigurationException if the configuration is invalid
	 */
	public final void load(final File[] files) throws IOException, InvalidConfigurationException {
		if (files != null)
			for (final File file : files) {
				if (file == null) continue;
				if (getCustomConfigFile() == null) {
					this.customConfigFile = file;
				}
				if (!file.exists()) {
					this.plugin.saveResource(file.getName(), false);
				}
				if (this.firstLoad) {
					this.customConfig = YamlConfiguration.loadConfiguration(file);
					this.firstLoad = false;
				} else
					this.customConfig.load(file);
				loadSettingsFromYaml(file);
			}
	}

	/**
	 * Get the last loaded file configuration loaded. Not optimal to use
	 * if you plan to load several files.
	 *
	 * @return the file configuration.
	 */
	public FileConfiguration getCustomConfig() {
		return customConfig;
	}

	/**
	 * Get the last loaded file. Not optimal to use
	 * * if you plan to load several files.
	 *
	 * @return the last loaded file.
	 */
	public File getCustomConfigFile() {
		return customConfigFile;
	}

	/**
	 * Check if the file name is missing an extension.
	 *
	 * @param name the name of the file.
	 * @return the name with the extension added if it is missing.
	 */
	public String setExtensionIfExist(String name) {
		Valid.checkBoolean(name != null && !name.isEmpty(), "The given path must not be empty!");
		if (!isSingelFile())
			return name;
		final int pos = name.lastIndexOf(".");
		if (pos > 0) {
			this.setExtension(name.substring(pos + 1));
			name = name.substring(0, pos);
		}
		if (!name.contains("/"))
			return "";

		return name.replace("/" + this.fileName, "");
	}

	/**
	 * Get the extension of the file.
	 *
	 * @return the extension without the dot.
	 */
	@Nonnull
	public String getExtension() {
		if (this.extension == null) {
			return "yml";
		} else {
			String extension = this.extension;
			if (extension.startsWith("."))
				extension = extension.substring(1);
			return extension;
		}
	}

	/**
	 * Set the extension of the file.
	 *
	 * @param extension the extension to set, without the dot.
	 */
	public void setExtension(final String extension) {
		this.extension = extension;
	}

	/**
	 * Get the plugin's data folder.
	 *
	 * @return the plugin's data folder.
	 */
	public File getDataFolder() {
		return dataFolder;
	}

	/**
	 * Get the full path to the file.
	 *
	 * @return the full path to the file.
	 */
	public String getPathWithExtension() {
		String path = this.getPath();
		return (path == null || path.isEmpty() ? "" : path + "/") + this.getFileName() + "." + this.getExtension();
	}

	/**
	 * Get the path to the file or files inside the plugin folder.
	 *
	 * @return the path to the file.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Get the full path from the data folder to the file.
	 *
	 * @return the full path from the plugin folder to the file.
	 */
	public String getFullPath() {
		return this.getDataFolder() + "/" + this.getPathWithExtension();
	}

	/**
	 * Get the filename without the extension.
	 *
	 * @return the filename.
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 * Check if it is set to a single file or not.
	 *
	 * @return true if it is a single file, false otherwise.
	 */
	public boolean isSingelFile() {
		return singelFile;
	}

	/**
	 * Get the version number used for the update process in the {@link #update(String...)} methods.
	 * Set the value to -1 if you want the update to be performed whenever the resource and the saved file on disk do not match.
	 * You can either override this method or use {@link #setVersion(int)} to set the version.
	 *
	 * @return the current version number.
	 */

	public int getVersion() {
		return version;
	}

	/**
	 * Set the version number used for the update process in the {@link #update(String...)} methods.
	 * Set the value to -1 if you want the update to be performed whenever the resource and the saved file on disk do not match.
	 *
	 * @param version the version number to set.
	 */
	public void setVersion(final int version) {
		this.version = version;
	}

	/**
	 * Set this to false to not generate defult files.
	 *
	 * @param shallGenerateFiles set to false if you not want to generate files.
	 */
	public void setShallGenerateFiles(final boolean shallGenerateFiles) {
		this.shallGenerateFiles = shallGenerateFiles;
	}

	/**
	 * Set this to false if it is more an one file. It will then check for all
	 * files in the folder you provide.
	 *
	 * @param singelFile false if it shall check for several files.
	 */
	public void setIsSingelFile(final boolean singelFile) {
		this.singelFile = singelFile;
	}

	/**
	 * check if the folder name is empty or null.
	 *
	 * @return true if folder name is empty or null.
	 */
	public boolean isFolderNameEmpty() {
		return this.getPathWithExtension() == null || this.getPathWithExtension().isEmpty();
	}


	public File[] getAllFilesInPluginJar() {

		if (this.shallGenerateFiles) {
			final List<String> filenamesFromDir = getAllFilesInDirectory();
			System.out.println("filenamesFromDir  " + filenamesFromDir);
			if (filenamesFromDir != null)
				filesFromResource = new HashSet<>(filenamesFromDir);
		}
		return getFilesInPluginFolder(getPath());
	}

	public List<String> getAllFilesInDirectory() {
		return getFilenamesForDirnameFromCP(getPath());
	}

	public boolean checkFolderExist(final String fileToSave, final File[] dataFolders) {
		if (fileToSave != null)
			for (final File file : dataFolders) {
				final String fileName = getNameOfFile(file.getName());
				if (fileName.equals(fileToSave))
					return true;
			}
		return false;
	}

	/**
	 * Check if the file exist, will also work with folders.
	 *
	 * @param path the file name or the file path.
	 * @return true if it exist.
	 */
	public boolean fileExists(final String path) {
		final File outFile;
		if (path.contains("/")) {
			outFile = new File(this.getDataFolder() + "/" + path);
		} else {
			outFile = new File(this.getFullPath());
		}
		return outFile.exists();
	}

	public File[] getFilesInPluginFolder(final String directory) {
		if (isSingelFile()) {
			final File checkFile = new File(this.getDataFolder(), this.getPathWithExtension());
			if (!checkFile.exists() && this.shallGenerateFiles)
				createMissingFile();
			return new File(checkFile.getParent()).listFiles(file -> !file.isDirectory() && file.getName().equals(getFileName(this.getPathWithExtension())));
		}
		final File dataFolder = new File(this.getDataFolder(), directory);
		if (!dataFolder.exists() && !directory.isEmpty())
			dataFolder.mkdirs();
		if (this.filesFromResource != null)
			createMissingFiles(dataFolder.listFiles(file -> !file.isDirectory() && file.getName().endsWith("." + getExtension())));

		return dataFolder.listFiles(file -> !file.isDirectory() && file.getName().endsWith("." + getExtension()));
	}

	public String getNameOfFile(String path) {
		Valid.checkBoolean(path != null && !path.isEmpty(), "The given path must not be empty!");
		int pos;

		if (path.lastIndexOf("/") == -1)
			pos = path.lastIndexOf("\\");
		else
			pos = path.lastIndexOf("/");
		if (pos > 0)
			path = path.substring(pos + 1);

		pos = path.lastIndexOf(".");

		if (pos > 0)
			path = path.substring(0, pos);
		return path;
	}

	public String getFileName(String path) {
		Valid.checkBoolean(path != null && !path.isEmpty(), "The given path must not be empty!");
		final int pos;

		if (path.lastIndexOf("/") == -1)
			pos = path.lastIndexOf("\\");
		else
			pos = path.lastIndexOf("/");

		if (pos > 0)
			path = path.substring(pos + 1);

		return path;
	}

	/**
	 * Method to save data to file
	 *
	 * @param file the file to save.
	 */
	private void saveData(final File file) {
		saveDataToFile(file);
	}


	/**
	 * Get data from resource folder from the path or filename.
	 * Need to be ether filename.yml or foldername/filename.yml.
	 *
	 * @param path or file name you want to get from resource folder.
	 * @return map with keys and values from the file.
	 */

	public Map<String, Object> createFileFromResource(final String path) {
		final InputStream inputStream = this.plugin.getResource(path);
		if (inputStream == null) return null;

		final Map<String, Object> values = new LinkedHashMap<>();
		final FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));

		for (final String key : newConfig.getKeys(true)) {
			final Object value = newConfig.get(key);
			if (value != null && !value.toString().startsWith("MemorySection")) {
				values.put(key, value);
			}
		}
		return values;
	}

	public List<String> getFilenamesForDirnameFromCP(final String directoryName) {
		final List<String> filenames = new ArrayList<>();
		final URL url = this.plugin.getClass().getClassLoader().getResource(directoryName);

		if (url != null) {
			if (url.getProtocol().equals("file")) {
				try {
					final File file = Paths.get(url.toURI()).toFile();
					final File[] files = file.listFiles();
					if (files != null) {
						for (final File filename : files) {
							filenames.add(filename.toString());
						}
					}
				} catch (final URISyntaxException e) {
					e.printStackTrace();
				}
			} else if (url.getProtocol().equals("jar")) {

				final String dirname = isSingelFile() ? directoryName : directoryName + "/";
				final String path = url.getPath();
				final String jarPath = path.substring(5, path.indexOf("!"));
				try (final JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()))) {
					final Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						final JarEntry entry = entries.nextElement();
						final String name = entry.getName();
						if (name.startsWith(dirname) && name.endsWith(this.getExtension())) {
							filenames.add(name);
						}
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		return filenames;
	}

	private void createMissingFile() {
		try {
			this.plugin.saveResource(this.getPathWithExtension(), false);
		} catch (final IllegalArgumentException ignore) {
			final InputStream inputStream = this.plugin.getResource(this.getPathWithExtension());
			if (inputStream == null) {
				final File checkFile = new File(this.getDataFolder(), this.getPathWithExtension());
				if (!checkFile.exists()) {
					try {
						checkFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return;
			}
			final FileConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
			try {
				newConfig.save(this.getPathWithExtension());
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createMissingFiles(final File[] listFiles) {
		if (this.filesFromResource == null) return;
		if (listFiles == null || listFiles.length < 1) {
			this.filesFromResource.forEach(file -> {
				if (file.endsWith(getExtension()))
					this.plugin.saveResource(file, false);
			});
			return;
		}

		this.filesFromResource.stream().filter((files) -> {
			if (!files.endsWith(getExtension())) return false;
			for (final File file : listFiles) {
				if (this.getFileName(files).equals(file.getName())) {
					return false;
				}
			}
			return true;
		}).forEach((files) -> this.plugin.saveResource(files, false));
	}

}