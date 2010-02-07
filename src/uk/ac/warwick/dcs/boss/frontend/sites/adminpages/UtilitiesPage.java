package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityFactory;
import uk.ac.warwick.dcs.boss.model.utilities.IAdminUtilityDirectory;

public class UtilitiesPage extends Page {
	
	public UtilitiesPage()
			throws PageLoadException {
		super("admin_utilities", AccessLevel.ADMIN);
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
		
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("utilities", f.getAdminUtilityDescriptions());

		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected POST");
	}	
}
