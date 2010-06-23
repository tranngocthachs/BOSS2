package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IMarkDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISherlockSessionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
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
		// TODO Auto-generated method stub
		return null;
	}
	

}
