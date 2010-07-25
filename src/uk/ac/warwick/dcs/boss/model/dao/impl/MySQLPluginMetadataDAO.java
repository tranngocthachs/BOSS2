package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IPluginMetadataDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;

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
				if ( i < libFilenames.length - 1)
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
		String libFilenameStr = databaseValues.getString(tableName + ".lib_filenames");
		if (libFilenameStr != null) {
			libFilenames = libFilenameStr.split("\\s*:\\s*");
		}
		pluginMetadata.setLibFilenames(libFilenames);
		pluginMetadata.setEnable(databaseValues.getBoolean(tableName + ".enable"));
		pluginMetadata.setConfigurable(databaseValues.getBoolean(tableName + ".configurable"));
		return pluginMetadata;
	}

	@Override
	public String getMySQLSortingString() {
		return "id DESC";
	}


	public void executeSQLScript(InputStream sqlScriptInStream) throws DAOException {
		Scanner s = new Scanner(sqlScriptInStream);
		s.useDelimiter("\\s*;\\s*");
		while (s.hasNext()) {
			String sql = s.next();
			try {		
				// Execute the statement.
				Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + sql);
				getConnection().createStatement().executeUpdate(sql);
			} catch (SQLException e) {
				throw new DAOException("SQL error", e);
			}
		}
	}

}
