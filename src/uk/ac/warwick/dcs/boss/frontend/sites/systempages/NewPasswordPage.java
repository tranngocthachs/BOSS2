package uk.ac.warwick.dcs.boss.frontend.sites.systempages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public class NewPasswordPage extends Page {

	public NewPasswordPage() throws PageLoadException {
		super("system_new_password", AccessLevel.NONE);
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
			IOException {
		templateContext.put("badCaptcha", pageContext.getParameter("bad_captcha") != null);
		templateContext.put("missingFields", pageContext.getParameter("missing") != null);
		templateContext.put("userNotFound", pageContext.getParameter("not_found") != null);
		pageContext.renderTemplate(template, templateContext);
	}
	
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Not implemented yet");
	}
	

}
