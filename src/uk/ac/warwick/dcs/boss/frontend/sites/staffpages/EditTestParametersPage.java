package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import uk.ac.warwick.dcs.boss.model.testing.tests.ITestMethodDirectory;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodDescription;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodFactory;

public class EditTestParametersPage extends Page {

	public EditTestParametersPage() throws PageLoadException {
		super("staff_edit_test_parameters", AccessLevel.USER);
	}
	
	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {

		IDAOSession f;
		ITestMethodDirectory methodDirectory;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			TestMethodFactory tmdf = (TestMethodFactory)FactoryRegistrar.getFactory(TestMethodFactory.class);
			f = df.getInstance();
			methodDirectory = tmdf.getInstance();
		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}

		Map<String, String> parameters = new HashMap<String, String>();
		Test test;
		Assignment assignment;
		Module module;
				
		try {
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
			test = testDao.retrievePersistentEntity(testId);
			assignment = assignmentDao.retrievePersistentEntity(test.getAssignmentId());
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}

			parameters = testDao.getTestParameters(testId);
			f.endTransaction();

			// Now get the assignment.
			f.beginTransaction();
			assignment = assignmentDao.retrievePersistentEntity(test.getAssignmentId());
			IModuleDAO moduleDao = f.getModuleDAOInstance();
			module = moduleDao.retrievePersistentEntity(assignment.getModuleId());
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}

		// Get tests
		Collection<TestMethodDescription> testMethods = methodDirectory.getTestMethodDescriptions();
		for (TestMethodDescription t: testMethods) {
			if (t.getClassName().equals(test.getTestClassName())) {
				templateContext.put("testParameters", t.getParameters());
				break;
			}
		}
		
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("module", module);
		templateContext.put("assignment", assignment);
		templateContext.put("test", test);
		templateContext.put("parameters", parameters);
		pageContext.renderTemplate(template, templateContext);
	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
		throw new ServletException("unexpected POST");
	}
	
}
