package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
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
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;

public class PerformUploadAssignmentResourcePage extends Page {

	public PerformUploadAssignmentResourcePage() throws PageLoadException {
		super("multi_edited", AccessLevel.USER);
	}
	
	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected GET");
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

		Assignment assignment;

		InputStream is = null;
		OutputStream os = null;
		try {
			// Get assignmentId
			String assignmentString = pageContext.getParameter("assignment");
			if (assignmentString == null) {
				throw new DAOException("No assignment parameter given");
			}
			Long assignmentId = Long
					.valueOf(pageContext.getParameter("assignment"));
			
			f.beginTransaction();		
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			assignment = assignmentDao.retrievePersistentEntity(assignmentId);
			
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}		

			// Go-go gadget upload!
			IResourceDAO resourceDao = f.getResourceDAOInstance();
			Resource resource = resourceDao.retrievePersistentEntity(assignment.getResourceId());		
			FileItemIterator fileIterator = pageContext.getUploadedFiles();
			
			FileItemStream fileItemStream = null;
			while (fileIterator.hasNext()) {
				FileItemStream nextFileItemStream = fileIterator.next();
				if (nextFileItemStream.getFieldName().equals("resource")) {
					fileItemStream = nextFileItemStream;
					break;
				}
			}
			
			if (fileItemStream == null) {
				throw new DAOException("Expected an uploaded file!");
			}
			
			if (!fileItemStream.getName().endsWith(".zip")) {
				throw new DAOException("Expected uploaded file to be a zip!");
			}
			
			resource.setFilename(fileItemStream.getName());
			resource.setMimeType("application/zip");
			
			is = fileItemStream.openStream();		
			os = resourceDao.openOutputStream(assignment.getResourceId());
						
			byte buffer[] = new byte[1024];
			int nread = -1;
			long total = 0;
			while ((nread = is.read(buffer)) != -1) {
				total += nread;
				os.write(buffer, 0, nread);
			}

			if (total == 0) {			
				throw new DAOException("Expected an uploaded file!");
			}
			
			is.close();
			os.flush();
			os.close();
			
			resourceDao.updatePersistentEntity(resource);
			
			templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
			templateContext.put("success", true);
			templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.ASSIGNMENTS_PAGE));
			templateContext.put("nextPageParamName", "assignment");
			templateContext.put("nextPageParamValue", assignmentId);
			
			f.endTransaction();
		} catch (Exception e) {
			f.abortTransaction();
			
			if (os != null) {
				os.flush();
				os.close();
			}
			
			if (is != null) {
				is.close();
			}
			
			throw new ServletException("upload error", e);
		}

				
		// Got the original.
		pageContext.renderTemplate(template, templateContext);
	}

}
