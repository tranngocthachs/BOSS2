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
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModelDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class ModuleAdministratorsPage extends Page {
	
	public ModuleAdministratorsPage()
			throws PageLoadException {
		super("admin_module_administrators", AccessLevel.ADMIN);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {	
		IDAOSession f;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			f = df.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}

		
		// Get module
		String moduleString = pageContext.getParameter("module");
		if (moduleString == null) {
			throw new ServletException("No module parameter given");
		}
		Long moduleId = Long
				.valueOf(pageContext.getParameter("module"));

		
		// Ascertain sorting
		IPersonDAO.SortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("unique_identifier_asc")) {
				sortingType = IPersonDAO.SortingType.UNIQUE_IDENTIFIER_ASCENDING;
				templateContext.put("sorting", "unique_identifier_asc");
			} else if (pageContext.getParameter("sorting").equals("unique_identifier_desc")) {
				sortingType = IPersonDAO.SortingType.UNIQUE_IDENTIFIER_DESCENDING;
				templateContext.put("sorting", "unique_identifier_desc");
			} else if (pageContext.getParameter("sorting").equals("email_asc")) {
				sortingType = IPersonDAO.SortingType.EMAIL_ADDRESS_ASCENDING;
				templateContext.put("sorting", "email_asc");
			} else if (pageContext.getParameter("sorting").equals("email_desc")) {
				sortingType = IPersonDAO.SortingType.EMAIL_ADDRESS_DESCENDING;
				templateContext.put("sorting", "email_desc");
			}
		} else {
			sortingType = IPersonDAO.SortingType.NONE;
			templateContext.put("sorting", "");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			Module module = moduleDao.retrievePersistentEntity(moduleId);
			
			IModelDAO modelDao = f.getModelDAOInstance();
			Model model = modelDao.retrievePersistentEntity(module.getModelId());

			Collection<Person> result = moduleDao.fetchAdministrators(sortingType, moduleId);

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("model", model);
			templateContext.put("administrators", result);

			pageContext.renderTemplate(template, templateContext);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected POST");
	}

}
