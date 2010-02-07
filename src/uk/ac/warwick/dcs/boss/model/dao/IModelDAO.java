package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.dao.beans.Model;

/**
 * This DAO contains operations pertaining to the Model DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IModelDAO extends IEntityDAO<Model> {

	/**
	 * Sorting for Model bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		UNIQUE_IDENTIFIER_ASCENDING, UNIQUE_IDENTIFIER_DESCENDING
	}

	/**
	 * Choose the sorting to use when Model bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	public abstract void setSortingType(SortingType sortingType)  throws DAOException;
	
	/**
	 * Return whether the given model has any child modules.
	 * @param modelId is the model.
	 * @return true if the given model has any child modules, false if not.
	 * @throws DAOException
	 */
	public abstract boolean hasModules(Long modelId) throws DAOException;
}