package uk.ac.warwick.dcs.boss.frontend.sites.systempages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.SystemPageFactory;
import uk.ac.warwick.dcs.boss.model.session.SessionException;
import uk.ac.warwick.dcs.boss.model.session.UserNotFoundException;

public class LoginPage extends Page {

	public LoginPage() throws PageLoadException {
		super("system_login", AccessLevel.NONE);
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
			IOException {
		templateContext.put("userNotFound", false);
		templateContext.put("formUrl", pageContext.getFullCurrentUrl());
		pageContext.renderTemplate(template, templateContext);
		
	}
	
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		if (pageContext.getParameter("username") != null && pageContext.getParameter("password") != null) {
			try {
				pageContext.performLogin("en", (String)pageContext.getParameter("username"), (String)pageContext.getParameter("password"));
				if (pageContext.getSession().getPersonBinding().isAdministrator()) {
					pageContext.log(Level.INFO, "Successful user login");
				} else {
					pageContext.log(Level.INFO, "Successful admin login");					
				}
			} catch (UserNotFoundException e) {
				pageContext.log(Level.WARN, "Failed login for " + pageContext.getParameter("username"));
				templateContext.put("userNotFound", true);
				templateContext.put("formUrl", pageContext.getFullCurrentUrl());
				pageContext.renderTemplate(template, templateContext);
				return;
			} catch (SessionException e) {
				throw new ServletException("session error", e);
			}
			
			if (pageContext.getParameter("redirectUrl") != null) {
				pageContext.performRedirect(pageContext.getParameter("redirectUrl"));
				return;
			} else {
				pageContext.performRedirect(pageContext.getPageUrl(
						SystemPageFactory.SITE_NAME, SystemPageFactory.LOGGED_IN_PAGE));
				return;
			}
		} else {
			templateContext.put("userNotFound", true);
			templateContext.put("formUrl", pageContext.getFullCurrentUrl());
			pageContext.renderTemplate(template, templateContext);
		}
	}
	

}
