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
import uk.ac.warwick.dcs.boss.model.dao.IModelDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;

public class MySQLModelDAO extends MySQLEntityDAO<Model> implements IModelDAO {

	private String mySQLSortingString = "id DESC";

	public MySQLModelDAO(Connection connection) throws DAOException {
		super(connection);
	}
	
	public String getTableName() {
		return "model";
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
	public Model createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues)
	throws SQLException, DAOException {
		Model model = new Model();
		model.setName(databaseValues.getString("model.name"));
		model.setUniqueIdentifier(databaseValues.getString("model.uniq"));

		return model;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("name");
		fieldNames.add("uniq");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(Model entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getName());
		output.add(entity.getUniqueIdentifier());
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
