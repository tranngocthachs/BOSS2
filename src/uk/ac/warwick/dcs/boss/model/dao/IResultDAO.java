package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.dao.beans.Result;

/**
 * This DAO contains operations pertaining to the Result DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IResultDAO extends IEntityDAO<Result> {

	/**
	 * Sorting for Model bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		RESULT_ASCENDING, RESULT_DESCENDING,
		INCOMPLETE_MARKING_ASCENDING, INCOMPLETE_MARKING_DESCENDING,
		TIMESTAMP_ASCENDING, TIMESTAMP_DESCENDING
	}

	/**
	 * Choose the sorting to use when Result bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	public abstract void setSortingType(SortingType sortingType)  throws DAOException;
}