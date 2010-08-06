package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

import uk.ac.warwick.dcs.boss.frontend.PageDispatcherServlet;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IPluginMetadataDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;
import uk.ac.warwick.dcs.boss.plugins.dbschema.SQLTableSchema;
import uk.ac.warwick.dcs.boss.plugins.spi.dao.PluginEntityDAO;

public class MySQLPluginMetadataDAO extends MySQLEntityDAO<PluginMetadata>
		implements IPluginMetadataDAO {

	public MySQLPluginMetadataDAO(Connection connection) throws DAOException {
		super(connection);
	}

	@Override
	public String getTableName() {
		return "pluginmetadata";
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		LinkedList<String> fieldNames = new LinkedList<String>();
		fieldNames.add("plugin_id");
		fieldNames.add("name");
		fieldNames.add("author");
		fieldNames.add("email");
		fieldNames.add("version");
		fieldNames.add("description");
		fieldNames.add("lib_filenames");
		fieldNames.add("enable");
		fieldNames.add("configurable");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(PluginMetadata entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getPluginId());
		output.add(entity.getName());
		output.add(entity.getAuthor());
		output.add(entity.getEmail());
		output.add(entity.getVersion());
		output.add(entity.getDescription());
		String[] libFilenames = entity.getLibFilenames();
		Object libFileNameObj = null;
		if (libFilenames != null) {
			StringBuffer libFilenameStr = new StringBuffer();
			for (int i = 0; i < libFilenames.length; i++) {
				libFilenameStr.append(libFilenames[i].trim());
				if (i < libFilenames.length - 1)
					libFilenameStr.append(":");
			}
			libFileNameObj = libFilenameStr.toString();
		}
		output.add(libFileNameObj);
		output.add(entity.getEnable());
		output.add(entity.getConfigurable());
		return output;
	}

	@Override
	public PluginMetadata createInstanceFromDatabaseValues(String tableName,
			ResultSet databaseValues) throws SQLException, DAOException {
		PluginMetadata pluginMetadata = new PluginMetadata();
		pluginMetadata.setPluginId(databaseValues.getString(tableName
				+ ".plugin_id"));
		pluginMetadata.setName(databaseValues.getString(tableName + ".name"));
		pluginMetadata.setAuthor(databaseValues
				.getString(tableName + ".author"));
		pluginMetadata.setEmail(databaseValues.getString(tableName + ".email"));
		pluginMetadata.setVersion(databaseValues.getString(tableName
				+ ".version"));
		pluginMetadata.setDescription(databaseValues.getString(tableName
				+ ".description"));
		String[] libFilenames = null;
		String libFilenameStr = databaseValues.getString(tableName
				+ ".lib_filenames");
		if (libFilenameStr != null) {
			libFilenames = libFilenameStr.split("\\s*:\\s*");
		}
		pluginMetadata.setLibFilenames(libFilenames);
		pluginMetadata.setEnable(databaseValues.getBoolean(tableName
				+ ".enable"));
		pluginMetadata.setConfigurable(databaseValues.getBoolean(tableName
				+ ".configurable"));
		return pluginMetadata;
	}

	@Override
	public String getMySQLSortingString() {
		return "id DESC";
	}

	public void createPluginCustomTables(String pluginId) throws DAOException {
		File pluginFile = new File(PageDispatcherServlet.realPath, "WEB-INF"
				+ File.separator + "plugins" + File.separator + pluginId
				+ ".jar");
		URL url = null;
		try {
			url = pluginFile.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new DAOException(e);
		}
		URL[] urls = {url};
		URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
		Lookup lookup = Lookups.metaInfServices(classLoader);
		Collection<? extends PluginEntityDAO> pluginDbs = lookup.lookupAll(PluginEntityDAO.class);
		for (PluginEntityDAO pluginDb : pluginDbs) {
			// make sure it's not the one that already installed. If this is something which was 
			// installed, the ClassLoader of it will not be the same as the one we have above
			if (pluginDb.getClass().getClassLoader() == classLoader) {
				SQLTableSchema tableSchema = pluginDb.getTableSchema();
				String createStr = tableSchema.getSQLCreateString();
				Logger.getLogger("mysql").log(Level.TRACE, "Executing " + createStr);
				try {
					Statement stm = getConnection().createStatement();
					stm.executeUpdate(createStr);
				} catch (SQLException e) {
					throw new DAOException("SQL error", e);
				}
			}
		}
	}

	public void destroyPluginCustomTables(String pluginId) throws DAOException {
		File pluginFile = new File(PageDispatcherServlet.realPath, "WEB-INF"
				+ File.separator + "plugins" + File.separator + pluginId
				+ ".jar");
		URL url = null;
		try {
			url = pluginFile.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new DAOException(e);
		}
		URL[] urls = {url};
		URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
		Lookup lookup = Lookups.metaInfServices(classLoader);
		Collection<? extends PluginEntityDAO> pluginDbs = lookup.lookupAll(PluginEntityDAO.class);
		for (PluginEntityDAO pluginDb : pluginDbs) {
			// make sure we get the right one
			// the line below will get the full path of the jar file of which pluginDb is loaded from
			// the correct one would be having the suffix <pluginId>.jar
			// (either the one currently at WEB-INF/lib/plugin_<plugin>.jar or the one in WEB-INF/plugins/<plugin>.jar)
			String pluginFilePath = pluginDb.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
			if (pluginFilePath.endsWith(pluginId + ".jar")) {
				String dropStr = "DROP TABLE IF EXISTS " + pluginDb.getTableName();
				Logger.getLogger("mysql").log(Level.TRACE, "Executing " + dropStr);
				try {
					Statement stm = getConnection().createStatement();
					stm.executeUpdate(dropStr);
				} catch (SQLException e) {
					throw new DAOException("SQL error", e);
				}
			}
		}
	}
}
