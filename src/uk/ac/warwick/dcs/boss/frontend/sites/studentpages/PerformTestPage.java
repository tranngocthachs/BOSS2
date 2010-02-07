package uk.ac.warwick.dcs.boss.frontend.sites.studentpages;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageContext;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.StudentPageFactory;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;
import uk.ac.warwick.dcs.boss.model.testing.ITestRunner;
import uk.ac.warwick.dcs.boss.model.testing.TestResult;
import uk.ac.warwick.dcs.boss.model.testing.TestRunnerFactory;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;

public class PerformTestPage extends Page {

	public PerformTestPage()
	throws PageLoadException {
		super("multi_testresults", AccessLevel.USER);
	}

	@Override
	protected void handleGet(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {

		// Check test results.
		for (Future<TestResult> testResult : pageContext.getSession().getTestResults()) {
			if (!testResult.isDone()) {
				templateContext.put("testsComplete", false);
				pageContext.renderTemplate(template, templateContext);
				return;
			}
		}

		Collection<TestResult> testResults = new LinkedList<TestResult>();
		for (Future<TestResult> t : pageContext.getSession().getTestResults() ) {
			try {
				testResults.add(t.get());
			} catch (Exception e) {
				throw new ServletException("Could not get test result", e);
			}
		}
		pageContext.getSession().getTestResults().clear();

		templateContext.put("greet", pageContext.getSession().getPersonBinding().getChosenName());
		templateContext.put("testResults", testResults);
		templateContext.put("testsComplete", true);
		pageContext.renderTemplate(template, templateContext);

	}

	@Override
	protected void handlePost(PageContext pageContext, Template template,
			VelocityContext templateContext) throws ServletException,
			IOException {
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
			throw new ServletException("Tests are already running..!  Navigate to " + pageContext.getPageUrl(StudentPageFactory.SITE_NAME, StudentPageFactory.PERFORM_TEST_PAGE) + " manually");
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

			IStudentInterfaceQueriesDAO studentInterfaceQueriesDAO = f.getStudentInterfaceQueriesDAOInstance();
			ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();

			if (!studentInterfaceQueriesDAO.isStudentSubmissionAccessAllowed(pageContext.getSession().getPersonBinding().getId(), submissionId)) {
				f.abortTransaction();
				throw new DAOException("permission denied");
			}

			Submission submission = submissionDao.retrievePersistentEntity(submissionId);
			Collection<Test> tests = studentInterfaceQueriesDAO.fetchSubmissionTests(submissionId);
			Collection<Test> testsToRun = new LinkedList<Test>();

			for (Test t : tests) {
				if (pageContext.getParameter("test" + t.getId()) != null) {
					testsToRun.add(t);
				}
			}

			if (testsToRun.isEmpty()) {
				f.endTransaction();
				pageContext.performRedirect(pageContext.getPageUrl(StudentPageFactory.SITE_NAME, StudentPageFactory.TEST_PAGE) + "?submission=" + submissionId + "&missing=true");
				return;
			}

			for (Test t : testsToRun) {
				if (t.getStudentTest()) {
					pageContext.log(Level.INFO, "Scheduling " + t.getName());
					Future<TestResult> schedule = testRunner.runTest(submission, t);
					pageContext.getSession().getTestResults().add(schedule);
				}
			}

			f.endTransaction();

			pageContext.performRedirect(pageContext.getPageUrl(StudentPageFactory.SITE_NAME, StudentPageFactory.PERFORM_TEST_PAGE));
		} catch (DAOException e) {
			f.abortTransaction();
			throw new ServletException("dao exception", e);
		} catch (TestingException e) {
			throw new ServletException("test exception", e);
		}

	}	
}
