package uk.ac.warwick.dcs.boss.frontend.sites.systempages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public class NotFoundPage extends Page {

	public NotFoundPage() throws PageLoadException {
		super("system_404", AccessLevel.NONE);
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
	IOException {
		templateContext.put("siteName", new String(pageContext.getSiteName()));
		templateContext.put("pageName", new String(pageContext.getPageName()));
		pageContext.renderTemplate(template, templateContext);
	}

	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		templateContext.put("siteName", new String(pageContext.getSiteName()));
		templateContext.put("pageName", new String(pageContext.getPageName()));
		pageContext.renderTemplate(template, templateContext);
	}
	
}
