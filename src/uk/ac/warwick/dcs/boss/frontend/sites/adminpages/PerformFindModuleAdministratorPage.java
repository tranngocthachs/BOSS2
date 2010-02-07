package uk.ac.warwick.dcs.boss.frontend.sites.adminpages;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.StaffPageFactory;
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

public class PerformFindModuleAdministratorPage extends Page {

	public PerformFindModuleAdministratorPage() throws PageLoadException {
		super("multi_found_person", AccessLevel.ADMIN);
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
		
		// Get id
		String moduleIdString = pageContext.getParameter("module");
		if (moduleIdString == null) {
			throw new ServletException("No module parameter given");
		}
		Long moduleId = Long.valueOf(moduleIdString);
		
		String nameCriterion = pageContext.getParameter("name");
		String uniqCriterion = pageContext.getParameter("uniq");
		if (nameCriterion == null && uniqCriterion == null) {
			pageContext.performRedirect(pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.FIND_MARKER_PAGE) + "?assignment=" + moduleId);
			return;
		}
		
		// Perform search
		try {
			f.beginTransaction();
			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			IModelDAO modelDao = f.getModelDAOInstance();
			
			Module module = moduleDao.retrievePersistentEntity(moduleId);
			Model model = modelDao.retrievePersistentEntity(module.getModelId());
				
			templateContext.put("module", module);
			templateContext.put("model", model);
			
			IPersonDAO personDao = f.getPersonDAOInstance();
			Person example = new Person();
			if (nameCriterion != null) {
				example.setChosenName("%" + nameCriterion + "%");
			}
			if (uniqCriterion != null) {
				example.setUniqueIdentifier("%" + uniqCriterion + "%");
			}
			Collection<Person> results = personDao.findPersistentEntitiesByWildcards(example);
			
			f.endTransaction();
			
			templateContext.put("results", results);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("DAO error", e);
		}
		
		// Show page.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("personType", "module administrator");
		pageContext.renderTemplate(template, templateContext);
	}

}
