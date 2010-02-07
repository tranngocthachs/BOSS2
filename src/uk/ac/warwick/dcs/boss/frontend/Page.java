package uk.ac.warwick.dcs.boss.frontend;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import uk.ac.warwick.dcs.boss.frontend.sites.HelpPageFactory;
import uk.ac.warwick.dcs.boss.frontend.sites.SystemPageFactory;

/**
 * A Page object represents a single URL that a person may visit.
 * @author davidbyard
 */
public abstract class Page {
	
	/**
	 * Access levels that a page may be accessed at.
	 * @author davidbyard
	 *
	 */
	public enum AccessLevel {
		NONE, USER, ADMIN
	}

	/**
	 * Velocity template associated with page.
	 */
	private Template template = null;
	
	/**
	 * Help file associated with page.
	 */
	private String helpFile = null;
	
	/**
	 * Velocity context associated with page.
	 */
	private VelocityContext templateContext = null;
	
	/**
	 * Access level required to access page.
	 */
	private AccessLevel accessLevel = null;

	/**
	 * Constructor for a page, called by subclasses.
	 * @param pageTemplate is the template to use (the page template will have .vm.html appended, and the help file .help.html appended)
	 * @param accessLevel is the level required to access the page.
	 * @throws PageLoadException when the page couldn't be loaded.
	 */
	public Page(String pageTemplate, AccessLevel accessLevel) throws PageLoadException {	
		this.templateContext = new VelocityContext();
		this.accessLevel = accessLevel;
		this.helpFile = pageTemplate;
		
		try
		{
			template = Velocity.getTemplate(pageTemplate + ".vm.html");
		}
		catch (ResourceNotFoundException e)
		{
			throw new PageLoadException(500, "Template resource not found", e);
		}
		catch (ParseErrorException e)
		{
			throw new PageLoadException(500, "Template could not be parsed", e);
		}
		catch (MethodInvocationException e)
		{
			throw new PageLoadException(500, "Template-induced exception caught", e);
		}
		catch (Exception e)
		{
			throw new PageLoadException(500, "Misc. template exception", e);			
		}
	}
		
	/**
	 * Handle a GET request.
	 * Check access level, store initial context in the Velocity context, then forward to the subclass.
	 * @param pageContext is the initial page context.
	 * @throws ServletException
	 * @throws IOException
	 */
	public void internalHandleGet(PageContext pageContext)
	throws ServletException, IOException {
		if (accessLevel != AccessLevel.NONE && pageContext.getSession() == null) {
			pageContext.performRedirect(pageContext.getLoginUrl(pageContext.getFullCurrentUrl()));
		} else if (accessLevel == AccessLevel.ADMIN && !pageContext.getSession().getPersonBinding().isAdministrator()) {
			pageContext.performRedirect(pageContext.getPageUrl(SystemPageFactory.SITE_NAME, SystemPageFactory.NOT_ADMIN_PAGE));
		} else {
			templateContext.put("context", pageContext);
			templateContext.put("helpUrl", pageContext.getPageUrl(HelpPageFactory.SITE_NAME, helpFile));
			try {
				handleGet(pageContext, template, templateContext);
			} catch (ServletException e) {
				pageContext.log(Level.ERROR, e);
				throw e;
			} catch (IOException e) {
				pageContext.log(Level.ERROR, e);
				throw e;
			}
		}
	}

	/**
	 * Handle a POST request.
	 * Check access level, store initial context in the Velocity context, then forward to the subclass.
	 * @param pageContext is the initial page context.
	 * @throws ServletException
	 * @throws IOException
	 */
	public void internalHandlePost(PageContext pageContext)
	throws ServletException, IOException {
		if (accessLevel != AccessLevel.NONE && pageContext.getSession() == null) {
			pageContext.performRedirect(pageContext.getLoginUrl(pageContext.getFullCurrentUrl()));
		} else if (accessLevel == AccessLevel.ADMIN && !pageContext.getSession().getPersonBinding().isAdministrator()) {
			pageContext.performRedirect(pageContext.getPageUrl(SystemPageFactory.SITE_NAME, SystemPageFactory.NOT_ADMIN_PAGE));
		} else {
			templateContext.put("context", pageContext);
			templateContext.put("helpUrl", pageContext.getPageUrl(HelpPageFactory.SITE_NAME, helpFile));
			try {
				handlePost(pageContext, template, templateContext);
			} catch (ServletException e) {
				pageContext.log(Level.ERROR, e);
				throw e;
			} catch (IOException e) {
				pageContext.log(Level.ERROR, e);
				throw e;
			}
		}
	}

	
	abstract protected void handleGet(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException, IOException;
	abstract protected void handlePost(PageContext pageContext, Template template, VelocityContext templateContext) throws ServletException, IOException;
}
