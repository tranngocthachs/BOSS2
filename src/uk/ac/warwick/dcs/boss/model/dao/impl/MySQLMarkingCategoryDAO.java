package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingCategoryDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingCategory;

public class MySQLMarkingCategoryDAO extends MySQLEntityDAO<MarkingCategory>
		implements IMarkingCategoryDAO {

	private String mySQLSortingString = "id DESC";

	public MySQLMarkingCategoryDAO(Connection connection) throws DAOException {
		super(connection);
	}
	
	public String getTableName() {
		return "markingcategory";
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
		case WEIGHTING_ASCENDING:
			this.mySQLSortingString = "weighting ASC";
			break;
		case WEIGHTING_DESCENDING:
			this.mySQLSortingString = "weighting DESC";
			break;
		default:
			throw new DAOException("Unsupported sorting type:" + sortingType);
		}
	}

	@Override
	public MarkingCategory createInstanceFromDatabaseValues(String tableName,
			ResultSet databaseValues) throws SQLException, DAOException {
		MarkingCategory markingCategory = new MarkingCategory();
		markingCategory.setName(databaseValues.getString("markingcategory.name"));
		markingCategory.setWeighting(databaseValues.getLong("weighting"));
		markingCategory.setMaximumMark(databaseValues.getLong("max_mark"));
		markingCategory.setAssignmentId(databaseValues.getLong("markingcategory.assignment_id"));

		return markingCategory;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("name");
		fieldNames.add("weighting");
		fieldNames.add("max_mark");
		fieldNames.add("assignment_id");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(MarkingCategory entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getName());
		output.add(entity.getWeighting());
		output.add(entity.getMaximumMark());
		output.add(entity.getAssignmentId());
		return output;
	}

	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}
	
}
