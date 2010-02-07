package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.StudentPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;

public class PerformDeletePage extends Page {
	
	public PerformDeletePage()
			throws PageLoadException {
		super("student_deleted", AccessLevel.USER);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected GET");
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
		
		// Get the assignment
		String submissionString = pageContext.getParameter("submission");
		if (submissionString == null) {
			throw new ServletException("No submission parameter given");
		}
		Long submissionId = Long.valueOf(pageContext.getParameter("submission"));
		
		// Make sure confirmation was given
		if (pageContext.getParameter("confirmation") == null) {
			pageContext.performRedirect(pageContext.getPageUrl(StudentPageFactory.SITE_NAME, StudentPageFactory.DELETE_PAGE) + "?submission=" + submissionId + "&missing=true");
			return;
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IStudentInterfaceQueriesDAO studentInterfaceQueriesDAO = f.getStudentInterfaceQueriesDAOInstance();
			ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			IResourceDAO resourceDao = f.getResourceDAOInstance();
			
			if (!studentInterfaceQueriesDAO.isStudentSubmissionAccessAllowed(pageContext.getSession().getPersonBinding().getId(), submissionId)) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}
			
			Submission submission = submissionDao.retrievePersistentEntity(submissionId);
			Assignment assignment = assignmentDao.retrievePersistentEntity(submission.getAssignmentId());
			
			if (!studentInterfaceQueriesDAO.isStudentAllowedToSubmit(pageContext.getSession().getPersonBinding().getId(), submission.getAssignmentId())) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			} else if (submission.getActive() == true) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			} else if (assignment.getAllowDeletion() == false) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}
			
			
			submissionDao.deletePersistentEntity(submissionId);
			resourceDao.deletePersistentEntity(submission.getResourceId());
			f.endTransaction();

			pageContext.log(Level.INFO, "student submission deletion successful - submission ID " + submission.getId());
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("submission", submission);
			pageContext.renderTemplate(template, templateContext);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}
	}	
}
