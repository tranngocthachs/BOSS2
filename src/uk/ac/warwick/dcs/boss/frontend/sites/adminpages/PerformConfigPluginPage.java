package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.AdminPageFactory;
import uk.ac.warwick.dcs.boss.plugins.PluginManager;
import uk.ac.warwick.dcs.boss.plugins.PluginNotConfigurableException;

public class PerformConfigPluginPage extends Page {

	public PerformConfigPluginPage()
			throws PageLoadException {
		super("multi_edited", AccessLevel.ADMIN);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected GET request");
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		String pluginId = pageContext.getParameter("plugin");
		if (pluginId == null)
			throw new ServletException("Unexpected POST request");
		
		try {
			Properties prop = PluginManager.getConfiguration(pluginId);
			for(Object key : prop.keySet()) {
				prop.setProperty((String)key, pageContext.getParameter((String)key));
			}
			PluginManager.setConfiguration(pluginId, prop);
		} catch (PluginNotConfigurableException e) {
			throw new ServletException("plugin " + pluginId + " is not configurable");
		}
		templateContext.put("greet", pageContext.getSession()
				.getPersonBinding().getChosenName());
		templateContext.put("success", true);
		templateContext.put("nextPage", pageContext.getPageUrl(AdminPageFactory.SITE_NAME, AdminPageFactory.PLUGINS_PAGE));
		templateContext.put("nextPageParamName", "dummy");
		templateContext.put("nextPageParamValue", "nothing");
		pageContext.renderTemplate(template, templateContext);
	}

}
