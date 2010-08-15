package uk.ac.warwick.dcs.boss.plugins;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page.AccessLevel;
import uk.ac.warwick.dcs.boss.frontend.PageContext;

public abstract class PluginPageProvider {

	public abstract String getPageName();
	
	public abstract String getPageTemplate();
	
	public AccessLevel getAccessLevel() {
		return AccessLevel.USER;
	}
	
	public abstract void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException, IOException;
	public abstract void handlePost(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException, IOException;
}
