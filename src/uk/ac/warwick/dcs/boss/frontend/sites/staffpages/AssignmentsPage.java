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
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffAssignmentsQueryResult;

public class AssignmentsPage extends Page {
	
	public AssignmentsPage()
			throws PageLoadException {
		super("staff_assignments", AccessLevel.USER);
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
		
		// Get moduleID
		String moduleString = pageContext.getParameter("module");
		if (moduleString == null) {
			throw new ServletException("No module parameter given");
		}
		Long moduleId = Long
				.valueOf(pageContext.getParameter("module"));

		
		// Get view
		String viewString = pageContext.getParameter("view");
		if (viewString == null) {
			viewString = "submission";
		}
		templateContext.put("view", viewString);
		
		// Ascertain sorting
		IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("marking_category_count_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType.MARKING_CATEGORY_COUNT_ASCENDING;
				templateContext.put("sorting", "marking_category_count_asc");
			} else if (pageContext.getParameter("sorting").equals("marking_category_count_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType.MARKING_CATEGORY_COUNT_DESCENDING;
				templateContext.put("sorting", "marking_category_count_desc");
			} else if (pageContext.getParameter("sorting").equals("marker_count_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType.MARKER_COUNT_ASCENDING;
				templateContext.put("sorting", "marker_count_asc");
			} else if (pageContext.getParameter("sorting").equals("marker_count_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType.MARKER_COUNT_DESCENDING;
				templateContext.put("sorting", "marker_count_desc");
			} else if (pageContext.getParameter("sorting").equals("test_count_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType.TEST_COUNT_ASCENDING;
				templateContext.put("sorting", "test_count_asc");
			} else if (pageContext.getParameter("sorting").equals("test_count_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType.TEST_COUNT_DESCENDING;
				templateContext.put("sorting", "test_count_desc");
			} else if (pageContext.getParameter("sorting").equals("result_count_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType.RESULT_COUNT_ASCENDING;
				templateContext.put("sorting", "result_count_asc");
			} else if (pageContext.getParameter("sorting").equals("result_count_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType.RESULT_COUNT_DESCENDING;
				templateContext.put("sorting", "result_count_desc");
			} 
		} else {
			sortingType = IStaffInterfaceQueriesDAO.StaffAssignmentsQuerySortingType.NONE;
			templateContext.put("sorting", "");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDAO = f.getStaffInterfaceQueriesDAOInstance();
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			if (!staffInterfaceQueriesDAO.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), moduleId)) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			Module module = moduleDao.retrievePersistentEntity(moduleId);
			
			IStaffInterfaceQueriesDAO staffDao = f.getStaffInterfaceQueriesDAOInstance();
			Collection<StaffAssignmentsQueryResult> result = staffDao.performStaffAssignmentsQuery(
					sortingType,
					moduleId);

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("assignments", result);

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
