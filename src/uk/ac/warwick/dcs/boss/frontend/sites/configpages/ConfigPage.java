package uk.ac.warwick.dcs.boss.frontend.sites.configpages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;

public class ConfigPage extends Page {
	
	public ConfigPage() throws PageLoadException {
		super("system_config", AccessLevel.NONE);
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
			IOException {
		templateContext.put("options", FactoryRegistrar.knownConfigurationOptions());
		pageContext.renderTemplate(template, templateContext);
	}

	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected POST");
	}

}
