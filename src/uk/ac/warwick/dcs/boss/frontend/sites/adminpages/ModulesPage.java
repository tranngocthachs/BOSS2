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
import uk.ac.warwick.dcs.boss.model.dao.IAdminInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModelDAO;
import uk.ac.warwick.dcs.boss.model.dao.IAdminInterfaceQueriesDAO.AdminModulesQuerySortingType;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.AdminModulesQueryResult;

public class ModulesPage extends Page {
	
	public ModulesPage()
			throws PageLoadException {
		super("admin_modules", AccessLevel.ADMIN);
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
		
		// Ascertain model ID.
		Long modelId = null;
		if (pageContext.getParameter("model") == null) {
			throw new ServletException("no model parameter given");
		} else {
			modelId = Long.valueOf(pageContext.getParameter("model"));
		}
		
		// Ascertain sorting
		AdminModulesQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("administrator_count_asc")) {
				sortingType = AdminModulesQuerySortingType.ADMINISTRATOR_COUNT_ASCENDING;
				templateContext.put("sorting", "administrator_count_asc");
			} else if (pageContext.getParameter("sorting").equals("administrator_count_desc")) {
				sortingType = AdminModulesQuerySortingType.ADMINISTRATOR_COUNT_DESCENDING;
				templateContext.put("sorting", "administrator_count_desc");
			} else if (pageContext.getParameter("sorting").equals("assignment_count_asc")) {
				sortingType = AdminModulesQuerySortingType.ASSIGNMENT_COUNT_ASCENDING;
				templateContext.put("sorting", "assignment_count_asc");
			} else if (pageContext.getParameter("sorting").equals("assignment_count_desc")) {
				sortingType = AdminModulesQuerySortingType.ASSIGNMENT_COUNT_DESCENDING;
				templateContext.put("sorting", "registration_required_asc");
			} else if (pageContext.getParameter("sorting").equals("registration_required_asc")) {
				sortingType = AdminModulesQuerySortingType.REGISTRATION_REQUIRED_ASC;
				templateContext.put("sorting", "registration_required_asc");
			} else if (pageContext.getParameter("sorting").equals("registration_required_desc")) {
				sortingType = AdminModulesQuerySortingType.REGISTRATION_REQUIRED_DESC;
				templateContext.put("sorting", "registration_required_desc");
			} else if (pageContext.getParameter("sorting").equals("module_id_asc")) {
				sortingType = AdminModulesQuerySortingType.MODULE_ID_ASCENDING;
				templateContext.put("sorting", "module_id_asc");
			} else if (pageContext.getParameter("sorting").equals("module_id_desc")) {
				sortingType = AdminModulesQuerySortingType.MODULE_ID_DESCENDING;
				templateContext.put("sorting", "module_id_desc");
			} else {
				sortingType = AdminModulesQuerySortingType.NONE;
				templateContext.put("sorting", "none");
			}
		} else {
			sortingType = AdminModulesQuerySortingType.NONE;
			templateContext.put("sorting", "none");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IModelDAO modelDao = f.getModelDAOInstance();
			IAdminInterfaceQueriesDAO dao = f.getAdminInterfaceQueriesDAOInstance();
			
			Model model = modelDao.retrievePersistentEntity(modelId);
			Collection<AdminModulesQueryResult> result = dao.performAdminModulesQuery(sortingType, modelId);

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("model", model);
			templateContext.put("modules", result);

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
