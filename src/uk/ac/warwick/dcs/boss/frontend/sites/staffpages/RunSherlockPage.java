package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.StaffPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISherlockSessionDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.SherlockSession;
import uk.ac.warwick.dcs.boss.model.testing.impl.TemporaryDirectory;
import uk.ac.warwick.dcs.cobalt.sherlock.Settings;

public class RunSherlockPage extends Page {

	public RunSherlockPage()
	throws PageLoadException {
		super("staff_run_sherlock", AccessLevel.USER);
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

		// Get assignmentId
		String assignmentString = pageContext.getParameter("assignment");
		if (assignmentString == null) {
			throw new ServletException("No assignment parameter given");
		}
		Long assignmentId = Long
		.valueOf(pageContext.getParameter("assignment"));

		// Get the requested action
		boolean rerun = false;
		boolean viewResult = false;
		boolean newSession = false;
		String doString = pageContext.getParameter("do");
		if (doString != null) {
			if (doString.equals("Rerun"))
				rerun = true;
			else if (doString.equals("View Result"))
				viewResult = true;
			else if (doString.equals("New Session"))
				newSession = true;
			else
				throw new ServletException("Unexpected GET request");
		}
		else {
			throw new ServletException("Unexpected GET request");
		}

		String sherlockSessionIdString = pageContext.getParameter("session");
		if ((rerun || viewResult) && sherlockSessionIdString == null) {
			throw new ServletException("Unexpected GET request");
		}
		Long sherlockSessionId = new Long(-1);
		if (sherlockSessionIdString != null)
			sherlockSessionId = Long.valueOf(pageContext.getParameter("session"));

		// Set the missing elements flag if needed.
		if (pageContext.getParameter("missing") != null) {
			templateContext.put("missingFields", true);
		}

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
			Collection<String> reqFiles = null;
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			Module module = moduleDao.retrievePersistentEntity(assignment.getModuleId());
			if (newSession) {
				reqFiles = assignmentDao.fetchRequiredFilenames(assignmentId);
			}
			else if (rerun) {
				ISherlockSessionDAO sherlockSessionDao = f.getSherlockSessionDAOInstance();
				reqFiles = sherlockSessionDao.fetchRequiredFilenames(sherlockSessionId);
			}
			f.endTransaction();
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("module", module);
			templateContext.put("assignment", assignment);
			templateContext.put("files", reqFiles);
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}

		if (Settings.getSourceDirectory() == null || !Settings.getSourceDirectory().exists()) {
			File sherlockTempDir;
			try {
				InputStream is = new FileInputStream(new File(pageContext.getConfigurationFilePath()));
				Properties prop = new Properties();
				prop.load(is);
				sherlockTempDir = TemporaryDirectory.createTempDir("sherlock", new File(prop.getProperty("testing.temp_dir")));
			} catch (IOException e) {
				throw new ServletException("couldn't create sherlock temp dir", e);
			}
			Settings.setSourceDirectory(sherlockTempDir);
			if (rerun || viewResult) {
				// extract the resource
				try {
					f.beginTransaction();
					IResourceDAO resourceDao = f.getResourceDAOInstance();
					SherlockSession sherlockSession = f.getSherlockSessionDAOInstance().retrievePersistentEntity(sherlockSessionId);
					InputStream resourceStream = resourceDao.openInputStream(sherlockSession.getResourceId());
					ZipInputStream zipResourceStream = new ZipInputStream(resourceStream);
					ZipEntry currentZipEntry;
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

						FileOutputStream fos = new FileOutputStream(destinationFile);
						int n;
						while ((n = zipResourceStream.read(buffer, 0, 1024)) > -1) {
							fos.write(buffer, 0, n);
						}

						fos.flush();
						fos.close();
					}	
					f.endTransaction();
				}
				catch (DAOException e) {
					f.abortTransaction();
					throw new ServletException("dao exception", e);
				}
				
				// clean the saved match folder if this is a rerun
				if (rerun)
					SaveSherlockSessionPage.killDirectory(new File(sherlockTempDir, "match"));
			}
			Settings.init();
			Settings.getSherlockSettings().load();
		}
		
		if (newSession) {
			templateContext.put("newSession", true);
		}
		else if (rerun) {
			templateContext.put("rerun", true);
			templateContext.put("session", sherlockSessionId);
		}
		
		templateContext.put("java", Settings.getSherlockSettings().isJava());
		templateContext.put("fileTypes", Settings.getFileTypes());
		// putting up the id of sentence parsing profile (required different treatment)
		templateContext.put("senProfile", Settings.SEN);
		pageContext.renderTemplate(template, templateContext);

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

		try {
			f.beginTransaction();

			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			Assignment assignment = assignmentDao.retrievePersistentEntity(assignmentId);

			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		}
		String doString = pageContext.getParameter("do");
		if (doString != null && doString.equals("Delete")) {
			// Get sherlock session id
			String sessionString = pageContext.getParameter("session");
			if (sessionString == null) {
				throw new ServletException("No session parameter given");
			}
			Long sessionId = Long
			.valueOf(pageContext.getParameter("session"));
			try {
				f.beginTransaction();
				ISherlockSessionDAO sherlockSessionDao = f.getSherlockSessionDAOInstance();
				sherlockSessionDao.removeRequiredFilenames(sessionId);
				SherlockSession sherlockSession = sherlockSessionDao.retrievePersistentEntity(sessionId);
				Long resourceId = sherlockSession.getResourceId();
				sherlockSessionDao.deletePersistentEntity(sessionId);
				f.getResourceDAOInstance().deletePersistentEntity(resourceId);
				f.endTransaction();
			} catch (DAOException e2) {
				throw new ServletException("dao exception", e2);
			}
		}
		else {
			throw new ServletException("Unexpected POST request");
		}
		pageContext.performRedirect(pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.INIT_SHERLOCK_PAGE) + "?assignment=" + assignmentId);
	}

}
