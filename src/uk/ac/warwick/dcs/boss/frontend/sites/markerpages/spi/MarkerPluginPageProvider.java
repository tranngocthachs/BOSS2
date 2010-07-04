package uk.ac.warwick.dcs.boss.frontend.sites.markerpages.spi;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public abstract class MarkerPluginPageProvider extends Page {

	private MarkerPluginPageProvider(String pageTemplate,
			AccessLevel accessLevel) throws PageLoadException {
		super(pageTemplate, accessLevel);
	}
	
	public MarkerPluginPageProvider(String pageTemplate) throws PageLoadException {
		super(pageTemplate, AccessLevel.USER);
	}
	
	public MarkerPluginPageProvider() throws PageLoadException {
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
