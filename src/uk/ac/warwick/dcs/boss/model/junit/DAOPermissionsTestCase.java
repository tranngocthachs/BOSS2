package uk.ac.warwick.dcs.boss.model.junit;

import java.util.Collection;
import java.util.Date;

import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IDeadlineRevisionDAO;
import uk.ac.warwick.dcs.boss.model.dao.IMarkerInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IMarkingAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStaffInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.IStudentInterfaceQueriesDAO;
import uk.ac.warwick.dcs.boss.model.dao.ISubmissionDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import junit.framework.TestCase;

public class DAOPermissionsTestCase extends TestCase {

	IDAOSession f;

	final int numberOfStudents = 25;
	final int numberOfStaff = 25;
	final int numberOfModels = 5;
	final int maxModulesPerModel = 20;
	final int maxAssignmentsPerModule = 5;
	final int maxStaffPerModule = 5;
	final int maxStudentsPerModule = 20;
	final int maxMarkersPerAssignment = 5;

	protected void setUp() throws Exception {
		super.setUp();
		DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
		f = df.getInstance();
		TestRigging.constructRigging(f, numberOfStudents, numberOfStaff,
				numberOfModels, maxModulesPerModel, maxAssignmentsPerModule,
				maxStaffPerModule, maxStudentsPerModule,
				maxMarkersPerAssignment);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		f.abortTransaction();
	}

	public void testMarkerInterfacePermissions() throws DAOException {
		f.beginTransaction();
		
		IMarkerInterfaceQueriesDAO markerInterfaceQueriesDao = f.getMarkerInterfaceQueriesDAOInstance();
		IPersonDAO personDao = f.getPersonDAOInstance();
		IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
		IMarkingAssignmentDAO markingAssignmentDao = f.getMarkingAssignmentDAOInstance();
		ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();

		for (Person person : personDao.retrieveAllPersistentEntities()) {
			// Assignment access
			for (Assignment assignment : assignmentDao.retrieveAllPersistentEntities()) {
				MarkingAssignment example = new MarkingAssignment();
				example.setAssignmentId(assignment.getId());
				example.setMarkerId(person.getId());
				Collection<MarkingAssignment> markingAssignments = markingAssignmentDao.findPersistentEntitiesByExample(example);
				
				if (markingAssignments.size() == 0) {
					assertEquals(false, markerInterfaceQueriesDao.isMarkerAssignmentAccessAllowed(person.getId(), assignment.getId()));
				} else {
					assertEquals(true, markerInterfaceQueriesDao.isMarkerAssignmentAccessAllowed(person.getId(), assignment.getId()));
				}	
			}
			
			// Submission access
			for (Submission submission : submissionDao.retrieveAllPersistentEntities()) {
				MarkingAssignment example = new MarkingAssignment();
				example.setAssignmentId(submission.getAssignmentId());
				example.setMarkerId(person.getId());
				Collection<MarkingAssignment> markingAssignments = markingAssignmentDao.findPersistentEntitiesByExample(example);
				
				if (markingAssignments.size() == 0) {
					assertEquals(false, markerInterfaceQueriesDao.isMarkerSubmissionAccessAllowed(person.getId(), submission.getId()));
				} else {
					assertEquals(true, markerInterfaceQueriesDao.isMarkerSubmissionAccessAllowed(person.getId(), submission.getId()));
				}	

			}
			
			// Marking Assignment access
			for (MarkingAssignment markingAssignment : markingAssignmentDao.retrieveAllPersistentEntities()) {
				boolean expected = markingAssignment.getMarkerId().equals(person.getId());
				assertEquals(expected, markerInterfaceQueriesDao.isMarkerMarkingAssignmentAccessAllowed(person.getId(), markingAssignment.getId()));
			}
		}
		
		f.abortTransaction();
	}
	
	public void testStaffInterfacePermissions() throws DAOException {
		f.beginTransaction();
		
		IStaffInterfaceQueriesDAO staffInterfaceQueriesDao = f.getStaffInterfaceQueriesDAOInstance();
		IPersonDAO personDao = f.getPersonDAOInstance();
		IModuleDAO moduleDao = f.getModuleDAOInstance();
		
		for (Person person : personDao.retrieveAllPersistentEntities()) {
			// Module access
			for (Module module : moduleDao.retrieveAllPersistentEntities()) {
				boolean expectedPermission = false;

				Collection<Person> administrators = moduleDao.fetchAdministrators(
						IPersonDAO.SortingType.ID_ASC, module.getId());
				for (Person administrator : administrators) {
					if (administrator.getId().equals(person.getId())) {
						expectedPermission = true;
						break;
					}
				}
				assertEquals(expectedPermission, staffInterfaceQueriesDao
						.isStaffModuleAccessAllowed(person.getId(), module
								.getId()));
			}
		}

	}
	
	public void testStudentInterfacePermissions() throws DAOException {
		f.beginTransaction();

		IAssignmentDAO assignmentDao = f.getAssignmentDAOInstance();
		IPersonDAO personDao = f.getPersonDAOInstance();
		IModuleDAO moduleDao = f.getModuleDAOInstance();
		ISubmissionDAO submissionDao = f.getSubmissionDAOInstance();
		IDeadlineRevisionDAO deadlineRevisionDao = f
				.getDeadlineRevisionDAOInstance();

		IStudentInterfaceQueriesDAO studentInterfaceQueriesDao = f
				.getStudentInterfaceQueriesDAOInstance();

		for (Person person : personDao.retrieveAllPersistentEntities()) {
			// Module access
			for (Module module : moduleDao.retrieveAllPersistentEntities()) {
				boolean expectedPermission = false;

				if (module.isRegistrationRequired()) {
					Collection<Person> students = moduleDao.fetchStudents(
							IPersonDAO.SortingType.ID_ASC, module.getId());
					for (Person student : students) {
						if (student.getId().equals(person.getId())) {
							expectedPermission = true;
							break;
						}
					}
				} else {
					expectedPermission = true;
				}

				assertEquals(expectedPermission, studentInterfaceQueriesDao
						.isStudentModuleAccessAllowed(person.getId(), module
								.getId()));
			}

			// Submission access
			for (Submission submission : submissionDao
					.retrieveAllPersistentEntities()) {
				assertEquals(submission.getPersonId().equals(person.getId()),
						studentInterfaceQueriesDao
								.isStudentSubmissionAccessAllowed(person
										.getId(), submission.getId()));
			}

			// Before deadline check
			for (Assignment assignment : assignmentDao
					.retrieveAllPersistentEntities()) {
				if (studentInterfaceQueriesDao.isStudentModuleAccessAllowed(person.getId(), assignment.getModuleId())) {
	
					
					DeadlineRevision example = new DeadlineRevision();
					deadlineRevisionDao
							.setSortingType(IDeadlineRevisionDAO.SortingType.DEADLINE_DESCENDING);
					example.setAssignmentId(assignment.getId());
					example.setPersonId(person.getId());
					Collection<DeadlineRevision> foundDeadlineRevisions = deadlineRevisionDao
							.findPersistentEntitiesByExample(example);
	
					if (new Date().after(assignment.getOpeningTime())) {
						if (foundDeadlineRevisions.size() > 0) {
							DeadlineRevision deadlineRevision = foundDeadlineRevisions.iterator().next();
							if (new Date().before(deadlineRevision.getNewDeadline())) {
								assertEquals(true, studentInterfaceQueriesDao
										.isStudentAllowedToSubmit(person.getId(),
												assignment.getId()));
							} else {
								assertEquals(false, studentInterfaceQueriesDao
										.isStudentAllowedToSubmit(person.getId(),
												assignment.getId()));
							}
						} else {
							if (new Date().before(assignment.getClosingTime())) {
								assertEquals(true, studentInterfaceQueriesDao
										.isStudentAllowedToSubmit(person.getId(),
												assignment.getId()));
							} else {
								assertEquals(false, studentInterfaceQueriesDao
										.isStudentAllowedToSubmit(person.getId(),
												assignment.getId()));
							}
						}
					} else {
						assertEquals(false, studentInterfaceQueriesDao
								.isStudentAllowedToSubmit(person.getId(),
										assignment.getId()));
					}
				} else {
					assertEquals(false, studentInterfaceQueriesDao
							.isStudentAllowedToSubmit(person.getId(),
									assignment.getId()));
				}
			}
		}

		f.abortTransaction();
	}
}
