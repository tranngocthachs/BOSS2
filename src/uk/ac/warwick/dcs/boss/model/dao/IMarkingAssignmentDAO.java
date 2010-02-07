package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;

/**
 * This DAO contains operations pertaining to the MarkAssignment DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IMarkingAssignmentDAO extends IEntityDAO<MarkingAssignment> {

	/**
	 * Sorting for MarkingAssignment bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
	}
	
	/**
	 * Choose the sorting to use when making assignment bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	abstract public void setSortingType(SortingType sortingType) throws DAOException;
}