package uk.ac.warwick.dcs.boss.model.dao.impl;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.plugins.spi.dao.PluginEntity;
import uk.ac.warwick.dcs.boss.plugins.spi.dao.PluginEntityDAO;

public class MySQLPluginEntityDAO<E extends PluginEntity> extends MySQLEntityDAO<E> {
	public MySQLPluginEntityDAO(Connection connection, PluginEntityDAO<E> daoProvider) throws DAOException {
		super(connection);
		this.daoProvider = daoProvider;
	}

	private PluginEntityDAO<E> daoProvider;
	
	/**
	 * @return the daoProvider
	 */
	public PluginEntityDAO<E> getDaoProvider() {
		return daoProvider;
	}

	/**
	 * @param daoProvider the daoProvider to set
	 */
	public void setDaoProvider(PluginEntityDAO<E> daoProvider) {
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
