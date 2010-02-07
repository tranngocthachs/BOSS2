package uk.ac.warwick.dcs.boss.model.dao;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

/**
 * This DAO contains operations pertaining to the Person DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IPersonDAO extends IEntityDAO<Person> {

	/**
	 * Sorting for Person bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		UNIQUE_IDENTIFIER_ASCENDING, UNIQUE_IDENTIFIER_DESCENDING,
		CHOSEN_NAME_ASCENDING, CHOSEN_NAME_DESCENDING,
		EMAIL_ADDRESS_ASCENDING, EMAIL_ADDRESS_DESCENDING,
		ADMINISTRATOR_ASCENDING, ADMINISTRATOR_DESCENDING
	}
	
	/**
	 * Choose the sorting to use when Person bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	public abstract void setSortingType(SortingType sortingType)  throws DAOException;

	/**
	 * Check whether a person has child submissions.
	 * @param personId is the person to check.
	 * @return true if the person has child submissions, false if not.
	 * @throws DAOException
	 */
	public abstract boolean hasSubmissions(Long personId) throws DAOException;
	
	/**
	 * Fetch a list of assignments that a person is a registered marker for.
	 * @param sortingType is the order to sort the returned assignments.
	 * @param markerId is the person (marker) to query for.
	 * @return a list of assignments that the person is a registered marker for.
	 * @throws DAOException
	 */
	public abstract Collection<Assignment> fetchAssignmentsToMark(IAssignmentDAO.SortingType sortingType, Long markerId) throws DAOException;
	
	/**
	 * Fetch a list of modules that a person is a registered administrator for.
	 * @param sortingType is the order to sort the returned assignments.
	 * @param administratorId is the person (administrator) to query for.
	 * @return a list of assignments that the person is a registered administrator for.
	 * @throws DAOException
	 */
	public abstract Collection<Module> fetchModulesToAdministrate(IModuleDAO.SortingType sortingType, Long administratorId) throws DAOException;

	/**
	 * Fetch a list of modules that a person is a registered student for.
	 * @param sortingType is the order to sort the returned assignments.
	 * @param administratorId is the person (student) to query for.
	 * @return a list of assignments that the person is a registered student for.
	 * @throws DAOException
	 */
	public abstract Collection<Module> fetchModulesToStudy(IModuleDAO.SortingType sortingType, Long studentId) throws DAOException;
	
	/**
	 * Find the person specified by the given unique identifier.
	 * @param uniqueIdentifier is the unique identifier.
	 * @return a person if one found, null if not.
	 */
	public abstract Person fetchPersonWithUniqueIdentifier(String uniqueIdentifier) throws DAOException;
}
