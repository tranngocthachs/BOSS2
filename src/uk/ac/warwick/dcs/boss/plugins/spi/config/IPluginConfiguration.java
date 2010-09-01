package uk.ac.warwick.dcs.boss.plugins.spi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import uk.ac.warwick.dcs.boss.frontend.PageDispatcherServlet;
import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.plugins.PluginNotConfigurableException;

public abstract class IPluginConfiguration {
	
	public abstract Collection<ConfigurationOption> getConfigurationOptions();

	public static Properties getConfiguration(String pluginId) throws IOException,
			PluginNotConfigurableException {
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

	public static void setConfiguration(String pluginId, Properties prop)
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
