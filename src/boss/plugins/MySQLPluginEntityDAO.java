package boss.plugins;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import boss.plugins.spi.dao.IPluginDBMapping;
import boss.plugins.spi.dao.IPluginEntity;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.impl.MySQLEntityDAO;

public class MySQLPluginEntityDAO<E extends IPluginEntity> extends MySQLEntityDAO<E> {
	public MySQLPluginEntityDAO(Connection connection, IPluginDBMapping<E> daoProvider) throws DAOException {
		super(connection);
		this.daoProvider = daoProvider;
	}

	private IPluginDBMapping<E> daoProvider;
	
	/**
	 * @return the daoProvider
	 */
	public IPluginDBMapping<E> getDaoProvider() {
		return daoProvider;
	}

	/**
	 * @param daoProvider the daoProvider to set
	 */
	public void setDaoProvider(IPluginDBMapping<E> daoProvider) {
		this.daoProvider = daoProvider;
	}

	@Override
	public String getTableName() {
		return daoProvider.getTableName();
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		return daoProvider.getDatabaseFieldNames();
	}

	@Override
	public Collection<Object> getDatabaseValues(E entity) {
		return daoProvider.getDatabaseValues(entity);
	}

	@Override
	public E createInstanceFromDatabaseValues(String tableName,
			ResultSet databaseValues) throws SQLException, DAOException {
		return daoProvider.createInstanceFromDatabaseValues(tableName, databaseValues);
	}

	@Override
	public String getMySQLSortingString() {
		return daoProvider.getMySQLSortingString();
	}
}
