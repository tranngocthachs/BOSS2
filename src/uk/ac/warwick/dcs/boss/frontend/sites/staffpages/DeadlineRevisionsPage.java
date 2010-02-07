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
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO.StaffDeadlineRevisionsQuerySortingType;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffDeadlineRevisionsQueryResult;

public class DeadlineRevisionsPage extends Page {
	
	public DeadlineRevisionsPage()
			throws PageLoadException {
		super("staff_deadlinerevisions", AccessLevel.USER);
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

		
		// Ascertain sorting - TODO more sorting
		StaffDeadlineRevisionsQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("deadline_asc")) {
				sortingType = StaffDeadlineRevisionsQuerySortingType.DEADLINE_ASCENDING;
				templateContext.put("sorting", "deadline_asc");
			} else if (pageContext.getParameter("sorting").equals("deadline_desc")) {
				sortingType = StaffDeadlineRevisionsQuerySortingType.DEADLINE_DESCENDING;
				templateContext.put("sorting", "deadline_desc");
			} else if (pageContext.getParameter("sorting").equals("student_id_asc")) {
				sortingType = StaffDeadlineRevisionsQuerySortingType.STUDENT_ID_ASCENDING;
				templateContext.put("sorting", "student_id_asc");
			} else if (pageContext.getParameter("sorting").equals("student_id_desc")) {
				sortingType = StaffDeadlineRevisionsQuerySortingType.STUDENT_ID_DESCENDING;
				templateContext.put("sorting", "student_id_desc");
			}
		} else {
			sortingType = StaffDeadlineRevisionsQuerySortingType.NONE;
			templateContext.put("sorting", "");
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDAO = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			if (!staffInterfaceQueriesDAO.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			Module module = moduleDao.retrievePersistentEntity(assignment.getModuleId());

			Collection<StaffDeadlineRevisionsQueryResult> result = staffInterfaceQueriesDAO.performStaffDeadlineRevisionsQuery(sortingType, assignmentId);

			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("assignment", assignment);
			templateContext.put("deadlineRevisions", result);

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
