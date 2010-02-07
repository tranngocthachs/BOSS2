package uk.ac.warwick.dcs.boss.frontend.sites.systempages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public class HomePage extends Page {

	public HomePage() throws PageLoadException {
		super("system_home", AccessLevel.NONE);
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
			IOException {
		if (pageContext.getParameter("x") != null) {
			int x = Integer.valueOf(pageContext.getParameter("x")) + 1;
			templateContext.put("x", x);
		} else {
			templateContext.put("x", 1);
		}
		pageContext.renderTemplate(template, templateContext);
	}

	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected POST");
	}
	
	

}
