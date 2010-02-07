package uk.ac.warwick.dcs.boss.model.dao;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.beans.queries.AdminModelsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.AdminModulesQueryResult;

/**
 * This DAO contains operations pertaining to the Admin interface.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if the Admin interface requires
 * them.
 * @author davidbyard
 */
public interface IAdminInterfaceQueriesDAO {

	/**
	 * Sorting for the Models query.
	 * @author davidbyard
	 *
	 */
	public enum AdminModelsQuerySortingType {
		NONE,
		MODULE_COUNT_ASCENDING, MODULE_COUNT_DESCENDING,
	}

	/**
	 * Sorting for the Modules query.
	 * @author davidbyard
	 *
	 */
	public enum AdminModulesQuerySortingType {
		NONE,
		ASSIGNMENT_COUNT_ASCENDING, ASSIGNMENT_COUNT_DESCENDING,
		ADMINISTRATOR_COUNT_ASCENDING, ADMINISTRATOR_COUNT_DESCENDING,
		REGISTRATION_REQUIRED_ASC, REGISTRATION_REQUIRED_DESC,
		MODULE_ID_ASCENDING, MODULE_ID_DESCENDING
	}
	
	/**
	 * Obtain all known models and the number of modules they contain.
	 * @param sortingType is the sorting to use.
	 * @return all known models.
	 * @throws DAOException
	 */
	public Collection<AdminModelsQueryResult> performAdminModelsQuery(AdminModelsQuerySortingType sortingType) throws DAOException;
	
	/**
	 * Obtain all known modules for a model, as well as the number of
	 * assignments and administrators that they have.
	 * @param sortingType is the sorting to use.
	 * @param modelId is the parent model.
	 * @return known modules for the given model.
	 * @throws DAOException
	 */
	public Collection<AdminModulesQueryResult> performAdminModulesQuery(AdminModulesQuerySortingType sortingType, Long modelId) throws DAOException;
}
