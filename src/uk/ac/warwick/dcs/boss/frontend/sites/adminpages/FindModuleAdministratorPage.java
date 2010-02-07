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
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModelDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

public class FindModuleAdministratorPage extends Page {

	public FindModuleAdministratorPage() throws PageLoadException {
		super("multi_find_person", AccessLevel.ADMIN);
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
		
		// Get id
		String moduleIdString = pageContext.getParameter("module");
		if (moduleIdString == null) {
			throw new ServletException("No module parameter given");
		}
		Long moduleId = Long.valueOf(moduleIdString);
				
		// Did we not specify something?
		if (pageContext.getParameter("missing") != null) {
			templateContext.put("missing", true);
		}
		
		try {
			f.beginTransaction();
			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			IModelDAO modelDao = f.getModelDAOInstance();
			
			Module module = moduleDao.retrievePersistentEntity(moduleId);
			Model model = modelDao.retrievePersistentEntity(module.getModelId());
			templateContext.put("module", module);
			templateContext.put("model", model);
			
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
		
		// Show page.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("personType", "module administrator");
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected POST");
	}

}
