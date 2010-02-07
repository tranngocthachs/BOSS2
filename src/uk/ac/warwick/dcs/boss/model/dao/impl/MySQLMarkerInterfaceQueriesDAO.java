package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingCategory;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerAssignmentsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerMarksQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerStudentsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.MarkerSubmissionsQueryResult;

public class MySQLMarkerInterfaceQueriesDAO implements IMarkerInterfaceQueriesDAO {
	
	private Connection connection;
	
	public MySQLMarkerInterfaceQueriesDAO(Connection connection) throws DAOException {
		this.connection = connection;
	}

	public Collection<MarkerAssignmentsQueryResult> performAssignmentsToMarkQuery(
			AssignmentStatus status,
			AssignmentsToMarkSortingType sortingType,
			Long markerId)
			throws DAOException {
//		Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		default:
			throw new DAOException("unimplemented"); 
		}
		
		String extraWhere = null;
		switch (status) {
		case ALL:
			extraWhere = "";
			break;
		case CLOSED:
			extraWhere = " AND NOW() >= assignment.closing";
			break;
		case OPEN:
			extraWhere = " AND NOW() < assignment.closing";
			break;
		case PUBLISHED:
			throw new DAOException("unimplemented");
		}
		
		try {
			// Construct the statement.
			String statementString = "SELECT assignment.*, module.*, COUNT(DISTINCT markingassignment.student_id) student_count"
				+ " FROM markingassignment"
				+ " INNER JOIN assignment ON assignment.id=markingassignment.assignment_id"
				+ " INNER JOIN module ON module.id=assignment.module_id"
				+ " WHERE markingassignment.marker_id=? " + extraWhere
				+ " GROUP BY assignment.id"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, markerId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<MarkerAssignmentsQueryResult> result = new LinkedList<MarkerAssignmentsQueryResult>();

			MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(connection);
			MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);
			
			while (rs.next()) {
				Assignment assignment = assignmentDAO.createInstanceFromDatabaseValues("assignment", rs);
				assignment.setId(rs.getLong("assignment.id"));
				
				Module parentModule = moduleDAO.createInstanceFromDatabaseValues("module", rs);
				parentModule.setId(rs.getLong("module.id"));
								
				MarkerAssignmentsQueryResult n = new MarkerAssignmentsQueryResult();
				n.setAssignment(assignment);
				n.setParentModule(parentModule);
				n.setStudentCount(rs.getLong("student_count"));				
				result.add(n);
			}

			rs.close();
			statementObject.close();
			
			// Done		
			return result;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}

	}

	public Collection<MarkerStudentsQueryResult> performStudentsToMarkQuery(
			StudentsToMarkSortingType sortingType, Long markerId,
			Long assignmentId) throws DAOException {
//		Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case LAST_MARKED_ASCENDING:
			orderingString = "ORDER BY last_marked ASC";
			break;
		case LAST_MARKED_DESCENDING:
			orderingString = "ORDER BY last_marked DESC";
			break;
		case STUDENT_ID_ASCENDING:
			orderingString = "ORDER BY person.uniq ASC";
			break;
		case STUDENT_ID_DESCENDING:
			orderingString = "ORDER BY person.uniq DESC";
			break;
		case SUBMISSION_COUNT_ASCENDING:
			orderingString = "ORDER BY n_subs ASC";
			break;
		case SUBMISSION_COUNT_DESCENDING:
			orderingString = "ORDER BY n_subs DESC";
			break;
		case NONE:
			orderingString = "";
			break;
		default:
			throw new DAOException("unimplemented"); 
		}
				
		try {
			// Construct the statement.
			String statementString = "SELECT markingassignment.*, person.*, COUNT(DISTINCT submission.id) n_subs, MAX(mark.timestamp) last_marked"
				+ " FROM markingassignment"
				+ " INNER JOIN person ON person.id=markingassignment.student_id"
				+ " LEFT JOIN mark ON mark.markingassignment_id=markingassignment.id"
				+ " LEFT JOIN submission ON submission.assignment_id=markingassignment.assignment_id AND submission.person_id=person.id"
				+ " WHERE markingassignment.marker_id=? AND markingassignment.assignment_id=?"
				+ " GROUP BY markingassignment.id"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, markerId);
			statementObject.setObject(2, assignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<MarkerStudentsQueryResult> result = new LinkedList<MarkerStudentsQueryResult>();

			MySQLMarkingAssignmentDAO markingAssignmentDAO = new MySQLMarkingAssignmentDAO(connection);
			MySQLPersonDAO personDAO = new MySQLPersonDAO(connection);
			
			while (rs.next()) {
				MarkingAssignment markingAssignment = markingAssignmentDAO.createInstanceFromDatabaseValues("markingassignment", rs);
				markingAssignment.setId(rs.getLong("markingassignment.id"));
				
				Person person = personDAO.createInstanceFromDatabaseValues("person", rs);
				person.setId(rs.getLong("person.id"));
				
				MarkerStudentsQueryResult n = new MarkerStudentsQueryResult();
				n.setMarkingAssignment(markingAssignment);
				n.setLastMarked(rs.getTimestamp("last_marked"));
				n.setSubmissionCount(rs.getLong("n_subs"));
				n.setStudent(person);
				result.add(n);
			}

			rs.close();
			statementObject.close();
			
			// Done		
			return result;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}

	}

	public Collection<MarkerMarksQueryResult> performMarkerMarksQuery(
			MarksSortingType sortingType, Long markingAssignmentId)
			throws DAOException {
//		Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case MARK_ASCENDING:
			orderingString = "ORDER BY mark.value ASC";
			break;
		case MARK_DESCENDING:
			orderingString = "ORDER BY mark.value DESC";
			break;
		case MARKER_ID_ASCENDING:
			orderingString = "ORDER BY person.uniq ASC";
			break;
		case MARKER_ID_DESCENDING:
			orderingString = "ORDER BY person.uniq DESC";
			break;
		case MARKING_CATEGORY_ID_ASCENDING:
			orderingString = "ORDER BY markingcategory.id ASC";
			break;
		case MARKING_CATEGORY_ID_DESCENDING:
			orderingString = "ORDER BY markingcategory.id DESC";
			break;
		case TIMESTAMP_ASCENDING:
			orderingString = "ORDER BY mark.timestamp ASC";
			break;
		case TIMESTAMP_DESCENDING:
			orderingString = "ORDER BY mark.timestamp DESC";
			break;
		case NONE:
			orderingString = "";
			break;
		default:
			throw new DAOException("unimplemented"); 
		}
				
		try {
			// Construct the statement.
			String statementString = "SELECT mark.*, markingcategory.*, person.*"
				+ " FROM mark"
				+ " INNER JOIN markingcategory ON markingcategory.id=mark.markingcategory_id"
				+ " INNER JOIN markingassignment ON markingassignment.id=mark.markingassignment_id"
				+ " INNER JOIN person ON person.id=markingassignment.marker_id"
				+ " WHERE mark.markingassignment_id=?"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, markingAssignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<MarkerMarksQueryResult> result = new LinkedList<MarkerMarksQueryResult>();

			MySQLMarkDAO markDAO = new MySQLMarkDAO(connection);
			MySQLMarkingCategoryDAO markingCategoryDAO = new MySQLMarkingCategoryDAO(connection);
			MySQLPersonDAO personDAO = new MySQLPersonDAO(connection);
			
			while (rs.next()) {				
				Mark mark = markDAO.createInstanceFromDatabaseValues("mark", rs);
				mark.setId(rs.getLong("mark.id"));
				
				MarkingCategory markingCategory = markingCategoryDAO.createInstanceFromDatabaseValues("markingcategory", rs);
				markingCategory.setId(rs.getLong("markingcategory.id"));
				
				Person person = personDAO.createInstanceFromDatabaseValues("person", rs);
				person.setId(rs.getLong("person.id"));
				
				MarkerMarksQueryResult n = new MarkerMarksQueryResult();
				n.setMarkingCategory(markingCategory);
				n.setMark(mark);
				n.setMarker(person);
				n.setEditable(true);
				result.add(n);
			}

			rs.close();
			statementObject.close();
			
			// Done		
			return result;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}

	public Collection<MarkerMarksQueryResult> performModeratorMarksQuery(
			MarksSortingType sortingType, Long markerId, Long studentId, Long assignmentId)
			throws DAOException {
//		Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case MARK_ASCENDING:
			orderingString = "ORDER BY mark.value ASC";
			break;
		case MARK_DESCENDING:
			orderingString = "ORDER BY mark.value DESC";
			break;
		case MARKER_ID_ASCENDING:
			orderingString = "ORDER BY person.uniq ASC";
			break;
		case MARKER_ID_DESCENDING:
			orderingString = "ORDER BY person.uniq DESC";
			break;
		case MARKING_CATEGORY_ID_ASCENDING:
			orderingString = "ORDER BY markingcategory.id ASC";
			break;
		case MARKING_CATEGORY_ID_DESCENDING:
			orderingString = "ORDER BY markingcategory.id DESC";
			break;
		case TIMESTAMP_ASCENDING:
			orderingString = "ORDER BY mark.timestamp ASC";
			break;
		case TIMESTAMP_DESCENDING:
			orderingString = "ORDER BY mark.timestamp DESC";
			break;
		case NONE:
			orderingString = "";
			break;
		default:
			throw new DAOException("unimplemented"); 
		}
				
		try {
			// Construct the statement.
			String statementString = "SELECT mark.*, markingcategory.*, person.*, markingassignment.marker_id marker_id"
				+ " FROM mark"
				+ " INNER JOIN markingcategory ON markingcategory.id=mark.markingcategory_id"
				+ " INNER JOIN markingassignment ON markingassignment.id=mark.markingassignment_id"
				+ " INNER JOIN person ON person.id=markingassignment.marker_id"
				+ " WHERE markingassignment.student_id=? AND markingassignment.assignment_id=?"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, studentId);
			statementObject.setObject(2, assignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<MarkerMarksQueryResult> result = new LinkedList<MarkerMarksQueryResult>();

			MySQLMarkDAO markDAO = new MySQLMarkDAO(connection);
			MySQLMarkingCategoryDAO markingCategoryDAO = new MySQLMarkingCategoryDAO(connection);
			MySQLPersonDAO personDAO = new MySQLPersonDAO(connection);
			
			while (rs.next()) {				
				Mark mark = markDAO.createInstanceFromDatabaseValues("mark", rs);
				mark.setId(rs.getLong("mark.id"));
				
				MarkingCategory markingCategory = markingCategoryDAO.createInstanceFromDatabaseValues("markingcategory", rs);
				markingCategory.setId(rs.getLong("markingcategory.id"));
				
				Person person = personDAO.createInstanceFromDatabaseValues("person", rs);
				person.setId(rs.getLong("person.id"));
				
				MarkerMarksQueryResult n = new MarkerMarksQueryResult();
				n.setMarkingCategory(markingCategory);
				n.setMark(mark);
				n.setMarker(person);
				n.setEditable(rs.getLong("marker_id") == markerId);
				result.add(n);
			}

			rs.close();
			statementObject.close();
			
			// Done		
			return result;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}
	
	public Collection<MarkerSubmissionsQueryResult> performSubmissionsQuery(MarkerSubmissionsQuerySortingType sortingType, Long markingAssignmentId) throws DAOException {
	//		Begin the transaction
			String orderingString = null;
			switch (sortingType) {
			case NONE:
				orderingString = "";
				break;
			case ACTIVE_ASCENDING:
				orderingString = "ORDER BY submission.active ASC, submission.submission_time DESC";
				break;
			case ACTIVE_DESCENDING: 
				orderingString = "ORDER BY submission.active DESC, submission.submission_time ASC";
				break;
			case SUBMISSION_TIME_ASCENDING:
				orderingString = "ORDER BY submission.submission_time ASC";
				break;
			case SUBMISSION_TIME_DESCENDING:
				orderingString = "ORDER BY submission.submission_time DESC";
				break;
			}
			
			try {
				// Construct the statement.
				String statementString = "SELECT submission.*, assignment.*, module.*"
					+ " FROM submission, markingassignment"
					+ " INNER JOIN assignment ON assignment.id=markingassignment.assignment_id"
					+ " INNER JOIN module ON module.id = assignment.module_id"
					+ " WHERE markingassignment.id=?"
					+ "   AND submission.person_id=markingassignment.student_id"
					+ "   AND submission.assignment_id=markingassignment.assignment_id"
					+ " " + orderingString;
				PreparedStatement statementObject = connection.prepareStatement(statementString);
				statementObject.setObject(1, markingAssignmentId);
	
				// Execute the statement.
				Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
				ResultSet rs = statementObject.executeQuery();
	
				// Bundle the results into a thingy
				LinkedList<MarkerSubmissionsQueryResult> result = new LinkedList<MarkerSubmissionsQueryResult>();
	
				MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(connection);
				MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);
				MySQLSubmissionDAO submissionDAO = new MySQLSubmissionDAO(connection);
				
				while (rs.next()) {
					Assignment assignment = assignmentDAO.createInstanceFromDatabaseValues("assignment", rs);
					assignment.setId(rs.getLong("assignment.id"));
					
					Module parentModule = moduleDAO.createInstanceFromDatabaseValues("module", rs);
					parentModule.setId(rs.getLong("module.id"));
					
					Submission submission = submissionDAO.createInstanceFromDatabaseValues("submission", rs);
					submission.setId(rs.getLong("submission.id"));
												
					MarkerSubmissionsQueryResult n = new MarkerSubmissionsQueryResult();
					n.setAssignment(assignment);
					n.setParentModule(parentModule);
					n.setSubmission(submission);
					result.add(n);
				}
	
				rs.close();
				statementObject.close();
				
				// Done		
				return result;
			} catch (SQLException e) {
				throw new DAOException("SQL error", e);
			}
		}
	
	public boolean isMarkerAssignmentAccessAllowed(Long markerId,
			Long assignmentId) throws DAOException {
		// Check if the marker and assignment are in a markingassignment
		try {
			PreparedStatement check = connection.prepareStatement(
					"SELECT markingassignment.id FROM markingassignment"
					+ " WHERE markingassignment.assignment_id=? AND"
					+ "       markingassignment.marker_id=?");
			check.setLong(1, assignmentId);
			check.setLong(2, markerId);

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
	
	public boolean isMarkerMarkingAssignmentAccessAllowed(Long markerId,
			Long markingAssignmentId) throws DAOException {
		// Check if marking the parent assignment.
		try {
			PreparedStatement check = connection.prepareStatement(
					"SELECT markingassignment.id FROM markingassignment"
					+ " WHERE markingassignment.marker_id = ? AND"
					+ "       markingassignment.id = ?");
			check.setLong(1, markerId);
			check.setLong(2, markingAssignmentId);
			
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
	
	public boolean isMarkerSubmissionAccessAllowed(Long markerId,
			Long submissionId) throws DAOException {
		// Check if the marker is marking the assignment.
		try {
			PreparedStatement check = connection.prepareStatement(
					"SELECT markingassignment.id FROM markingassignment, submission"
					+ " WHERE markingassignment.assignment_id=submission.assignment_id AND"
					+ "       markingassignment.student_id=submission.person_id AND"
					+ "       markingassignment.marker_id=? AND"
					+ "       submission.id = ?");
			check.setLong(1, markerId);
			check.setLong(2, submissionId);

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
