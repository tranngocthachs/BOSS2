package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.util.Date;

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
import uk.ac.warwick.dcs.boss.model.dao.ITestDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;

public class PerformEditTestPage extends Page {

	public PerformEditTestPage() throws PageLoadException {
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

		Test incoming = new Test();

		if (pageContext.getParameter("assignment") == null) {
			throw new ServletException("missing assignment parameter");
		}
		incoming.setAssignmentId(Long.valueOf(pageContext.getParameter("assignment")));

		if (pageContext.getParameter("test") == null) {
			throw new ServletException("missing test parameter");
		}
		incoming.setId(Long.valueOf(pageContext.getParameter("test")));

		if (pageContext.getParameter("class_name") == null) {
			throw new ServletException("missing class_name parameter");
		}
		incoming.setTestClassName(pageContext.getParameter("class_name"));

		if (pageContext.getParameter("executor_class_name") == null) {
			throw new ServletException("missing executor_class_name parameter");
		}
		incoming.setExecutorClassName(pageContext.getParameter("executor_class_name"));

		if (pageContext.getParameter("command") == null) {
			throw new ServletException("missing command parameter");
		}
		incoming.setCommand(pageContext.getParameter("command"));
		
		if (pageContext.getParameter("max_time") == null) {
			throw new ServletException("missing max_time parameter");
		}
		incoming.setMaximumExecutionTime(Integer.valueOf(pageContext.getParameter("max_time")));
		
		if (pageContext.getParameter("student_test") == null) {
			incoming.setStudentTest(false);
		} else {
			incoming.setStudentTest(true);
		}

		if (pageContext.getParameter("name") == null) {
			throw new ServletException("missing name parameter");
		}
		incoming.setName(pageContext.getParameter("name"));


		try {
			f.beginTransaction();
			
			if (pageContext.getParameter("create") != null) {
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				Assignment assignment = assignmentDao.retrievePersistentEntity(incoming.getAssignmentId());
				
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
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
				incoming.setLibraryResourceId(createdResourceId);
				
				ITestDAO testDao = f.getTestDAOInstance();
				incoming.setId(testDao.createPersistentCopy(incoming));
				templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.EDIT_TEST_PARAMETERS_PAGE));
				templateContext.put("nextPageParamName", "test");
				templateContext.put("nextPageParamValue", incoming.getId());
			} else {	
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();				
				ITestDAO testDao = f.getTestDAOInstance();
				Assignment assignment = assignmentDao.retrievePersistentEntity(incoming.getAssignmentId());
				
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				
				if (pageContext.getParameter("delete") != null) {
					// Fetch resource ID
					IResourceDAO resourceDao = f.getResourceDAOInstance();
					Long resourceId = testDao.retrievePersistentEntity(incoming.getId()).getLibraryResourceId();

					// Delete assignment
					testDao.deletePersistentEntity(incoming.getId());
					
					// Delete resource
					resourceDao.deletePersistentEntity(resourceId);
					
					templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.TESTS_PAGE));
					templateContext.put("nextPageParamName", "assignment");
					templateContext.put("nextPageParamValue", incoming.getAssignmentId());

				} else {
					// Fetch resource ID
					Long resourceId = testDao.retrievePersistentEntity(incoming.getId()).getLibraryResourceId();
					incoming.setLibraryResourceId(resourceId);
					testDao.updatePersistentEntity(incoming);
					templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.EDIT_TEST_PARAMETERS_PAGE));
					templateContext.put("nextPageParamName", "test");
					templateContext.put("nextPageParamValue", incoming.getId());
				}
			}

			// Done.
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}

		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("success", true);
		
		// Display success page
		pageContext.renderTemplate(template, templateContext);

	}
}
