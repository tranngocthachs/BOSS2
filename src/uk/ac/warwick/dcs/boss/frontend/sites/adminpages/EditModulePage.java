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

public class EditModulePage extends Page {

	public EditModulePage() throws PageLoadException {
		super("admin_edit_module", AccessLevel.ADMIN);
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
		
		// defs
		Module original;
		Model parent;
				
		try {
			if (pageContext.getParameter("create") != null) {
				// Get moduleId
				String modelString = pageContext.getParameter("model");
				if (modelString == null) {
					throw new ServletException("No model parameter given");
				}
				Long modelId = Long
					.valueOf(pageContext.getParameter("model"));
							
				original = new Module();
				original.setId(-1L);
				original.setUniqueIdentifier("module");
				original.setName("General Studies");
				original.setRegistrationRequired(true);
				original.setModelId(modelId);
				
				templateContext.put("create", true);
			} else {	
				// Get moduleId
				String moduleString = pageContext.getParameter("module");
				if (moduleString == null) {
					throw new ServletException("No module parameter given");
				}
				Long moduleId = Long
					.valueOf(pageContext.getParameter("module"));
				
				f.beginTransaction();		
				IModuleDAO moduleDao = f.getModuleDAOInstance();
				original = moduleDao.retrievePersistentEntity(moduleId);
				templateContext.put("hasChildren", moduleDao.hasAssignments(moduleId));
				f.endTransaction();
				
				templateContext.put("create", false);
			}			
			
			f.beginTransaction();
			IModelDAO modelDao = f.getModelDAOInstance();
			parent = modelDao.retrievePersistentEntity(original.getModelId());
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
				
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("module", original);
		templateContext.put("model", parent);
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}
	
}
