package uk.ac.warwick.dcs.boss.frontend.sites.staffpages;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

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
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.ITestDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;
import uk.ac.warwick.dcs.boss.model.testing.tests.ITestMethodDirectory;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodDescription;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodFactory;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodParameterDescription;

public class PerformEditTestParametersPage extends Page {

	public PerformEditTestParametersPage() throws PageLoadException {
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
		ITestMethodDirectory methodDirectory;
		try {
			DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			TestMethodFactory tmdf = (TestMethodFactory)FactoryRegistrar.getFactory(TestMethodFactory.class);
			f = df.getInstance();
			methodDirectory = tmdf.getInstance();

		} catch (FactoryException e) {
			throw new ServletException("dao init error", e);
		}

		Test test;
		Assignment assignment;
		
		// Get testId
		String testString = pageContext.getParameter("test");
		if (testString == null) {
			throw new ServletException("No test parameter given");
		}
		Long testId = Long
		.valueOf(pageContext.getParameter("test"));

		
		try {
			f.beginTransaction();		
			IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
			IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
			ITestDAO testDao = f.getTestDAOInstance();
			test = testDao.retrievePersistentEntity(testId);
			assignment = assignmentDao.retrievePersistentEntity(test.getAssignmentId());
			
			if (!staffInterfaceQueriesDao.isStaffModuleAccessAllowed(pageContext.getSession().getPersonBinding().getId(), assignment.getModuleId())) {
				f.abortTransaction();
				throw new ServletException("permission denied");
			}
			f.endTransaction();

		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}

		// Get tests
		Collection<TestMethodParameterDescription> testParameters = null;
		Collection<TestMethodDescription> testMethods = methodDirectory.getTestMethodDescriptions();
		for (TestMethodDescription t: testMethods) {
			if (t.getClassName().equals(test.getTestClassName())) {
				testParameters = t.getParameters();
				break;
			}
		}
		
		if (testParameters == null) {
			throw new ServletException("test " + test.getTestClassName() + " is not known about");
		}
		
		// Extract 
		HashMap<String, String> newParameters = new HashMap<String, String>();
		for (TestMethodParameterDescription t : testParameters) {
			if (pageContext.getParameter(t.getName()) == null) {
				if (!t.isOptional()) {
					// TODO: redirect back with a reson
					throw new ServletException("Parameter " + t.getName() + " not provided");
				}
			} else {
				newParameters.put(t.getName(), pageContext.getParameter(t.getName()));
			}
		}
	
		// Replace the parameters.
		try {
			f.beginTransaction();
			
			ITestDAO testDao = f.getTestDAOInstance();
			testDao.setTestParameters(testId, newParameters);
			f.endTransaction();
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao error", e);
		}
		
		// Got the original.
		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("nextPage", pageContext.getPageUrl(StaffPageFactory.SITE_NAME, StaffPageFactory.TESTS_PAGE));
		templateContext.put("nextPageParamName", "assignment");
		templateContext.put("nextPageParamValue", assignment.getId());
		templateContext.put("success", true);
		pageContext.renderTemplate(template, templateContext);

	}
	
}
