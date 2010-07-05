package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.frontend.PageDispatcherServlet;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IPluginMetadataDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;

public class MySQLPluginMetadata extends MySQLEntityDAO<PluginMetadata>
		implements IPluginMetadataDAO {

	public MySQLPluginMetadata(Connection connection) throws DAOException {
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
		fieldNames.add("author");
		fieldNames.add("email");
		fieldNames.add("version");
		fieldNames.add("description");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(PluginMetadata entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getPluginId());
		output.add(entity.getAuthor());
		output.add(entity.getEmail());
		output.add(entity.getVersion());
		output.add(entity.getDescription());
		return output;
	}

	@Override
	public PluginMetadata createInstanceFromDatabaseValues(String tableName,
			ResultSet databaseValues) throws SQLException, DAOException {
		PluginMetadata pluginMetadata = new PluginMetadata();
		pluginMetadata.setPluginId(databaseValues.getString(tableName
				+ ".plugin_id"));
		pluginMetadata.setAuthor(databaseValues
				.getString(tableName + ".author"));
		pluginMetadata.setEmail(databaseValues.getString(tableName + ".email"));
		pluginMetadata
				.setVersion(databaseValues.getInt(tableName + ".version"));
		pluginMetadata.setDescription(databaseValues.getString(tableName)
				+ ".description");
		return pluginMetadata;

	}

	@Override
	public String getMySQLSortingString() {
		return "id DESC";
	}

	@Override
	public File getMainJarFile(Long id) throws DAOException {
		PluginMetadata pluginMetadata = retrievePersistentEntity(id);
		return new File(PageDispatcherServlet.realPath, "WEB-INF"
				+ File.separator + "lib" + File.separator
				+ pluginMetadata.getPluginId() + ".jar");
	}

	@Override
	public File[] getLibJarFiles(Long id) throws DAOException {

		try {
			PluginMetadata pluginMetadata = retrievePersistentEntity(id);
			LinkedList<File> libJarFiles = null;
			String statementString = "SELECT filename FROM plugin_libfilenames WHERE plugin_id=?";
			PreparedStatement statementObject = getConnection()
					.prepareStatement(statementString);
			statementObject.setObject(1, id);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE,
					"Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();
			libJarFiles = new LinkedList<File>();
			if (rs.first()) {
				while (rs.next()) {
					String fileName = rs.getString(1);
					libJarFiles.add(new File(PageDispatcherServlet.realPath,
							"WEB-INF" + File.separator + "lib" + File.separator
									+ pluginMetadata.getPluginId() + "_"
									+ fileName));
				}
			}
			rs.close();
			statementObject.close();
			return libJarFiles.toArray(new File[0]);
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}

	}

	@Override
	public void setLibJarFileNames(Long id, String[] fileNames)
			throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"DELETE FROM plugin_libfilenames "
					+ "WHERE plugin_id=?");
			statement.setLong(1, id);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();

			statement = getConnection().prepareStatement(
					"INSERT INTO plugin_libfilenames (plugin_id, filename)"
					+ "VALUES (?, ?)");
			for (String fileName : fileNames) {
				statement.setLong(1, id);
				statement.setString(2, fileName);
				Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
				statement.executeUpdate();
			}
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
		
	}


}
