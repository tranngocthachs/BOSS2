package uk.ac.warwick.dcs.boss.model.dao;

import java.io.InputStream;

import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;

public interface IPluginMetadataDAO extends IEntityDAO<PluginMetadata> {
	public void executeSQLScript(InputStream sqlScriptInStream) throws DAOException;
}
