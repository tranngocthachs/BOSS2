package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
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
import uk.ac.warwick.dcs.boss.frontend.sites.MarkerPageFactory;
import uk.ac.warwick.dcs.boss.frontend.sites.StaffPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;
import uk.ac.warwick.dcs.boss.model.testing.impl.TemporaryDirectory;

public class PerformRunSherlockPage extends Page {
	
	public PerformRunSherlockPage()
			throws PageLoadException {
		super("staff_run_sherlock_result", AccessLevel.USER);
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
		
		// Get files to run
		String[] files = pageContext.getParameterValues("files");
		if (files == null || files.length == 0) {
			pageContext.performRedirect(pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.RUN_SHERLOCK_PAGE) + "&assignment=" + assignmentId + "&missing=true");
			return;
		}
		Collection<String> selectedFiles = Arrays.asList(files);
		// Render page
		try {
			f.beginTransaction();
			
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			
			
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			Module module = moduleDao.retrievePersistentEntity(assignment.getModuleId());
			
			// Obtain submissions
			ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();
			Submission exampleSubmission = new Submission();
			exampleSubmission.setAssignmentId(assignmentId);
			Collection<Submission> submissionsToProcess = submissionDao.findPersistentEntitiesByExample(exampleSubmission);
			pageContext.log(Level.DEBUG, "Extracting " + submissionsToProcess.size() + " submission(s)");

			
			try {
				// Begin extraction
				IResourceDAO resourceDao = f.getResourceDAOInstance();
				File sherlockTempDir;
				try {
					InputStream is = new FileInputStream(new File(pageContext.getConfigurationFilePath()));
					Properties prop = new Properties();
					prop.load(is);
					sherlockTempDir = TemporaryDirectory.createTempDir("sherlock", new File(prop.getProperty("testing.temp_dir")));
				} catch (IOException e) {
					throw new IOException("couldn't create sherlock temp dir", e);
				}
				
				for (Submission submission : submissionsToProcess) {
					InputStream resourceStream = resourceDao.openInputStream(submission.getResourceId());
					ZipInputStream zipResourceStream = new ZipInputStream(resourceStream);
					ZipEntry currentZipEntry;
//					// Create dir for this current submission
//					File subDir = new File(sherlockTempDir.getAbsolutePath() + File.separator + submission.getId());
//					if (!subDir.mkdir()) {
//						throw new IOException("Could not create " + subDir);
//					}
					byte buffer[] = new byte[1024];
					
					while ((currentZipEntry = zipResourceStream.getNextEntry()) != null) {
						String destination = sherlockTempDir.getAbsolutePath() + File.separator + currentZipEntry.getName();
						File destinationFile = new File(destination);
						
						if (!currentZipEntry.isDirectory() && !destinationFile.getParentFile().exists()) {
							destinationFile.getParentFile().mkdirs();
						}
						
						if (currentZipEntry.isDirectory() && !destinationFile.exists()) {
							destinationFile.getParentFile().mkdirs();
						}
						
						if (selectedFiles.contains(destinationFile.getName())) {
							FileOutputStream fos = new FileOutputStream(destinationFile);
							int n;
							while ((n = zipResourceStream.read(buffer, 0, 1024)) > -1) {
			                    fos.write(buffer, 0, n);
							}
							
							fos.flush();
							fos.close();
						}
					}					
				}
				// running Sherlock on temp folder
				System.out.println("Place holder");
				// kill temp folder
				killDirectory(sherlockTempDir);


			} catch (Exception e) {
				pageContext.log(Level.ERROR, e);
			}
			
			// perform
			
			f.endTransaction();
		
			// TODO: localisation
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("assignment", assignment);
			templateContext.put("files", selectedFiles);

			pageContext.renderTemplate(template, templateContext);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}
	}
	
	static public boolean killDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					killDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		
		return( path.delete() );
	}

	

}
