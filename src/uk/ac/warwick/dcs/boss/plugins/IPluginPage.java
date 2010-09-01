package uk.ac.warwick.dcs.boss.plugins;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page.AccessLevel;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.plugins.spi.pages.IMarkerPluginPage;
import uk.ac.warwick.dcs.boss.plugins.spi.pages.IStaffPluginPage;

/**
 * Base class for contracts of plugin page controllers. Plugin should NOT
 * extends this. Instead, use the child classes.
 * 
 * @author tranngocthachs
 * @see IStudentPluginPage
 * @see IStaffPluginPage
 * @see IMarkerPluginPage
 * @see IAdminPluginPage
 */
public abstract class IPluginPage {

	/**
	 * Get the page name. Plugin page should override this to indicate its page
	 * name.
	 * 
	 * @return the page name
	 */
	public abstract String getPageName();

	/**
	 * Get the name of the template file. Template file must be in the root
	 * folder of plugin's jar file and must end with ".vm.html". Typically, the
	 * template file will be get by getResource("/"+getPageTemplate()+".vm.html").
	 * 
	 * @return name of the template file
	 */
	public abstract String getPageTemplate();

	/**
	 * Get the access level of this page. NONE means no login will be required. USER means login will be required. ADMIN means the authenticated user has to be admin to see this page. This method by default will specify all plugin pages to has USER access level. Subclass can override to get the desired effects. 
	 * @return the access level of this page
	 */
	public AccessLevel getAccessLevel() {
		return AccessLevel.USER;
	}

	/**
	 * handling HTTP GET request
	 * @param pageContext the current page context
	 * @param template the source template
	 * @param templateContext the dynamic content of the template
	 * @throws ServletException
	 * @throws IOException
	 */
	public abstract void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException;

	/**
	 * handling HTTP POST request
	 * @param pageContext the current page context
	 * @param template the source template
	 * @param templateContext the dynamic content of the template
	 * @throws ServletException
	 * @throws IOException
	 */
	public abstract void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException;
}
