package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.util.Collection;

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
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.ITestDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;
import uk.ac.warwick.dcs.boss.model.testing.executors.ITestExecutorDirectory;
import uk.ac.warwick.dcs.boss.model.testing.executors.TestExecutorDescription;
import uk.ac.warwick.dcs.boss.model.testing.executors.TestExecutorFactory;
import uk.ac.warwick.dcs.boss.model.testing.tests.ITestMethodDirectory;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodDescription;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodFactory;

public class EditTestPage extends Page {

	public EditTestPage() throws PageLoadException {
		super("staff_edit_test", AccessLevel.USER);
	}
	
	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {

		IDAOSession f;
		ITestExecutorDirectory executorDirectory;
		ITestMethodDirectory methodDirectory;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			TestExecutorFactory tedf = (TestExecutorFactory)FactoryRegistrar.getFactory(TestExecutorFactory.class);
			TestMethodFactory tmdf = (TestMethodFactory)FactoryRegistrar.getFactory(TestMethodFactory.class);
			f = df.getInstance();
			executorDirectory = tedf.getInstance();
			methodDirectory = tmdf.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}

		Test original;
		Assignment originalParent;
		Module originalParentParent;
				
		try {
			if (pageContext.getParameter("create") != null) {
				// Get assignmentId
				String assignmentString = pageContext.getParameter("assignment");
				if (assignmentString == null) {
					throw new ServletException("No assignment parameter given");
				}
				Long assignmentId = Long
						.valueOf(pageContext.getParameter("assignment"));
				
				f.beginTransaction();		
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				originalParent = assignmentDao.retrievePersistentEntity(assignmentId);
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), originalParent.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}
				f.endTransaction();
				
				original = new Test();
				original.setId(-1L);
				original.setName("New Test");
				original.setTestClassName("uk.ac.warwick.dcs.boss.testing.tests.RandomTest");
				original.setExecutorClassName("uk.ac.warwick.dcs.boss.testing.executors.NothingExecutor");
				original.setCommand("");
				original.setMaximumExecutionTime(10);
				original.setStudentTest(false);
				original.setAssignmentId(assignmentId);
				original.setLibraryResourceId(null);
				
				templateContext.put("create", true);
			} else {	
				// Get testId
				String testString = pageContext.getParameter("test");
				if (testString == null) {
					throw new ServletException("No test parameter given");
				}
				Long testId = Long
					.valueOf(pageContext.getParameter("test"));
				
				f.beginTransaction();		
				IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
				ITestDAO testDao = f.getTestDAOInstance();
				IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
				original = testDao.retrievePersistentEntity(testId);
				originalParent = assignmentDao.retrievePersistentEntity(original.getAssignmentId());
				if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), originalParent.getModuleId())) {
					f.abortTransaction();
					throw new ServletException("permission denied");
				}

				f.endTransaction();
				
				templateContext.put("create", false);
			}
			
			// Now get the assignment.
			f.beginTransaction();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			originalParent = assignmentDao.retrievePersistentEntity(original.getAssignmentId());
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			originalParentParent = moduleDao.retrievePersistentEntity(originalParent.getModuleId());
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
		
		// Get tests
		Collection<TestMethodDescription> testMethods = methodDirectory.getTestMethodDescriptions();
		templateContext.put("testMethods", testMethods);
		
		// Get executors
		Collection<TestExecutorDescription> executorMethods = executorDirectory.getTestExecutorDescriptions();
		templateContext.put("executorMethods", executorMethods);
		
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("module", originalParentParent);
		templateContext.put("assignment", originalParent);
		templateContext.put("test", original);
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}
	
}
