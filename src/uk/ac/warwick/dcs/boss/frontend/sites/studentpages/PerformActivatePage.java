package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

import java.io.IOException;

import javax.servlet.ServletException;

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
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;

public class PerformActivatePage extends Page {
	
	public PerformActivatePage()
			throws PageLoadException {
		super("student_activated", AccessLevel.USER);
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
			pageContext.performRedirect(pageContext.getPageUrl(StudentPageFactory.SITE_NAME, StudentPageFactory.ACTIVATE_PAGE) + "?submission=" + submissionId + "&missing=true");
			return;
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();
			IStudentInterfaceQueriesDAO studentInterfaceDao = f.getStudentInterfaceQueriesDAOInstance();
			
			if (!studentInterfaceDao.isStudentSubmissionAccessAllowed(pageContext.getSession().getPersonBinding().getId(), submissionId)) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}
			
			Submission submission = submissionDao.retrievePersistentEntity(submissionId);		
			studentInterfaceDao.makeSubmissionActive(submission.getPersonId(), submission.getAssignmentId(), submissionId);
			f.endTransaction();
						
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("submission", submission);
			pageContext.renderTemplate(template, templateContext);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}
	}	
}
