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
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class MySQLPersonDAO extends MySQLEntityDAO<Person> implements IPersonDAO {

	private String mySQLSortingString = "id DESC";
	
	public MySQLPersonDAO(Connection connection) throws DAOException {
		super(connection);
	}
	
	public String getTableName() {
		return "person";
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
		case EMAIL_ADDRESS_ASCENDING:
			this.mySQLSortingString = "email ASC";
			break;
		case EMAIL_ADDRESS_DESCENDING:
			this.mySQLSortingString = "email DESC";
			break;
		case CHOSEN_NAME_ASCENDING:
			this.mySQLSortingString = "chosen_name ASC";
			break;
		case CHOSEN_NAME_DESCENDING:
			this.mySQLSortingString = "chosen_name DESC";
			break;
		case UNIQUE_IDENTIFIER_ASCENDING:
			this.mySQLSortingString = "uniq ASC";
			break;
		case UNIQUE_IDENTIFIER_DESCENDING:
			this.mySQLSortingString = "uniq DESC";
			break;
		case ADMINISTRATOR_ASCENDING:
			this.mySQLSortingString = "administrator ASC";
			break;
		case ADMINISTRATOR_DESCENDING:
			this.mySQLSortingString = "administrator DESC";
			break;
		default:
			throw new DAOException("Unsupported sorting type:" + sortingType);
		}
	}

	@Override
	public Person createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues)
			throws SQLException, DAOException {
		Person person = new Person();
		person.setChosenName(databaseValues.getString(tableName + ".chosen_name"));
		person.setUniqueIdentifier(databaseValues.getString(tableName + ".uniq"));
		person.setEmailAddress(databaseValues.getString(tableName + ".email"));
		person.setPassword(databaseValues.getString(tableName + ".password"));
		person.setAdministrator(databaseValues.getBoolean(tableName + ".administrator"));

		return person;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("chosen_name");
		fieldNames.add("uniq");
		fieldNames.add("email");
		fieldNames.add("password");
		fieldNames.add("administrator");
		return fieldNames;
	}

	@Override
	public Collection<Object> getDatabaseValues(Person entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getChosenName());
		output.add(entity.getUniqueIdentifier());
		output.add(entity.getEmailAddress());
		output.add(entity.getPassword());
		output.add(entity.isAdministrator());
		return output;
	}

	@Override
	public void deletePersistentEntity(Long personId)
			throws DAOException {

		PreparedStatement statement = null;
		
		try {
			// markers
			statement = getConnection().prepareStatement(
					"DELETE FROM assignment_markers"
							+  " WHERE marker_id = ?");
			statement.setLong(1, personId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
			
			// administrators
			statement = getConnection().prepareStatement(
					"DELETE FROM module_administrators"
							+ " WHERE administrator_id = ?");
			statement.setLong(1, personId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
			
			// students
			statement = getConnection().prepareStatement(
					"DELETE FROM module_students"
							+ " WHERE student_id = ?");
			statement.setLong(1, personId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
		
		super.deletePersistentEntity(personId);
	}
	
	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}

	public Collection<Assignment> fetchAssignmentsToMark(
			IAssignmentDAO.SortingType sortingType,
			Long markerId) throws DAOException {
		MySQLAssignmentDAO dao = new MySQLAssignmentDAO(getConnection());
		dao.setSortingType(sortingType);
		return dao.fetchEntitiesFromJoin(
				"assignment_markers",
				"marker_id", markerId,
				"assignment_id", "id"
		);
	}

	public Collection<Assignment> fetchAssignmentsToModerate(
			IAssignmentDAO.SortingType sortingType,
			Long moderatorId) throws DAOException {
		MySQLAssignmentDAO dao = new MySQLAssignmentDAO(getConnection());
		dao.setSortingType(sortingType);
		return dao.fetchEntitiesFromJoin(
				"assignment_moderators",
				"moderator_id", moderatorId,
				"assignment_id", "id"
		);
	}
	
	public Collection<Module> fetchModulesToStudy(
			IModuleDAO.SortingType sortingType,
			Long studentId) throws DAOException {
		MySQLModuleDAO dao = new MySQLModuleDAO(getConnection());
		dao.setSortingType(sortingType);
		return dao.fetchEntitiesFromJoin(
				"module_students",
				"student_id", studentId,
				"module_id", "id"
		);
	}

	public Collection<Module> fetchModulesToAdministrate(
			IModuleDAO.SortingType sortingType,
			Long administratorId) throws DAOException {
		MySQLModuleDAO dao = new MySQLModuleDAO(getConnection());
		dao.setSortingType(sortingType);
		return dao.fetchEntitiesFromJoin(
				"module_administrators",
				"administrator_id", administratorId,
				"module_id", "id"
		);
	}
	
	public boolean hasSubmissions(Long personId) throws DAOException {
		try {
			PreparedStatement check = getConnection().prepareStatement(
					"SELECT id FROM submission"
					+ " WHERE person_id=?");
			check.setLong(1, personId);

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
	

	public Person fetchPersonWithUniqueIdentifier(String uniqueIdentifier)
			throws DAOException {
		try {
			PreparedStatement check = getConnection().prepareStatement(
					"SELECT person.* FROM person"
					+ " WHERE uniq=?");
			check.setString(1, uniqueIdentifier);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + check);
			ResultSet rs = check.executeQuery();
			if (rs.first()) {
				Person person = createInstanceFromDatabaseValues("person", rs);
				person.setId(rs.getLong("person.id"));
				rs.close();
				check.close();
				return person;
			} else {
				rs.close();
				check.close();
				return null;
			}
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}

	}
}
