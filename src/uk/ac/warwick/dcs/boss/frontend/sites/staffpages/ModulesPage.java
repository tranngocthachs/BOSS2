package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

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
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffModulesQueryResult;

public class ModulesPage extends Page {
	
	public ModulesPage()
			throws PageLoadException {
		super("staff_modules", AccessLevel.USER);
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

			
		// Ascertain sorting
		IStaffInterfaceQueriesDAO.StaffModulesQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("assignment_count_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffModulesQuerySortingType.ASSIGNMENT_COUNT_ASCENDING;
				templateContext.put("sorting", "assignment_count_asc");
			} else if (pageContext.getParameter("sorting").equals("assignment_count_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffModulesQuerySortingType.ASSIGNMENT_COUNT_DESCENDING;
				templateContext.put("sorting", "assignment_count_desc");
			} else if (pageContext.getParameter("sorting").equals("student_count_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffModulesQuerySortingType.STUDENT_COUNT_ASCENDING;
				templateContext.put("sorting", "student_count_asc");
			} else if (pageContext.getParameter("sorting").equals("student_count_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffModulesQuerySortingType.STUDENT_COUNT_DESCENDING;
				templateContext.put("sorting", "student_count_desc");
			} else if (pageContext.getParameter("sorting").equals("model_id_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffModulesQuerySortingType.MODEL_ID_ASCENDING;
				templateContext.put("sorting", "model_id_asc");
			} else if (pageContext.getParameter("sorting").equals("model_id_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffModulesQuerySortingType.MODEL_ID_DESCENDING;
				templateContext.put("sorting", "model_id_desc");
			} 
		} else {
			sortingType = IStaffInterfaceQueriesDAO.StaffModulesQuerySortingType.MODEL_ID_DESCENDING;
			templateContext.put("sorting", "model_id_desc");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO dao = f.getStaffInterfaceQueriesDAOInstance();
			Collection<StaffModulesQueryResult> result = dao.performStaffModulesQuery(
					sortingType,
					pageContext.getSession().getPersonBinding().getId());

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
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
