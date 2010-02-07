package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

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
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;

public class PerformEditAssignmentPage extends Page {

	public PerformEditAssignmentPage() throws PageLoadException {
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

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Assignment incoming = new Assignment();

		if (pageContext.getParameter("module") == null) {
			throw new ServletException("missing module parameter");
		}
		incoming.setModuleId(Long.valueOf(pageContext.getParameter("module")));

		if (pageContext.getParameter("assignment") == null) {
			throw new ServletException("missing assignment parameter");
		}
		incoming.setId(Long.valueOf(pageContext.getParameter("assignment")));

		if (pageContext.getParameter("deadline") == null) {
			throw new ServletException("missing deadline parameter");
		}	
		try {
			incoming.setDeadline(simpleDateFormat.parse(pageContext.getParameter("deadline")));
		} catch (ParseException e) {
			throw new ServletException("date parse error", e);
		}

		if (pageContext.getParameter("opening") == null) {
			throw new ServletException("missing deadline parameter");
		}
		try {
			incoming.setOpeningTime(simpleDateFormat.parse(pageContext.getParameter("opening")));
		} catch (ParseException e) {
			throw new ServletException("date parse error", e);
		}

		if (pageContext.getParameter("closing") == null) {
			throw new ServletException("missing deadline parameter");
		}
		try {
			incoming.setClosingTime(simpleDateFormat.parse(pageContext.getParameter("closing")));
		} catch (ParseException e) {
			throw new ServletException("date parse error", e);
		}

		if (pageContext.getParameter("name") == null) {
			throw new ServletException("missing name parameter");
		}
		incoming.setName(pageContext.getParameter("name"));
	
		if (pageContext.getParameter("allow_deletion") != null) {
			incoming.setAllowDeletion(true);
		} else {
			incoming.setAllowDeletion(false);
		}
		
		try {
			f.beginTransaction();

			if (pageContext.getParameter("create") != null) {
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();

				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), incoming.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}

				IResourceDAO resourceDao = f.getResourceDAOInstance();
				Long createdResourceId = null;
				Resource resource = new Resource();
				resource.setTimestamp(new Date());
				resource.setFilename("empty.txt");
				resource.setMimeType("text/plain");
				createdResourceId = resourceDao.createPersistentCopy(resource);
				incoming.setResourceId(createdResourceId);
				
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				incoming.setId(assignmentDao.createPersistentCopy(incoming));
			} else {	
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), incoming.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
			
				if (pageContext.getParameter("delete") != null) {
					if (assignmentDao.hasDeadlineRevisions(incoming.getId())
						|| assignmentDao.hasMarkingCategories(incoming.getId())
						|| assignmentDao.hasSubmissions(incoming.getId())
						|| assignmentDao.hasTests(incoming.getId()))
						throw new ServletException("assignment has children");

					// Fetch resource ID
					IResourceDAO resourceDao = f.getResourceDAOInstance();
					Long resourceId = assignmentDao.retrievePersistentEntity(incoming.getId()).getResourceId();

					// Delete assignment
					assignmentDao.deletePersistentEntity(incoming.getId());
					
					// Delete resource
					resourceDao.deletePersistentEntity(resourceId);
				} else {
					// Update assignment (copy over resourceId)
					Long resourceId = assignmentDao.retrievePersistentEntity(incoming.getId()).getResourceId();
					incoming.setResourceId(resourceId);
					assignmentDao.updatePersistentEntity(incoming);
				}
			}

			// Done.
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}

		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("success", true);
		
		if (pageContext.getParameter("delete") == null) {
			templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.ASSIGNMENT_FILES_PAGE));
			templateContext.put("nextPageParamName", "assignment");
			templateContext.put("nextPageParamValue", incoming.getId());
		} else {
			templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.ASSIGNMENTS_PAGE));
			templateContext.put("nextPageParamName", "module");
			templateContext.put("nextPageParamValue", incoming.getModuleId());			
		}
		pageContext.renderTemplate(template, templateContext);
	}
}
