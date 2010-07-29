package uk.ac.warwick.dcs.boss.model.dao;

import java.io.IOException;

import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;

public interface IPluginMetadataDAO extends IEntityDAO<PluginMetadata> {
	public void initCustomTables(String statements) throws DAOException;
	public void destroyCustomTables(String pluginId) throws DAOException, IOException;
}
