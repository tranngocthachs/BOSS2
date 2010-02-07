package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.ITestDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;

public class MySQLTestDAO extends MySQLEntityDAO<Test> implements ITestDAO {

	public static final String TABLE_NAME = "test";
	public static final String ASSIGNMENT_ID_FIELD = "assignment_id";

	private String mySQLSortingString = "id DESC";

	public MySQLTestDAO(Connection connection) throws DAOException {
		super(connection);
	}

	public String getTableName() {
		return TABLE_NAME;
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
		case TEST_CLASS_NAME_ASCENDING:
			this.mySQLSortingString = "test_classname ASC";
			break;
		case TEST_CLASS_NAME_DESCENDING:
			this.mySQLSortingString = "test_classname DESC";
			break;
		case UNIQUE_IDENTIFIER_ASCENDING:
			this.mySQLSortingString = "uniq ASC";
			break;
		case UNIQUE_IDENTIFIER_DESCENDING:
			this.mySQLSortingString = "uniq DESC";
			break;
		case STUDENT_TEST_ASCENDING:
			this.mySQLSortingString = "student_test ASC";
			break;
		case STUDENT_TEST_DESCENDING:
			this.mySQLSortingString = "student_test DESC";
			break;
		default:
			throw new DAOException("Unsupported sorting type:" + sortingType);
		}
	}

	@Override
	public Test createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues)
	throws SQLException, DAOException {
		Test test = new Test();
		test.setName(databaseValues.getString(tableName + ".name"));
		test.setStudentTest(databaseValues.getBoolean(tableName + ".student_test"));
		test.setCommand(databaseValues.getString(tableName + ".command"));
		test.setTestClassName(databaseValues.getString(tableName + ".classname"));
		test.setExecutorClassName(databaseValues.getString(tableName + ".executor_classname"));
		test.setMaximumExecutionTime(databaseValues.getInt(tableName + ".max_time"));
		test.setAssignmentId(databaseValues.getLong(tableName + ".assignment_id"));
		test.setLibraryResourceId(databaseValues.getLong(tableName + ".resource_id"));
		return test;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("name");
		fieldNames.add("student_test");
		fieldNames.add("command");
		fieldNames.add("classname");
		fieldNames.add("executor_classname");
		fieldNames.add("max_time");
		fieldNames.add("assignment_id");
		fieldNames.add("resource_id");

		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(Test entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getName());
		output.add(entity.getStudentTest());
		output.add(entity.getCommand());
		output.add(entity.getTestClassName());
		output.add(entity.getExecutorClassName());
		output.add(entity.getMaximumExecutionTime());
		output.add(entity.getAssignmentId());
		output.add(entity.getLibraryResourceId());
		return output;
	}

	@Override
	public void deletePersistentEntity(Long databaseIdentifier)
	throws DAOException {
		this.setTestParameters(databaseIdentifier, null);

		super.deletePersistentEntity(databaseIdentifier);
	}

	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}

	public Map<String, String> getTestParameters(Long testId)
	throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"SELECT key_name, value FROM test_parameters" +
			" WHERE test_id=?");
			statement.setLong(1, testId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement);
			ResultSet rs = statement.executeQuery();

			HashMap<String, String> parameters = new HashMap<String, String>();
			while (rs.next()) {
				parameters.put(rs.getString("key_name"), rs.getString("value"));
			}

			rs.close();
			statement.close();
			return parameters;
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}
	}

	public void setExecutorParameters(Long testId, Map<String, String> parameters)
	throws DAOException {
		try {
			PreparedStatement clear = getConnection().prepareStatement(
			"DELETE FROM test_parameters WHERE test_id=?");
			clear.setLong(1, testId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + clear);
			clear.executeUpdate();

			clear.close();
			
			if (parameters == null) {
				return;
			}

			PreparedStatement statement = getConnection().prepareStatement(
					"INSERT INTO executor_parameters (test_id, key_name, value)" +
			" VALUES (?, ?, ?)");
			statement.setLong(1, testId);

			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				statement.setString(2, entry.getKey());
				statement.setString(3, entry.getValue());

				Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement);
				statement.executeUpdate();
			}
			
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}			

	}

	public Map<String, String> getExecutorParameters(Long testId)
	throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"SELECT key_name, value FROM executor_parameters" +
			" WHERE test_id=?");
			statement.setLong(1, testId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement);
			ResultSet rs = statement.executeQuery();

			HashMap<String, String> parameters = new HashMap<String, String>();
			while (rs.next()) {
				parameters.put(rs.getString("key_name"), rs.getString("value"));
			}

			rs.close();
			statement.close();
			return parameters;
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}
	}

	public void setTestParameters(Long testId, Map<String, String> parameters)
	throws DAOException {
		try {
			PreparedStatement clear = getConnection().prepareStatement(
			"DELETE FROM test_parameters WHERE test_id=?");
			clear.setLong(1, testId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + clear);
			clear.executeUpdate();

			clear.close();
			
			if (parameters == null) {
				return;
			}

			PreparedStatement statement = getConnection().prepareStatement(
					"INSERT INTO test_parameters (test_id, key_name, value)" +
			" VALUES (?, ?, ?)");
			statement.setLong(1, testId);

			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				statement.setString(2, entry.getKey());
				statement.setString(3, entry.getValue());

				Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement);
				statement.executeUpdate();
			}
			
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}			

	}

}
