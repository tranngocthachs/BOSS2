package uk.ac.warwick.dcs.boss.model.dao;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;
import uk.ac.warwick.dcs.boss.model.dao.beans.SherlockSession;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffAssignmentsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffDeadlineRevisionsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffMarkingAssignmentQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffModulesQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffResultsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffSubmissionsQueryResult;

/**
 * This DAO contains operations pertaining to the Staff interface.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if the Staff interface requires
 * them.
 * @author davidbyard
 */
public interface IStaffInterfaceQueriesDAO {

	/**
	 * Sorting methods for the submissions query.
	 * @author davidbyard
	 *
	 */
	public enum StaffSubmissionsQuerySortingType {
		NONE,
		STUDENT_UNIQUE_IDENTIFIER_ASCENDING, STUDENT_UNIQUE_IDENTIFIER_DESCENDING,
		SUBMISSION_TIME_ASCENDING, SUBMISSION_TIME_DESCENDING
	}
	
	/**
	 * Sorting methods for the modules query.
	 * @author davidbyard
	 *
	 */
	public enum StaffModulesQuerySortingType {
		NONE,
		MODEL_ID_ASCENDING, MODEL_ID_DESCENDING,
		ASSIGNMENT_COUNT_ASCENDING, ASSIGNMENT_COUNT_DESCENDING,
		STUDENT_COUNT_ASCENDING, STUDENT_COUNT_DESCENDING
	}
	
	/**
	 * Sorting methods for the assignments query.
	 * @author davidbyard
	 *
	 */
	public enum StaffAssignmentsQuerySortingType {
		NONE,
		MARKING_CATEGORY_COUNT_ASCENDING, MARKING_CATEGORY_COUNT_DESCENDING,
		TEST_COUNT_ASCENDING, TEST_COUNT_DESCENDING,
		MARKER_COUNT_ASCENDING, MARKER_COUNT_DESCENDING,
		SUBMISSION_COUNT_ASCENDING, SUBMISSION_COUNT_DESCENDING,
		DEADLINEREVISION_COUNT_ASCENDING, DEADLINEREVISION_COUNT_DESCENDING,
		MARKINGASSIGNMENT_COUNT_ASCENDING, MARKINGASSIGNMENT_COUNT_DESCENDING,
		RESULT_COUNT_ASCENDING, RESULT_COUNT_DESCENDING
	}
	
	/**
	 * Sorting methods for the deadline revisions query.
	 * @author davidbyard
	 *
	 */
	public enum StaffDeadlineRevisionsQuerySortingType {
		NONE,
		STUDENT_ID_ASCENDING, STUDENT_ID_DESCENDING,
		DEADLINE_ASCENDING, DEADLINE_DESCENDING
	}

	/**
	 * Sorting methods for the marking assignments query.
	 * @author davidbyard
	 *
	 */
	public enum StaffMarkingAssignmentsQuerySortingType {
		NONE,
		STUDENT_ID_ASCENDING, STUDENT_ID_DESCENDING,
		MARKER_ID_ASCENDING, MARKER_ID_DESCENDING,
		BLIND_MARKING_ASCENDING, BLIND_MARKING_DESCENDING,
		MODERATING_ASCENDING, MODERATING_DESCENDING
	}
	
	/**
	 * Sorting methods for the results query.
	 */
	public enum StaffResultsQuerySortingType {
		NONE,
		STUDENT_ID_ASCENDING, STUDENT_ID_DESCENDING,
		TIMESTAMP_ASCENDING, TIMESTAMP_DESCENDING,
		RESULT_ASCENDING, RESULT_DESCENDING,
		INCOMPLETE_MARKS_ASCENDING, INCOMPLETE_MARKS_DESCENDING
	}
	
	/**
	 * Fetch a list of modules that a staff member is allowed to administrate.
	 * @param sortingType is how to sort the returned list.
	 * @param staffId is the person (staff) to query for.
	 * @return a list of modules that a staff member is allowed to administrate.
	 * @throws DAOException
	 */
	public Collection<StaffModulesQueryResult> performStaffModulesQuery(StaffModulesQuerySortingType sortingType, Long staffId) throws DAOException;
	
	/**
	 * Fetch a list of assignments under the given module.
	 * @param sortingType is how to sort the returned list.
	 * @param moduleId is the module to query under.
	 * @return a list of assignments under the given module.
	 * @throws DAOException
	 */
	public Collection<StaffAssignmentsQueryResult> performStaffAssignmentsQuery(StaffAssignmentsQuerySortingType sortingType, Long moduleId) throws DAOException;
	
	/**
	 * Fetch a list of submissions for the given assignment.
	 * @param sortingType is how to sort the returned list.
	 * @param assignmentId is the assignment to query under.
	 * @return a list of submissions for the given assignment.
	 * @throws DAOException
	 */
	public Collection<StaffSubmissionsQueryResult> performStaffSubmissionsQuery(StaffSubmissionsQuerySortingType sortingType, Long assignmentId) throws DAOException;
	
	/**
	 * Fetch a list of deadline revision for the given assignment.
	 * @param sortingType is how to sort the returned list.
	 * @param assignmentId is the assignment to query under.
	 * @return a list of deadline revisions for the given assignment.
	 * @throws DAOException
	 */
	public Collection<StaffDeadlineRevisionsQueryResult> performStaffDeadlineRevisionsQuery(StaffDeadlineRevisionsQuerySortingType sortingType, Long assignmentId) throws DAOException;
	
	/**
	 * Fetch a list of marking assignments for the given assignment.
	 * @param sortingType is how to sort the returned list.
	 * @param assignmentId is the assignment to query under.
	 * @return a list of marking assignments for the given assignment.
	 * @throws DAOException
	 */
	public Collection<StaffMarkingAssignmentQueryResult> performStaffMarkingAssignmentsQuery(StaffMarkingAssignmentsQuerySortingType sortingType, Long assignmentId) throws DAOException;
	
	/**
	 * Obtain the most recent mark for a student in the given marking category.
	 * @param sortingType is the order to sort the marks by.
	 * @param markingCategoryId is the parent market category.
	 * @param studentId is the student.
	 * @return the top mark for the given student under the given marking category
	 *         or null if not found.
	 */
	public abstract Mark fetchLatestMarkForStudent(Long markingCategoryId, Long studentId) throws DAOException;

	/**
	 * Obtain the results for an assignment.
	 * @param sortingType is the order to sort the results by.
	 * @param assignmentId is the assignment to fetch for.
	 * @return the results for an assignment.
	 */
	public abstract Collection<StaffResultsQueryResult> performStaffResultsQuery(StaffResultsQuerySortingType sortingType, Long assignmentId) throws DAOException;
	
	public abstract Collection<SherlockSession> performStaffSherlockSessionsQuery(Long assignmentId) throws DAOException;
	/**
	 * Check whether a given person is allowed to administrate the given module.
	 * 
	 * This is the case if the person is a registered administrator for the given module.
	 * @param staffId is the person to check permissions for (staff)
	 * @param entityId is the module to check permissions with
	 * @return true if the given person is allowed to administrate the given module, false if not.
	 * @throws DAOException
	 */
	abstract public boolean isStaffModuleAccessAllowed(Long staffId, Long moduleId) throws DAOException;

}
