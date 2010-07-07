package uk.ac.warwick.dcs.boss.plugins.spi.pages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public abstract class AdminPluginPageProvider extends Page {

	private AdminPluginPageProvider(String pageTemplate,
			AccessLevel accessLevel) throws PageLoadException {
		super(pageTemplate, accessLevel);
	}
	
	public AdminPluginPageProvider(String pageTemplate) throws PageLoadException {
		super(pageTemplate, AccessLevel.ADMIN);
	}
	
	public AdminPluginPageProvider() throws PageLoadException {
		this(null, null);
	}

	public abstract String getName();
	
	@Override
	protected abstract void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException;
	
	@Override
	protected abstract void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException;
}
