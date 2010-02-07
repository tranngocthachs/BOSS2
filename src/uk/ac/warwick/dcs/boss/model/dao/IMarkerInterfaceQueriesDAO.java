package uk.ac.warwick.dcs.boss.model.dao;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerAssignmentsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerMarksQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerStudentsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerSubmissionsQueryResult;

/**
 * This DAO contains operations pertaining to the Marker interface.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if the Marker interface requires
 * them.
 * @author davidbyard
 */
public interface IMarkerInterfaceQueriesDAO {

	/**
	 * Sorting for the assignments to mark query.
	 * @author davidbyard
	 *
	 */
	public enum AssignmentsToMarkSortingType {
		NONE,
		STUDENT_COUNT_ASCENDING, STUDENT_COUNT_DESCENDING
	}
	
	/**
	 * Sorting for the students to mark query.
	 * @author davidbyard
	 *
	 */
	public enum StudentsToMarkSortingType {
		NONE,
		STUDENT_ID_ASCENDING, STUDENT_ID_DESCENDING,
		SUBMISSION_COUNT_ASCENDING, SUBMISSION_COUNT_DESCENDING,
		LAST_MARKED_ASCENDING, LAST_MARKED_DESCENDING
	}
	
	/**
	 * Sorting for the list of marks query.
	 * @author davidbyard
	 *
	 */
	public enum MarksSortingType {
		NONE,
		MARKER_ID_ASCENDING, MARKER_ID_DESCENDING,
		MARKING_CATEGORY_ID_ASCENDING, MARKING_CATEGORY_ID_DESCENDING,
		TIMESTAMP_ASCENDING, TIMESTAMP_DESCENDING,
		MARK_ASCENDING, MARK_DESCENDING
	}
	
	/**
	 * Sorting for the list of submissions query
	 * @author davidbyard
	 *
	 */
	public enum MarkerSubmissionsQuerySortingType {
		NONE,
		ACTIVE_ASCENDING, ACTIVE_DESCENDING,
		SUBMISSION_TIME_ASCENDING, SUBMISSION_TIME_DESCENDING
	}
	
	/**
	 * Possible statuses of an assignment as far as a marker is concerned.
	 * @author davidbyard
	 *
	 */
	public enum AssignmentStatus {
		OPEN,
		CLOSED,
		PUBLISHED,
		ALL
	}

	/**
	 * Get assignments to mark given a marker.  
	 * @param status is the status of the assignments to mark (open, closed, published...)
	 * @param sortingType is the sorting to use.
	 * @param markerId is the marker to query for.
	 * @return a sorted list of assignments to mark.
	 * @throws DAOException
	 */
	public abstract Collection<MarkerAssignmentsQueryResult> performAssignmentsToMarkQuery(AssignmentStatus status, AssignmentsToMarkSortingType sortingType, Long markerId) throws DAOException;
	
	/**
	 * Get students to mark given an assignment and a marker. 
	 * @param sortingType is the sorting to use.
	 * @param markerId is the marker to query for.
	 * @param assignmentId is the assignment to mark.
	 * @return a sorted list of students to mark.
	 * @throws DAOException
	 */
	public abstract Collection<MarkerStudentsQueryResult> performStudentsToMarkQuery(StudentsToMarkSortingType sortingType, Long markerId, Long assignmentId) throws DAOException;
	
	/**
	 * Get list of created marks for a marking assignment.
	 * @param sortingType is the sorting to use.
	 * @param markingAssignmentId is the marking assignment to query.
	 * @return list of marks for a marking assignment.
	 * @throws DAOException
	 */
	public abstract Collection<MarkerMarksQueryResult> performMarkerMarksQuery(MarksSortingType sortingType, Long markingAssignmentId) throws DAOException;
	
	/**
	 * Get list of all marks for a given marker, student and assignment.  Moderators can use this list to
	 * draw statistics create their own marks.
	 * @param sortingType is the sorting to use.
	 * @param markerId is the marker.
	 * @param studentId is the student.
	 * @param assignmentId is the assignment.
	 * @return a sorted list of marks for a given marker, student and assignment.
	 * @throws DAOException
	 */
	public abstract Collection<MarkerMarksQueryResult> performModeratorMarksQuery(MarksSortingType sortingType, Long markerId, Long studentId, Long assignmentId) throws DAOException;
	
	/**
	 * Get list of submissions to download or test for the given marking assignment.
	 * @param sortingType is the sorting to use.
	 * @param markingAssignmentId is the marking assignment to query.
	 * @return a sorted list of submissions for a given marking assignment.
	 * @throws DAOException
	 */
	public abstract Collection<MarkerSubmissionsQueryResult> performSubmissionsQuery(MarkerSubmissionsQuerySortingType sortingType, Long markingAssignmentId) throws DAOException;

	/**
	 * Check whether a person is permitted to access the given assignment.
	 * 
	 * This is the case if there is a MarkingAssignment in the database specifying the marker and the given assignment.
	 * @param markerId is the person to check permissions for (marker).
	 * @param assignmentId is the assignment to check permissions with.
	 * @return true if the person is permitted to mark the given assignment, false if not.
	 * @throws DAOException
	 */
	public boolean isMarkerAssignmentAccessAllowed(Long markerId, Long assignmentId) throws DAOException;

	/**
	 * Check whether a person is permitted to access the given submissions.
	 * 
	 * This is the case if there is a MarkingAssignment in the database specifying the marker, the submission's assignment, and the submission's student.
	 * @param markerId is the person to check permissions for (marker).
	 * @param submissionId is the submission to check permissions with.
	 * @return true if the person is permitted to mark the given assignment, false if not.
	 * @throws DAOException
	 */
	public boolean isMarkerSubmissionAccessAllowed(Long markerId, Long submissionId) throws DAOException;

	/**
	 * Check whether a person is permitted to access the given marking assignment.
	 * 
	 * This is the case if the person is the marker of the MarkingAssignment.
	 * @param markerId is the person to check permissions for (marker).
	 * @param markingAssignmentId is the marking assignment to check permissions with.
	 * @return true if the person is permitted to mark the given assignment, false if not.
	 * @throws DAOException
	 */
	public boolean isMarkerMarkingAssignmentAccessAllowed(Long markerId, Long markingAssignmentId) throws DAOException;

	
}
