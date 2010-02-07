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
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffMarkingAssignmentQueryResult;

public class MarkingAssignmentsPage extends Page {
	
	public MarkingAssignmentsPage()
			throws PageLoadException {
		super("staff_markingassignments", AccessLevel.USER);
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
		
		// Get assignmentId
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long
				.valueOf(pageContext.getParameter("assignment"));

		
		// Ascertain sorting
		IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("student_id_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType.STUDENT_ID_ASCENDING;
				templateContext.put("sorting", "student_id_asc");
			} else if (pageContext.getParameter("sorting").equals("student_id_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType.STUDENT_ID_DESCENDING;
				templateContext.put("sorting", "student_id_desc");
			} else if (pageContext.getParameter("sorting").equals("marker_id_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType.MARKER_ID_ASCENDING;
				templateContext.put("sorting", "marker_id_asc");
			} else if (pageContext.getParameter("sorting").equals("marker_id_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType.MARKER_ID_DESCENDING;
				templateContext.put("sorting", "marker_id_desc");
			} else if (pageContext.getParameter("sorting").equals("blind_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType.BLIND_MARKING_ASCENDING;
				templateContext.put("sorting", "blind_asc");
			} else if (pageContext.getParameter("sorting").equals("blind_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType.BLIND_MARKING_DESCENDING;
				templateContext.put("sorting", "blind_desc");
			} else if (pageContext.getParameter("sorting").equals("moderator_asc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType.MODERATING_ASCENDING;
				templateContext.put("sorting", "moderator_asc");
			} else if (pageContext.getParameter("sorting").equals("moderator_desc")) {
				sortingType = IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType.MODERATING_DESCENDING;
				templateContext.put("sorting", "moderator_desc");
			}
		} else {
			sortingType = IStaffInterfaceQueriesDAO.StaffMarkingAssignmentsQuerySortingType.NONE;
			templateContext.put("sorting", "none");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			Module module = moduleDao.retrievePersistentEntity(assignment.getModuleId());

			IStaffInterfaceQueriesDAO staffInterfaceQueriesDAO = f.getStaffInterfaceQueriesDAOInstance();
			Collection<StaffMarkingAssignmentQueryResult> result = staffInterfaceQueriesDAO.performStaffMarkingAssignmentsQuery(sortingType, assignmentId);

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("assignment", assignment);
			templateContext.put("markingAssignments", result);

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
