package uk.ac.warwick.dcs.boss.frontend.sites;

import java.util.Iterator;

import org.openide.util.Lookup;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageFactory;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.markerpages.AssignmentsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.markerpages.DownloadSubmissionPage;
import uk.ac.warwick.dcs.boss.frontend.sites.markerpages.EditMarkPage;
import uk.ac.warwick.dcs.boss.frontend.sites.markerpages.MarksPage;
import uk.ac.warwick.dcs.boss.frontend.sites.markerpages.PerformEditMarkPage;
import uk.ac.warwick.dcs.boss.frontend.sites.markerpages.PerformTestPage;
import uk.ac.warwick.dcs.boss.frontend.sites.markerpages.StudentsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.markerpages.SubmissionsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.markerpages.TestPage;
import uk.ac.warwick.dcs.boss.plugins.PluginPage;
import uk.ac.warwick.dcs.boss.plugins.spi.pages.IMarkerPluginPage;

public class MarkerPageFactory extends PageFactory {

	public static String SITE_NAME = "marker";
	
	public static String ASSIGNMENTS_PAGE = "assignments";
	public static String STUDENTS_PAGE = "students";
	public static String MARKS_PAGE = "marks";
	public static String EDIT_MARK_PAGE = "edit_mark";
	public static String PERFORM_EDIT_MARK_PAGE = "perform_edit_mark";
	public static String SUBMISSIONS_PAGE = "submissions";
	public static String TEST_PAGE = "test";
	public static String PERFORM_TEST_PAGE = "perform_test";
	public static String DOWNLOAD_SUBMISSION_PAGE = "download_submission";
	
	@Override
	protected Page getPage(String pageName) throws PageLoadException {
		if (pageName.equals(ASSIGNMENTS_PAGE)) {
			return new AssignmentsPage();
		} else if (pageName.equals(STUDENTS_PAGE)) {
			return new StudentsPage();
		} else if (pageName.equals(MARKS_PAGE)) {
			return new MarksPage();
		} else if (pageName.equals(EDIT_MARK_PAGE)) {
			return new EditMarkPage();
		} else if (pageName.equals(PERFORM_EDIT_MARK_PAGE)) {
			return new PerformEditMarkPage();
		} else if (pageName.equals(SUBMISSIONS_PAGE)) {
			return new SubmissionsPage();
		} else if (pageName.equals(TEST_PAGE)) { 
			return new TestPage();
		} else if (pageName.equals(PERFORM_TEST_PAGE)) { 
			return new PerformTestPage();
		} else if (pageName.equals(DOWNLOAD_SUBMISSION_PAGE)) { 
			return new DownloadSubmissionPage();
		} else {
			Iterator<? extends IMarkerPluginPage> markerPluginPagesIter = Lookup.getDefault().lookupAll(IMarkerPluginPage.class).iterator();
			while (markerPluginPagesIter.hasNext()) {
				IMarkerPluginPage provider = markerPluginPagesIter.next();
				if (provider.getPageName().equals(pageName))
					return new PluginPage(provider);
			}
			
			throw new PageLoadException(404, "Unknown page identifier");
		}
	}

}
