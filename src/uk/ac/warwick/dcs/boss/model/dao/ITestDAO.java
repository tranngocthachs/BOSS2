package uk.ac.warwick.dcs.boss.model.dao;

import java.util.Map;

import uk.ac.warwick.dcs.boss.model.dao.beans.Test;

/**
 * This DAO contains operations pertaining to the Test DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface ITestDAO extends IEntityDAO<Test> {

	/**
	 * Sorting for Test bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		UNIQUE_IDENTIFIER_ASCENDING, UNIQUE_IDENTIFIER_DESCENDING,
		TEST_CLASS_NAME_ASCENDING, TEST_CLASS_NAME_DESCENDING,
		STUDENT_TEST_ASCENDING, STUDENT_TEST_DESCENDING
	}
	
	/**
	 * Choose the sorting to use when Test bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	abstract public void setSortingType(SortingType sortingType) throws DAOException;
	
	/**
	 * Get a map of key->value pairs representing the parameters for a test's test method.
	 * @param testId is the test to get parameters for.
	 * @return a map of key->value pairs.
	 * @throws DAOException
	 */
	abstract public Map<String, String> getTestParameters(Long testId) throws DAOException;
	
	/**
	 * set the parameters for a test's test method.
	 * @param testId is the test to set parameters for.
	 * @param parameters is a map of key->value pairs representing the parameters for the test's test method
	 * @throws DAOException
	 */
	abstract public void setTestParameters(Long testId, Map<String, String> parameters) throws DAOException;
}
