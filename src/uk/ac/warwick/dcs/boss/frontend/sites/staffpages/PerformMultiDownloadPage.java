package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
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
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;

public class PerformMultiDownloadPage extends Page {
	
	public PerformMultiDownloadPage()
			throws PageLoadException {
		super("multi_edited", AccessLevel.USER);
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
		
		// Get assignmentId
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long
				.valueOf(pageContext.getParameter("assignment"));
		
		// Obtain options
		boolean oneDirectory = (pageContext.getParameter("one_directory") != null);
		boolean activeOnly = (pageContext.getParameter("active_only") != null);
		
	
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}

			// Obtain submissions
			ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();
			Submission exampleSubmission = new Submission();
			exampleSubmission.setAssignmentId(assignmentId);
			if (activeOnly) {
				exampleSubmission.setActive(true);
			}
			Collection<Submission> submissionsToDownload = submissionDao.findPersistentEntitiesByExample(exampleSubmission);
			pageContext.log(Level.DEBUG, "Downloading " + submissionsToDownload.size() + " submission(s)");

			// Begin the transfer
			try {
				IResourceDAO resourceDao = f.getResourceDAOInstance();
				OutputStream outputStream = pageContext.performManualSendFile("application/zip", "assignment-" + assignment.getId() + "-submissions.zip");
				ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
				for (Submission submission : submissionsToDownload) {
					InputStream resourceStream = resourceDao.openInputStream(submission.getResourceId());
					ZipInputStream zipResourceStream = new ZipInputStream(resourceStream);
					ZipEntry currentZipEntry;
					while ((currentZipEntry = zipResourceStream.getNextEntry()) != null) {
						String fileName = new File(currentZipEntry.getName()).getName();
						if (oneDirectory) {
							fileName = "Assignment " + assignment.getId() + "_" + submission.getResourceSubdirectory() + "_Submission " + submission.getId() + "_" + fileName;
						} else {
							fileName = "Assignment " + assignment.getId() + "/" + submission.getResourceSubdirectory() + "/Submission " + submission.getId() + "/" + fileName;
						}
						
						zipOutputStream.putNextEntry(new ZipEntry(fileName));
						byte buffer[] = new byte[1024];
						int nread = -1;
						long total = 0;
						while ((nread = zipResourceStream.read(buffer)) != -1) {
							total += nread;
							zipOutputStream.write(buffer, 0, nread);
						}
					}					
				}

				zipOutputStream.finish();
				zipOutputStream.flush();
				outputStream.flush();					
			} catch (Exception e) {
				pageContext.log(Level.ERROR, e);
			}
			
			f.endTransaction();		
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}
	}

}
