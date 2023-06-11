package org.broken.arrow.yaml.library;


import org.broken.arrow.serialize.library.SerializeData;
import org.broken.arrow.serialize.library.utility.serialize.ConfigurationSerializable;
import org.broken.arrow.yaml.library.utillity.ConfigUpdater;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Helper clas to load and save data. You get data from one or several files and can save it also
 * have a serialize method you can use.
 */

public abstract class YamlFileManager {

	private boolean shallGenerateFiles = true;
	private boolean singelFile = true;
	private boolean firstLoad = true;
	private FileConfiguration customConfig;
	private final String name;
	private int version;
	private String extension;
	private File customConfigFile;
	private final File dataFolder;
	private Set<String> filesFromResource;
	protected Plugin plugin;
	private ConfigUpdater configUpdater;

	public YamlFileManager(Plugin plugin, final String name) {
		if (plugin == null)
			throw new RuntimeException("The yml file is null");
		this.plugin = plugin;
		this.dataFolder = this.plugin.getDataFolder();
		this.name = this.checkIfFileHasExtension(name);
		final File folder = this.plugin.getDataFolder();
		if (!folder.exists())
			folder.mkdir();
	}

	public YamlFileManager(Plugin plugin, final String name, boolean singleFile, boolean shallGenerateFiles) {
		this(plugin, name);
		this.singelFile = singleFile;
		this.shallGenerateFiles = shallGenerateFiles;
	}

	protected abstract void saveDataToFile(final File file);

	protected abstract void loadSettingsFromYaml(final File file);

	protected void update(final String... ignoredSections) {
		this.update(null, null, ignoredSections);
	}

	protected void update(File file, final String... ignoredSections) {
		this.update(file, null, ignoredSections);
	}

	protected void update(File file, final String resource, final String... ignoredSections) {
		if (this.configUpdater == null)
			this.configUpdater = new ConfigUpdater(this.plugin, ignoredSections);
		if (file == null)
			file = new File(this.getPath());
		try {
			this.configUpdater.update(getVersion(), resource != null ? resource : this.name, file);
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			customConfig = YamlConfiguration.loadConfiguration(file);
		}
	}

	public void reload() {
		try {
			if (this.getCustomConfigFile() == null || this.firstLoad) {
				load(getAllFilesInPluginJar());
			} else {
				load(getFilesInPluginFolder(this.getName()));
			}
		} catch (final IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void load(final File[] files) throws IOException, InvalidConfigurationException {
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

	public void saveToFile(final File file) {
		try {
			this.customConfig.save(file);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void saveToFile(final File file, boolean updateData) {
		try {
			this.customConfig.save(file);
			if (updateData)
				update(file);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getCustomConfig() {
		return customConfig;
	}

	public File getCustomConfigFile() {
		return customConfigFile;
	}

	/**
	 * Check if the file name missing extension.
	 *
	 * @param name of the file.
	 * @return name with extension added if it is missing.
	 */
	public String checkIfFileHasExtension(final String name) {
		Valid.checkBoolean(name != null && !name.isEmpty(), "The given path must not be empty!");
		if (!isSingelFile())
			return name;
		final int pos = name.lastIndexOf(".");
		if (pos > 0)
			this.setExtension(name.substring(pos + 1));
		return name;
	}

	/**
	 * Get the extension of a file.
	 *
	 * @return the extension without the dot.
	 */
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
	 * Set the extension of the file. If you not set
	 * it in the Name.
	 *
	 * @param extension to the file, with out the dot.
	 */
	public void setExtension(final String extension) {
		this.extension = extension;
	}


	public File getDataFolder() {
		return dataFolder;
	}

	public boolean removeFile(final String fileName) {
		final File dataFolder = new File(this.isSingelFile() ? this.getDataFolder().getParent() : this.getPath(), fileName + "." + getExtension());
		return dataFolder.delete();
	}

	public void save() {
		save(null);
	}

	public void save(final String fileToSave) {
		final File dataFolder = new File(getPath());
		if (!dataFolder.isDirectory()) {
			saveData(dataFolder);
			return;
		}
		final File[] listOfFiles = dataFolder.listFiles();

		if (dataFolder.exists() && listOfFiles != null) {
			if (fileToSave != null) {
				if (!checkFolderExist(fileToSave, listOfFiles)) {
					final File newDataFolder = new File(getPath(), fileToSave + "." + this.getExtension());
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

	private void saveData(final File file) {
		saveDataToFile(file);
	}

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
		final Method deserializeMethod = getMethod(clazz, "deserialize", Map.class);
		return invokeStatic(clazz, deserializeMethod, fileData);
	}

	public void setData(@Nonnull final String path, @Nonnull final ConfigurationSerializable configuration) {
		Valid.checkBoolean(path != null, "path can't be null");
		Valid.checkBoolean(configuration != null, "Serialize utility can't be null, need provide a class instance some implements ConfigurationSerializeUtility");
		Valid.checkBoolean(configuration.serialize() != null, "Missing serialize method or it is null, can't serialize the class data.");

		this.getCustomConfig().set(path, null);
		for (final Map.Entry<String, Object> key : configuration.serialize().entrySet()) {
			this.getCustomConfig().set(path + "." + key.getKey(), SerializeData.serialize(key.getValue()));
		}
	}

	/**
	 * Get file or folder name.
	 *
	 * @return file or folder name.
	 */
	public String getName() {
		return name;
	}

	public boolean isSingelFile() {
		return singelFile;
	}

	public int getVersion() {
		return version;
	}

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
		return this.getName() == null || this.getName().isEmpty();
	}


	public String getFileName() {
		return getNameOfFile(getName());
	}

	public File[] getAllFilesInPluginJar() {

		if (this.shallGenerateFiles) {
			final List<String> filenamesFromDir = getFilenamesForDirnameFromCP(getName());
			if (filenamesFromDir != null)
				filesFromResource = new HashSet<>(filenamesFromDir);
		}
		return getFilesInPluginFolder(getName());
	}

	public List<String> getFiles() {
		return getFilenamesForDirnameFromCP(getName());
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
			outFile = new File(this.getPath());
		}
		return outFile.exists();
	}

	public String getPath() {
		return this.getDataFolder() + "/" + this.getName();
	}

	public File[] getFilesInPluginFolder(final String directory) {
		if (isSingelFile()) {
			final File checkFile = new File(this.getDataFolder(), this.getName());
			if (!checkFile.exists() && this.shallGenerateFiles)
				createMissingFile();
			return new File(checkFile.getParent()).listFiles(file -> !file.isDirectory() && file.getName().equals(getName(this.getName())));
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

	public String getName(String path) {
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
						if (!this.isSingelFile() && name.startsWith(this.getFileName())) {
							filenames.add(name);
						} else if (name.startsWith(dirname)) {
							final URL resource = this.plugin.getClass().getClassLoader().getResource(name);
							if (resource != null) {
								filenames.add(name);
							} else
								this.plugin.getLogger().warning("Missing files in plugins/" + this.plugin + ".jar/" + directoryName + "/, contact the author of " + this.plugin.getName() + ".");
						}
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
		return filenames;
	}

	/**
	 * Gets a class method
	 *
	 * @param clazz
	 * @param methodName
	 * @param args
	 * @return
	 */
	private Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... args) {
		for (final Method method : clazz.getMethods())
			if (method.getName().equals(methodName) && isClassListEqual(args, method.getParameterTypes())) {
				method.setAccessible(true);
				return method;
			}

		return null;
	}

	private <T extends ConfigurationSerializable> T invokeStatic(final Class<T> clazz, final Method method, final Object... params) {
		if (method == null) return null;
		try {
			Valid.checkBoolean(Modifier.isStatic(method.getModifiers()), "deserialize method need to be static");
			return clazz.cast(method.invoke(method, params));
		} catch (final IllegalAccessException | InvocationTargetException ex) {
			throw new Valid.CatchExceptions(ex, "Could not invoke static method " + method + " with params " + Arrays.toString(params));
		}
	}

	private void createMissingFile() {
		try {
			this.plugin.saveResource(this.getName(), false);
		} catch (final IllegalArgumentException ignore) {
			final InputStream inputStream = this.plugin.getResource(this.getName());
			if (inputStream == null) {
				final File checkFile = new File(this.getDataFolder(), this.getName());
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
				newConfig.save(this.getName());
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createMissingFiles(final File[] listFiles) {
		if (this.filesFromResource == null) return;
		if (listFiles == null || listFiles.length < 1) {
			this.filesFromResource.forEach(files -> {
				if (files.endsWith(getExtension()))
					this.plugin.saveResource(files, false);
			});
			return;
		}

		this.filesFromResource.stream().filter((files) -> {
			if (!files.endsWith(getExtension())) return false;
			for (final File file : listFiles) {
				if (this.getName(files).equals(file.getName())) {
					return false;
				}
			}
			return true;
		}).forEach((files) -> this.plugin.saveResource(files, false));
	}

	private boolean isClassListEqual(final Class<?>[] first, final Class<?>[] second) {
		if (first.length != second.length) {
			return false;
		} else {
			for (int i = 0; i < first.length; ++i) {
				if (first[i] != second[i]) {
					return false;
				}
			}

			return true;
		}
	}

	public static class Valid extends RuntimeException {
		public static void checkBoolean(final boolean b, final String s) {
			if (!b)
				throw new CatchExceptions(s);
		}

		public static void checkNotNull(final Object b, final String s) {
			if (b == null)
				throw new CatchExceptions(s);
		}

		private static class CatchExceptions extends RuntimeException {
			public CatchExceptions(final String message) {
				super(message);
			}

			public CatchExceptions(final Throwable cause, final String message) {
				super(message, cause);
			}
		}
	}
}