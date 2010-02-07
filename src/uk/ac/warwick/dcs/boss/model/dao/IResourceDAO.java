package uk.ac.warwick.dcs.boss.model.dao;

import java.io.InputStream;
import java.io.OutputStream;

import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;

/**
 * This DAO contains operations pertaining to the Resource DAO beans.  Queries requiring joins or
 * needing particularly complex/hybrid operations are placed in here if interfaces require them.
 * @author davidbyard
 */
public interface IResourceDAO extends IEntityDAO<Resource> {

	/**
	 * Sorting for Resource bean queries.
	 * @author davidbyard
	 *
	 */
	public enum SortingType {
		NONE,
		ID_ASC, ID_DESC,
		FILENAME_ASCENDING, FILENAME_DESCENDING,
		TIMESTAMP_ASCENDING, TIMESTAMP_DESCENDING
	}
	
	/**
	 * Choose the sorting to use when Resource bean queries are made.
	 * @param sortingType is the sorting to use.
	 * @throws DAOException
	 */
	abstract public void setSortingType(SortingType sortingType) throws DAOException;

	/**
	 * Remove the data associated with the given resource and provide an output stream for
	 * writing new data.
	 * @param resourceId is the resource to write to.
	 * @return an output stream representing the location to write resource data to.
	 * @throws DAOException
	 */
	public OutputStream openOutputStream(Long resourceId) throws DAOException;
	
	/**
	 * Access the data associated with the given resource.
	 * @param resourceId is the resource to read from.
	 * @return an input stream representing the location to read resource data from.
	 * @throws DAOException
	 */
	public InputStream openInputStream(Long resourceId) throws DAOException;

}
