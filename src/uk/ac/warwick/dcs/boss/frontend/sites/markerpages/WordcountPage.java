package uk.ac.warwick.dcs.boss.frontend.sites.markerpages;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
import uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.ITestDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;

public class WordcountPage extends Page {
	
	public WordcountPage()
			throws PageLoadException {
		super("marker_wordcount", AccessLevel.USER);
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
		
		// Get the assignment
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long.valueOf(pageContext.getParameter("assignment"));
		
		// Set the missing elements flag if needed.
		if (pageContext.getParameter("missing") != null) {
			templateContext.put("missingFields", true);
		}
		
		// Render page
		try {
			f.beginTransaction();
			
			IMarkerInterfaceQueriesDAO markerInterfaceQueriesDao = f.getMarkerInterfaceQueriesDAOInstance();
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();
			IResourceDAO resourceDAO = f.getResourceDAOInstance();
			if (!markerInterfaceQueriesDao.isMarkerSubmissionAccessAllowed(pageContext.getSession().getPersonBinding().getId(), submissionId)) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}
			Submission submission = submissionDao.retrievePersistentEntity(submissionId);
			Assignment assignment = assignmentDao.retrievePersistentEntity(submission.getAssignmentId());
			Module module = moduleDao.retrievePersistentEntity(assignment.getModuleId());
			Collection<String> reqFiles = assignmentDao.fetchRequiredFilenames(assignmentId);
			Collection<String> files = new LinkedList<String>();
//			Resource resource = resourceDAO.retrievePersistentEntity(submission.getResourceId());
			InputStream is = resourceDAO.openInputStream(submission.getResourceId());
//			File submissionFile = File.createTempFile(resource.getFilename(), "");
//			try {
//				FileOutputStream tempOS = new FileOutputStream(submissionFile);
//				byte buffer[] = new byte[1024];
//				int nread = -1;
//				while ((nread = is.read(buffer)) != -1) {
//					tempOS.write(buffer, 0, nread);
//				}
//				tempOS.close();
//			} catch (IOException e) {
//				throw new ServletException("IO error returning file stream", e);
//			}
//			ZipFile zipFile = new ZipFile(submissionFile);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry entry;
			try {
				while((entry = zis.getNextEntry()) != null && !entry.isDirectory()) {
					String[] temp = entry.getName().split(File.separator);
					if (reqFiles.contains(temp[temp.length-1]))
						files.add(temp[temp.length-1]);
				}
				zis.close();
			} catch (IOException e) {
				throw new ServletException("IO error while accessing submitted file", e);
			}
//			ITestDAO testDao = f.getTestDAOInstance();
//			Test example = new Test();
//			example.setAssignmentId(submission.getAssignmentId());
//			Collection<Test> tests = testDao.findPersistentEntitiesByExample(example);
			
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("assignment", assignment);
			templateContext.put("submission", submission);
			templateContext.put("files", files);
			
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
