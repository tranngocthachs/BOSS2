package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

public class MySQLAssignmentDAO extends MySQLEntityDAO<Assignment> implements
IAssignmentDAO {

	private String mySQLSortingString = "id DESC";

	public MySQLAssignmentDAO(Connection connection) throws DAOException {
		super(connection);
	}

	public String getTableName() {
		return "assignment";
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
	public Assignment createInstanceFromDatabaseValues(String tableName, ResultSet databaseValues)
	throws SQLException, DAOException {
		Assignment assignment = new Assignment();
		assignment.setName(databaseValues.getString(tableName + ".name"));
		assignment.setOpeningTime(new Date(databaseValues.getTimestamp(tableName + ".opening")
				.getTime()));
		assignment.setDeadline(new Date(databaseValues.getTimestamp(tableName + ".deadline")
				.getTime()));
		assignment.setClosingTime(new Date(databaseValues.getTimestamp(tableName + ".closing")
				.getTime()));
		assignment.setResourceId(databaseValues.getLong(tableName + ".resource_id"));
		assignment.setModuleId(databaseValues.getLong(tableName + ".module_id"));
		assignment.setAllowDeletion(databaseValues.getBoolean(tableName + ".allow_deletion"));

		return assignment;
	}

	@Override
	public Collection<String> getDatabaseFieldNames() {
		Vector<String> fieldNames = new Vector<String>();
		fieldNames.add("name");
		fieldNames.add("deadline");
		fieldNames.add("opening");
		fieldNames.add("closing");
		fieldNames.add("resource_id");
		fieldNames.add("module_id");
		fieldNames.add("allow_deletion");
		return fieldNames;
	}

	@Override
	public void deletePersistentEntity(Long assignmentId)
	throws DAOException {

		PreparedStatement statement = null;

		try {	
			// markers
			statement = getConnection().prepareStatement(
					"DELETE FROM assignment_markers"
					+ " WHERE assignment_id = ?");
			statement.setLong(1, assignmentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();

			// filenames
			statement = getConnection().prepareStatement(
					"DELETE FROM assignment_requiredfilenames"
					+ " WHERE assignment_id = ?");
			statement.setLong(1, assignmentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}

		super.deletePersistentEntity(assignmentId);
	}

	@Override
	public Collection<Object> getDatabaseValues(Assignment entity) {
		Vector<Object> output = new Vector<Object>();
		output.add(entity.getName());

		if (entity.getDeadline() != null) {
			output.add(new Timestamp(entity.getDeadline().getTime()));
		} else {
			output.add(null);
		}

		if (entity.getOpeningTime() != null) {
			output.add(new Timestamp(entity.getOpeningTime().getTime()));
		} else {
			output.add(null);
		}

		if (entity.getClosingTime() != null) {
			output.add(new Timestamp(entity.getClosingTime().getTime()));
		} else {
			output.add(null);
		}

		output.add(entity.getResourceId());
		output.add(entity.getModuleId());
		output.add(entity.getAllowDeletion());
		return output;
	}

	@Override
	public String getMySQLSortingString() {
		return mySQLSortingString;
	}

	public void addMarkerAssociation(
			Long assignmentId,
			Long markerId) throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"INSERT INTO assignment_markers (assignment_id, marker_id)"
					+ "VALUES (?, ?)");
			statement.setLong(1, assignmentId);
			statement.setLong(2, markerId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}

	public Collection<Person> fetchMarkers(
			IPersonDAO.SortingType sortingType,
			Long assignmentId) throws DAOException {
		MySQLPersonDAO dao = new MySQLPersonDAO(getConnection());
		dao.setSortingType(sortingType);
		return dao.fetchEntitiesFromJoin(
				"assignment_markers",
				"assignment_id", assignmentId,
				"marker_id", "id");
	}

	public void removeMarkerAssociation(
			Long assignmentId,
			Long markerId) throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"DELETE FROM assignment_markers "
					+ "WHERE assignment_id=? AND marker_id=?");
			statement.setLong(1, assignmentId);
			statement.setLong(2, markerId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}

	public void addRequiredFilename(Long assignmentId, String submissionFilename)
	throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"INSERT INTO assignment_requiredfilenames (assignment_id, filename)"
					+ "VALUES (?, ?)");
			statement.setLong(1, assignmentId);
			statement.setString(2, submissionFilename);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}	
	}

	public Collection<String> fetchRequiredFilenames(Long assignmentId)
	throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"SELECT filename FROM assignment_requiredfilenames"
					+ " WHERE assignment_requiredfilenames.assignment_id=?");
			statement.setLong(1, assignmentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());

			ResultSet rs = statement.executeQuery();
			Vector<String> result = new Vector<String>();
			while (rs.next()) {
				result.add(rs.getString("filename"));
			}

			rs.close();
			statement.close();
			return result;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}	
	}

	public void removeRequiredFilename(Long assignmentId,
			String requiredFilename) throws DAOException {
		try {
			PreparedStatement statement = getConnection().prepareStatement(
					"DELETE FROM assignment_requiredfilenames "
					+ "WHERE assignment_id=? AND filename=?");
			statement.setLong(1, assignmentId);
			statement.setString(2, requiredFilename);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statement.toString());
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}		
	}

	public boolean hasDeadlineRevisions(Long assignmentId) throws DAOException {
		try {
			PreparedStatement check = getConnection().prepareStatement(
					"SELECT id FROM deadlinerevision"
					+ " WHERE assignment_id=?");
			check.setLong(1, assignmentId);

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

	public boolean hasMarkingCategories(Long assignmentId) throws DAOException {
		try {
			PreparedStatement check = getConnection().prepareStatement(
					"SELECT id FROM markingcategory"
					+ " WHERE assignment_id=?");
			check.setLong(1, assignmentId);

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

	public boolean hasSubmissions(Long assignmentId) throws DAOException {
		try {
			PreparedStatement check = getConnection().prepareStatement(
					"SELECT id FROM submission"
					+ " WHERE assignment_id=?");
			check.setLong(1, assignmentId);

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

	public boolean hasTests(Long assignmentId) throws DAOException {
		try {
			PreparedStatement check = getConnection().prepareStatement(
					"SELECT id FROM " + MySQLTestDAO.TABLE_NAME
					+ " WHERE " + MySQLTestDAO.ASSIGNMENT_ID_FIELD  + "=?");
			check.setLong(1, assignmentId);

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

	public Collection<Person> fetchStudentsWithIncompleteMarks(
			IPersonDAO.SortingType sortingType,
			Long assignmentId) throws DAOException {
		HashSet<Long> studentIds = new HashSet<Long>();
		long markingCategoryCount;
		
		try {
			// Get number of marking categories for this assignment.
			PreparedStatement query = getConnection().prepareStatement(
					"SELECT COUNT(markingcategory.id)"
					+ " FROM markingcategory"
					+ " WHERE markingcategory.assignment_id=?");
			query.setLong(1, assignmentId);
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + query);
			
			ResultSet rs = query.executeQuery();
			if (!rs.first()) {
				rs.close();
				query.close();
				throw new DAOException("no result from marking category count query");
			}
			markingCategoryCount = rs.getLong(1);

			rs.close();
			query.close();
			
			// Get students registered on this module and add
			// to set
			query = getConnection().prepareStatement(
					"SELECT DISTINCT module_students.student_id"
					+ " FROM module_students, assignment"
					+ " WHERE module_students.module_id=assignment.module_id AND"
					+ "       assignment.id=?");
			query.setLong(1, assignmentId);
			
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + query);
			
			rs = query.executeQuery();
			while (rs.next()) {
				studentIds.add(rs.getLong(1));
			}
			
			rs.close();
			query.close();

			// Find students with a submission for the assignment
			// and add to the set (to pick up non-registered students)
			query = getConnection().prepareStatement(
					"SELECT DISTINCT submission.person_id"
					+ " FROM submission"
					+ " WHERE submission.assignment_id=?");
			query.setLong(1, assignmentId);
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + query);
			
			rs = query.executeQuery();
			while (rs.next()) {
				studentIds.add(rs.getLong(1));
			}

			rs.close();
			query.close();
			
			// Find students with all marks in the database.
			// Then remove them from the list.
			// My very first HAVING clause!  I'm a grownup now!
			query = getConnection().prepareStatement(
					  "SELECT DISTINCT markingassignment.student_id"
					+ " FROM mark, markingassignment"
					+ " WHERE markingassignment.id=mark.markingassignment_id AND markingassignment.assignment_id=?"
					+ " GROUP BY markingassignment.student_id"
					+ " HAVING COUNT(DISTINCT mark.markingcategory_id) = ?"
					);
			query.setLong(1, assignmentId);
			query.setLong(2, markingCategoryCount);
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + query);
			
			rs = query.executeQuery();
			while (rs.next()) {
				studentIds.remove(rs.getLong(1));
			}
			
			rs.close();
			query.close();
			
			// Fetch these people.
			if(studentIds.size() > 0) {
				MySQLPersonDAO personDao = new MySQLPersonDAO(getConnection());
				personDao.setSortingType(sortingType);
				return personDao.retrievePersistentEntities(studentIds);
			} else {
				return new LinkedList<Person>();
			}
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}


	}

	public Collection<Person> fetchStudentsWithNoSubmissions(
			IPersonDAO.SortingType sortingType,
			Long assignmentId) throws DAOException {
		HashSet<Long> studentIds = new HashSet<Long>();

		try {
			// Get students registered on this module and add
			// to set
			PreparedStatement query = getConnection().prepareStatement(
					"SELECT DISTINCT module_students.student_id"
					+ " FROM module_students, assignment"
					+ " WHERE module_students.module_id=assignment.module_id AND"
					+ "       assignment.id=?");
			query.setLong(1, assignmentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing:" + query.toString());
			ResultSet rs = query.executeQuery();
			while (rs.next()) {
				studentIds.add(rs.getLong(1));
			}

			rs.close();
			query.close();
			
			// Find students with a submission for the assignment
			// and remove from the set
			query = getConnection().prepareStatement(
					"SELECT DISTINCT submission.person_id"
					+ " FROM submission"
					+ " WHERE submission.assignment_id=?");
			query.setLong(1, assignmentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + query);
			rs = query.executeQuery();
			while (rs.next()) {
				studentIds.remove(rs.getLong(1));
			}
			
			rs.close();
			query.close();

			// Fetch these people.
			if(studentIds.size() > 0) {
				MySQLPersonDAO personDao = new MySQLPersonDAO(getConnection());
				personDao.setSortingType(sortingType);
				return personDao.retrievePersistentEntities(studentIds);
			} else {
				return new LinkedList<Person>();
			}
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}

	}
	
	public Collection<Person> fetchSubmittersAndStudents(
			IPersonDAO.SortingType sortingType, Long assignmentId) throws DAOException {
		HashSet<Long> studentIds = new HashSet<Long>();

		try {			
			// Get students registered on this module and add
			// to set
			PreparedStatement query = getConnection().prepareStatement(
					"SELECT DISTINCT module_students.student_id"
					+ " FROM module_students, assignment"
					+ " WHERE module_students.module_id=assignment.module_id AND"
					+ "       assignment.id=?");
			query.setLong(1, assignmentId);
	
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + query);
			ResultSet rs = query.executeQuery();
			while (rs.next()) {
				studentIds.add(rs.getLong(1));
			}
	
			rs.close();
			query.close();
			
			// Find students with a submission for the assignment
			// and add to the set (to pick up non-registered students)
			query = getConnection().prepareStatement(
					"SELECT DISTINCT submission.person_id"
					+ " FROM submission"
					+ " WHERE submission.assignment_id=?");
			query.setLong(1, assignmentId);
	
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + query);
			rs = query.executeQuery();
			while (rs.next()) {
				studentIds.add(rs.getLong(1));
			}
			
			rs.close();
			query.close();
			
			// Fetch these people.
			if(studentIds.size() > 0) {
				MySQLPersonDAO personDao = new MySQLPersonDAO(getConnection());
				personDao.setSortingType(sortingType);
				return personDao.retrievePersistentEntities(studentIds);
			} else {
				return new LinkedList<Person>();
			}

		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}
		
	}
}
