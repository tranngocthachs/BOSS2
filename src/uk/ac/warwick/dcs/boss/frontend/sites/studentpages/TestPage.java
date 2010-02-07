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
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentAssignmentsQueryResult;

public class TestPage extends Page {
	
	public TestPage()
			throws PageLoadException {
		super("student_test", AccessLevel.USER);
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
		
		// Get the submission
		String submissionString = pageContext.getParameter("submission");
		if (submissionString == null) {
			throw new ServletException("No submission parameter given");
		}
		Long submissionId = Long.valueOf(pageContext.getParameter("submission"));
		
		// Set the missing elements flag if needed.
		if (pageContext.getParameter("missing") != null) {
			templateContext.put("missingFields", true);
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStudentInterfaceQueriesDAO studentInterfaceQueriesDAO = f.getStudentInterfaceQueriesDAOInstance();
			ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();
			
			if (!studentInterfaceQueriesDAO.isStudentSubmissionAccessAllowed(pageContext.getSession().getPersonBinding().getId(), submissionId)) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}
			
			Submission submission = submissionDao.retrievePersistentEntity(submissionId);
			StudentAssignmentsQueryResult assignment = studentInterfaceQueriesDAO.performAssignmentDetailsQuery(pageContext.getSession().getPersonBinding().getId(), submission.getAssignmentId());
			Collection<Test> tests = studentInterfaceQueriesDAO.fetchSubmissionTests(submissionId);
			
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("assignment", assignment);
			templateContext.put("submission", submission);
			templateContext.put("tests", tests);
			
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
