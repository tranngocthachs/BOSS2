package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;

public class MySQLMarkingAssignmentDAO extends MySQLEntityDAO<MarkingAssignment> implements IMarkingAssignmentDAO {

	private String mySQLSortingString = "id DESC";
	
	public MySQLMarkingAssignmentDAO(Connection connection) throws DAOException {
		super(connection);
	}
	
	@Override
	public String getTableName() {
		return "markingassignment";
	}

	public void setSortingType(SortingType sortingType) throws DAOException {
		switch (sortingType) {
		case ID_ASC:
			this.mySQLSortingString = "id ASC";
			break;
		case NONE:
		case ID_DESC:
			this.mySQLSortingString = "id DESC";
			break;
		default:
			throw new DAOException("Unsupported sorting type:" + sortingType);
		}
	}

	@Override
	public MarkingAssignment createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues)
			throws SQLException, DAOException {
		MarkingAssignment result = new MarkingAssignment();
		result.setAssignmentId(databaseValues.getLong(tableName + ".assignment_id"));
		result.setStudentId(databaseValues.getLong(tableName + ".student_id"));
		result.setMarkerId(databaseValues.getLong(tableName + ".marker_id"));
		result.setBlind(databaseValues.getBoolean(tableName + ".blind"));
		result.setModerator(databaseValues.getBoolean(tableName + ".moderator"));
		
		return result;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("assignment_id");
		fieldNames.add("student_id");
		fieldNames.add("marker_id");
		fieldNames.add("blind");
		fieldNames.add("moderator");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(MarkingAssignment entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getAssignmentId());
		output.add(entity.getStudentId());
		output.add(entity.getMarkerId());
		output.add(entity.getBlind());
		output.add(entity.getModerator());
		return output;
	}

	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}

}
