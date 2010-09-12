package boss.plugins;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import boss.plugins.spi.config.IPluginConfiguration;

import uk.ac.warwick.dcs.boss.frontend.PageDispatcherServlet;
import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;

class PluginManager {
	static final Attributes.Name PLUGIN_ID = new Attributes.Name(
			"BOSS-Plugin-Id");
	static final Attributes.Name PLUGIN_NAME = new Attributes.Name(
			"BOSS-Plugin-Name");
	static final Attributes.Name PLUGIN_AUTHOR = new Attributes.Name(
			"BOSS-Plugin-Author");
	static final Attributes.Name PLUGIN_EMAIL = new Attributes.Name(
			"BOSS-Plugin-Email");
	static final Attributes.Name PLUGIN_VERSION = new Attributes.Name(
			"BOSS-Plugin-Version");
	static final Attributes.Name PLUGIN_DESCRIPTION = new Attributes.Name(
			"BOSS-Plugin-Description");
	// static final Attributes.Name PLUGIN_CONFIGURATION = new Attributes.Name(
	// "BOSS-Plugin-Configuration");
	static Logger logger = Logger.getLogger("plugin manager");

	static PluginMetadata installPlugin(File pluginFile) throws IOException,
			InvalidPluginException, DAOException {
		JarFile jarFile = null;
		Attributes atts = null;
		try {
			// we have the plugin in a jar file
			jarFile = new JarFile(pluginFile);
			atts = jarFile.getManifest().getMainAttributes();
		} catch (IOException e) {
			throw new InvalidPluginException(
					"Supplied file is not a valid BOSS plugin");
		}

		// manifest file is required to supplied at least plugin's id,
		// name, and version
		if (!atts.containsKey(PLUGIN_ID) || !atts.containsKey(PLUGIN_NAME)
				|| !atts.containsKey(PLUGIN_VERSION)) {
			throw new InvalidPluginException(
					"Supplied file is not a valid BOSS plugin");
		}

		// check if this plugin exists
		IDAOSession f = null;
		try {
			DAOFactory df = (DAOFactory) FactoryRegistrar
					.getFactory(DAOFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			e.printStackTrace();
		}
		try {
			f.beginTransaction();
			PluginMetadata pluginInfo = new PluginMetadata();
			pluginInfo.setPluginId(atts.getValue(PLUGIN_ID));
			Collection<PluginMetadata> plugins = f
					.getPluginMetadataDAOInstance()
					.findPersistentEntitiesByExample(pluginInfo);
			f.endTransaction();
			if (!plugins.isEmpty()) {
				throw new InvalidPluginException(
						"The "
								+ atts.getValue(PLUGIN_ID)
								+ " plugin is already installed. Consider upgrade the plugin if this is a newer version.");
			}
		} catch (DAOException e) {
			f.abortTransaction();
			throw e;
		}

		// we have a valid plugin file (as far as MANIFEST file goes)
		String pluginId = atts.getValue(PluginManager.PLUGIN_ID).trim();
		File webInfDir = new File(PageDispatcherServlet.realPath, "WEB-INF");
		File pluginFolder = new File(webInfDir, "plugins");

		// archiving the plugin file under WEB-INF/plugins/
		// since the pluginId is unique, there shouldn't be a collision
		// regarding name
		File pluginJarFile = new File(pluginFolder, pluginId + ".jar");
		try {
			logger.log(Level.INFO, "Storing " + pluginId
					+ " plugin into WEB-INF/plugins folder");
			FileUtils.copyFile(pluginFile, pluginJarFile);
		} catch (IOException e) {
			if (pluginJarFile.exists()) {
				logger.log(Level.INFO, "Deleting " + pluginId
						+ " plugin file in WEB-INF/plugins folder");
				pluginJarFile.delete();
			}

			// rethrow exception
			throw e;
		}

		// get lib filenames if present
		List<String> libFileNames = new LinkedList<String>();
		Enumeration<JarEntry> enumeration = jarFile.entries();
		while (enumeration.hasMoreElements()) {
			JarEntry entry = enumeration.nextElement();
			String entryName = entry.getName();
			if (entryName.startsWith("lib/") && entryName.endsWith(".jar")) {
				String[] entryPathComps = entryName.split("/");
				String libFileName = entryPathComps[entryPathComps.length - 1];
				libFileNames.add(libFileName);
			}
		}

		// create an entity
		PluginMetadata pluginMetadata = new PluginMetadata();
		pluginMetadata.setPluginId(atts.getValue(PLUGIN_ID));
		pluginMetadata.setName(atts.getValue(PluginManager.PLUGIN_NAME));
		pluginMetadata.setVersion(atts.getValue(PluginManager.PLUGIN_VERSION));
		pluginMetadata.setAuthor(atts.getValue(PluginManager.PLUGIN_AUTHOR));
		pluginMetadata.setEmail(atts.getValue(PluginManager.PLUGIN_EMAIL));
		pluginMetadata.setDescription(atts
				.getValue(PluginManager.PLUGIN_DESCRIPTION));
		if (!libFileNames.isEmpty()) {
			pluginMetadata.setLibFilenames(libFileNames.toArray(new String[0]));
		} else
			pluginMetadata.setLibFilenames(null);

		try {
			// init custom tables if required, otherwise it this piece
			// of code doesn't have any effect
			try {
				f.beginTransaction();
				f.getPluginMetadataDAOInstance().createPluginCustomTables(
						pluginId);
				f.endTransaction();
			} catch (DAOException e) {
				f.abortTransaction();
				throw e;
			} catch(InvalidPluginException e) {
				f.abortTransaction();
				if (pluginJarFile.exists()) {
					logger.log(Level.INFO, "Deleting " + pluginId
							+ " plugin file in WEB-INF/plugins folder");
					pluginJarFile.delete();
				}
				// rethrow exception
				throw e;
			}

			// enable the plugin
			PluginManager.enablePlugin(pluginMetadata);

			// handling plugin configuration if present
			pluginMetadata.setConfigurable(PluginManager
					.initPluginConfig(pluginId));
		} catch (IOException e) {
			// installation is unsuccessful, delete plugin file in
			// WEB-INF/plugins
			if (pluginJarFile.exists()) {
				logger.log(Level.INFO, "Deleting " + pluginId
						+ " plugin file in WEB-INF/plugins folder");
				pluginJarFile.delete();
			}

			// rethrow exception
			throw e;
		}
		return pluginMetadata;
	}

	static void uninstallPlugin(PluginMetadata pluginInfo) throws IOException,
			DAOException, InvalidPluginException {
		// delete plugin's database tables
		// if this plugin didn't introduce any table, this piece of code simply
		// does nothing
		String pluginId = pluginInfo.getPluginId();
		IDAOSession f = null;
		try {
			DAOFactory df = (DAOFactory) FactoryRegistrar
					.getFactory(DAOFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			e.printStackTrace();
		}
		try {
			f.beginTransaction();
			f.getPluginMetadataDAOInstance()
					.destroyPluginCustomTables(pluginId);
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw e;
		}

		// if this plugin is currently enable, disable it first
		if (pluginInfo.getEnable())
			PluginManager.disablePlugin(pluginInfo);

		// delete plugin storage
		File webInfDir = new File(PageDispatcherServlet.realPath, "WEB-INF");
		File pluginStorageDir = new File(webInfDir, "plugins");
		File thisPluginJar = new File(pluginStorageDir,
				pluginInfo.getPluginId() + ".jar");
		logger.log(Level.INFO, "Deleting " + pluginInfo.getPluginId()
				+ " plugin file in WEB-INF/plugins folder");
		thisPluginJar.delete();

		if (pluginInfo.getConfigurable()) {
			File thisPluginConf = new File(pluginStorageDir,
					pluginInfo.getPluginId() + ".properties");
			if (thisPluginConf.exists())
				thisPluginConf.delete();
		}
	}

	static void enablePlugin(PluginMetadata pluginInfo) throws IOException {
		String pluginId = pluginInfo.getPluginId();
		File webInfDir = new File(PageDispatcherServlet.realPath, "WEB-INF");
		// make the plugin active by copy the jar file into WEB-INF/lib
		File pluginFile = new File(webInfDir, "plugins" + File.separator
				+ pluginId + ".jar");
		File webAppLibFolder = new File(webInfDir, "lib");
		File pluginJarFile = new File(webAppLibFolder, "plugin_" + pluginId
				+ ".jar");
		try {
			logger.log(Level.INFO, "Copying main jar file of " + pluginId
					+ " plugin into WEB-INF/lib folder");
			FileUtils.copyFile(pluginFile, pluginJarFile);
		} catch (IOException e) {
			if (pluginJarFile.exists()) {
				logger.log(Level.INFO, "Deleting " + pluginId
						+ " plugin file in WEB-INF/lib folder");
				pluginJarFile.delete();
			}
			// rethrow exception
			throw e;
		}

		// copy the dependencies of the plugin (residing under lib folder of
		// the plugin's jar file) if exists.
		JarFile jarFile = new JarFile(pluginFile);
		String[] libFNStrs = pluginInfo.getLibFilenames();
		if (libFNStrs != null) {
			List<String> libFileNames = Arrays.asList(libFNStrs);
			try {
				Enumeration<JarEntry> enumeration = jarFile.entries();
				while (enumeration.hasMoreElements()) {
					JarEntry entry = enumeration.nextElement();
					String entryName = entry.getName();
					if (entryName.startsWith("lib/")
							&& entryName.endsWith(".jar")) {
						String[] entryPathComps = entryName.split("/");
						String libFileName = entryPathComps[entryPathComps.length - 1];
						if (libFileNames.contains(libFileName)) {
							File destLibFile = new File(webAppLibFolder,
									"plugin_" + pluginId + "_" + libFileName);
							InputStream in = null;
							OutputStream out = null;
							logger.log(Level.INFO, "Extracting lib jar file "
									+ libFileName + " of " + pluginId
									+ " plugin into WEB-INF/lib folder");
							try {
								in = new BufferedInputStream(
										jarFile.getInputStream(entry));
								out = new BufferedOutputStream(
										new FileOutputStream(destLibFile));
								int c;
								while ((c = in.read()) != -1) {
									out.write(c);
								}
								out.flush();
							} finally {
								if (in != null)
									in.close();
								if (out != null)
									out.close();
							}
						}
					}
				}
			} catch (IOException e) {
				// clean up any lib file which has been activated (copied to
				// WEB-INF/lib)
				for (String filename : libFileNames) {
					File destLibFile = new File(webAppLibFolder, "plugin_"
							+ pluginId + "_" + filename);
					if (destLibFile.exists()) {
						logger.log(Level.INFO, "Deleting lib jar file "
								+ filename + " of " + pluginId
								+ " plugin in WEB-INF/lib folder");
						destLibFile.delete();
					}
				}

				// rethrow exception
				throw e;
			}
		}
		pluginInfo.setEnable(true);
	}

	static void disablePlugin(PluginMetadata pluginInfo) {

		// delete main jar file in webapp's lib folder
		File mainJarFile = new File(PageDispatcherServlet.realPath, "WEB-INF"
				+ File.separator + "lib" + File.separator + "plugin_"
				+ pluginInfo.getPluginId() + ".jar");
		logger.log(Level.INFO, "Deleting " + pluginInfo.getPluginId()
				+ " plugin file in WEB-INF/lib folder");
		mainJarFile.delete();

		// delete lib jar file also in webapp's lib folder
		String[] libFNs = pluginInfo.getLibFilenames();
		if (libFNs != null) {
			File webInfLibDir = new File(PageDispatcherServlet.realPath,
					"WEB-INF" + File.separator + "lib");
			for (int i = 0; i < libFNs.length; i++) {
				File libFile = new File(webInfLibDir, "plugin_"
						+ pluginInfo.getPluginId() + "_" + libFNs[i]);
				logger.log(Level.INFO, "Deleting lib jar file " + libFNs[i]
						+ " of " + pluginInfo.getPluginId()
						+ " plugin in WEB-INF/lib folder");
				libFile.delete();
			}
		}
		pluginInfo.setEnable(false);
	}

	static void upgradePlugin(File newPluginFile, PluginMetadata pluginInfo)
			throws IOException, InvalidPluginException {
		JarFile jarFile = null;
		Attributes atts = null;
		try {
			// we have the plugin in a jar file
			jarFile = new JarFile(newPluginFile);
			atts = jarFile.getManifest().getMainAttributes();
		} catch (IOException e) {
			throw new InvalidPluginException(
					"Supplied file is not a valid BOSS plugin");
		}

		// manifest file is required to supplied at least plugin's id,
		// name, and version
		if (!atts.containsKey(PLUGIN_ID) || !atts.containsKey(PLUGIN_NAME)
				|| !atts.containsKey(PLUGIN_VERSION)) {
			throw new InvalidPluginException(
					"Supplied file is not a valid BOSS plugin");
		}

		// check if this plugin is indeed of newer version
		String pluginId = atts.getValue(PluginManager.PLUGIN_ID).trim();
		String newVerStr = atts.getValue(PLUGIN_VERSION).trim();
		if (!pluginId.equals(pluginInfo.getPluginId())
				|| compareVersion(newVerStr, pluginInfo.getVersion()) <= 0)
			throw new InvalidPluginException(
					"Supplied file is not a newer version of "
							+ pluginInfo.getPluginId());

		// we have a valid plugin file (as far as MANIFEST file goes)

		// if this plugin is currently enable, disable it first
		if (pluginInfo.getEnable())
			PluginManager.disablePlugin(pluginInfo);

		File webInfDir = new File(PageDispatcherServlet.realPath, "WEB-INF");
		File pluginFolder = new File(webInfDir, "plugins");

		// replacing the plugin file under WEB-INF/plugins/
		File pluginJarFile = new File(pluginFolder, pluginId + ".jar");
		File oldPluginJarFile = new File(pluginFolder, pluginId + ".jar.old");
		try {
			logger.log(Level.INFO, "Replacing " + pluginId
					+ " plugin file in WEB-INF/plugins folder");
			pluginJarFile.renameTo(oldPluginJarFile);
			FileUtils.copyFile(newPluginFile, pluginJarFile);
		} catch (IOException e) {
			if (pluginJarFile.exists()) {
				pluginJarFile.delete();
			}
			if (oldPluginJarFile.exists()) {
				oldPluginJarFile.renameTo(pluginJarFile);
			}
			// rethrow exception
			throw e;
		}

		// get lib filenames if present
		List<String> libFileNames = new LinkedList<String>();
		Enumeration<JarEntry> enumeration = jarFile.entries();
		while (enumeration.hasMoreElements()) {
			JarEntry entry = enumeration.nextElement();
			String entryName = entry.getName();
			if (entryName.startsWith("lib/") && entryName.endsWith(".jar")) {
				String[] entryPathComps = entryName.split("/");
				String libFileName = entryPathComps[entryPathComps.length - 1];
				libFileNames.add(libFileName);
			}
		}

		// update the entity
		pluginInfo.setName(atts.getValue(PluginManager.PLUGIN_NAME));
		pluginInfo.setVersion(atts.getValue(PluginManager.PLUGIN_VERSION));
		pluginInfo.setAuthor(atts.getValue(PluginManager.PLUGIN_AUTHOR));
		pluginInfo.setEmail(atts.getValue(PluginManager.PLUGIN_EMAIL));
		pluginInfo.setDescription(atts
				.getValue(PluginManager.PLUGIN_DESCRIPTION));
		if (!libFileNames.isEmpty()) {
			pluginInfo.setLibFilenames(libFileNames.toArray(new String[0]));
		} else
			pluginInfo.setLibFilenames(null);

		try {
			// enable the plugin
			if (pluginInfo.getEnable())
				PluginManager.enablePlugin(pluginInfo);

			// handling plugin configuration if present
			pluginInfo
					.setConfigurable(PluginManager.initPluginConfig(pluginId));
		} catch (IOException e) {
			// upgrade is unsuccessful for some reason revert
			// to previous version
			if (oldPluginJarFile.exists()) {
				oldPluginJarFile.renameTo(pluginJarFile);
			}
			// rethrow exception
			throw e;
		}

		// seem to be successful, delete the backup old plugin file
		if (oldPluginJarFile.exists())
			oldPluginJarFile.delete();
	}

	
	private static boolean initPluginConfig(String pluginId) throws IOException {
		// have to append the plugin's jar file into CLASSPATH before lookup
		// since plugin is not installed and activated yet hence its binary will
		// not be in CLASSPATH
		
		boolean retval = false;
		File pluginFolder = new File(PageDispatcherServlet.realPath, "WEB-INF"
				+ File.separator + "plugins");
		File pluginFile = new File(pluginFolder, pluginId + ".jar");
		URL url = null;
		try {
			url = pluginFile.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		URL[] urls = { url };
		URLClassLoader classLoader = new URLClassLoader(urls, Thread
				.currentThread().getContextClassLoader());
		Lookup lookup = Lookups.metaInfServices(classLoader);
		Collection<? extends IPluginConfiguration> configs = lookup
				.lookupAll(IPluginConfiguration.class);
		for (IPluginConfiguration config : configs) {
			// make sure it's not the one that already installed. If this is
			// something which was
			// installed, the ClassLoader of it will not be the same as the one
			// we have above
			if (config.getClass().getClassLoader() == classLoader) {
				Collection<ConfigurationOption> props = config
						.getConfigurationOptions();
				Properties properties = new Properties();
				for (ConfigurationOption prop : props) {
					properties.setProperty(prop.getName(),
							prop.getDefaultValue());
				}

				// write the properties file
				FileOutputStream writer = null;
				try {
					File propertiesFile = new File(pluginFolder, pluginId
							+ ".properties");
					writer = new FileOutputStream(propertiesFile);
					properties
							.store(writer, "Automatically generated by BOSS2");
				} finally {
					if (writer != null)
						writer.close();
				}
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Get configuration options supplied by plugin (in the form of an 
	 * implementation of IPluginConfiguration)
	 * @param pluginId id of the plugin
	 * @return list of ConfigurationOption
	 */
	static Collection<ConfigurationOption> getPluginConfigOption(String pluginId) {
		// have to append the plugin's jar file into CLASSPATH before lookup
		// since plugin might be inactive and its binary is not in CLASSPATH
		
		File pluginFolder = new File(PageDispatcherServlet.realPath, "WEB-INF"
				+ File.separator + "plugins");
		File pluginFile = new File(pluginFolder, pluginId + ".jar");
		URL url = null;
		try {
			url = pluginFile.toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		URL[] urls = { url };
		URLClassLoader classLoader = new URLClassLoader(urls, Thread
				.currentThread().getContextClassLoader());
		Lookup lookup = Lookups.metaInfServices(classLoader);
		Collection<? extends IPluginConfiguration> configs = lookup
				.lookupAll(IPluginConfiguration.class);
		for (IPluginConfiguration config : configs) {
			// make sure we get the right one
			// the line below will get the full path of the jar file of which
			// config is loaded from
			// the correct one would be having suffix plugin_<pluginId>.jar
			String pluginFilePath = config.getClass().getProtectionDomain()
					.getCodeSource().getLocation().getPath();
			if (pluginFilePath.endsWith(pluginId + ".jar")) {
				return config.getConfigurationOptions();
			}
		}
		return null;
	}

	private static int compareVersion(String firstVersionStr,
			String secondVersionStr) throws IllegalArgumentException {
		// a version string can be a sequence of number, separated by .
		String versionStrRegex = "(\\d+)(\\.\\d+)*";
		if (!firstVersionStr.matches(versionStrRegex)
				|| !secondVersionStr.matches(versionStrRegex))
			throw new IllegalArgumentException("Malformed version string");
		String[] firstVersion = firstVersionStr.split("\\.");
		String[] secondVersion = secondVersionStr.split("\\.");
		int minLen = (firstVersion.length < secondVersion.length) ? firstVersion.length
				: secondVersion.length;
		int maxLen = (firstVersion.length > secondVersion.length) ? firstVersion.length
				: secondVersion.length;

		// comparing version number starting from the left most portion (major
		// number)
		for (int i = 0; i < minLen; i++) {
			int first = Integer.parseInt(firstVersion[i]);
			int second = Integer.parseInt(secondVersion[i]);
			if (first < second)
				return -1;
			else if (first > second)
				return 1;
		}

		// comparing the rest with padded 0
		for (int i = minLen; i < maxLen; i++) {
			int first = 0;
			int second = 0;
			if (firstVersion.length > secondVersion.length) {
				first = Integer.parseInt(firstVersion[i]);
			} else {
				second = Integer.parseInt(secondVersion[i]);
			}
			if (first < second)
				return -1;
			else if (first > second)
				return 1;
		}

		// all portions equals
		return 0;
	}

	public static void main(String[] args) throws IOException {
		File tmpFile = File.createTempFile("tnt", ".jar");
		System.out.println("original file: " + tmpFile.getAbsolutePath());
		File newTmpFile = new File(tmpFile.getAbsolutePath() + ".old");
		tmpFile.renameTo(newTmpFile);
		System.out.println(tmpFile + " " + tmpFile.exists());
		System.out.println(newTmpFile + " " + newTmpFile.exists());
	}

}
