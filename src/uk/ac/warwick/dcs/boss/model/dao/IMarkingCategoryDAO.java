package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingCategory;

/**
 * This DAO contains operations pertaining to the MarkingCategory DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IMarkingCategoryDAO extends IEntityDAO<MarkingCategory> {

	/**
	 * Sorting for MarkingCategory bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		UNIQUE_IDENTIFIER_ASCENDING, UNIQUE_IDENTIFIER_DESCENDING,
		WEIGHTING_ASCENDING, WEIGHTING_DESCENDING
	}
	
	/**
	 * Choose the sorting to use when marking category bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	public abstract void setSortingType(SortingType sortingType)  throws DAOException;	
}
