package uk.ac.warwick.dcs.boss.plugins.spi.dao;


import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.impl.MySQLEntityDAO;

/**
 * This DAO uses subclassing for specific operations
 * @author davidbyard
 *
 */
public abstract class MySQLPluginEntityDAO<E extends PluginEntity> extends MySQLEntityDAO<E> {
	
	abstract public boolean tableCreated();
	abstract public void createTable() throws DAOException;
}
