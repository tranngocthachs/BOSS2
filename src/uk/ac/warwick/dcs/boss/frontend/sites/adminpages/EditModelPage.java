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
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;

public class EditModelPage extends Page {

	public EditModelPage() throws PageLoadException {
		super("admin_edit_model", AccessLevel.ADMIN);
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
		Model original;
				
		try {
			if (pageContext.getParameter("create") != null) {			
				original = new Model();
				original.setId(-1L);
				original.setUniqueIdentifier("model");
				original.setName("Curriculum");
				
				templateContext.put("create", true);
			} else {	
				// Get personId
				String modelString = pageContext.getParameter("model");
				if (modelString == null) {
					throw new ServletException("No person parameter given");
				}
				Long modelId = Long
					.valueOf(pageContext.getParameter("model"));
				
				f.beginTransaction();		
				IModelDAO modelDao = f.getModelDAOInstance();
				original = modelDao.retrievePersistentEntity(modelId);
				templateContext.put("hasChildren", modelDao.hasModules(modelId));
				f.endTransaction();
				
				templateContext.put("create", false);
			}			
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
				
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("model", original);
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}
	
}
