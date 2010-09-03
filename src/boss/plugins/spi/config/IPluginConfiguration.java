package boss.plugins.spi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import boss.plugins.PluginNotConfigurableException;

import uk.ac.warwick.dcs.boss.frontend.PageDispatcherServlet;
import uk.ac.warwick.dcs.boss.model.ConfigurationOption;

/**
 * Contract for plugin configurations. Plugin will have to extends this to
 * provide configurable options for its execution. Once installed,
 * configurations can be viewed and set in the admin > plugin management
 * interface.
 * 
 * @author tranngocthachs
 * 
 */
public abstract class IPluginConfiguration {

	/**
	 * Get a list of ConfigurationOption instances where each represents an
	 * option/property of the plugin (each has name, default value, and
	 * description but not the actual/current value of the property). Plugin
	 * must implement this method to indicate the configurable options
	 * associated with its execution. During execution, plugin can call get and
	 * set its properties using {@link #getConfiguration(String pluginId)} and
	 * {@link #setConfiguration(String, Properties)}
	 * 
	 * @return list of configurable options
	 */
	public abstract Collection<ConfigurationOption> getConfigurationOptions();

	/**
	 * get current properties of the plugin identified by pluginId
	 * 
	 * @param pluginId
	 * @return the requested properties
	 * @throws IOException
	 * @throws PluginNotConfigurableException
	 */
	public static final Properties getConfiguration(String pluginId)
			throws IOException, PluginNotConfigurableException {
		Properties prop = new Properties();
		File propFile = new File(PageDispatcherServlet.realPath, "WEB-INF"
				+ File.separator + "plugins" + File.separator + pluginId
				+ ".properties");
		if (propFile.exists()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(propFile);
				prop.load(in);
			} finally {
				if (in != null)
					in.close();
			}
		} else
			throw new PluginNotConfigurableException("plugin " + pluginId
					+ " is not configurable");
		return prop;
	}

	/**
	 * set the current properties/configurations of the plugin identified by
	 * pluginId
	 * 
	 * @param pluginId
	 *            the plugin id
	 * @param prop
	 *            the properties to set
	 * @throws IOException
	 * @throws PluginNotConfigurableException
	 */
	public static final void setConfiguration(String pluginId, Properties prop)
			throws IOException, PluginNotConfigurableException {
		File propFile = new File(PageDispatcherServlet.realPath, "WEB-INF"
				+ File.separator + "plugins" + File.separator + pluginId
				+ ".properties");
		if (propFile.exists()) {
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(propFile);
				prop.store(out, null);
			} finally {
				if (out != null)
					out.close();
			}
		} else
			throw new PluginNotConfigurableException("plugin " + pluginId
					+ " is not configurable");
	}
}
