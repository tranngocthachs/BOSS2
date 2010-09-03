package boss.plugins.spi.dao;

import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import boss.plugins.dbschema.SQLTableSchema;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;

public abstract class IPluginDBMapping<E extends IPluginEntity> {
	
	protected Class<E> entityType;
	public Class<E> getEntityType() {
		return entityType;
	}
	
	@SuppressWarnings("unchecked")
	public IPluginDBMapping() {
		this.entityType = ((Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}
	
	/**
	 * Get the name of the table that this DAO deals with
	 */
	public abstract String getTableName();
	
	/**
	 * Get the database field names this DAO deals with
	 * @return collection of field names this DAO deals with
	 */
	public abstract Collection<String> getDatabaseFieldNames();
	
	/**
	 * Get the values (mapping to the field names of getDatabaseFieldNames)
	 * from the entity this DAO is dealing with
	 * @return collection of JDBC-serializable objects
	 */
	public abstract Collection<Object> getDatabaseValues(E entity);
	
	/**
	 * Create an instance of the entity this DAO deals with from a query
	 * that has been executed on the database
	 * @param databaseValues is the resultset to create from
	 * @return a ModelEntity instance
	 */
	public abstract E createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues) throws SQLException, DAOException;

	/**
	 * Get the sorting string
	 * @return sorting string (e.g., x DESC, y ASC)
	 */
	public abstract String getMySQLSortingString();
	
	public abstract SQLTableSchema getTableSchema();
}
