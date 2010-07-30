package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;
import uk.ac.warwick.dcs.boss.plugins.InvalidPluginException;

public interface IPluginMetadataDAO extends IEntityDAO<PluginMetadata> {
//	public void initCustomTables(String statements) throws DAOException;
//	public void destroyCustomTables(String pluginId) throws DAOException, IOException;
	public void createPluginCustomTables(String pluginId) throws DAOException, InvalidPluginException;
	public void destroyPluginCustomTables(String pluginId) throws DAOException, InvalidPluginException;
}
