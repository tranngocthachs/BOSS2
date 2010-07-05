package uk.ac.warwick.dcs.boss.model.dao;

import java.io.File;

import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;

public interface IPluginMetadataDAO extends IEntityDAO<PluginMetadata> {
	public File getMainJarFile(Long id) throws DAOException;
	public File[] getLibJarFiles(Long id) throws DAOException;
	public void setLibJarFileNames(Long id, String[] fileNames) throws DAOException;
}
