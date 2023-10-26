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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
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
import java.util.logging.Level;

/**
 * Helper class to load and save data from one or several files.
 * Provides methods for serialization and file operations.
 */
public abstract class YamlFileManager {

	private FileConfiguration currentConfig;
	private File currentConfigFile;
	private final File dataFolder;
	private boolean shallGenerateFiles;
	private boolean singleFile;
	private final String path;
	private final String fileName;
	private int version;
	private String extension;
	private String resourcePath;
	private Set<String> filesFromResource;

	protected Plugin plugin;
	private ConfigUpdater configUpdater;

	/**
	 * Constructs a new `YamlFileManager` object with the specified plugin and path. This constructor
	 * will default generate files and can only handle single file.
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
		this.singleFile = singleFile;
		this.shallGenerateFiles = shallGenerateFiles;
		this.plugin = plugin;
		this.dataFolder = plugin.getDataFolder();
		if (singleFile)
			this.fileName = this.getNameOfFile(path);
		else
			this.fileName = "";
		this.path = this.setExtensionIfExist(path);
		this.resourcePath = singleFile ? path : this.path;
		final File folder = this.dataFolder;
		if (!folder.exists())
			folder.mkdir();
	}

	/**
	 * Subclasses must implement this method to save data to the specified file.
	 *
	 * @param file the file to which the data should be saved
	 */
	protected abstract void saveDataToFile(final File file);

	/**
	 * <p>
	 * NOTE: this method is deprecated and will soon be removed.
	 * Use {@link #loadSettingsFromYaml(File, FileConfiguration)}
	 * </p>
	 * Subclasses must implement this method to load settings from the specified YAML file.
	 * The argument is most useful when you have a list of files to load.
	 *
	 * @param file the YAML file from which to load the settings.
	 */
	@Deprecated
	protected void loadSettingsFromYaml(final File file) {
	}

	/**
	 * Subclasses must implement this method to load settings from the specified YAML file.
	 *
	 * @param file         the YAML file from which to load the settings.
	 * @param loadedConfig the loaded config for the file.
	 */
	protected abstract void loadSettingsFromYaml(final File file, FileConfiguration loadedConfig);

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
	 *                        than what you have in the resource folder. You can also use {@link #setResourcePath(String)} to
	 *                        set the resource path.
	 * @param ignoredSections The sections to ignore during the update.
	 */
	public final void update(@Nullable File file, final String resource, final String... ignoredSections) {
		if (this.configUpdater == null)
			this.configUpdater = new ConfigUpdater(this.plugin, ignoredSections);
		if (file == null)
			file = new File(this.getFullPath());
		String resourcePathToFile = resource;
		if (resourcePathToFile == null)
			resourcePathToFile = this.resourcePath;
		try {
			this.configUpdater.update(getVersion(), resourcePathToFile != null ? resourcePathToFile : this.getPathWithExtension(), file);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			currentConfig = YamlConfiguration.loadConfiguration(file);
		}
	}

	/**
	 * Reloads the configuration file.
	 */
	public void reload() {
		try {
			Set<String> fromResource = this.filesFromResource;
			if (fromResource == null || fromResource.isEmpty()) {
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
		return this.getData(path, this.currentConfigFile, clazz);
	}

	/**
	 * Retrieves data from the specified path in the configuration and deserializes it into an object of the given class.
	 *
	 * @param path  the path to the data in the configuration
	 * @param file  the file to load the configuration from.
	 * @param clazz the class to deserialize the data into
	 * @param <T>   the type of the deserialized object
	 * @return the deserialized object, or null if the path or class is invalid
	 */
	@Nullable
	public <T extends ConfigurationSerializable> T getData(final String path, @Nonnull final File file, final Class<T> clazz) {
		Valid.checkBoolean(path != null, "path can't be null");
		if (clazz == null) return null;
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		final Map<String, Object> fileData = new HashMap<>();
		final ConfigurationSection configurationSection = config.getConfigurationSection(path);
		if (configurationSection != null)
			for (final String data : configurationSection.getKeys(true)) {
				final Object object = config.get(path + "." + data);
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
		this.setData(this.currentConfigFile, path, configuration);
	}

	/**
	 * Sets the serialized data of the specified ConfigurationSerializable object at the given path in the configuration.
	 *
	 * @param path          the path to set the serialized data at
	 * @param updateData    If true, the method updates the file with the serialized data and keeps the existing commits.
	 * @param configuration the ConfigurationSerializable object to serialize and set
	 * @throws IllegalArgumentException if the path or configuration object is null, or if the serialization fails
	 */
	public void setData(@Nonnull final String path, final boolean updateData, @Nonnull final ConfigurationSerializable configuration) {
		this.setData(this.currentConfigFile, path, updateData, configuration);
	}

	/**
	 * Sets the serialized data of the specified ConfigurationSerializable object at the given path in the configuration.
	 *
	 * @param path          the path to set the serialized data at
	 * @param configuration the ConfigurationSerializable object to serialize and set
	 * @throws IllegalArgumentException if the path or configuration object is null, or if the serialization fails
	 */
	public void setData(@Nonnull final File file, @Nonnull final String path, @Nonnull final ConfigurationSerializable configuration) {
		this.setData(file, path, false, configuration);
	}

	/**
	 * Sets the serialized data of the specified ConfigurationSerializable object at the given path in the configuration.
	 *
	 * @param file          the file to save the serialized data to.
	 * @param path          the path to set the serialized data at.
	 * @param updateData    If true, the method updates the file with the serialized data and keeps the existing commits.
	 * @param configuration the ConfigurationSerializable object to serialize and set
	 * @throws IllegalArgumentException if the path or configuration object is null, or if the serialization fails
	 */
	public void setData(@Nonnull final File file, @Nonnull final String path, final boolean updateData, @Nonnull final ConfigurationSerializable configuration) {
		Valid.checkNotNull(file, "file can't be null");
		Valid.checkNotNull(path, "path can't be null");
		Valid.checkNotNull(configuration, "Serialize utility can't be null, need provide a class instance some implements ConfigurationSerializeUtility");
		Valid.checkNotNull(configuration.serialize(), "Missing serialize method or it is null, can't serialize the class data.");
		FileConfiguration config = new YamlConfiguration();

		for (final Map.Entry<String, Object> key : configuration.serialize().entrySet()) {
			config.set(path + "." + key.getKey(), DataSerializer.serialize(key.getValue()));
		}
		this.saveToFile(file, config, updateData);
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
		if (this.singleFile) {
			final File file = new File(this.getFullPath());
			this.saveData(file);
			return;
		}
		
		final File folder = new File(this.getDataFolder(), this.getPath());
		if (!folder.isDirectory()) return;
		final File[] listOfFiles = folder.listFiles();

		if (folder.exists() && listOfFiles != null) {
			if (fileToSave != null) {
				this.saveSpecificFile(fileToSave, folder, listOfFiles);
			} else {
				for (final File file : listOfFiles) {
					saveData(file);
				}
			}
		}
	}

	private void saveSpecificFile(final String fileToSave, final File folder, final File[] listOfFiles) {
		if (!checkFolderExist(fileToSave, listOfFiles)) {
			final File newDataFolder = new File(folder, fileToSave + "." + this.getExtension());
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
	}

	/**
	 * Saves the configuration to the specified file.
	 *
	 * @param file         the file to which the configuration should be saved
	 * @param customConfig the config you want to save to the file.
	 */
	public void saveToFile(@Nonnull final File file, @Nonnull final FileConfiguration customConfig) {
		this.saveToFile(file, customConfig, false);
	}

	/**
	 * Saves the configuration to the specified file and updates the data if you set update data to true.
	 *
	 * @param file         The file to which the configuration should be saved.
	 * @param customConfig The FileConfiguration you want to save to the file.
	 * @param updateData   Specifies whether the data related to the saved configuration should be updated after saving.
	 *                     This involve additional commits or changes to the original file shall be added to the file saved on disk.
	 */
	//  IOException Thrown when the given file cannot be written to for any reason.
	public void saveToFile(@Nonnull final File file, @Nonnull final FileConfiguration customConfig, boolean updateData) {
		try {
			customConfig.save(file);
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
		final File folder = new File(this.getPath(), fileName + "." + getExtension());
		return folder.delete();
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
				if (!file.exists()) {
					this.plugin.saveResource(file.getName(), false);
				}
				FileConfiguration customConfig = YamlConfiguration.loadConfiguration(file);
				this.currentConfigFile = file;
				this.currentConfig = customConfig;
				loadSettingsFromYaml(file, customConfig);
				loadSettingsFromYaml(file);
			}
	}

	/**
	 * Get the last loaded file configuration loaded. Not optimal to use
	 * if you plan to load several files.
	 *
	 * @return the file configuration.
	 */
	@Deprecated
	public FileConfiguration getCustomConfig() {
		return currentConfig;
	}

	/**
	 * Get the last loaded file. Not optimal to use
	 * * if you plan to load several files.
	 *
	 * @return the last loaded file.
	 */
	@Deprecated
	public File getCustomConfigFile() {
		return currentConfigFile;
	}

	/**
	 * Set the extension and also format the path to the file.
	 *
	 * @param name the full path and file name with the extension.
	 * @return the name without the extension added if it is missing.
	 */
	public String setExtensionIfExist(String name) {
		Valid.checkBoolean(name != null && !name.isEmpty(), "The given path must not be empty!");
		if (!isSingleFile())
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
			String fileExtension = this.extension;
			if (fileExtension.startsWith("."))
				fileExtension = fileExtension.substring(1);
			return fileExtension;
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
		String filePath = this.getPath();
		return (filePath == null || filePath.isEmpty() ? "" : filePath + "/") + this.getFileName() + "." + this.getExtension();
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
	public boolean isSingleFile() {
		return singleFile;
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
	 * @param singleFile false if it shall check for several files.
	 */
	public void setIsSingleFile(final boolean singleFile) {
		this.singleFile = singleFile;
	}

	/**
	 * Set the path were the file or files is located in the resource folder.
	 *
	 * @param resourcePath the path to the folder the files is located or path to the file self.
	 */
	public void setResourcePath(final String resourcePath) {
		this.resourcePath = resourcePath;
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
			if (filenamesFromDir != null)
				filesFromResource = new HashSet<>(filenamesFromDir);
		}
		return getFilesInPluginFolder(getPath());
	}

	public List<String> getAllFilesInDirectory() {
		return getFilenamesForDirnameFromCP(this.resourcePath);
	}

	public boolean checkFolderExist(final String fileToSave, final File[] dataFolders) {
		if (fileToSave != null)
			for (final File file : dataFolders) {
				final String name = getNameOfFile(file.getName());
				if (name.equals(fileToSave))
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
		if (isSingleFile()) {
			final File checkFile = new File(this.getDataFolder(), this.getPathWithExtension());
			if (!checkFile.exists() && this.shallGenerateFiles)
				this.saveResource(this.resourcePath);
			return new File(checkFile.getParent()).listFiles(file -> !file.isDirectory() && file.getName().equals(getFileName(this.getPathWithExtension())));
		}
		final File folder = new File(this.getDataFolder(), directory);
		if (!folder.exists() && !directory.isEmpty())
			folder.mkdirs();
		if (this.filesFromResource != null)
			createMissingFiles(folder.listFiles(file -> !file.isDirectory() && file.getName().endsWith("." + getExtension())));

		return folder.listFiles(file -> !file.isDirectory() && file.getName().endsWith("." + getExtension()));
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
				getFileFromJar(directoryName, filenames, url);
			}
		}
		return filenames;
	}

	/**
	 * Retrieve the file from your plugin.jar and will sort out al files that don't match the
	 * directory or file nane.
	 *
	 * @param directoryName the name on the directory you want to get all files from or a file name for only get one file.
	 * @param filenames the list to add the name of the files you want to add.
	 * @param url The URL path to your plugin.jar resources that contains your files you want to get.
	 */
	private void getFileFromJar(final String directoryName, final List<String> filenames, final URL url) {
		final String dirname = isSingleFile() ? directoryName : directoryName + "/";
		final String urlPathToFile = url.getPath();
		final String jarPath = urlPathToFile.substring(5, urlPathToFile.indexOf("!"));

		try (final JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()))) {
			final Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				final String filePath = entry.getName();
				if (filePath.startsWith(dirname) && filePath.endsWith(this.getExtension())) {
					filenames.add(filePath);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void createMissingFile() {
		final File checkFile = new File(this.getDataFolder(), this.getPathWithExtension());
		if (checkFile.exists()) {
			return;
		}
		this.saveResource(this.resourcePath);
	}

	private void createMissingFiles(final File[] listFiles) {
		if (this.filesFromResource == null) return;
		if (listFiles == null || listFiles.length < 1) {
			this.filesFromResource.forEach(file -> {
				if (file.endsWith(getExtension()))
					this.saveResource(file);
			});
			return;
		}

		this.filesFromResource.forEach((file) -> {
			if (!file.endsWith(getExtension())) return;
			for (final File fileList : listFiles) {
				if (this.getFileName(file).equals(fileList.getName())) {
					return;
				}
			}
			this.saveResource(file);
		});
	}

	private void saveResource(@Nonnull final String path) {
		String resourcePath = path;
		if (resourcePath.equals("")) {
			throw new IllegalArgumentException("ResourcePath cannot be empty.");
		}
		resourcePath = resourcePath.replace('\\', '/');
		InputStream in = getResource(resourcePath);
		if (in == null) {
			plugin.getLogger().log(Level.SEVERE, "The embedded resource '" + resourcePath + "' cannot be found in " + path);
			return;
		}
		File outFile = new File(dataFolder, this.getPath() + "/" + this.getFileName(path));

		try {
			if (!outFile.exists()) {
				new File(dataFolder, this.getPath()).mkdir();
				outFile.createNewFile();
				OutputStream out = new FileOutputStream(outFile);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
				in.close();
			}
		} catch (IOException exception) {
			plugin.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, exception);
		}
	}

	public InputStream getResource(final String filename) {
		if (filename == null) {
			throw new IllegalArgumentException("Filename cannot be null");
		}
		try {
			URL url = this.plugin.getClass().getClassLoader().getResource(filename);
			if (url == null) {
				return null;
			}
			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			return connection.getInputStream();
		} catch (IOException ex) {
			return null;
		}
	}
}