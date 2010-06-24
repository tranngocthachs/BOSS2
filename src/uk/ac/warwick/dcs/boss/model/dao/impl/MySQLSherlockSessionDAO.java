package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.ISherlockSessionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.SherlockSession;

public class MySQLSherlockSessionDAO extends MySQLEntityDAO<SherlockSession> implements ISherlockSessionDAO {

	private String mySQLSortingString = "id DESC";
	
	public MySQLSherlockSessionDAO(Connection connection) throws DAOException {
		super(connection);
	}

	
	@Override
	public SherlockSession createInstanceFromDatabaseValues(String tableName,
			ResultSet databaseValues) throws SQLException, DAOException {
		SherlockSession result = new SherlockSession();
		result.setAssignmentId(databaseValues.getLong(tableName + ".assignment_id"));
		result.setResourceId(databaseValues.getLong(tableName + ".resource_id"));
		
		return result;
	}

	
	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("assignment_id");
		fieldNames.add("resource_id");
		return fieldNames;
	}

	
	@Override
	public Collection<Object> getDatabaseValues(SherlockSession entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getAssignmentId());
		output.add(entity.getResourceId());
		return output;
	}

	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}

	@Override
	public String getTableName() {
		return "sherlocksession";
	}


	@Override
	public void addRequiredFilenames(Long sherlockSessionId, Collection<String> fileNames)
		throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"DELETE FROM sherlocksession_requiredfilenames "
					+ "WHERE sherlocksession_id=?");
			statement.setLong(1, sherlockSessionId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();

			statement = getConnection().prepareStatement(
					"INSERT INTO sherlocksession_requiredfilenames (sherlocksession_id, filename)"
					+ "VALUES (?, ?)");
			for (String fileName : fileNames) {
				statement.setLong(1, sherlockSessionId);
				statement.setString(2, fileName);
				Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
				statement.executeUpdate();
			}
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
		
	}


	@Override
	public Collection<String> fetchRequiredFilenames(Long sherlockSessionId)
			throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"SELECT filename FROM sherlocksession_requiredfilenames"
					+ " WHERE sherlocksession_id=?");
			statement.setLong(1, sherlockSessionId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());

			ResultSet rs = statement.executeQuery();
			Vector<String> result = new Vector<String>();
			while (rs.next()) {
				result.add(rs.getString("filename"));
			}

			rs.close();
			statement.close();
			return result;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}
	

}
