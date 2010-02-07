package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;

/**
 * This DAO contains operations pertaining to the Mark DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IMarkDAO extends IEntityDAO<Mark> {

	/**
	 * Sorting for mark bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		TIMESTAMP_ASCENDING, TIMESTAMP_DESCENDING,
		VALUE_ASCENDING, VALUE_DESCENDING
	}
	
	/**
	 * Choose the sorting to use when mark bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	abstract public void setSortingType(SortingType sortingType) throws DAOException;
}
