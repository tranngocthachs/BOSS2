package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;

public interface IPluginMetadataDAO extends IEntityDAO<PluginMetadata> {
	public void createPluginCustomTables(String pluginId) throws DAOException;
	public void destroyPluginCustomTables(String pluginId) throws DAOException;
}
