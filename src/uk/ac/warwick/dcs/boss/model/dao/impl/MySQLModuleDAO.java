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
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class MySQLModuleDAO extends MySQLEntityDAO<Module> implements IModuleDAO {

	private String mySQLSortingString = "id DESC";

	public MySQLModuleDAO(Connection connection) throws DAOException {
		super(connection);
	}
	
	public String getTableName() {
		return "module";
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
	public Module createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues)
	throws SQLException, DAOException {
		Module module = new Module();
		module.setName(databaseValues.getString(tableName + ".name"));
		module.setUniqueIdentifier(databaseValues.getString(tableName + ".uniq"));
		module.setModelId(databaseValues.getLong(tableName + ".model_id"));
		module.setRegistrationRequired(databaseValues.getBoolean(tableName + ".registration_required"));

		return module;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("name");
		fieldNames.add("uniq");
		fieldNames.add("model_id");
		fieldNames.add("registration_required");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(Module entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getName());
		output.add(entity.getUniqueIdentifier());
		output.add(entity.getModelId());
		output.add(entity.isRegistrationRequired());
		return output;
	}

	@Override
	public void deletePersistentEntity(Long moduleId)
			throws DAOException {

		PreparedStatement statement = null;
		
		try {
			// markers
			statement = getConnection().prepareStatement(
					"DELETE FROM module_students"
							+ " WHERE module_id = ?");
			statement.setLong(1, moduleId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
			
			// administrators
			statement = getConnection().prepareStatement(
					"DELETE FROM module_administrators"
							+ " WHERE module_id = ?");
			statement.setLong(1, moduleId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
		
		super.deletePersistentEntity(moduleId);
	}
	
	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}

	public void addStudentAssociation(Long moduleId,
			Long studentId) throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"INSERT INTO module_students (module_id, student_id)"
					+ "VALUES (?, ?)");
			statement.setLong(1, moduleId);
			statement.setLong(2, studentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}

	public void addAdministratorAssociation(
			Long moduleId,
			Long administratorId) throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"INSERT INTO module_administrators (module_id, administrator_id)"
					+ "VALUES (?, ?)");
			statement.setLong(1, moduleId);
			statement.setLong(2, administratorId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}

	public Collection<Person> fetchStudents(IPersonDAO.SortingType sortingType,
			Long moduleId) throws DAOException {
		MySQLPersonDAO dao = new MySQLPersonDAO(getConnection());
		dao.setSortingType(sortingType);
		return dao.fetchEntitiesFromJoin("module_students",
				"module_id", moduleId,
				"student_id", "id");
	}

	public Collection<Person> fetchAdministrators(
			IPersonDAO.SortingType sortingType,
			Long moduleId) throws DAOException {
		MySQLPersonDAO dao = new MySQLPersonDAO(getConnection());
		dao.setSortingType(sortingType);
		return dao.fetchEntitiesFromJoin("module_administrators",
				"module_id", moduleId,
				"administrator_id", "id");
	}

	public void removeStudentAssociation(Long moduleId,
			Long studentId) throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"DELETE FROM module_students "
					+ "WHERE module_id=? AND student_id=?");
			statement.setLong(1, moduleId);
			statement.setLong(2, studentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}

	}

	public void removeAdministratorAssociation(
			Long moduleId,
			Long administratorId) throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"DELETE FROM module_administrators "
					+ "WHERE module_id=? AND administrator_id=?");
			statement.setLong(1, moduleId);
			statement.setLong(2, administratorId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}

	public boolean hasAssignments(Long moduleId) throws DAOException {
		try {
			PreparedStatement check = getConnection().prepareStatement(
					"SELECT id FROM assignment"
					+ " WHERE module_id=?");
			check.setLong(1, moduleId);

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
