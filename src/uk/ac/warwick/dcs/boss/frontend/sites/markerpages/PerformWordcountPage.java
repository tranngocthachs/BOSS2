package uk.ac.warwick.dcs.boss.frontend.sites.markerpages;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.MarkerPageFactory;
import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
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
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;


public class PerformWordcountPage extends Page {

	public PerformWordcountPage()
	throws PageLoadException {
		super("marker_wordcounted", AccessLevel.USER);
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
		
		String[] files = pageContext.getParameterValues("count");
		if (files == null) {
			pageContext.performRedirect(pageContext.getPageUrl(MarkerPageFactory.SITE_NAME, MarkerPageFactory.WORDCOUNT_PAGE) + "?submission=" + submissionId + "&assignment=" + assignmentId + "&missing=true");
			return;
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
			InputStream is = resourceDAO.openInputStream(submission.getResourceId());

			Collection<String> fileColl = Arrays.asList(files);
			HashMap<String, Integer> wcs = new HashMap<String, Integer>();
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry entry;
			try {
				Properties opts = new Properties();
				opts.load(new FileInputStream(pageContext.getConfigurationFilePath()));
				String baseDirStr = opts.getProperty("testing.temp_dir");
				File baseDir = (baseDirStr == null) ? null : (new File(baseDirStr));
				
				while((entry = zis.getNextEntry()) != null && !entry.isDirectory()) {
					String[] temp = entry.getName().split(File.separator);
					if (fileColl.contains(temp[temp.length-1])) {
						int wc = -1;
						if (temp[temp.length-1].endsWith(".txt")) {
							int size;
							byte[] buffer = new byte[2048];
							File tempF = File.createTempFile("boss", ".txt", baseDir);
							FileOutputStream fos = new FileOutputStream(tempF);
							BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);

			                while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
			                    bos.write(buffer, 0, size);
			                }
			                bos.flush();
			                bos.close();
			                
							wc = wordCount(tempF);
							tempF.delete();
						}
						wcs.put(temp[temp.length-1], new Integer(wc));
					}
				}
				zis.close();
			} catch (IOException e) {
				throw new ServletException("IO error while accessing submitted file", e);
			}
			
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("assignment", assignment);
			templateContext.put("submission", submission);
			templateContext.put("files", wcs);
			
			f.endTransaction();
			
			pageContext.renderTemplate(template, templateContext);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}
		/*
		ITestRunner testRunner;
		IDAOSession f;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			TestRunnerFactory tf = (TestRunnerFactory)FactoryRegistrar.getFactory(TestRunnerFactory.class);
			f = df.getInstance();
			testRunner = tf.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("factory error", e);
		}

		// If tests are running, refuse
		if (!pageContext.getSession().getTestResults().isEmpty()) {
			throw new ServletException("Tests are already running..!  Navigate to " + pageContext.getPageUrl(MarkerPageFactory.SITE_NAME, MarkerPageFactory.PERFORM_TEST_PAGE) + " manually");
		}

		// Get the assignment
		String submissionString = pageContext.getParameter("submission");
		if (submissionString == null) {
			throw new ServletException("No submission parameter given");
		}
		Long submissionId = Long.valueOf(pageContext.getParameter("submission"));

		// Render page
		try {
			f.beginTransaction();

			IMarkerInterfaceQueriesDAO markerInterfaceQueriesDao = f.getMarkerInterfaceQueriesDAOInstance();
			ITestDAO testDao = f.getTestDAOInstance();
			ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();

			if (!markerInterfaceQueriesDao.isMarkerSubmissionAccessAllowed(pageContext.getSession().getPersonBinding().getId(), submissionId)) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}

			Submission submission = submissionDao.retrievePersistentEntity(submissionId);
			Test example = new Test();
			example.setAssignmentId(submission.getAssignmentId());
			Collection<Test> tests = testDao.findPersistentEntitiesByExample(example);
			Collection<Test> testsToRun = new LinkedList<Test>();

			for (Test t : tests) {
				if (pageContext.getParameter("test" + t.getId()) != null) {
					testsToRun.add(t);
				}
			}

			if (testsToRun.isEmpty()) {
				f.endTransaction();
				pageContext.performRedirect(pageContext.getPageUrl(MarkerPageFactory.SITE_NAME, MarkerPageFactory.TEST_PAGE) + "?submission=" + submissionId + "&assignment=" + submission.getAssignmentId() + "&missing=true");
				return;
			}

			for (Test t : testsToRun) {
				pageContext.log(Level.INFO, "Scheduling " + t.getName());
				Future<TestResult> schedule = testRunner.runTest(submission, t);
				pageContext.getSession().getTestResults().add(schedule);
			}

			f.endTransaction();

			pageContext.performRedirect(pageContext.getPageUrl(MarkerPageFactory.SITE_NAME, MarkerPageFactory.PERFORM_TEST_PAGE));
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		} catch (TestingException e) {
			if (f != null) {
				f.abortTransaction();
			}
			throw new ServletException("test exception", e);
		}
		*/
	}

	private int wordCount(File file) throws ServletException {
		int wc = 0;
		try {
			Scanner sc = new Scanner(file);
			while (sc.hasNext()) {
				sc.next();
				wc++;
			}
			sc.close();
		} catch (Exception e) {
			throw new ServletException("Error while counting");
		}
		return wc;

	}	
}
