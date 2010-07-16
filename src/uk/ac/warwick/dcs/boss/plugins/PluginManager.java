package uk.ac.warwick.dcs.boss.plugins;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;

import uk.ac.warwick.dcs.boss.frontend.PageDispatcherServlet;
import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;

public class PluginManager {
	public static final Attributes.Name PLUGIN_ID = new Attributes.Name(
			"BOSS-Plugin-Id");
	public static final Attributes.Name PLUGIN_NAME = new Attributes.Name(
			"BOSS-Plugin-Name");
	public static final Attributes.Name PLUGIN_AUTHOR = new Attributes.Name(
			"BOSS-Plugin-Author");
	public static final Attributes.Name PLUGIN_EMAIL = new Attributes.Name(
			"BOSS-Plugin-Email");
	public static final Attributes.Name PLUGIN_VERSION = new Attributes.Name(
			"BOSS-Plugin-Version");
	public static final Attributes.Name PLUGIN_DESCRIPTION = new Attributes.Name(
			"BOSS-Plugin-Description");

	public static PluginMetadata installPlugin(File pluginFile) throws IOException, InvalidPluginException {
		JarFile jarFile = null;
		Attributes atts = null;

		try {
			// we have the plugin in a jar file
			jarFile = new JarFile(pluginFile);
			atts = jarFile.getManifest().getMainAttributes();

			// manifest file is required to supplied at least plugin's id,
			// name, and version
			if (!atts.containsKey(PLUGIN_ID)
					|| !atts.containsKey(PLUGIN_NAME)
					|| !atts.containsKey(PLUGIN_VERSION)) {
				throw new IOException();
			}
		} catch (IOException e) {
			throw new InvalidPluginException("Supplied file is not a BOSS plugin");
		}

		// we have a valid plugin file (as far as MANIFEST file goes)
		String pluginId = atts.getValue(PluginManager.PLUGIN_ID);
		File webInfDir = new File(PageDispatcherServlet.realPath, "WEB-INF");
		File pluginFolder = new File(webInfDir, "plugins");

		// archiving the plugin file under WEB-INF/plugins/
		// since the pluginId is unique, there shouldn't be a collision
		// regarding name
		File pluginJarFile = new File(pluginFolder, pluginId + ".jar");
		FileUtils.copyFile(pluginFile, pluginJarFile);

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
		pluginMetadata.setPluginId(atts.getValue(PluginManager.PLUGIN_ID));
		pluginMetadata.setName(atts.getValue(PluginManager.PLUGIN_NAME));
		pluginMetadata.setVersion(atts.getValue(PluginManager.PLUGIN_VERSION));
		if (atts.containsKey(PluginManager.PLUGIN_AUTHOR))
			pluginMetadata.setAuthor(atts.getValue(PluginManager.PLUGIN_AUTHOR));
		if (atts.containsKey(PluginManager.PLUGIN_EMAIL))
			pluginMetadata.setEmail(atts.getValue(PluginManager.PLUGIN_EMAIL));
		if (atts.containsKey(PluginManager.PLUGIN_DESCRIPTION))
			pluginMetadata.setDescription(atts
					.getValue(PluginManager.PLUGIN_DESCRIPTION));
		if (!libFileNames.isEmpty()) {
			pluginMetadata.setLibFilenames(libFileNames.toArray(new String[0]));
		}

		// enable the plugin
		PluginManager.enablePlugin(pluginMetadata);
		
		return pluginMetadata;
	}

	public static void uninstallPlugin(PluginMetadata pluginInfo) {
		// if this plugin is currently enable, disable it first
		if (pluginInfo.getEnable())
			PluginManager.disablePlugin(pluginInfo);
		
		// delete plugin storage
		File webInfDir = new File(PageDispatcherServlet.realPath,
				"WEB-INF");
		File pluginStorageDir = new File(webInfDir, "plugins");
		File thisPluginJar = new File(pluginStorageDir,
				pluginInfo.getPluginId() + ".jar");
		thisPluginJar.delete();
	}

	public static void enablePlugin(PluginMetadata pluginInfo) throws IOException {
		String pluginId = pluginInfo.getPluginId();
		File webInfDir = new File(PageDispatcherServlet.realPath, "WEB-INF");
		// make the plugin active by copy the jar file into WEB-INF/lib
		File pluginFile = new File(webInfDir, "plugins" + File.separator + pluginId + ".jar");
		File webAppLibFolder = new File(webInfDir, "lib");
		File pluginJarFile = new File(webAppLibFolder, "plugin_" + pluginId
				+ ".jar");
		FileUtils.copyFile(pluginFile, pluginJarFile);

		// copy the dependencies of the plugin (residing under lib folder of
		// the plugin's jar file) if exists.
		JarFile jarFile = new JarFile(pluginFile);
		List<String> libFileNames = Arrays.asList(pluginInfo.getLibFilenames());
		Enumeration<JarEntry> enumeration = jarFile.entries();
		while (enumeration.hasMoreElements()) {
			JarEntry entry = enumeration.nextElement();
			String entryName = entry.getName();
			if (entryName.startsWith("lib/") && entryName.endsWith(".jar")) {
				String[] entryPathComps = entryName.split("/");
				String libFileName = entryPathComps[entryPathComps.length - 1];
				if (libFileNames.contains(libFileName)) {
					File destLibFile = new File(webAppLibFolder, "plugin_"
							+ pluginId + "_" + libFileName);
					InputStream in = null;
					OutputStream out = null;
					try {
						in = new BufferedInputStream(
								jarFile.getInputStream(entry));
						out = new BufferedOutputStream(new FileOutputStream(
								destLibFile));
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
		pluginInfo.setEnable(true);
	}

	public static void disablePlugin(PluginMetadata pluginInfo) {
		
		// delete main jar file in webapp's lib folder
		File mainJarFile = new File(PageDispatcherServlet.realPath, "WEB-INF" + File.separator + "lib" + File.separator + "plugin_" + pluginInfo.getPluginId() +".jar");
		mainJarFile.delete();
		
		// delete lib jar file also in webapp's lib folder
		String[] libFNs = pluginInfo.getLibFilenames();
		if (libFNs != null) {
			File webInfLibDir = new File(PageDispatcherServlet.realPath, "WEB-INF" + File.separator + "lib");
			for (int i = 0; i < libFNs.length; i++) {
				File libFile = new File(webInfLibDir, "plugin_" + pluginInfo.getPluginId() + "_" + libFNs[i]);
				System.out.println("Deleting " + libFile.getAbsolutePath());
				libFile.delete();
			}
		}
		pluginInfo.setEnable(false);
	}
}
