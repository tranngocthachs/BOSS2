package uk.ac.warwick.dcs.boss.model.dao;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.beans.Test;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentAssignmentsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentModulesQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentSubmissionCountsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentSubmissionsQueryResult;

/**
 * This DAO contains operations pertaining to the Student interface. Queries
 * requiring joins or needing particularly complex/hybrid operations are placed
 * in here if the Staff interface requires them.
 * 
 * @author davidbyard
 */
public interface IStudentInterfaceQueriesDAO {

	/**
	 * Sorting times for the assignments query.
	 * 
	 * @author davidbyard
	 * 
	 */
	public enum StudentAssignmentsQuerySortingType {
		NONE, DEADLINE_ASCENDING, DEADLINE_DESCENDING, CLOSING_TIME_ASCENDING, CLOSING_TIME_DESCENDING, LAST_SUBMITTED_ASCENDING, LAST_SUBMITTED_DESCENDING
	}

	/**
	 * Sorting types for the modules query.
	 * 
	 * @author davidbyard
	 * 
	 */
	public enum StudentModulesQuerySortingType {
		NONE, MODEL_ID_ASCENDING, MODEL_ID_DESCENDING, ASSIGNMENT_COUNT_ASCENDING, ASSIGNMENT_COUNT_DESCENDING,
	}

	/**
	 * Sorting ttypes for the submission counts query.
	 * 
	 * @author davidbyard
	 * 
	 */
	public enum StudentSubmissionCountsQuerySortingType {
		NONE, SUBMISSION_COUNT_ASCENDING, SUBMISSION_COUNT_DESCENDING
	}

	/**
	 * Sorting types for the previous submissions query.
	 * 
	 * @author davidbyard
	 * 
	 */
	public enum StudentSubmissionsQuerySortingType {
		NONE, ACTIVE_ASCENDING, ACTIVE_DESCENDING, SUBMISSION_TIME_ASCENDING, SUBMISSION_TIME_DESCENDING
	}

	/**
	 * Possible statuses for an assignment to be in as far as a student is
	 * concerned.
	 * 
	 * @author davidbyard
	 * 
	 */
	public enum AssignmentStatus {
		OPEN, CLOSED, BOTH
	}

	/**
	 * Fetch tests that a student may run on a given submission.
	 * 
	 * @param submissionId
	 *            is the submission in consideration.
	 * @return tests that a student may run on a given submission.
	 * @throws DAOException
	 */
	public Collection<Test> fetchSubmissionTests(Long submissionId)
			throws DAOException;

	/**
	 * Fetch details for a particular assignment.
	 * 
	 * @param studentId
	 *            is the student requesting the assignment details.
	 * @param assignmentId
	 *            is the assignment to fetch details for.
	 * @return details for a particular assignment.
	 * @throws DAOException
	 */
	public StudentAssignmentsQueryResult performAssignmentDetailsQuery(
			Long studentId, Long assignmentId) throws DAOException;

	/**
	 * Fetch the number of previous submissions for assignments with a given
	 * status.
	 * 
	 * @param status
	 *            is the status mask.
	 * @param sortingType
	 *            is the order to sort the result in.
	 * @param studentId
	 *            is the student to fetch previous submission details for.
	 * @return the number of previous submissions for assignments with the given
	 *         status.
	 * @throws DAOException
	 */
	public Collection<StudentSubmissionCountsQueryResult> performStudentSubmissionCountsQuery(
			AssignmentStatus status,
			IStudentInterfaceQueriesDAO.StudentSubmissionCountsQuerySortingType sortingType,
			Long studentId) throws DAOException;

	/**
	 * Fetch the previous submissions for a given assignment by a given student.
	 * 
	 * @param sortingType
	 *            is the order to sort the result in.
	 * @param studentId
	 *            is the student to fetch previous submissions for.
	 * @param assignmentId
	 *            is the assignment to fetch previous submissions for.
	 * @return the previous submissions for a given assignment by a given
	 *         student.
	 * @throws DAOException
	 */
	public Collection<StudentSubmissionsQueryResult> performStudentSubmissionsQuery(
			IStudentInterfaceQueriesDAO.StudentSubmissionsQuerySortingType sortingType,
			Long studentId, Long assignmentId) throws DAOException;

	/**
	 * Fetch all modules that a student is able to access (useful for accessing
	 * modules that don't need registration).
	 * 
	 * @param sortingType
	 *            is the order to sort the result in.
	 * @param studentId
	 *            is the student to fetch accessible modules for.
	 * @return all modules that a student is able to access.
	 * @throws DAOException
	 */
	public Collection<StudentModulesQueryResult> performStudentModulesQuery(
			StudentModulesQuerySortingType sortingType, Long studentId)
			throws DAOException;

	/**
	 * Fetch all assignments that a student is able to access given a particular
	 * status.
	 * 
	 * @param status
	 *            is the status mask.
	 * @param sortingType
	 *            is the order to sort the result in.
	 * @param studentId
	 *            is the student to fetch accessible assignments for.
	 * @return all assignments that a student able to access with the given
	 *         status.
	 * @throws DAOException
	 */
	public Collection<StudentAssignmentsQueryResult> performStudentAssignmentsQuery(
			AssignmentStatus status,
			StudentAssignmentsQuerySortingType sortingType, Long studentId)
			throws DAOException;

	/**
	 * Fetch all assignments that a student is able to access for a particular
	 * module.
	 * 
	 * @param sortingType
	 *            is the order to sort the result in.
	 * @param studentId
	 *            is the student to fetch accessible assignments for.
	 * @param moduleId
	 *            is the module to filter by.
	 * @return all assignments that a student is able to access for the given
	 *         module.
	 * @throws DAOException
	 */
	public Collection<StudentAssignmentsQueryResult> performStudentAssignmentsQuery(
			StudentAssignmentsQuerySortingType sortingType, Long studentId,
			Long moduleId) throws DAOException;

	/**
	 * Make a given submission active. This will make all other submissions by
	 * that person for the given assignment inactive.
	 * 
	 * @param personId
	 *            is the person to make a submission active for.
	 * @param assignmentId
	 *            is the assignment that the submission is for.
	 * @param submissionId
	 *            is the submission to make active.
	 * @throws DAOException
	 */
	public void makeSubmissionActive(Long personId, Long assignmentId,
			Long submissionId) throws DAOException;

	/**
	 * Check whether a student may access a module.
	 * 
	 * This is the case if a student is registered on it or it doesn't require
	 * registration.
	 * 
	 * @param personId
	 *            is the person to check permissions for (Student)
	 * @param moduleId
	 *            is the module to check permissions with
	 * @return whether a student may access a module.
	 * @throws DAOException
	 */
	public boolean isStudentModuleAccessAllowed(Long personId, Long moduleId)
			throws DAOException;

	/**
	 * Check whether a student is in time to submit a solution to a given
	 * assignment.
	 * 
	 * This is the case if the assignment has opened and has not yet closed or
	 * the student has a deadline revision overriding the assignment's closing
	 * time.
	 * 
	 * Also checks if the student is registered on the parent module.
	 * 
	 * @param personId
	 *            is the person to check permissions for (Student)
	 * @param assignmentId
	 *            is the assignment to check permissions with
	 * @return whether a student may submit a solution the given assignment.
	 * @throws DAOException
	 */
	public boolean isStudentAllowedToSubmit(Long personId, Long assignmentId)
			throws DAOException;

	/**
	 * Check whether a student may access a given submission.
	 * 
	 * Currently this is only the case if the student submitted it.
	 * 
	 * @param personId
	 *            is the person to check permissions for (Student)
	 * @param assignmentId
	 *            is the assignment to check permissions with
	 * @return whether a student may submit a solution the given assignment.
	 * @throws DAOException
	 */
	public boolean isStudentSubmissionAccessAllowed(Long personId,
			Long submissionId) throws DAOException;
}
