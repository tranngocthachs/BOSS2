package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IDeadlineRevisionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;

public class MySQLDeadlineRevisionDAO extends MySQLEntityDAO<DeadlineRevision> implements IDeadlineRevisionDAO {

	private String mySQLSortingString = "id DESC";
	
	public MySQLDeadlineRevisionDAO(Connection connection) throws DAOException {
		super(connection);
	}
	
	public String getTableName() {
		return "deadlinerevision";
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
		case DEADLINE_ASCENDING:
			this.mySQLSortingString = "deadline ASC";
			break;
		case DEADLINE_DESCENDING:
			this.mySQLSortingString = "deadline DESC";
			break;
		default:
			throw new DAOException("Unsupported sorting type:" + sortingType);
		}
	}

	@Override
	public DeadlineRevision createInstanceFromDatabaseValues(String tableName,
			ResultSet databaseValues) throws SQLException, DAOException {
		DeadlineRevision deadlineRevision= new DeadlineRevision();
		deadlineRevision.setComment(databaseValues.getString(tableName + ".comment"));
		deadlineRevision.setNewDeadline(new Date(databaseValues.getTimestamp(tableName + ".deadline").getTime()));
		deadlineRevision.setAssignmentId(databaseValues.getLong(tableName + ".assignment_id"));
		deadlineRevision.setPersonId(databaseValues.getLong(tableName + ".person_id"));

		return deadlineRevision;
	}
	
	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("comment");
		fieldNames.add("deadline");
		fieldNames.add("assignment_id");
		fieldNames.add("person_id");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(DeadlineRevision entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getComment());
		
		if (entity.getNewDeadline() != null) {
			output.add(new Timestamp(entity.getNewDeadline().getTime()));
		} else {
			output.add(null);
		}
		
		output.add(entity.getAssignmentId());
		output.add(entity.getPersonId());
		return output;
	}

	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}

}
