package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

import java.io.IOException;

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
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentAssignmentsQueryResult;

public class DeletePage extends Page {
	
	public DeletePage()
			throws PageLoadException {
		super("student_delete", AccessLevel.USER);
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
			
			if (!studentInterfaceQueriesDAO.isStudentAllowedToSubmit(pageContext.getSession().getPersonBinding().getId(), submission.getAssignmentId())) {
				templateContext.put("closed", true);
			}
			
			if (assignment.getAssignment().getAllowDeletion() == false) {
				templateContext.put("disallowed", true);
			} else if (submission.getActive() == true) {
				templateContext.put("active", true);
			}
			
			templateContext.put("assignment", assignment);
			templateContext.put("submission", submission);
			
			f.endTransaction();
			
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
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
