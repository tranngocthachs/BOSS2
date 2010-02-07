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
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

public class PerformEditModulePage extends Page {

	public PerformEditModulePage() throws PageLoadException {
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
		Module incoming = new Module();

		if (pageContext.getParameter("model") == null) {
			throw new ServletException("missing model parameter");
		}
		incoming.setModelId(Long.valueOf(pageContext.getParameter("model")));
		
		if (pageContext.getParameter("module") == null) {
			throw new ServletException("missing module parameter");
		}
		incoming.setId(Long.valueOf(pageContext.getParameter("module")));

		if (pageContext.getParameter("uniq") == null) {
			throw new ServletException("missing uniq parameter");
		}	
		incoming.setUniqueIdentifier(pageContext.getParameter("uniq"));

		if (pageContext.getParameter("name") == null) {
			throw new ServletException("missing name parameter");
		}
		incoming.setName(pageContext.getParameter("name"));
	
		if (pageContext.getParameter("registration_required") == null) {
			incoming.setRegistrationRequired(false);
		} else {
			incoming.setRegistrationRequired(true);
		}
		
		try {
			f.beginTransaction();

			if (pageContext.getParameter("create") != null) {			
				// Create
				IModuleDAO moduleDao = f.getModuleDAOInstance();
				incoming.setId(moduleDao.createPersistentCopy(incoming));
			} else {	
				if (pageContext.getParameter("delete") != null) {
					// Delete
					IModuleDAO moduleDao = f.getModuleDAOInstance();
					
					if (moduleDao.hasAssignments(incoming.getId())) {
						throw new DAOException("entity has children");
					}
					
					moduleDao.deletePersistentEntity(incoming.getId());
				} else {
					// Update
					IModuleDAO moduleDao = f.getModuleDAOInstance();
					moduleDao.updatePersistentEntity(incoming);
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
		templateContext.put("nextPage", pageContext.getPageUrl(AdminPageFactory.SITE_NAME, AdminPageFactory.MODULES_PAGE));
		templateContext.put("nextPageParamName", "model");
		templateContext.put("nextPageParamValue", incoming.getModelId());
		pageContext.renderTemplate(template, templateContext);
	}
}
