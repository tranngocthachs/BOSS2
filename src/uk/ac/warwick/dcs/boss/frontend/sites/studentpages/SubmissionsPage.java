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
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentSubmissionCountsQueryResult;

public class SubmissionsPage extends Page {

	public SubmissionsPage() throws PageLoadException {
		super("student_submissions", AccessLevel.USER);
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

		// Ascertain status filter
		IStudentInterfaceQueriesDAO.AssignmentStatus status = IStudentInterfaceQueriesDAO.AssignmentStatus.OPEN;
		if (pageContext.getParameter("show") != null) {
			if (pageContext.getParameter("show").equals("both")) {
				status = IStudentInterfaceQueriesDAO.AssignmentStatus.BOTH;
				templateContext.put("showing", "both");
			} else if (pageContext.getParameter("show").equals("closed")) {
				status = IStudentInterfaceQueriesDAO.AssignmentStatus.CLOSED;
				templateContext.put("showing", "closed");
			} else {
				templateContext.put("showing", "open");
			}
		} else {
			templateContext.put("showing", "open");
		}

		IStudentInterfaceQueriesDAO.StudentSubmissionCountsQuerySortingType sortingType = null;
		if (pageContext.getParameter("sorting") != null) {
			if (pageContext.getParameter("sorting").equals("count_asc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentSubmissionCountsQuerySortingType.SUBMISSION_COUNT_ASCENDING;
				templateContext.put("sorting", "count_asc");
			} else if (pageContext.getParameter("sorting").equals(
			"count_desc")) {
				sortingType = IStudentInterfaceQueriesDAO.StudentSubmissionCountsQuerySortingType.SUBMISSION_COUNT_DESCENDING;
				templateContext.put("sorting", "count_desc");
			}
		} else {
			sortingType = IStudentInterfaceQueriesDAO.StudentSubmissionCountsQuerySortingType.SUBMISSION_COUNT_ASCENDING;
			templateContext.put("sorting", "count_asc");
		}

		// Render page
		try {
			f.beginTransaction();

			IStudentInterfaceQueriesDAO dao = f
			.getStudentInterfaceQueriesDAOInstance();
			Collection<StudentSubmissionCountsQueryResult> result = dao
			.performStudentSubmissionCountsQuery(status, sortingType,
					pageContext.getSession().getPersonBinding()
					.getId());

			f.endTransaction();

			// TODO: localisation
			templateContext.put("greet", pageContext.getSession()
					.getPersonBinding().getChosenName());
			templateContext.put("submissions", result);

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
