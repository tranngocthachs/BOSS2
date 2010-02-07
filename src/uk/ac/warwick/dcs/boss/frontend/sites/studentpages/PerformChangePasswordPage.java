package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.StudentPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.session.ISessionAuthenticator;
import uk.ac.warwick.dcs.boss.model.session.SessionAutenticatorFactory;
import uk.ac.warwick.dcs.boss.model.session.SessionException;
import uk.ac.warwick.dcs.boss.model.session.UserNotFoundException;

public class PerformChangePasswordPage extends Page {

	public PerformChangePasswordPage() throws PageLoadException {
		super("multi_edited", AccessLevel.NONE);
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected GET");
	}
	
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		ISessionAuthenticator authenticator;
		try {
			SessionAutenticatorFactory sf = (SessionAutenticatorFactory)FactoryRegistrar.getFactory(SessionAutenticatorFactory.class);
			authenticator = sf.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("factory error", e);
		}
		
		// Check for parameters..
		if (pageContext.getParameter("password") == null || pageContext.getParameter("verify") == null) {
			pageContext.performRedirect(pageContext.getPageUrl(StudentPageFactory.SITE_NAME, StudentPageFactory.DETAILS_PAGE) + "?missing=true");
			return;
		}
		
		// Check passwords equal
		if (!pageContext.getParameter("password").equals(pageContext.getParameter("verify"))) {
			pageContext.performRedirect(pageContext.getPageUrl(StudentPageFactory.SITE_NAME, StudentPageFactory.DETAILS_PAGE) + "?mismatch=true");
			return;
		}
		String newPassword = pageContext.getParameter("password");
		
		// Attempt to set new password
		try {
			authenticator.updatePassword(pageContext.getSession().getPersonBinding(), newPassword);
			pageContext.log(Level.WARN, "user password manually changed: " + pageContext.getSession().getPersonBinding().getUniqueIdentifier());
		} catch (UserNotFoundException e) {
			pageContext.log(Level.ERROR, "user password not manually changed due to incorrect credentials: " + pageContext.getSession().getPersonBinding().getUniqueIdentifier());
			throw new ServletException("session error", e);			
		} catch (SessionException e) {
			pageContext.log(Level.WARN, "user password not manually changed due to error: " + pageContext.getSession().getPersonBinding().getUniqueIdentifier());
			throw new ServletException("session error", e);
		}

		// Display page
		templateContext.put("success", true);
		templateContext.put("nextPage", pageContext.getPageUrl(StudentPageFactory.SITE_NAME, StudentPageFactory.DETAILS_PAGE));
		templateContext.put("nextPageParamName", "updated_password");
		templateContext.put("nextPageParamValue", "true");
		pageContext.renderTemplate(template, templateContext);		

	}
	

}
