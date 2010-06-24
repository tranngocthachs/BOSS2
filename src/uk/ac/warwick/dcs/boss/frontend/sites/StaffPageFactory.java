package uk.ac.warwick.dcs.boss.frontend.sites;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageFactory;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.AddAssignmentFilePage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.AssignmentFilesPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.AssignmentsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.AutoAssignMarkersPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.DeadlineRevisionsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.DownloadSubmissionPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.EditAssignmentPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.EditDeadlineRevisionPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.EditMarkingAssignmentPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.EditMarkingCategoryPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.EditTestPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.EditTestParametersPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.EmailResultsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.FindMarkerPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.FindModeratorPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.FindStudentPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.InitSherlockPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.MarkersPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.MarkingAssignmentsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.MarkingCategoriesPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.ModulesPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.MultiDownloadPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformAddAssignmentFilePage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformAddMarkerPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformAddStudentPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformAutoAssignMarkersPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformEditAssignmentPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformEditDeadlineRevisionPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformEditMarkingAssignmentPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformEditMarkingCategoryPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformEditTestPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformEditTestParametersPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformEmailResultsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformFindMarkerPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformFindModeratorPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformFindStudentPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformMultiDownloadPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformPublishResultsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformRemoveAssignmentFilePage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformRemoveMarkerPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformRemoveStudentPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformRunSherlockPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformTestHashPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformUploadAssignmentResourcePage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PerformUploadTestResourcePage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.PublishResultsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.ResultsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.SaveSherlockSessionPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.SherlockOneMatchPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.StudentsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.SubmissionsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.TestHashPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.TestsPage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.UploadAssignmentResourcePage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.UploadTestResourcePage;
import uk.ac.warwick.dcs.boss.frontend.sites.staffpages.RunSherlockPage;

public class StaffPageFactory extends PageFactory {

	public static String SITE_NAME = "staff";

	public static String MODULES_PAGE = "modules";
	public static String ASSIGNMENTS_PAGE = "assignments";
	public static String EDIT_ASSIGNMENT_PAGE = "edit_assignment";
	public static String PERFORM_EDIT_ASSIGNMENT_PAGE = "perform_edit_assignment";
	public static String ASSIGNMENT_FILES_PAGE = "assignment_files";
	public static String ADD_ASSIGNMENT_FILE_PAGE = "add_assignment_file";
	public static String PERFORM_ADD_ASSIGNMENT_FILE_PAGE = "perform_add_assignment_file";
	public static String PERFORM_REMOVE_ASSIGNMENT_FILE_PAGE = "perform_remove_assignment_file";
	public static String MARKING_CATEGORIES_PAGE = "markingcategories";
	public static String EDIT_MARKING_CATEGORY_PAGE = "edit_markingcategory";
	public static String PERFORM_EDIT_MARKING_CATEGORY_PAGE = "perform_edit_markingcategory";
	public static String EDIT_TEST_PAGE = "edit_test";
	public static String PERFORM_EDIT_TEST_PAGE = "perform_edit_test";
	public static String EDIT_TEST_PARAMETERS_PAGE = "edit_test_parameters";
	public static String PERFORM_EDIT_TEST_PARAMETERS_PAGE = "perform_edit_test_parameters";
	public static String DEADLINE_REVISIONS_PAGE = "deadlinerevisions";
	public static String EDIT_DEADLINE_REVISION_PAGE = "edit_deadlinerevision";
	public static String PERFORM_EDIT_DEADLINE_REVISION_PAGE = "perform_edit_deadlinerevision";
	public static String TESTS_PAGE = "tests";
	public static String SUBMISSIONS_PAGE = "submissions";
	public static String DOWNLOAD_SUBMISSION_PAGE = "download_submission";
	public static String MARKERS_PAGE = "markers";
	public static String STUDENTS_PAGE = "students";
	public static String FIND_STUDENT_PAGE = "find_student";
	public static String PERFORM_FIND_STUDENT_PAGE = "perform_find_student";
	public static String PERFORM_ADD_STUDENT_PAGE = "perform_add_student";
	public static String PERFORM_REMOVE_STUDENT_PAGE = "perform_remove_student";
	public static String FIND_MARKER_PAGE = "find_marker";
	public static String PERFORM_FIND_MARKER_PAGE = "perform_find_marker";
	public static String PERFORM_ADD_MARKER_PAGE = "perform_add_marker";
	public static String PERFORM_REMOVE_MARKER_PAGE = "perform_remove_marker";
	public static String FIND_MODERATOR_PAGE = "find_moderator";
	public static String PERFORM_FIND_MODERATOR_PAGE = "perform_find_moderator";
	public static String PERFORM_ADD_MODERATOR_PAGE = "perform_add_moderator";
	public static String PERFORM_REMOVE_MODERATOR_PAGE = "perform_remove_moderator";
	public static String UPLOAD_ASSIGNMENT_RESOURCE_PAGE = "upload_assignment_resource";
	public static String PERFORM_UPLOAD_ASSIGNMENT_RESOURCE_PAGE = "perform_upload_assignment_resource";
	public static String UPLOAD_TEST_RESOURCE_PAGE = "upload_test_resource";
	public static String PERFORM_UPLOAD_TEST_RESOURCE_PAGE = "perform_upload_test_resource";
	public static String MARKING_ASSIGNMENTS_PAGE = "markingassignments";
	public static String EDIT_MARKING_ASSIGNMENT_PAGE = "edit_markingassignment";
	public static String PERFORM_EDIT_MARKING_ASSIGNMENT_PAGE = "perform_edit_markingassignment";
	public static String AUTO_ASSIGN_MARKERS_PAGE = "auto_assign_markers";
	public static String PERFORM_AUTO_ASSIGN_MARKERS_PAGE = "perform_auto_assign_markers";
	public static String PUBLISH_RESULTS_PAGE = "publish_results";
	public static String PERFORM_PUBLISH_RESULTS_PAGE = "perform_publish_results";
	public static String RESULTS_PAGE = "results";
	public static String EMAIL_RESULTS_PAGE = "email_results";
	public static String PERFORM_EMAIL_RESULTS_PAGE = "perform_email_results";
	public static String TEST_HASH_PAGE = "test_hash";
	public static String PERFORM_TEST_HASH_PAGE = "perform_test_hash";
	public static String MULTI_DOWNLOAD_PAGE = "multi_download";
	public static String PERFORM_MULTI_DOWNLOAD_PAGE = "perform_multi_download";
	public static String RUN_SHERLOCK_PAGE = "run_sherlock";
	public static String PERFORM_RUN_SHERLOCK_PAGE = "perform_run_sherlock";
	public static String SHERLOCK_ONE_MATCH_PAGE = "sherlock_one_match";
	public static String SAVE_SHERLOCK_SESSION_PAGE = "save_sherlock_session";
	public static String INIT_SHERLOCK_PAGE = "init_sherlock";

	@Override
	protected Page getPage(String pageName) throws PageLoadException {
		if (pageName.equals(MODULES_PAGE)) {
			return new ModulesPage();
		} else if (pageName.equals(ASSIGNMENTS_PAGE)) {
			return new AssignmentsPage();
		} else if (pageName.equals(EDIT_ASSIGNMENT_PAGE)) {
			return new EditAssignmentPage();
		} else if (pageName.equals(PERFORM_EDIT_ASSIGNMENT_PAGE)) {
			return new PerformEditAssignmentPage();
		} else if (pageName.equals(ASSIGNMENT_FILES_PAGE)) {
			return new AssignmentFilesPage();
		} else if (pageName.equals(ADD_ASSIGNMENT_FILE_PAGE)) {
			return new AddAssignmentFilePage();
		} else if (pageName.equals(PERFORM_ADD_ASSIGNMENT_FILE_PAGE)) {
			return new PerformAddAssignmentFilePage();
		} else if (pageName.equals(PERFORM_REMOVE_ASSIGNMENT_FILE_PAGE)) {
			return new PerformRemoveAssignmentFilePage();
		} else if (pageName.equals(MARKING_CATEGORIES_PAGE)) {
			return new MarkingCategoriesPage();
		} else if (pageName.equals(EDIT_MARKING_CATEGORY_PAGE)) {
			return new EditMarkingCategoryPage();
		} else if (pageName.equals(PERFORM_EDIT_MARKING_CATEGORY_PAGE)) {
			return new PerformEditMarkingCategoryPage();
		} else if (pageName.equals(TESTS_PAGE)) {
			return new TestsPage();
		} else if (pageName.equals(SUBMISSIONS_PAGE)) {
			return new SubmissionsPage();
		} else if (pageName.equals(DOWNLOAD_SUBMISSION_PAGE)) { 
			return new DownloadSubmissionPage();
		}else if (pageName.equals(EDIT_TEST_PAGE)) {
			return new EditTestPage();
		} else if (pageName.equals(PERFORM_EDIT_TEST_PAGE)) {
			return new PerformEditTestPage();
		} else if (pageName.equals(EDIT_TEST_PARAMETERS_PAGE)) {
			return new EditTestParametersPage();
		} else if (pageName.equals(PERFORM_EDIT_TEST_PARAMETERS_PAGE)) {
			return new PerformEditTestParametersPage();
		} else if (pageName.equals(DEADLINE_REVISIONS_PAGE)) {
			return new DeadlineRevisionsPage();
		} else if (pageName.equals(EDIT_DEADLINE_REVISION_PAGE)) {
			return new EditDeadlineRevisionPage();
		} else if (pageName.equals(PERFORM_EDIT_DEADLINE_REVISION_PAGE)) {
			return new PerformEditDeadlineRevisionPage();
		} else if (pageName.equals(MARKERS_PAGE)) {	
			return new MarkersPage();
		} else if (pageName.equals(STUDENTS_PAGE)) {
			return new StudentsPage();
		} else if (pageName.equals(FIND_STUDENT_PAGE)) {
			return new FindStudentPage();
		} else if (pageName.equals(PERFORM_FIND_STUDENT_PAGE)) {
			return new PerformFindStudentPage();
		} else if (pageName.equals(FIND_MARKER_PAGE)) {
			return new FindMarkerPage();
		} else if (pageName.equals(PERFORM_FIND_MARKER_PAGE)) {
			return new PerformFindMarkerPage();
		} else if (pageName.equals(PERFORM_ADD_MARKER_PAGE)) {
			return new PerformAddMarkerPage();
		}  else if (pageName.equals(FIND_MODERATOR_PAGE)) {
			return new FindModeratorPage();
		} else if (pageName.equals(PERFORM_FIND_MODERATOR_PAGE)) {
			return new PerformFindModeratorPage();
		} else if (pageName.equals(PERFORM_ADD_STUDENT_PAGE)) {
			return new PerformAddStudentPage();
		} else if (pageName.equals(PERFORM_REMOVE_MARKER_PAGE)) {
			return new PerformRemoveMarkerPage();
		} else if (pageName.equals(PERFORM_REMOVE_STUDENT_PAGE)) {
			return new PerformRemoveStudentPage();
		} else if (pageName.equals(UPLOAD_ASSIGNMENT_RESOURCE_PAGE)) {
			return new UploadAssignmentResourcePage();
		} else if (pageName.equals(PERFORM_UPLOAD_ASSIGNMENT_RESOURCE_PAGE)) {
			return new PerformUploadAssignmentResourcePage();
		} else if (pageName.equals(UPLOAD_TEST_RESOURCE_PAGE)) {
			return new UploadTestResourcePage();
		} else if (pageName.equals(PERFORM_UPLOAD_TEST_RESOURCE_PAGE)) {
			return new PerformUploadTestResourcePage();
		} else if (pageName.equals(MARKING_ASSIGNMENTS_PAGE)) {
			return new MarkingAssignmentsPage();
		} else if (pageName.equals(EDIT_MARKING_ASSIGNMENT_PAGE)) {
			return new EditMarkingAssignmentPage();
		} else if (pageName.equals(PERFORM_EDIT_MARKING_ASSIGNMENT_PAGE)) {
			return new PerformEditMarkingAssignmentPage();
		} else if (pageName.equals(AUTO_ASSIGN_MARKERS_PAGE)) {
			return new AutoAssignMarkersPage();
		} else if (pageName.equals(PERFORM_AUTO_ASSIGN_MARKERS_PAGE)) {
			return new PerformAutoAssignMarkersPage();
		} else if (pageName.equals(PUBLISH_RESULTS_PAGE)) {
			return new PublishResultsPage();
		} else if (pageName.equals(PERFORM_PUBLISH_RESULTS_PAGE)) {
			return new PerformPublishResultsPage();
		} else if (pageName.equals(RESULTS_PAGE)) {
			return new ResultsPage();
		} else if (pageName.equals(EMAIL_RESULTS_PAGE)) {
			return new EmailResultsPage();
		} else if (pageName.equals(PERFORM_EMAIL_RESULTS_PAGE)) {
			return new PerformEmailResultsPage();
		} else if (pageName.equals(TEST_HASH_PAGE)) {
			return new TestHashPage();
		} else if (pageName.equals(PERFORM_TEST_HASH_PAGE)) {
			return new PerformTestHashPage();
		} else if (pageName.equals(MULTI_DOWNLOAD_PAGE)) {
			return new MultiDownloadPage();
		} else if (pageName.equals(PERFORM_MULTI_DOWNLOAD_PAGE)) {
			return new PerformMultiDownloadPage();
		} else if (pageName.equals(RUN_SHERLOCK_PAGE)) {
			return new RunSherlockPage();
		} else if (pageName.equals(PERFORM_RUN_SHERLOCK_PAGE)) {
			return new PerformRunSherlockPage();
		} else if (pageName.equals(SHERLOCK_ONE_MATCH_PAGE)) {
			return new SherlockOneMatchPage();
		} else if (pageName.equals(SAVE_SHERLOCK_SESSION_PAGE)) {
			return new SaveSherlockSessionPage();
		} else if (pageName.equals(INIT_SHERLOCK_PAGE)) {
			return new InitSherlockPage();
		} else {
			throw new PageLoadException(404, "page not found");
		}
	}

}
