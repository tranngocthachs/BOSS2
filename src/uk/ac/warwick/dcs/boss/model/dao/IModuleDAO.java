package uk.ac.warwick.dcs.boss.model.dao;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

/**
 * This DAO contains operations pertaining to the Module DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IModuleDAO extends IEntityDAO<Module> {

	/**
	 * Sorting for Module bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		UNIQUE_IDENTIFIER_ASCENDING, UNIQUE_IDENTIFIER_DESCENDING
	}

	/**
	 * Choose the sorting to use when Module bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	public abstract void setSortingType(SortingType sortingType)  throws DAOException;
	
	/**
	 * Associate an administrator with a module.
	 * @param moduleId is the module.
	 * @param administratorId is the person (administrator).
	 * @throws DAOException
	 */
	public abstract void addAdministratorAssociation(Long moduleId, Long administratorId) throws DAOException;

	/**
	 * Associate a student with a module.
	 * @param moduleId is the module.
	 * @param studentId is the person (student).
	 * @throws DAOException
	 */
	public abstract void addStudentAssociation(Long moduleId, Long studentId) throws DAOException;

	/**
	 * Fetch administrators for a module.
	 * @param sortingType is the sorting used for the returned administrators.
	 * @param moduleId is the module.
	 * @return a sorted list of administrators associated with the given module.
	 * @throws DAOException
	 */
	public abstract Collection<Person> fetchAdministrators(IPersonDAO.SortingType sortingType, Long moduleId) throws DAOException;
	
	/**
	 * Fetch students for a module.
	 * @param sortingType is the sorting used for the returned students.
	 * @param moduleId is the module.
	 * @return a sorted list of students associated with the given module.
	 * @throws DAOException
	 */
	public abstract Collection<Person> fetchStudents(IPersonDAO.SortingType sortingType, Long moduleId) throws DAOException;

	/**
	 * Check whether a module has child assignments.
	 * @param moduleId is the module to check.
	 * @return true if the module has child assignments, false if not.
	 * @throws DAOException
	 */
	public abstract boolean hasAssignments(Long moduleId) throws DAOException;

	/**
	 * Remove an administrator from a module.
	 * @param moduleId is the module.
	 * @param administratorId is the person (administrator).
	 * @throws DAOException
	 */
	public abstract void removeAdministratorAssociation(Long moduleId, Long administratorId) throws DAOException;

	/**
	 * Remove a student from a module.
	 * @param moduleId is the module.
	 * @param studentId is the person (student).
	 * @throws DAOException
	 */
	public abstract void removeStudentAssociation(Long moduleId, Long studentId) throws DAOException;
}