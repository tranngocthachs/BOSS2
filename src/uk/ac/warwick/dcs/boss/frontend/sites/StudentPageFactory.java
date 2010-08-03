package uk.ac.warwick.dcs.boss.frontend.sites;

import java.util.Iterator;

import org.openide.util.Lookup;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageFactory;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.ActivatePage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.AssignmentPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.AssignmentSubmissionsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.AssignmentsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.DeletePage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.DetailsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.DownloadAssignmentResourcePage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.DownloadSubmissionPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.ModulePage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.ModulesPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.PerformActivatePage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.PerformChangePasswordPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.PerformDeletePage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.PerformSubmitPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.PerformTestPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.SubmissionsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.SubmitPage;
import uk.ac.warwick.dcs.boss.frontend.sites.studentpages.TestPage;
import uk.ac.warwick.dcs.boss.plugins.PluginPage;
import uk.ac.warwick.dcs.boss.plugins.spi.pages.StudentPluginPageProvider;

public class StudentPageFactory extends PageFactory {

	public static String SITE_NAME = "student";
	
	public static String ASSIGNMENTS_PAGE = "assignments";
	public static String ASSIGNMENT_PAGE = "assignment";
	public static String DOWNLOAD_ASSIGNMENT_RESOURCE_PAGE = "download_assignment_resource";
	public static String MODULES_PAGE = "modules";
	public static String MODULE_PAGE = "module";
	public static String DETAILS_PAGE = "details";
	public static String SUBMIT_PAGE = "submit";
	public static String PERFORM_SUBMIT_PAGE = "perform_submit";
	public static String SUBMISSIONS_PAGE = "submissions";
	public static String DOWNLOAD_SUBMISSION_PAGE = "download_submission";
	public static String ASSIGNMENT_SUBMISSIONS_PAGE = "assignment_submissions";
	public static String TEST_PAGE = "test";
	public static String PERFORM_TEST_PAGE = "perform_test";
	public static String DELETE_PAGE = "delete";
	public static String PERFORM_DELETE_PAGE = "perform_delete";
	public static String ACTIVATE_PAGE = "activate";
	public static String PERFORM_ACTIVATE_PAGE = "perform_activate";
	public static String PERFORM_CHANGE_PASSWORD_PAGE = "perform_change_password";
	
	@Override
	protected Page getPage(String pageName) throws PageLoadException {
		if (pageName.equals(ASSIGNMENTS_PAGE)) {
			return new AssignmentsPage();
		} else if (pageName.equals(ASSIGNMENT_PAGE)) {
			return new AssignmentPage();
		} else if (pageName.equals(MODULES_PAGE)) {
			return new ModulesPage();
		} else if (pageName.equals(MODULE_PAGE)) {
			return new ModulePage();
		} else if (pageName.equals(DETAILS_PAGE)) {
			return new DetailsPage();
		} else if (pageName.equals(SUBMIT_PAGE)) {
			return new SubmitPage();
		} else if (pageName.equals(PERFORM_SUBMIT_PAGE)) {
			return new PerformSubmitPage();
		} else if (pageName.equals(SUBMISSIONS_PAGE)) {
			return new SubmissionsPage();
		} else if (pageName.equals(DOWNLOAD_SUBMISSION_PAGE)) {
			return new DownloadSubmissionPage();
		} else if (pageName.equals(ASSIGNMENT_SUBMISSIONS_PAGE)) {
			return new AssignmentSubmissionsPage();
		} else if (pageName.equals(TEST_PAGE)) {
			return new TestPage();
		} else if (pageName.equals(PERFORM_TEST_PAGE)) {
			return new PerformTestPage();
		} else if (pageName.equals(DELETE_PAGE)) {
			return new DeletePage();
		} else if (pageName.equals(PERFORM_DELETE_PAGE)) {
			return new PerformDeletePage();
		} else if (pageName.equals(ACTIVATE_PAGE)) {
			return new ActivatePage();
		} else if (pageName.equals(PERFORM_ACTIVATE_PAGE)) {
			return new PerformActivatePage();
		} else if (pageName.equals(DOWNLOAD_ASSIGNMENT_RESOURCE_PAGE)) {
			return new DownloadAssignmentResourcePage();
		} else if (pageName.equals(PERFORM_CHANGE_PASSWORD_PAGE)) {
			return new PerformChangePasswordPage();
		} else {
			Iterator<? extends StudentPluginPageProvider> studentPluginPagesIter = Lookup.getDefault().lookupAll(StudentPluginPageProvider.class).iterator();
			while (studentPluginPagesIter.hasNext()) {
				StudentPluginPageProvider provider = studentPluginPagesIter.next();
				if (provider.getName().equals(pageName))
					return new PluginPage(provider);
			}
			
			throw new PageLoadException(404, "page not found");
		}
	}

}
