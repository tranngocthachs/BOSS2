package uk.ac.warwick.dcs.boss.frontend.sites.systempages;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.SystemPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.mail.IMailSender;
import uk.ac.warwick.dcs.boss.model.mail.MailException;
import uk.ac.warwick.dcs.boss.model.mail.MailFactory;
import uk.ac.warwick.dcs.boss.model.session.ISessionAuthenticator;
import uk.ac.warwick.dcs.boss.model.session.SessionException;
import uk.ac.warwick.dcs.boss.model.session.SessionAutenticatorFactory;
import uk.ac.warwick.dcs.boss.model.session.UserNotFoundException;

public class PerformNewPasswordPage extends Page {

	Template emailTemplate = null;
	
	public PerformNewPasswordPage() throws PageLoadException {
		super("multi_edited", AccessLevel.NONE);
		try
		{
			emailTemplate = Velocity.getTemplate("email_newpassword.vm.txt");
		}
		catch (ResourceNotFoundException e)
		{
			throw new PageLoadException(500, "Email template resource not found", e);
		}
		catch (ParseErrorException e)
		{
			throw new PageLoadException(500, "Email template could not be parsed", e);
		}
		catch (MethodInvocationException e)
		{
			throw new PageLoadException(500, "Email template-induced exception caught", e);
		}
		catch (Exception e)
		{
			throw new PageLoadException(500, "Misc. email template exception", e);			
		}
	}

	public void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected GET");
	}
	
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		IDAOSession f;
		ISessionAuthenticator authenticator;
		IMailSender mailSender;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			SessionAutenticatorFactory sf = (SessionAutenticatorFactory)FactoryRegistrar.getFactory(SessionAutenticatorFactory.class);
			MailFactory mf = (MailFactory)FactoryRegistrar.getFactory(MailFactory.class);
			f = df.getInstance();
			mailSender = mf.getInstance();
			authenticator = sf.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("factory error", e);
		}
		
		// Check for invalid captcha
		String expectedCaptchaString = pageContext.getCaptchaString();
		
		if (pageContext.getParameter("captcha") == null) {
			pageContext.performRedirect(pageContext.getPageUrl(SystemPageFactory.SITE_NAME, SystemPageFactory.NEW_PASSWORD_PAGE) + "?bad_captcha=true");
			return;
		}
		
		if (!pageContext.getParameter("captcha").equals(expectedCaptchaString)) {
			pageContext.performRedirect(pageContext.getPageUrl(SystemPageFactory.SITE_NAME, SystemPageFactory.NEW_PASSWORD_PAGE) + "?bad_captcha=true");
			return;
		}
		
		// Check for parameters..
		if (pageContext.getParameter("uniq") == null || pageContext.getParameter("email") == null) {
			pageContext.performRedirect(pageContext.getPageUrl(SystemPageFactory.SITE_NAME, SystemPageFactory.NEW_PASSWORD_PAGE) + "?missing=true");
			return;
		}
		
		// Obtain individual
		Person person = null;
		try {
			f.beginTransaction();
			IPersonDAO dao = f.getPersonDAOInstance();
			person = dao.fetchPersonWithUniqueIdentifier(pageContext.getParameter("uniq"));
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
			
		if (person == null) {
			pageContext.performRedirect(pageContext.getPageUrl(SystemPageFactory.SITE_NAME, SystemPageFactory.NEW_PASSWORD_PAGE) + "?not_found=true");
			return;
		} else if (!person.getEmailAddress().equals(pageContext.getParameter("email"))) {
			pageContext.performRedirect(pageContext.getPageUrl(SystemPageFactory.SITE_NAME, SystemPageFactory.NEW_PASSWORD_PAGE) + "?not_found=true");
			return;		
		}
		
		// Generate a new password
		final String passwordPartsListOne[] = {
			"a", "e", "i", "o", "ou", "oo", "ee"
		};
		final String passwordPartsListTwo[] = {
			"b", "c", "ch", "d", "f", "g", "gr", "h", "j", "k", "kr",
			"l", "m", "n", "p", "pr", "r", "s", "sh", "st",
			"t", "tr", "v", "w", "z"
		};
		final String passwordPartsListThree[] = {
			"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"
		};
		
		Random random = new Random(new Date().getTime());
		StringBuffer newPassword = new StringBuffer();
		
		for (int i = 0; i < 3 + random.nextInt(2); i++) {
			newPassword.append(passwordPartsListTwo[random.nextInt(passwordPartsListTwo.length)]);
			newPassword.append(passwordPartsListOne[random.nextInt(passwordPartsListOne.length)]);
		}

		for (int i = 0; i < 2 + random.nextInt(2); i++) {
			newPassword.append(passwordPartsListThree[random.nextInt(passwordPartsListThree.length)]);
		}

		// Attempt to set new password
		try {
			authenticator.updatePassword(person, newPassword.toString());
			pageContext.log(Level.WARN, "user password updated: " + person.getUniqueIdentifier());
		} catch (UserNotFoundException e) {
			pageContext.performRedirect(pageContext.getPageUrl(SystemPageFactory.SITE_NAME, SystemPageFactory.NEW_PASSWORD_PAGE) + "?not_found=true");
		} catch (SessionException e) {
			pageContext.log(Level.ERROR, "user password update failed: " + person.getUniqueIdentifier());
			throw new ServletException("session error", e);
		}

		// Attempt to email new password.
		VelocityContext emailContext = new VelocityContext();
		emailContext.put("person", person);
		emailContext.put("newPassword", newPassword);
		
		StringWriter pw = new StringWriter();
		emailTemplate.merge(emailContext, pw);
		pw.close();

		try {
			mailSender.sendMail(
					person.getEmailAddress(),
					"Password Update",
					pw.toString());
			pageContext.log(Level.INFO, "new password email sent: " + person.getEmailAddress());
		} catch (MailException e) {
			pageContext.log(Level.ERROR, "new password email NOT sent: " + person.getEmailAddress());
			throw new ServletException("mail error", e);
		}

		// Display page
		templateContext.put("success", true);
		templateContext.put("nextPage", pageContext.getPageUrl(SystemPageFactory.SITE_NAME, SystemPageFactory.LOGIN_PAGE));
		templateContext.put("nextPageParamName", "requested_new");
		templateContext.put("nextPageParamValue", "true");
		pageContext.renderTemplate(template, templateContext);		
	}
	

}
