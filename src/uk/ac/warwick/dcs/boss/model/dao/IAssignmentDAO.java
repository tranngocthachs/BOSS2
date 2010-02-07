package uk.ac.warwick.dcs.boss.model.dao;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

/**
 * This DAO contains operations pertaining to the Assignment DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IAssignmentDAO extends IEntityDAO<Assignment> {

	/**
	 * Sorting for assignment bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		DEADLINE_ASCENDING, DEADLINE_DESCENDING,
		UNIQUE_IDENTIFIER_ASCENDING, UNIQUE_IDENTIFIER_DESCENDING
	}
	
	/**
	 * Choose the sorting to use when assignment bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	public abstract void setSortingType(SortingType sortingType) throws DAOException;
	
	/**
	 * Associate a marker with an assignment.
	 * @param assignmentId is the assignment.
	 * @param markerId is the person (marker).
	 * @throws DAOException
	 */
	public abstract void addMarkerAssociation(Long assignmentId, Long markerId) throws DAOException;
	
	/**
	 * Add a filename required for submission to this assignment.
	 * @param assignmentId is the assignment.
	 * @param submissionFilename is the filename required.
	 * @throws DAOException
	 */
	public abstract void addRequiredFilename(Long assignmentId, String submissionFilename) throws DAOException;
	
	/**
	 * Fetch markers for an assignment.
	 * @param sortingType is the sorting used for the returned markers.
	 * @param assignmentId is the assignment.
	 * @return a sorted list of markers associated with this assignment.
	 * @throws DAOException
	 */
	public abstract Collection<Person> fetchMarkers(IPersonDAO.SortingType sortingType, Long assignmentId) throws DAOException;
	
	/**
	 * Fetch filenames required for submission to an assignment.
	 * @param assignmentId is the assignment.
	 * @return filenames required for submission to an assignment.
	 * @throws DAOException
	 */
	public abstract Collection<String> fetchRequiredFilenames(Long assignmentId) throws DAOException;

	/**
	 * Return whether the given assignment has any child tests.
	 * @param assignmentId is the assignment.
	 * @return true if the given assignment has any child tests, false if not.
	 * @throws DAOException
	 */
	public abstract boolean hasTests(Long assignmentId) throws DAOException;
	
	/**
	 * Return whether the given assignment has any child marking categories.
	 * @param assignmentId is the assignment.
	 * @return true if the given assignment has any child marking categories, false if not.
	 * @throws DAOException
	 */
	public abstract boolean hasMarkingCategories(Long assignmentId) throws DAOException;
	
	/**
	 * Return whether the given assignment has any child deadline revisions.
	 * @param assignmentId is the assignment.
	 * @return true if the given assignment has any child deadline revisions, false if not.
	 * @throws DAOException
	 */
	public abstract boolean hasDeadlineRevisions(Long assignmentId) throws DAOException;
	
	/**
	 * Return whether the given assignment has any child submissions.
	 * @param assignmentId is the assignment.
	 * @return true if the given assignment has any child submissions, false if not.
	 * @throws DAOException
	 */
	public abstract boolean hasSubmissions(Long assignmentId) throws DAOException;
	
	/**
	 * Remove a marker association with an assignment.
	 * @param assignmentId is the assignment.
	 * @param markerId is the person (marker).
	 * @throws DAOException
	 */
	public abstract void removeMarkerAssociation(Long assignmentId, Long markerId) throws DAOException;
	
	/**
	 * Remove a filename required for submission to this assignment.
	 * @param assignmentId is the assignment.
	 * @param submissionFilename is the filename to remove.
	 * @throws DAOException
	 */
	public abstract void removeRequiredFilename(Long assignmentId, String requiredFilename) throws DAOException;

	/**
	 * Return a list of people that should be submitting to an assignment but have not
	 * submitted anything yet.
	 * @param sortingType is the order the result will be sorted in.
	 * @param assignmentId is the assignment to find the late-comers.
	 * @return a list of people that should be submitting to the given assignment but have not submitted anything yet.
	 * @throws DAOException
	 */
	public abstract Collection<Person> fetchStudentsWithNoSubmissions(IPersonDAO.SortingType sortingType, Long assignmentId) throws DAOException;
	
	/**
	 * Return a list of people that have not yet had all of their marks submitted for an assignment.
	 * @param sortingType is the order the result will be sorted in.
	 * @param assignmentId is the assignment which to find those without marks for yet.
	 * @return a list of people that have not yet had all of their marks submitted for the given assignment.
	 * @throws DAOException 
	 */
	public abstract Collection<Person> fetchStudentsWithIncompleteMarks(IPersonDAO.SortingType sortingType, Long assignmentId) throws DAOException;
	
	/**
	 * Return a list of people that are registered on the parent module or have submitted regardless.
	 * @param sortingType is the order the result will be sorted in.
	 * @param assignmentId is the assignment to find submitters for.
	 * @return a list of people that are registered on the parent module or have submitted regardless.
	 * @throws DAOException
	 */
	public abstract Collection<Person> fetchSubmittersAndStudents(IPersonDAO.SortingType sortingType, Long assignmentId) throws DAOException;
}