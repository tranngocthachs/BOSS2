package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityDescription;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityFactory;
import uk.ac.warwick.dcs.boss.model.utilities.IAdminUtilityDirectory;

public class ExecuteUtilityPage extends Page {
	
	public ExecuteUtilityPage()
			throws PageLoadException {
		super("admin_execute_utility", AccessLevel.ADMIN);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		IAdminUtilityDirectory f;
		try {
			AdminUtilityFactory df = (AdminUtilityFactory)FactoryRegistrar.getFactory(AdminUtilityFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("admin utility init error", e);
		}
 
		
		if (pageContext.getParameter("utility") == null) {
			throw new ServletException("utility parameter was null");
		}
		String utilityClassName = pageContext.getParameter("utility").trim();
		

		Collection<AdminUtilityDescription> utilities = f.getAdminUtilityDescriptions();
		AdminUtilityDescription foundUtility = null;
		for (AdminUtilityDescription utility : utilities) {
			if (utility.getClassName().equals(utilityClassName)) {
				foundUtility = utility;
				break;
			}
		}
		
		if (foundUtility == null) {
			throw new ServletException("unknown utility class: " + utilityClassName);
		}
		
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("utility", foundUtility);

		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected POST");
	}	
}
