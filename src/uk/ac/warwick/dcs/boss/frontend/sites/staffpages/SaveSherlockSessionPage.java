package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;
import uk.ac.warwick.dcs.boss.model.dao.beans.SherlockSession;
import uk.ac.warwick.dcs.cobalt.sherlock.Marking;
import uk.ac.warwick.dcs.cobalt.sherlock.Match;
import uk.ac.warwick.dcs.cobalt.sherlock.MatchTableDataStruct;
import uk.ac.warwick.dcs.cobalt.sherlock.Settings;

public class SaveSherlockSessionPage extends Page {

	public SaveSherlockSessionPage()
	throws PageLoadException {
		super("staff_save_sherlock_session", AccessLevel.USER);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("Unexpected GET request");
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

		// Get files
		String[] fileStrings = pageContext.getParameterValues("files");
		if (fileStrings == null || fileStrings.length == 0) {
			pageContext.performRedirect(pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.RUN_SHERLOCK_PAGE) + "?assignment=" + assignmentId + "&missing=true");
			return;
		}
		Collection<String> files = Arrays.asList(fileStrings);

		if (pageContext.getParameter("ok") == null || !pageContext.getParameter("ok").equals("ok")) {
			throw new ServletException("Unexpected POST request");
		}

		String[] marked = pageContext.getParameterValues("marked");
		if (marked != null && marked.length > 0) {
			// saving all the marking into file
			Marking marking = new Marking();
			Match[] matches = MatchTableDataStruct.loadMatches();
			marking.setMatches(matches);
			for (int i = 0; i < marked.length; i++) {
				try {
					int index = Integer.parseInt(marked[i].trim());
					if (index != -1)
						marking.add(index);
				} catch (NumberFormatException e) {
					throw new ServletException("Unexpected POST request");
				}
			}
			File markingFile = new File(Settings.getSourceDirectory(), "marking.txt");
			marking.save(markingFile);
		}
		try {
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
				templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
				templateContext.put("module", module);
				templateContext.put("assignment", assignment);
				f.endTransaction();
			} catch (DAOException e) {
				f.abortTransaction();
				throw new ServletException("dao exception", e);
			}

			// Preparing saving Sherlock session

			// Create resource.
			Long resourceId = null;
			Resource resource = new Resource();
			resource.setTimestamp(new Date());
			resource.setFilename("sherlocksession-" + assignmentId + "-" + resource.getTimestamp().getTime() + ".zip");
			resource.setMimeType("application/zip");

			try {
				f.beginTransaction();
				IResourceDAO resourceDao = f.getResourceDAOInstance();
				resourceId = resourceDao.createPersistentCopy(resource);
				f.endTransaction();
			} catch (DAOException e) {
				f.abortTransaction();
				throw new ServletException("dao exception", e);
			}

			// Begin zipping up and storing the sherlock working folder
			OutputStream resourceStream = null;
			ZipOutputStream resourceZipStream = null;

			try {
				f.beginTransaction();

				IResourceDAO resourceDao = f.getResourceDAOInstance();
				resourceStream = resourceDao.openOutputStream(resourceId);
				resourceZipStream = new ZipOutputStream(resourceStream);
				zip(Settings.getSourceDirectory(), Settings.getSourceDirectory(), resourceZipStream);
				
				// test code
				File zipFile = new File(System.getProperty("user.home")+File.separator+"temp.zip");
				ZipOutputStream zos = new ZipOutputStream( new FileOutputStream( zipFile ) );
				zip( Settings.getSourceDirectory(), Settings.getSourceDirectory(), zos );
				zos.close();
				// end test code
				resourceZipStream.flush();
				resourceStream.flush();

				resourceZipStream.close();
				resourceStream.close();
				f.endTransaction();
			} catch (Exception e) {
				resourceStream.close();
				f.abortTransaction();

				try {
					f.beginTransaction();

					IResourceDAO resourceDao = f.getResourceDAOInstance();
					resourceDao.deletePersistentEntity(resourceId);

					f.endTransaction();
				} catch (DAOException e2) {
					throw new ServletException("error storing the Sherlock working folder - additional error cleaning stale resource " + resourceId, e);
				}

				throw new ServletException("error saving Sherlock session", e);
			}

			// Creating and storing a SherlockSession 
			SherlockSession sherlockSession = new SherlockSession();
			sherlockSession.setAssignmentId(assignmentId);
			sherlockSession.setResourceId(resourceId);
			try {
				f.beginTransaction();
				ISherlockSessionDAO sherlockSessionDao = f.getSherlockSessionDAOInstance();
				Long sherlockSessionId = sherlockSessionDao.createPersistentCopy(sherlockSession); 
				sherlockSession.setId(sherlockSessionId);
				sherlockSessionDao.addRequiredFilenames(sherlockSessionId, files);
				f.endTransaction();
			} catch (DAOException e) {
				f.abortTransaction();
				try {
					f.beginTransaction();

					IResourceDAO resourceDao = f.getResourceDAOInstance();
					resourceDao.deletePersistentEntity(resourceId);

					f.endTransaction();
				} catch (DAOException e2) {
					throw new ServletException("dao error occured - additional error cleaning stale resource " + resourceId, e);
				}

				throw new ServletException("dao error occured", e);
			}
		} finally {
			killDirectory(Settings.getSourceDirectory());
			Settings.setSourceDirectory(null);
			Settings.setFileList(null);
		}

		pageContext.renderTemplate(template, templateContext);
	}

	public static boolean killDirectory(File path) {
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

	private static final void zip(File directory, File base,
			ZipOutputStream zos) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[8192];
		int read = 0;
		for (int i = 0, n = files.length; i < n; i++) {
			if (files[i].isDirectory()) {
				zip(files[i], base, zos);
			} else {
				FileInputStream in = new FileInputStream(files[i]);
				ZipEntry entry = new ZipEntry(files[i].getPath().substring(
						base.getPath().length() + 1));
				zos.putNextEntry(entry);
				while (-1 != (read = in.read(buffer))) {
					zos.write(buffer, 0, read);
				}
				in.close();
			}
		}
	}
}
