package uk.ac.warwick.dcs.boss.frontend.sites.systempages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public class RegisterPage extends Page {

	public RegisterPage() throws PageLoadException {
		super("system_register", AccessLevel.NONE);
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
			IOException {
		templateContext.put("userNotFound", false);
		pageContext.renderTemplate(template, templateContext);
	}
	
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Not implemented yet");
	}
	

}
