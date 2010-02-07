package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

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
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentAssignmentsQueryResult;

public class AssignmentPage extends Page {

	public AssignmentPage()
	throws PageLoadException {
		super("student_assignment", AccessLevel.USER);
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

		// Get the assignment
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long.valueOf(pageContext.getParameter("assignment"));

		// Render page
		try {
			f.beginTransaction();

			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			IStudentInterfaceQueriesDAO studentInterfaceQueriesDAO = f.getStudentInterfaceQueriesDAOInstance();
			StudentAssignmentsQueryResult assignment = studentInterfaceQueriesDAO.performAssignmentDetailsQuery(pageContext.getSession().getPersonBinding().getId(), assignmentId);
			
			if (!studentInterfaceQueriesDAO.isStudentModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getAssignment().getModuleId())) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}

			Collection<Person> markers = assignmentDao.fetchMarkers(IPersonDAO.SortingType.CHOSEN_NAME_ASCENDING, assignmentId);

			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("assignment", assignment);
			templateContext.put("markers", markers);

			f.endTransaction();

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
