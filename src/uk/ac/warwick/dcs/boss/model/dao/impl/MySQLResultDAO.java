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
import uk.ac.warwick.dcs.boss.model.dao.IResultDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Result;

public class MySQLResultDAO extends MySQLEntityDAO<Result> implements IResultDAO {

	private String mySQLSortingString = "id DESC";

	public MySQLResultDAO(Connection connection) throws DAOException {
		super(connection);
	}
	
	public String getTableName() {
		return "result";
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
		case INCOMPLETE_MARKING_ASCENDING:
			this.mySQLSortingString = "incomplete_marking ASC";
			break;
		case INCOMPLETE_MARKING_DESCENDING:
			this.mySQLSortingString = "incomplete_marking DESC";
			break;
		case RESULT_ASCENDING:
			this.mySQLSortingString = "result ASC";
			break;
		case RESULT_DESCENDING:
			this.mySQLSortingString = "result DESC";
			break;
		case TIMESTAMP_ASCENDING:
			this.mySQLSortingString = "timestamp ASC";
			break;
		case TIMESTAMP_DESCENDING:
			this.mySQLSortingString = "timestamp DESC";
			break;
		default:
			throw new DAOException("Unsupported sorting type:" + sortingType);
		}
	}

	@Override
	public Result createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues)
	throws SQLException, DAOException {
		Result result = new Result();
		
		result.setAssignmentId(databaseValues.getLong(tableName + ".assignment_id"));
		result.setHadIncompleteMarking(databaseValues.getBoolean(tableName + ".incomplete_marking"));
		result.setResult(databaseValues.getDouble(tableName + ".result"));
		result.setStudentId(databaseValues.getLong(tableName + ".student_id"));
		result.setTimestamp(databaseValues.getTimestamp(tableName + ".timestamp"));
		
		return result;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("assignment_id");
		fieldNames.add("incomplete_marking");
		fieldNames.add("result");
		fieldNames.add("student_id");
		fieldNames.add("timestamp");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(Result entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getAssignmentId());
		output.add(entity.getHadIncompleteMarking());
		output.add(entity.getResult());
		output.add(entity.getStudentId());
		output.add(entity.getTimestamp());
		return output;
	}

	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}

	public boolean hasModules(Long modelId) throws DAOException {
		try {
			PreparedStatement check = getConnection().prepareStatement(
					"SELECT id FROM module"
					+ " WHERE model_id=?");
			check.setLong(1, modelId);
			
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + check);
			ResultSet rs = check.executeQuery();
			boolean result = rs.first();
			rs.close();
			check.close();
			return result;
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}

	}

}
