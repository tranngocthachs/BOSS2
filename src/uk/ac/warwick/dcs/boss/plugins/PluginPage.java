package uk.ac.warwick.dcs.boss.plugins;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public class PluginPage extends Page {

	private IPluginPage provider = null;
	
	public PluginPage(IPluginPage provider)
			throws PageLoadException {
		super(provider.getPageTemplate(), provider.getAccessLevel());
		this.provider = provider;
	}

	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		provider.handleGet(pageContext, template, templateContext);
	}

	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		provider.handlePost(pageContext, template, templateContext);
	}

}
