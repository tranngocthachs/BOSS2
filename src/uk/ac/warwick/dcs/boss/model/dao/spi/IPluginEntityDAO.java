package uk.ac.warwick.dcs.boss.model.dao.spi;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.beans.spi.PluginEntity;

/**
 * Generic DAO for retrieving and storing beans in the database.  It's used as a basis for many
 * of the DAOs, but not all.
 * 
 * @author davidbyard
 *
 * @param <E> is the Entity bean to be a DAO for.
 */
public interface IPluginEntityDAO<E extends PluginEntity> {
	/**
	 * Create a persistent copy of the entity.
	 * @param entity is the entity to make persistent.
	 * @return a persistent copy of the entity.
	 * @throws DAOException
	 */
	public Long createPersistentCopy(E entity) throws DAOException;

	/**
	 * Delete a persistent copy of the entity.
	 * @param entity is the entity to delete.
	 * @throws DAOException
	 */
	public void deletePersistentEntity(Long id) throws DAOException;

	/**
	 * Retrieve a persistent entity from the database.
	 * This retrieves via id.
	 * @param identifier is the identifier to fetch by.
	 * @return the entity with the given ID.
	 * @throws DAOException
	 */
	public E retrievePersistentEntity(Long id) throws DAOException;
	
	/**
	 * Retrieve persistent entities from the database.
	 * This retrieves via id.
	 * @param identifiers are the identifiers to fetch by.
	 * @return the identifiers with the given IDs.
	 * @throws DAOException 
	 */
	public Collection<E> retrievePersistentEntities(Collection<Long> ids) throws DAOException;
	
	
	/**
	 * Retrieve all persistent entities of this type from the database.
	 * @return a collection of entities.
	 * @throws DAOException
	 */
	public Collection<E> retrieveAllPersistentEntities() throws DAOException;
	
	/**
	 * Retrieve a persistent entity from the database.
	 * This retrieves via example - every field that isn't null
	 * is used to find the first match.
	 * @param entity is the example entity to fetch by example.
	 * @return collection of persistent entities that match the example.
	 * @throws DAOException
	 */
	public Collection<E> findPersistentEntitiesByExample(E entity) throws DAOException;

	/**
	 * Retrieve a persistent entity from the database.
	 * This retrieves via example - every field that isn't null
	 * is used to find the first match using SQL-style LIKE wildcards.
	 * @param entity is the example entity to fetch by example.
	 * @return collection of persistent entities that match the example.
	 * @throws DAOException
	 */
	public Collection<E> findPersistentEntitiesByWildcards(E entity) throws DAOException;

	
	/**
	 * Update a persistent entity in the database.
	 * @param entity is the persistent entity to commit changes for.
	 * @throws DAOException
	 */
	public void updatePersistentEntity(E entity) throws DAOException;
}
