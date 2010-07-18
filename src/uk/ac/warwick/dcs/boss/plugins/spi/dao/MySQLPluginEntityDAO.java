package uk.ac.warwick.dcs.boss.plugins.spi.dao;


import java.lang.reflect.ParameterizedType;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.impl.MySQLEntityDAO;

/**
 * This DAO uses subclassing for specific operations
 * @author davidbyard
 *
 */
public abstract class MySQLPluginEntityDAO<E extends PluginEntity> extends MySQLEntityDAO<E> {
	protected Class<E> entityType;
	public Class<E> getEntityType() {
		return entityType;
	}
	
	@SuppressWarnings("unchecked")
	public MySQLPluginEntityDAO() {
		this.entityType = ((Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}
}
