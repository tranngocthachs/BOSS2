package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.AdminPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;

public class PerformRemoveModuleAdministratorPage extends Page {

	public PerformRemoveModuleAdministratorPage() throws PageLoadException {
		super("multi_edited", AccessLevel.ADMIN);
	}
	
	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected GET");
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		IDAOSession f;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}
		
		// Get IDs
		String moduleIdString = pageContext.getParameter("module");
		if (moduleIdString == null) {
			throw new ServletException("No module parameter given");
		}
		Long moduleId = Long.valueOf(moduleIdString);
		
		String personIdString = pageContext.getParameter("administrator");
		if (personIdString == null) {
			throw new ServletException("No administrator parameter given");
		}
		Long personId = Long.valueOf(personIdString);
		
		// Perform remove
		try {
			f.beginTransaction();
			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			
			moduleDao.removeAdministratorAssociation(moduleId, personId);
			
			f.endTransaction();			
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("DAO error", e);
		}
		
		// Show page.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("nextPage", pageContext.getPageUrl(AdminPageFactory.SITE_NAME, AdminPageFactory.MODULE_ADMINISTRATORS_PAGE));
		templateContext.put("nextPageParamName", "module");
		templateContext.put("nextPageParamValue", moduleId);
		templateContext.put("success", true);
		
		pageContext.renderTemplate(template, templateContext);
	}

}
