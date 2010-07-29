package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.frontend.PageDispatcherServlet;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IPluginMetadataDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.PluginMetadata;
import uk.ac.warwick.dcs.boss.plugins.PluginManager;

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

	public void initCustomTables(String statements) throws DAOException {
		CCJSqlParserManager pm = new CCJSqlParserManager();
		Scanner s = new Scanner(statements);
		s.useDelimiter("\\s*;\\s*");
		List<String> createdTbls = new LinkedList<String>();
		boolean success = true;
		java.sql.Statement statement = null;
		try {
			statement = getConnection().createStatement(); 
		} catch (SQLException e) {
			throw new DAOException("SQL Error", e);
		}
		
		while (s.hasNext()) {
			String stmStr = s.next();
			String temp = stmStr.replaceAll("`", "");
			Statement stm;
			try {
				stm = pm.parse(new StringReader(temp));
			} catch (JSQLParserException e) {
				success = false;
				break;
			}
			if (stm instanceof CreateTable) {
				Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + stmStr); 
				try {
					// execute create table statement
					statement.executeUpdate(stmStr);
				} catch (SQLException e) {
					success = false;
					break;
				}
				createdTbls.add(((CreateTable) stm).getTable().getName());
			}
			else {
				success = false;
				break;
			}
		}
		try {
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL Error", e);
		}
		
		if (!success) {
			try {
				statement = getConnection().createStatement();
				for (String table : createdTbls) {
					String sql = "DROP TABLE IF EXISTS " + table;
					Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + sql);
					statement.executeUpdate(sql);
				}
				statement.close();
			} catch (SQLException e) {
				throw new DAOException("SQL Error", e);
			}
			
			// throw exception to signal operation failed
			throw new DAOException("Unexpecting sql script");
		}
	}

	public void destroyCustomTables(String pluginId) throws DAOException, IOException {
		File pluginFile = new File(PageDispatcherServlet.realPath, "WEB-INF" + File.separator + "plugins" + File.separator + pluginId + ".jar");
		JarFile jarFile = new JarFile(pluginFile);
		Attributes atts = jarFile.getManifest().getMainAttributes();

		// only proceed if this plugin introduced new tables
		if (atts.containsKey(PluginManager.PLUGIN_DATABASE) && atts.getValue(PluginManager.PLUGIN_DATABASE).equalsIgnoreCase("true")
				&& atts.containsKey(PluginManager.PLUGIN_DATABASE_CREATE_SCRIPT)) {
			String createSQLFilename = atts.getValue(PluginManager.PLUGIN_DATABASE_CREATE_SCRIPT);
			if (createSQLFilename != null) {
				ZipEntry entry = jarFile.getEntry(createSQLFilename);
				if (entry != null) {
					InputStream createSQLIS = jarFile.getInputStream(entry);
					CCJSqlParserManager pm = new CCJSqlParserManager();
					Scanner s = new Scanner(createSQLIS);
					s.useDelimiter("\\s*;\\s*");
					List<String> tables = new LinkedList<String>();
					while (s.hasNext()) {
						String stmStr = s.next();
						String temp = stmStr.replaceAll("`", "");
						Statement stm;
						try {
							stm = pm.parse(new StringReader(temp));
						} catch (JSQLParserException e) {
							throw new DAOException("Unrecognised SQL statement: " + stmStr, e);
						}
						if (stm instanceof CreateTable) {
							tables.add(((CreateTable) stm).getTable().getName());
						}
						else {
							throw new DAOException("Not CREATE TABLE statement: " + stmStr);
						}
					}


					try {
						java.sql.Statement statement = getConnection().createStatement();
						for (String table : tables) {
							// execute drop table statement
							String sql = "DROP TABLE IF EXISTS " + table;
							Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + sql);
							statement.executeUpdate(sql);
						}
						statement.close();
					} catch (SQLException e) {
						throw new DAOException("SQL error", e);

					}

				}
			}
		}
	}
}
