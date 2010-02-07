package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentAssignmentsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentModulesQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentSubmissionCountsQueryResult;
import uk.ac.warwick.dcs.boss.model.dao.beans.queries.StudentSubmissionsQueryResult;

public class MySQLStudentInterfaceQueriesDAO implements
		IStudentInterfaceQueriesDAO {

	private Connection connection;

	public MySQLStudentInterfaceQueriesDAO(Connection connection)
			throws DAOException {
		this.connection = connection;
	}

	public StudentAssignmentsQueryResult performAssignmentDetailsQuery(
			Long studentId, Long assignmentId) throws DAOException {
		// Begin the transaction
		try {
			// Construct the statement.
			String statementString = "SELECT assignment.*, module.*, MAX(submission.submission_time) submission_time, deadlinerevision.*"
					+ " FROM assignment"
					+ " INNER JOIN module ON module.id=assignment.module_id"
					+ " LEFT JOIN submission ON submission.assignment_id=assignment.id AND submission.person_id=?"
					+ " LEFT JOIN deadlinerevision ON deadlinerevision.assignment_id=assignment.id AND deadlinerevision.person_id=?"
					+ " WHERE assignment.id=?" + " GROUP BY assignment.id";
			PreparedStatement statementObject = connection
					.prepareStatement(statementString);
			statementObject.setObject(1, studentId);
			statementObject.setObject(2, studentId);
			statementObject.setObject(3, assignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();
			rs.first();

			// Bundle the results into a thingy
			MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(
					connection);
			MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);
			MySQLDeadlineRevisionDAO deadlineRevisionDAO = new MySQLDeadlineRevisionDAO(
					connection);

			Assignment assignment = assignmentDAO
					.createInstanceFromDatabaseValues("assignment", rs);
			assignment.setId(rs.getLong("assignment.id"));

			Module parentModule = moduleDAO.createInstanceFromDatabaseValues(
					"module", rs);
			parentModule.setId(rs.getLong("module.id"));

			DeadlineRevision deadlineRevision = null;

			long id = rs.getLong("deadlinerevision.id");
			if (!rs.wasNull()) {
				deadlineRevision = deadlineRevisionDAO
						.createInstanceFromDatabaseValues("deadlinerevision",
								rs);
				deadlineRevision.setId(id);
			}

			StudentAssignmentsQueryResult n = new StudentAssignmentsQueryResult();
			n.setAssignment(assignment);
			n.setParentModule(parentModule);
			n.setLastSubmissionTime(rs.getTimestamp("submission_time"));
			n.setDeadlineRevision(deadlineRevision);

			rs.close();
			statementObject.close();

			// Done
			return n;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}

	}

	public Collection<StudentAssignmentsQueryResult> performStudentAssignmentsQuery(
			AssignmentStatus status,
			IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType sortingType,
			Long studentId) throws DAOException {
		// Status where clause
		String extraWhere = null;
		switch (status) {
		case OPEN:
			extraWhere = "AND NOW() > assignment.opening AND NOW() < assignment.closing";
			break;
		case CLOSED:
			extraWhere = "AND NOW() > assignment.closing";
			break;
		case BOTH:
			extraWhere = "AND NOW() > assignment.opening";
			break;
		}

		// Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		case DEADLINE_ASCENDING:
			orderingString = "ORDER BY assignment.deadline ASC";
			break;
		case DEADLINE_DESCENDING:
			orderingString = "ORDER BY assignment.deadline DESC";
			break;
		case CLOSING_TIME_ASCENDING:
			orderingString = "ORDER BY assignment.closing DESC";
			break;
		case CLOSING_TIME_DESCENDING:
			orderingString = "ORDER BY assignment.closing ASC";
			break;
		case LAST_SUBMITTED_ASCENDING:
			orderingString = "ORDER BY submission.submission_time ASC";
			break;
		case LAST_SUBMITTED_DESCENDING:
			orderingString = "ORDER BY submission.submission_time DESC";
			break;
		}

		try {
			// Construct the statement.
			String statementString = "SELECT assignment.*, module.*, MAX(submission.submission_time) submission_time, deadlinerevision.*"
					+ " FROM module_students"
					+ " INNER JOIN module ON module.id=module_students.module_id"
					+ " INNER JOIN assignment ON assignment.module_id=module_students.module_id"
					+ " LEFT JOIN submission ON submission.assignment_id=assignment.id AND submission.person_id=module_students.student_id"
					+ " LEFT JOIN deadlinerevision ON deadlinerevision.assignment_id=assignment.id AND deadlinerevision.person_id=module_students.student_id"
					+ " WHERE module_students.student_id=?"
					+ " "
					+ extraWhere
					+ " GROUP BY assignment.id" + " " + orderingString;
			PreparedStatement statementObject = connection
					.prepareStatement(statementString);
			statementObject.setObject(1, studentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StudentAssignmentsQueryResult> result = new LinkedList<StudentAssignmentsQueryResult>();

			MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(
					connection);
			MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);
			MySQLDeadlineRevisionDAO deadlineRevisionDAO = new MySQLDeadlineRevisionDAO(
					connection);

			while (rs.next()) {
				Assignment assignment = assignmentDAO
						.createInstanceFromDatabaseValues("assignment", rs);
				assignment.setId(rs.getLong("assignment.id"));

				Module parentModule = moduleDAO
						.createInstanceFromDatabaseValues("module", rs);
				parentModule.setId(rs.getLong("module.id"));

				DeadlineRevision deadlineRevision = null;

				long id = rs.getLong("deadlinerevision.id");
				if (!rs.wasNull()) {
					deadlineRevision = deadlineRevisionDAO
							.createInstanceFromDatabaseValues(
									"deadlinerevision", rs);
					deadlineRevision.setId(id);
				}

				StudentAssignmentsQueryResult n = new StudentAssignmentsQueryResult();
				n.setAssignment(assignment);
				n.setParentModule(parentModule);

				Timestamp timestamp = rs.getTimestamp("submission_time");
				if (timestamp == null) {
					n.setLastSubmissionTime(null);
				} else {
					n.setLastSubmissionTime(new Date(rs.getTimestamp(
							"submission_time").getTime()));
				}

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

	public Collection<StudentAssignmentsQueryResult> performStudentAssignmentsQuery(
			IStudentInterfaceQueriesDAO.StudentAssignmentsQuerySortingType sortingType,
			Long studentId, Long moduleId) throws DAOException {

		// Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		case DEADLINE_ASCENDING:
			orderingString = "ORDER BY assignment.deadline ASC";
			break;
		case DEADLINE_DESCENDING:
			orderingString = "ORDER BY assignment.deadline DESC";
			break;
		case CLOSING_TIME_ASCENDING:
			orderingString = "ORDER BY assignment.closing DESC";
			break;
		case CLOSING_TIME_DESCENDING:
			orderingString = "ORDER BY assignment.closing ASC";
			break;
		case LAST_SUBMITTED_ASCENDING:
			orderingString = "ORDER BY submission.submission_time ASC";
			break;
		case LAST_SUBMITTED_DESCENDING:
			orderingString = "ORDER BY submission.submission_time DESC";
			break;
		}

		try {
			// Construct the statement.
			String statementString = "SELECT assignment.*, module.*, MAX(submission.submission_time) submission_time, deadlinerevision.*"
					+ " FROM assignment"
					+ " INNER JOIN module ON assignment.module_id=module.id"
					+ " LEFT JOIN submission ON submission.assignment_id=assignment.id AND submission.person_id=?"
					+ " LEFT JOIN deadlinerevision ON deadlinerevision.assignment_id=assignment.id AND deadlinerevision.person_id=submission.person_id"
					+ " WHERE assignment.module_id=?"
					+ " AND NOW() > assignment.opening"
					+ " GROUP BY assignment.id" + " " + orderingString;
			PreparedStatement statementObject = connection
					.prepareStatement(statementString);
			statementObject.setObject(1, studentId);
			statementObject.setObject(2, moduleId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StudentAssignmentsQueryResult> result = new LinkedList<StudentAssignmentsQueryResult>();

			MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(
					connection);
			MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);
			MySQLDeadlineRevisionDAO deadlineRevisionDAO = new MySQLDeadlineRevisionDAO(
					connection);

			while (rs.next()) {
				Assignment assignment = assignmentDAO
						.createInstanceFromDatabaseValues("assignment", rs);
				assignment.setId(rs.getLong("assignment.id"));

				Module parentModule = moduleDAO
						.createInstanceFromDatabaseValues("module", rs);
				parentModule.setId(rs.getLong("module.id"));

				DeadlineRevision deadlineRevision = null;

				long id = rs.getLong("deadlinerevision.id");
				if (!rs.wasNull()) {
					deadlineRevision = deadlineRevisionDAO
							.createInstanceFromDatabaseValues(
									"deadlinerevision", rs);
					deadlineRevision.setId(id);
				}

				StudentAssignmentsQueryResult n = new StudentAssignmentsQueryResult();
				n.setAssignment(assignment);
				n.setParentModule(parentModule);

				Timestamp timestamp = rs.getTimestamp("submission_time");
				if (timestamp == null) {
					n.setLastSubmissionTime(null);
				} else {
					n.setLastSubmissionTime(new Date(rs.getTimestamp(
							"submission_time").getTime()));
				}

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

	public Collection<StudentModulesQueryResult> performStudentModulesQuery(
			StudentModulesQuerySortingType sortingType, Long studentId)
			throws DAOException {
		// Begin the transaction
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
		case MODEL_ID_ASCENDING:
			orderingString = "ORDER BY model.id ASC";
			break;
		case MODEL_ID_DESCENDING:
			orderingString = "ORDER BY model.id DESC";
			break;
		}

		try {
			// Construct the statement.
			String statementString = "SELECT model.*, module.*"
					+ " FROM module"
					+ "   INNER JOIN model ON model.id = module.model_id"
					+ " WHERE module.id IN ("
					+ "   SELECT id FROM module WHERE registration_required = 0"
					+ "     UNION"
					+ "   SELECT module_id id FROM module_students WHERE student_id = ?"
					+ " ) GROUP BY module.id" + " " + orderingString;
			PreparedStatement statementObject = connection
					.prepareStatement(statementString);
			statementObject.setObject(1, studentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StudentModulesQueryResult> result = new LinkedList<StudentModulesQueryResult>();

			MySQLModelDAO modelDAO = new MySQLModelDAO(connection);
			MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);

			while (rs.next()) {
				Model model = modelDAO.createInstanceFromDatabaseValues(
						"model", rs);
				model.setId(rs.getLong("model.id"));

				Module module = moduleDAO.createInstanceFromDatabaseValues(
						"module", rs);
				module.setId(rs.getLong("module.id"));

				StudentModulesQueryResult n = new StudentModulesQueryResult();
				n.setModel(model);
				n.setModule(module);

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

	public Collection<StudentSubmissionCountsQueryResult> performStudentSubmissionCountsQuery(
			AssignmentStatus status,
			IStudentInterfaceQueriesDAO.StudentSubmissionCountsQuerySortingType sortingType,
			Long studentId) throws DAOException {

		// Status where clause
		String extraWhere = null;
		switch (status) {
		case OPEN:
			extraWhere = "WHERE NOW() > assignment.opening AND NOW() < assignment.closing";
			break;
		case CLOSED:
			extraWhere = "WHERE NOW() > assignment.closing";
			break;
		case BOTH:
			extraWhere = "WHERE NOW() > assignment.opening";
			break;
		}

		// Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		case SUBMISSION_COUNT_ASCENDING:
			orderingString = "ORDER BY n_subs ASC";
			break;
		case SUBMISSION_COUNT_DESCENDING:
			orderingString = "ORDER BY n_subs DESC";
			break;
		}

		try {
			// Construct the statement.
			String statementString = "SELECT assignment.*, module.*, COUNT(other_submissions.id) n_subs"
					+ " FROM assignment"
					+ " INNER JOIN module ON module.id = assignment.module_id"
					+ " INNER JOIN submission other_submissions ON other_submissions.assignment_id=assignment.id AND other_submissions.person_id=?"
					+ " "
					+ extraWhere
					+ " GROUP BY assignment.id"
					+ " "
					+ orderingString;
			PreparedStatement statementObject = connection
					.prepareStatement(statementString);
			statementObject.setObject(1, studentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StudentSubmissionCountsQueryResult> result = new LinkedList<StudentSubmissionCountsQueryResult>();

			MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(
					connection);
			MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);

			while (rs.next()) {
				Assignment assignment = assignmentDAO
						.createInstanceFromDatabaseValues("assignment", rs);
				assignment.setId(rs.getLong("assignment.id"));

				Module parentModule = moduleDAO
						.createInstanceFromDatabaseValues("module", rs);
				parentModule.setId(rs.getLong("module.id"));

				StudentSubmissionCountsQueryResult n = new StudentSubmissionCountsQueryResult();
				n.setAssignment(assignment);
				n.setParentModule(parentModule);
				n.setNumberOfSubmissions(rs.getLong("n_subs"));

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

	public Collection<StudentSubmissionsQueryResult> performStudentSubmissionsQuery(
			IStudentInterfaceQueriesDAO.StudentSubmissionsQuerySortingType sortingType,
			Long studentId, Long assignmentId) throws DAOException {
		// Begin the transaction
		String orderingString = null;
		switch (sortingType) {
		case NONE:
			orderingString = "";
			break;
		case ACTIVE_ASCENDING:
			orderingString = "ORDER BY submission.active ASC";
			break;
		case ACTIVE_DESCENDING:
			orderingString = "ORDER BY submission.active DESC";
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
					+ " FROM submission"
					+ " INNER JOIN assignment ON submission.assignment_id=assignment.id"
					+ " INNER JOIN module ON module.id = assignment.module_id"
					+ " WHERE submission.person_id=? AND submission.assignment_id=?"
					+ " " + orderingString;
			PreparedStatement statementObject = connection
					.prepareStatement(statementString);
			statementObject.setObject(1, studentId);
			statementObject.setObject(2, assignmentId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<StudentSubmissionsQueryResult> result = new LinkedList<StudentSubmissionsQueryResult>();

			MySQLAssignmentDAO assignmentDAO = new MySQLAssignmentDAO(
					connection);
			MySQLModuleDAO moduleDAO = new MySQLModuleDAO(connection);
			MySQLSubmissionDAO submissionDAO = new MySQLSubmissionDAO(
					connection);

			while (rs.next()) {
				Assignment assignment = assignmentDAO
						.createInstanceFromDatabaseValues("assignment", rs);
				assignment.setId(rs.getLong("assignment.id"));

				Module parentModule = moduleDAO
						.createInstanceFromDatabaseValues("module", rs);
				parentModule.setId(rs.getLong("module.id"));

				Submission submission = submissionDAO
						.createInstanceFromDatabaseValues("submission", rs);
				submission.setId(rs.getLong("submission.id"));

				StudentSubmissionsQueryResult n = new StudentSubmissionsQueryResult();
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

	public void makeSubmissionActive(Long personId, Long assignmentId,
			Long submissionId) throws DAOException {
		PreparedStatement statementObject;

		try {
			String clearString = " UPDATE submission" + " SET active=0"
					+ " WHERE person_id=?" + " AND assignment_id=?";

			statementObject = connection.prepareStatement(clearString);
			statementObject.setObject(1, personId);
			statementObject.setObject(2, assignmentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			statementObject.execute();

			statementObject.close();

			String updateString = "UPDATE submission" + " SET active=1"
					+ " WHERE person_id=?" + " AND assignment_id=?"
					+ " AND id=?";

			statementObject = connection.prepareStatement(updateString);
			statementObject.setObject(1, personId);
			statementObject.setObject(2, assignmentId);
			statementObject.setObject(3, submissionId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			statementObject.execute();

			statementObject.close();

		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}

	public Collection<Test> fetchSubmissionTests(Long submissionId)
			throws DAOException {
		// Begin the transaction
		try {
			// Construct the statement.
			String statementString = "SELECT test.*" + " FROM test, submission"
					+ " WHERE test.student_test=1 AND"
					+ "       test.assignment_id=submission.assignment_id AND"
					+ "       submission.id=?";

			PreparedStatement statementObject = connection
					.prepareStatement(statementString);
			statementObject.setObject(1, submissionId);

			// Execute the statement.
			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + statementObject.toString());
			ResultSet rs = statementObject.executeQuery();

			// Bundle the results into a thingy
			LinkedList<Test> result = new LinkedList<Test>();

			MySQLTestDAO testDao = new MySQLTestDAO(connection);
			while (rs.next()) {
				Test t = testDao.createInstanceFromDatabaseValues(
						MySQLTestDAO.TABLE_NAME, rs);
				t.setId(rs.getLong("test.id"));

				result.add(t);
			}

			rs.close();
			statementObject.close();

			// Done
			return result;
		} catch (SQLException e) {
			throw new DAOException("SQL error", e);
		}
	}

	public boolean isStudentSubmissionAccessAllowed(Long personId,
			Long submissionId) throws DAOException {
		// Check if the student one of the submitters
		try {
			PreparedStatement check = connection
					.prepareStatement("SELECT id FROM submission"
							+ " WHERE id=? AND person_id=?");
			check.setLong(1, submissionId);
			check.setLong(2, personId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + check);
			boolean result = check.executeQuery().first();
			check.close();
			return result;
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}
	}

	public boolean isStudentAllowedToSubmit(Long studentId, Long assignmentId)
			throws DAOException {
		
		// Check if the student is registered.
		try {
			// Is the module open to all?
			PreparedStatement check = connection
					.prepareStatement("SELECT module.registration_required FROM module, assignment"
							+ " WHERE assignment.id=? AND module.id=assignment.module_id");
			check.setLong(1, assignmentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + check);
			ResultSet rs = check.executeQuery();
			if(!rs.first()) {
				rs.close();
				check.close();
				return false;
			}
			
			boolean result = rs.getBoolean(1);
			rs.close();
			check.close();
			if (result) {
				// Otherwise check registration
				check = connection
						.prepareStatement("SELECT module_students.module_id FROM module_students, assignment"
								+ " WHERE assignment.id=?"
								+ "  AND module_students.module_id=assignment.module_id"
								+ "  AND module_students.student_id=?");
				check.setLong(1, assignmentId);
				check.setLong(2, studentId);
	
				Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + check);
				rs = check.executeQuery();
				result = rs.first();
				rs.close();
				check.close();
	
				if (!result) {
					return false;
				}
			}
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}

		// Check if the student is within time bound
		try {
			PreparedStatement check = connection
					.prepareStatement("SELECT assignment.opening a_o, assignment.closing a_c, MAX(deadlinerevision.deadline) d_r FROM module_students, assignment"
							+ " LEFT JOIN deadlinerevision ON (deadlinerevision.assignment_id=assignment.id AND deadlinerevision.person_id=?)"
							+ " WHERE assignment.id=?"
							+ " GROUP BY assignment.id");
			check.setLong(1, studentId);
			check.setLong(2, assignmentId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + check);
			ResultSet rs = check.executeQuery();
			if (!rs.first()) {
				rs.close();
				check.close();
				return false;
			}

			Date openingTime = rs.getTimestamp("a_o");
			if (new Date().before(openingTime)) {
				rs.close();
				check.close();
				return false;
			}

			Date closingTime = rs.getTimestamp("d_r");
			if (rs.wasNull()) {
				closingTime = rs.getTimestamp("a_c");
			}

			rs.close();
			check.close();
			if (new Date().after(closingTime)) {
				return false;
			} else {
				return true;
			} 
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}

	}

	public boolean isStudentModuleAccessAllowed(Long personId, Long moduleId)
			throws DAOException {
		// Check if the student is registered.
		try {
			// Is the module open to all?
			PreparedStatement check = connection
					.prepareStatement("SELECT module.id FROM module"
							+ " WHERE id=? AND registration_required=0");
			check.setLong(1, moduleId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + check);
			ResultSet rs = check.executeQuery();
			if (rs.first()) {
				rs.close();
				check.close();
				return true;
			}

			rs.close();
			check.close();

			// Otherwise check registration
			check = connection
					.prepareStatement("SELECT module_students.module_id FROM module_students"
							+ " WHERE module_id=? AND student_id=?");
			check.setLong(1, moduleId);
			check.setLong(2, personId);

			Logger.getLogger("mysql").log(Level.TRACE, "Executing: " + check);
			rs = check.executeQuery();
			boolean result = rs.first();
			rs.close();
			check.close();
			return result;
		} catch (SQLException e) {
			throw new DAOException("sql error", e);
		}

	}

}
