package uk.ac.warwick.dcs.boss.plugins;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;
import uk.ac.warwick.dcs.boss.plugins.spi.config.PluginConfiguration;

public class ConfigPluginPage extends Page {

	public ConfigPluginPage() throws PageLoadException {
		super("admin_config_plugin", AccessLevel.ADMIN);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		IDAOSession f;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}
		String pluginStr = pageContext.getParameter("plugin");
		if (pluginStr == null)
			throw new ServletException("Unexpected GET request");
		Long pluginId = Long.valueOf(pluginStr);
		PluginMetadata pluginMetadata = null;
		try {
			f.beginTransaction();		
			pluginMetadata = f.getPluginMetadataDAOInstance().retrievePersistentEntity(pluginId);
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
		
		String pId = pluginMetadata.getPluginId();
		try {
			templateContext.put("prop", PluginConfiguration.getConfiguration(pluginMetadata.getPluginId()));
			templateContext.put("options", PluginManager.getPluginConfigOption(pluginMetadata.getPluginId()));
			templateContext.put("pName", pluginMetadata.getName());
			templateContext.put("pId", pluginMetadata.getPluginId());
		}
		catch (PluginNotConfigurableException e) {
			throw new ServletException("plugin " + pId + " is not configurable");
		}

		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}

}
