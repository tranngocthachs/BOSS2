package uk.ac.warwick.dcs.boss.frontend.sites.systempages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public class NotAdminPage extends Page {

	public NotAdminPage() throws PageLoadException {
		super("system_not_admin", AccessLevel.NONE);
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
			IOException {
		templateContext.put("chosenName", pageContext.getSession().getPersonBinding().getChosenName());
		pageContext.renderTemplate(template, templateContext);
	}
	
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected POST");
	}
	

}
