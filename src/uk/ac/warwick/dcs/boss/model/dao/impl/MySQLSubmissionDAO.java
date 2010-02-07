package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;

public class MySQLSubmissionDAO extends MySQLEntityDAO<Submission> implements
		ISubmissionDAO {

	private String mySQLSortingString = "id DESC";

	public MySQLSubmissionDAO(Connection connection) throws DAOException {
		super(connection);
	}
	
	public String getTableName() {
		return "submission";
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
		case UNIQUE_IDENTIFIER_ASCENDING:
			this.mySQLSortingString = "uniq ASC";
			break;
		case UNIQUE_IDENTIFIER_DESCENDING:
			this.mySQLSortingString = "uniq DESC";
			break;
		default:
			throw new DAOException("Unsupported sorting type:" + sortingType);
		}
	}

	@Override
	public Submission createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues)
			throws SQLException, DAOException {
		Submission submission = new Submission();
		submission.setSubmissionTime(new Date(databaseValues.getTimestamp(tableName + ".submission_time").getTime()));
		submission.setResourceId(databaseValues.getLong(tableName + ".resource_id"));
		submission.setResourceSubdirectory(databaseValues.getString(tableName + ".resource_subdirectory"));
		submission.setPersonId(databaseValues.getLong(tableName + ".person_id"));
		submission.setAssignmentId(databaseValues.getLong(tableName + ".assignment_id"));
		submission.setSecurityCode(databaseValues.getString(tableName + ".security_code"));
		submission.setActive(databaseValues.getBoolean(tableName + ".active"));
		return submission;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("submission_time");
		fieldNames.add("resource_id");
		fieldNames.add("resource_subdirectory");
		fieldNames.add("person_id");
		fieldNames.add("assignment_id");
		fieldNames.add("security_code");
		fieldNames.add("active");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(Submission entity) {
		Vector<Object> output = new Vector<Object>();
		
		if (entity.getSubmissionTime() != null) {
			output.add(new Timestamp(entity.getSubmissionTime().getTime()));
		} else {
			output.add(null);
		}
		
		output.add(entity.getResourceId());
		output.add(entity.getResourceSubdirectory());
		output.add(entity.getPersonId());
		output.add(entity.getAssignmentId());
		output.add(entity.getSecurityCode());
		output.add(entity.getActive());
		return output;
	}

	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}
}
