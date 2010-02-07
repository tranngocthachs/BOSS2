package uk.ac.warwick.dcs.boss.model.dao.impl;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IMarkDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;

public class MySQLMarkDAO extends MySQLEntityDAO<Mark> implements
		IMarkDAO {

	private String mySQLSortingString = "id DESC";
	
	public MySQLMarkDAO(Connection connection) throws DAOException {
		super(connection);
	}
	
	public String getTableName() {
		return "mark";
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
		case TIMESTAMP_ASCENDING:
			this.mySQLSortingString = "timestamp ASC";
			break;
		case TIMESTAMP_DESCENDING:
			this.mySQLSortingString = "timestamp DESC";
			break;
		case VALUE_ASCENDING:
			this.mySQLSortingString = "value ASC";
			break;
		case VALUE_DESCENDING:
			this.mySQLSortingString = "value DESC";
			break;			
		default:
			throw new DAOException("Unsupported sorting type:" + sortingType);
		}
	}

	@Override
	public Mark createInstanceFromDatabaseValues(String tableName,
			ResultSet databaseValues) throws SQLException, DAOException {
				Mark markRevision = new Mark();
		markRevision.setComment(databaseValues.getString(tableName + ".comment"));
		markRevision.setTimestamp(new Date(databaseValues.getTimestamp(tableName + ".timestamp").getTime()));
		markRevision.setValue(databaseValues.getInt(tableName + ".value"));
		markRevision.setMarkingAssignmentId(databaseValues.getLong(tableName + ".markingassignment_id"));
		markRevision.setMarkingCategoryId(databaseValues.getLong(tableName + ".markingcategory_id"));
		
		return markRevision;
	}
	
	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("comment");
		fieldNames.add("timestamp");
		fieldNames.add("value");
		fieldNames.add("markingassignment_id");
		fieldNames.add("markingcategory_id");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(Mark entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getComment());
		if (entity.getTimestamp() != null) {
			output.add(entity.getTimestamp());
		} else {
			output.add(null);
		}
		output.add(entity.getValue());
		output.add(entity.getMarkingAssignmentId());
		output.add(entity.getMarkingCategoryId());
		return output;
	}

	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}

}
