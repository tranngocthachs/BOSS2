package uk.ac.warwick.dcs.boss.frontend.sites.markerpages;

import java.io.IOException;
import java.io.InputStream;

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
import uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;

public class DownloadSubmissionPage extends Page {

	public DownloadSubmissionPage() throws PageLoadException {
		super("multi_edited", AccessLevel.USER);
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
		Long submissionId = Long
				.valueOf(pageContext.getParameter("submission"));

		// Render page
		try {
			f.beginTransaction();

			IMarkerInterfaceQueriesDAO markerInterfaceQueriesDAO = f
					.getMarkerInterfaceQueriesDAOInstance();
			ISubmissionDAO submissionDAO = f.getSubmissionDAOInstance();
			IResourceDAO resourceDAO = f.getResourceDAOInstance();

			if (!markerInterfaceQueriesDAO.isMarkerSubmissionAccessAllowed(
					pageContext.getSession().getPersonBinding().getId(),
					submissionId)) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}

			Submission submission = submissionDAO
					.retrievePersistentEntity(submissionId);

			Resource resource = resourceDAO.retrievePersistentEntity(submission
					.getResourceId());

			InputStream is = resourceDAO.openInputStream(submission
					.getResourceId());

			pageContext.performSendFile(resource.getMimeType(), resource
					.getFilename(), is);
			f.endTransaction();
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
