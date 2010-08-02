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
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;
import uk.ac.warwick.dcs.boss.model.dao.beans.Mark;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.dao.beans.Result;
import uk.ac.warwick.dcs.boss.model.dao.beans.SherlockSession;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffAssignmentsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffDeadlineRevisionsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffMarkingAssignmentQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffModulesQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffResultsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StaffSubmissionsQueryResult;

public class MySQLStaffInterfaceQueriesDAO implements IStaffInterfaceQueriesDAO {
	
	private Connection connection;
		
	public MySQLStaffInterfaceQueriesDAO(Connection connection) throws DAOException {
		this.connection = connection;
	}

	public Collection<StaffModulesQueryResult> performStaffModulesQuery(
			StaffModulesQuerySortingType sortingType, Long staffId)
			throws DAOException {
		//  Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		case ASSIGNMENT_COUNT_ASCENDING:
			orderingString = "ORDER BY n_ass ASC";
			break;
		case ASSIGNMENT_COUNT_DESCENDING:
			orderingString = "ORDER BY n_ass DESC";
			break;
		case STUDENT_COUNT_ASCENDING:
			orderingString = "ORDER BY n_sts ASC";
			break;
		case STUDENT_COUNT_DESCENDING:
			orderingString = "ORDER BY n_sts DESC";
			break;
		case MODEL_ID_ASCENDING:
			orderingString = "ORDER BY model.id ASC";
			break;
		case MODEL_ID_DESCENDING:
			orderingString = "ORDER BY model.id DESC";
			break;
		}
		
		try {
			// Construct the statement.
			String statementString = "SELECT model.*, module.*, COUNT(DISTINCT assignment.id) n_ass, COUNT(DISTINCT module_students.student_id) n_sts"
				+ " FROM module_administrators"
				+ " INNER JOIN module ON module.id = module_administrators.module_id"
				+ " INNER JOIN model ON model.id = module.model_id"
				+ " LEFT JOIN assignment ON assignment.module_id = module.id"
				+ " LEFT JOIN module_students ON module_students.module_id = module.id"
				+ " WHERE module_administrators.administrator_id=?"
				+ " GROUP BY module.id"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, staffId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StaffModulesQueryResult> result = new LinkedList<StaffModulesQueryResult>();

			MySQLModelDAO modelDAO = new MySQLModelDAO(connection);
			MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);
			
			while (rs.next()) {
				Model model = modelDAO.createInstanceFromDatabaseValues("model", rs);
				model.setId(rs.getLong("model.id"));
				
				Module module = moduleDAO.createInstanceFromDatabaseValues("module", rs);
				module.setId(rs.getLong("module.id"));
															
				StaffModulesQueryResult n = new StaffModulesQueryResult();
				n.setModel(model);
				n.setModule(module);
				n.setAssignmentCount(rs.getLong("n_ass"));
				n.setStudentCount(rs.getLong("n_sts"));
				
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

	public Collection<StaffAssignmentsQueryResult> performStaffAssignmentsQuery(
			StaffAssignmentsQuerySortingType sortingType, Long moduleId)
			throws DAOException {
		//  Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		case MARKING_CATEGORY_COUNT_ASCENDING:
			orderingString = "ORDER BY n_mcs ASC";
			break;
		case MARKING_CATEGORY_COUNT_DESCENDING:
			orderingString = "ORDER BY n_mcs DESC";
			break;
		case TEST_COUNT_ASCENDING:
			orderingString = "ORDER BY n_ts ASC";
			break;
		case TEST_COUNT_DESCENDING:
			orderingString = "ORDER BY n_ts DESC";
			break;
		case MARKER_COUNT_ASCENDING:
			orderingString = "ORDER BY n_mks ASC";
			break;
		case MARKER_COUNT_DESCENDING:
			orderingString = "ORDER BY n_mks DESC";
			break;
		case SUBMISSION_COUNT_ASCENDING:
			orderingString = "ORDER BY n_sub ASC";
			break;
		case SUBMISSION_COUNT_DESCENDING:
			orderingString = "ORDER BY n_sub DESC";
			break;
		case DEADLINEREVISION_COUNT_ASCENDING:
			orderingString = "ORDER BY n_dr ASC";
			break;
		case DEADLINEREVISION_COUNT_DESCENDING:
			orderingString = "ORDER BY n_dr DESC";
			break;
		case MARKINGASSIGNMENT_COUNT_ASCENDING:
			orderingString = "ORDER BY n_ma ASC";
			break;
		case MARKINGASSIGNMENT_COUNT_DESCENDING:
			orderingString = "ORDER BY n_ma DESC";
			break;
		case RESULT_COUNT_ASCENDING:
			orderingString = "ORDER BY n_rs ASC";
			break;
		case RESULT_COUNT_DESCENDING:
			orderingString = "ORDER BY n_rs DESC";
			break;
		}
		
		try {
			// Construct the statement.
			String statementString = "SELECT assignment.*, COUNT(DISTINCT assignment_requiredfilenames.filename) n_fls, COUNT(DISTINCT markingcategory.id) n_mcs, COUNT(DISTINCT test.id) n_ts, COUNT(DISTINCT assignment_markers.marker_id) n_mks, COUNT(DISTINCT submission.id) n_sub, COUNT(DISTINCT deadlinerevision.id) n_dr, COUNT(DISTINCT markingassignment.id) n_ma, COUNT(DISTINCT result.id) n_rs"
				+ " FROM assignment"
				+ " LEFT JOIN markingcategory ON markingcategory.assignment_id = assignment.id"
				+ " LEFT JOIN assignment_requiredfilenames ON assignment_requiredfilenames.assignment_id = assignment.id"
				+ " LEFT JOIN test ON test.assignment_id = assignment.id"
				+ " LEFT JOIN assignment_markers ON assignment_markers.assignment_id = assignment.id"
				+ " LEFT JOIN submission ON submission.assignment_id = assignment.id"
				+ " LEFT JOIN deadlinerevision ON deadlinerevision.assignment_id = assignment.id"
				+ " LEFT JOIN markingassignment ON markingassignment.assignment_id = assignment.id"
				+ " LEFT JOIN result ON result.assignment_id = assignment.id"
				+ " WHERE assignment.module_id=?"
				+ " GROUP BY assignment.id"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, moduleId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StaffAssignmentsQueryResult> result = new LinkedList<StaffAssignmentsQueryResult>();

			MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(connection);
			
			while (rs.next()) {			
				Assignment assignment = assignmentDAO.createInstanceFromDatabaseValues("assignment", rs);
				assignment.setId(rs.getLong("assignment.id"));
															
				StaffAssignmentsQueryResult n = new StaffAssignmentsQueryResult();
				n.setAssignment(assignment);
				n.setFilesCount(rs.getLong("n_fls"));
				n.setMarkingCategoryCount(rs.getLong("n_mcs"));
				n.setTestCount(rs.getLong("n_ts"));
				n.setMarkersCount(rs.getLong("n_mks"));
				n.setMarkingAssignmentCount(rs.getLong("n_ma"));
				n.setSubmissionCount(rs.getLong("n_sub"));
				n.setDeadlineRevisionCount(rs.getLong("n_dr"));
				n.setResultCount(rs.getLong("n_rs"));
				
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

	public Collection<StaffSubmissionsQueryResult> performStaffSubmissionsQuery(
			StaffSubmissionsQuerySortingType sortingType, Long assignmentId)
			throws DAOException {
		//  Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		case STUDENT_UNIQUE_IDENTIFIER_ASCENDING:
			orderingString = "ORDER BY person.uniq ASC, submission.submission_time DESC";
			break;
		case STUDENT_UNIQUE_IDENTIFIER_DESCENDING:
			orderingString = "ORDER BY person.uniq DESC, submission.submission_time DESC";
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
			String statementString = "SELECT submission.*, person.*"
				+ " FROM submission"
				+ " INNER JOIN person ON submission.person_id = person.id"
				+ " WHERE submission.assignment_id=?"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, assignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StaffSubmissionsQueryResult> result = new LinkedList<StaffSubmissionsQueryResult>();

			MySQLSubmissionDAO submissionDAO = new MySQLSubmissionDAO(connection);
			MySQLPersonDAO personDAO = new MySQLPersonDAO(connection);
			
			while (rs.next()) {			
				Submission submission = submissionDAO.createInstanceFromDatabaseValues("submission", rs);
				submission.setId(rs.getLong("submission.id"));
				
				Person person = personDAO.createInstanceFromDatabaseValues("person", rs);
				person.setId(rs.getLong("person.id"));
															
				StaffSubmissionsQueryResult n = new StaffSubmissionsQueryResult();
				n.setPerson(person);
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

	public Collection<StaffDeadlineRevisionsQueryResult> performStaffDeadlineRevisionsQuery(
			StaffDeadlineRevisionsQuerySortingType sortingType, Long assignmentId)
			throws DAOException {
		//  Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case DEADLINE_ASCENDING:
			orderingString = "ORDER BY deadlinerevision.deadline ASC";
			break;
		case DEADLINE_DESCENDING:
			orderingString = "ORDER BY deadlinerevision.deadline DESC";
			break;
		case STUDENT_ID_ASCENDING:
			orderingString = "ORDER BY person.uniq ASC";
			break;
		case STUDENT_ID_DESCENDING:
			orderingString = "ORDER BY person.uniq DESC";
			break;
		case NONE:
			orderingString = "";
			break;
		}
		
		try {
			// Construct the statement.
			String statementString = "SELECT person.*, deadlinerevision.*"
				+ " FROM deadlinerevision"
				+ " INNER JOIN person ON deadlinerevision.person_id = person.id"
				+ " INNER JOIN assignment ON deadlinerevision.assignment_id = assignment.id"
				+ " WHERE deadlinerevision.assignment_id=?"
				+ " GROUP BY deadlinerevision.id"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, assignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StaffDeadlineRevisionsQueryResult> result = new LinkedList<StaffDeadlineRevisionsQueryResult>();

			MySQLDeadlineRevisionDAO deadlineRevisionDAO = new MySQLDeadlineRevisionDAO(connection);
			MySQLPersonDAO personDAO = new MySQLPersonDAO(connection);
			
			while (rs.next()) {			
				DeadlineRevision deadlineRevision = deadlineRevisionDAO.createInstanceFromDatabaseValues("deadlinerevision", rs);
				deadlineRevision.setId(rs.getLong("deadlinerevision.id"));
				
				Person person = personDAO.createInstanceFromDatabaseValues("person", rs);
				person.setId(rs.getLong("person.id"));
															
				StaffDeadlineRevisionsQueryResult n = new StaffDeadlineRevisionsQueryResult();
				n.setPerson(person);
				n.setDeadlineRevision(deadlineRevision);
				
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


	public Collection<StaffMarkingAssignmentQueryResult> performStaffMarkingAssignmentsQuery(
			StaffMarkingAssignmentsQuerySortingType sortingType, Long assignmentId)
			throws DAOException {
		//  Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case STUDENT_ID_ASCENDING:
			orderingString = "ORDER BY student.uniq ASC";
			break;
		case STUDENT_ID_DESCENDING:
			orderingString = "ORDER BY student.uniq DESC";
			break;
		case MARKER_ID_ASCENDING:
			orderingString = "ORDER BY marker.uniq ASC";
			break;
		case MARKER_ID_DESCENDING:
			orderingString = "ORDER BY marker.uniq DESC";
			break;
		case BLIND_MARKING_ASCENDING:
			orderingString = "ORDER BY markingassignment.blind ASC";
			break;
		case BLIND_MARKING_DESCENDING:
			orderingString = "ORDER BY markingassignment.blind DESC";
			break;
		case MODERATING_ASCENDING:
			orderingString = "ORDER BY markingassignment.moderator ASC";
			break;
		case MODERATING_DESCENDING:
			orderingString = "ORDER BY markingassignment.moderator DESC";
			break;
		case NONE:
			orderingString = "";
		}
		
		try {
			// Construct the statement.
			String statementString = "SELECT markingassignment.*, marker.*, student.*, assignment.*"
				+ " FROM markingassignment, person AS marker, person AS student, assignment"
				+ " WHERE markingassignment.assignment_id=?"
				+ "   AND student.id=markingassignment.student_id"
				+ "   AND marker.id=markingassignment.marker_id"
				+ "   AND assignment.id=markingassignment.assignment_id"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, assignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StaffMarkingAssignmentQueryResult> result = new LinkedList<StaffMarkingAssignmentQueryResult>();

			MySQLMarkingAssignmentDAO markingAssignmentDAO = new MySQLMarkingAssignmentDAO(connection);
			MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(connection);
			MySQLPersonDAO personDAO = new MySQLPersonDAO(connection);
			
			while (rs.next()) {			
				MarkingAssignment markingAssignment = markingAssignmentDAO.createInstanceFromDatabaseValues("markingassignment", rs);
				markingAssignment.setId(rs.getLong("markingassignment.id"));
				
				Assignment assignment = assignmentDAO.createInstanceFromDatabaseValues("assignment", rs);
				assignment.setId(rs.getLong("assignment.id"));
				
				Person marker = personDAO.createInstanceFromDatabaseValues("marker", rs);
				marker.setId(rs.getLong("marker.id"));
			
				Person student = personDAO.createInstanceFromDatabaseValues("student", rs);
				student.setId(rs.getLong("student.id"));
			
				StaffMarkingAssignmentQueryResult n = new StaffMarkingAssignmentQueryResult();
				n.setMarkingAssignment(markingAssignment);
				n.setMarker(marker);
				n.setStudent(student);
				n.setAssignment(assignment);
				
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

	public Mark fetchLatestMarkForStudent(Long markingCategoryId, Long studentId)
			throws DAOException {
		try {
			// Construct the statement.
			String statementString = "SELECT mark.*"
				+ " FROM markingassignment, mark"
				+ " WHERE mark.markingassignment_id=markingassignment.id"
				+ "   AND mark.markingcategory_id=?"
				+ "   AND markingassignment.student_id=?"
				+ " ORDER BY mark.timestamp DESC"
				+ " LIMIT 1";
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, markingCategoryId);
			statementObject.setObject(2, studentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();
			
			// Return result
			if (!rs.first()) {
				rs.close();
				statementObject.close();
				return null;
			}
			
			Mark mark = new MySQLMarkDAO(connection).createInstanceFromDatabaseValues("mark", rs);
			rs.close();
			statementObject.close();
			return mark;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}

	}

	public Collection<StaffResultsQueryResult> performStaffResultsQuery(
			StaffResultsQuerySortingType sortingType, Long assignmentId)
			throws DAOException {
		//  Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case STUDENT_ID_ASCENDING:
			orderingString = "ORDER BY student.uniq ASC";
			break;
		case STUDENT_ID_DESCENDING:
			orderingString = "ORDER BY student.uniq DESC";
			break;
		case INCOMPLETE_MARKS_ASCENDING:
			orderingString = "ORDER BY result.incomplete_marking ASC";
			break;
		case INCOMPLETE_MARKS_DESCENDING:
			orderingString = "ORDER BY result.incomplete_marking DESC";
			break;
		case RESULT_ASCENDING:
			orderingString = "ORDER BY result.result ASC";
			break;
		case RESULT_DESCENDING:
			orderingString = "ORDER BY result.result DESC";
			break;
		case TIMESTAMP_ASCENDING:
			orderingString = "ORDER BY result.timestamp ASC";
			break;
		case TIMESTAMP_DESCENDING:
			orderingString = "ORDER BY result.timestamp DESC";
			break;
		case NONE:
			orderingString = "";
		}
		
		try {
			// Construct the statement.
			String statementString = "SELECT assignment.*, student.*, result.*"
				+ " FROM result, person AS student, assignment"
				+ " WHERE result.assignment_id=?"
				+ "   AND assignment.id = result.assignment_id"
				+ "   AND student.id = result.student_id"
				+ " " + orderingString;
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, assignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StaffResultsQueryResult> output = new LinkedList<StaffResultsQueryResult>();

			MySQLResultDAO resultDao = new MySQLResultDAO(connection);
			MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(connection);
			MySQLPersonDAO personDAO = new MySQLPersonDAO(connection);
			
			while (rs.next()) {			
				Result result = resultDao.createInstanceFromDatabaseValues("result", rs);
				result.setId(rs.getLong("result.id"));
				
				Assignment assignment = assignmentDAO.createInstanceFromDatabaseValues("assignment", rs);
				assignment.setId(rs.getLong("assignment.id"));
							
				Person student = personDAO.createInstanceFromDatabaseValues("student", rs);
				student.setId(rs.getLong("student.id"));
			
				StaffResultsQueryResult n = new StaffResultsQueryResult();
				n.setAssignment(assignment);
				n.setResult(result);
				n.setStudent(student);
				
				output.add(n);
			}

			rs.close();
			statementObject.close();
			
			// Done		
			return output;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}	
	
	public boolean isStaffModuleAccessAllowed(Long staffId, Long moduleId)
			throws DAOException {
		// Check if the student is registered.
		try {
			PreparedStatement check = connection.prepareStatement(
					"SELECT module_administrators.module_id FROM module_administrators"
					+ " WHERE module_id=? AND administrator_id=?");
			check.setLong(1, moduleId);
			check.setLong(2, staffId);

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

	public Collection<SherlockSession> performStaffSherlockSessionsQuery(
			Long assignmentId) throws DAOException {
		try {
			// Construct the statement.
			String statementString = "SELECT sherlocksession.*"
				+ " FROM sherlocksession"
				+ " WHERE sherlocksession.assignment_id=?";
			PreparedStatement statementObject = connection.prepareStatement(statementString);
			statementObject.setObject(1, assignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();
			MySQLSherlockSessionDAO sherlockSessionDAO = new MySQLSherlockSessionDAO(connection);
			// Bundle the results into a thingy
			LinkedList<SherlockSession> output = new LinkedList<SherlockSession>();

			while (rs.next()) {			
				SherlockSession sherlockSession = sherlockSessionDAO.createInstanceFromDatabaseValues("sherlocksession", rs);
				sherlockSession.setId(rs.getLong("sherlocksession.id"));
				output.add(sherlockSession);
			}

			rs.close();
			statementObject.close();
			
			// Done		
			return output;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}

}
