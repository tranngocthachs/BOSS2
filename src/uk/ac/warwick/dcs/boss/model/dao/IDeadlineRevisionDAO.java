package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;

/**
 * This DAO contains operations pertaining to the DeadlineRevision DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IDeadlineRevisionDAO extends IEntityDAO<DeadlineRevision> {

	/**
	 * Sorting for deadline revision bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		DEADLINE_ASCENDING, DEADLINE_DESCENDING
	}
	
	/**
	 * Choose the sorting to use when deadline revision bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	abstract public void setSortingType(SortingType sortingType) throws DAOException;
}
