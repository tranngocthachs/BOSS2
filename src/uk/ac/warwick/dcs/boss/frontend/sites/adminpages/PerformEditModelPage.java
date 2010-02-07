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
import uk.ac.warwick.dcs.boss.model.dao.IModelDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;

public class PerformEditModelPage extends Page {

	public PerformEditModelPage() throws PageLoadException {
		super("multi_edited", AccessLevel.ADMIN);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {

		throw new ServletException("unexpected GET");
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
		
		// defs
		Model incoming = new Model();

		if (pageContext.getParameter("model") == null) {
			throw new ServletException("missing model parameter");
		}
		incoming.setId(Long.valueOf(pageContext.getParameter("model")));

		if (pageContext.getParameter("uniq") == null) {
			throw new ServletException("missing uniq parameter");
		}	
		incoming.setUniqueIdentifier(pageContext.getParameter("uniq"));

		if (pageContext.getParameter("name") == null) {
			throw new ServletException("missing name parameter");
		}
		incoming.setName(pageContext.getParameter("name"));
	
		try {
			f.beginTransaction();

			if (pageContext.getParameter("create") != null) {
				// Create
				IModelDAO modelDao = f.getModelDAOInstance();
				incoming.setId(modelDao.createPersistentCopy(incoming));
			} else {	
				if (pageContext.getParameter("delete") != null) {
					// Delete
					IModelDAO modelDao = f.getModelDAOInstance();
					if (modelDao.hasModules(incoming.getId())) {
						throw new DAOException("entity has children");
					}
					
					modelDao.deletePersistentEntity(incoming.getId());
				} else {
					// Update
					IModelDAO modelDao = f.getModelDAOInstance();
					modelDao.updatePersistentEntity(incoming);
				}
			}

			// Done.
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}

		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("success", true);
		templateContext.put("nextPage", pageContext.getPageUrl(AdminPageFactory.SITE_NAME, AdminPageFactory.MODELS_PAGE));
		templateContext.put("nextPageParamName", "dummy");
		templateContext.put("nextPageParamValue", "nothing");
		pageContext.renderTemplate(template, templateContext);
	}
}
