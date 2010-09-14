package uk.ac.warwick.dcs.boss.model.junit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import uk.ac.warwick.dcs.boss.model.ModelViolationException;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.DeadlineRevision;
import uk.ac.warwick.dcs.boss.model.dao.beans.MarkingAssignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;

public class TestRigging {
	
	public static void constructRigging(
			IDAOSession f,
			int numberOfStudents,
			int numberOfStaff,
			int numberOfModels,
			int maxModulesPerModel,
			int maxAssignmentsPerModule,
			int maxStaffPerModule,
			int maxStudentsPerModule,
			int maxMarkersPerAssignment
	) throws DAOException, ModelViolationException {
		f.beginTransaction();
		f.initialiseStorage(true);
		
		// Create students
		ArrayList<Person> students = new ArrayList<Person>();
		for (int studentNumber = 0; studentNumber < numberOfStudents; studentNumber++) {
			Person person = new Person();
			person.setUniqueIdentifier("student" + studentNumber);
			person.setPassword(Person.passwordHash("student" + studentNumber));
			person.setEmailAddress("student" + studentNumber + "@institution.example");
			person.setChosenName("Student Number " + studentNumber);
			person.setAdministrator(false);
			person.setId(f.getPersonDAOInstance().createPersistentCopy(person));
			students.add(person);
		}
		
		// Create staff
		ArrayList<Person> staff = new ArrayList<Person>();
		for (int staffNumber = 0; staffNumber < numberOfStaff; staffNumber++) {
			Person person = new Person();
			person.setUniqueIdentifier("staff" + staffNumber);
			person.setPassword(Person.passwordHash("staff" + staffNumber));
			person.setEmailAddress("staff" + staffNumber + "@institution.example");
			person.setChosenName("Staff Number " + staffNumber);
			person.setAdministrator(staffNumber == 0);
			person.setId(f.getPersonDAOInstance().createPersistentCopy(person));
			staff.add(person);
		}
		
		// Create models
		Random random = new Random(new Date().getTime());
		for (int modelNumber = 0; modelNumber < 5; modelNumber++) {
			Model model = new Model();
			model.setName("Model " + modelNumber);
			model.setUniqueIdentifier("model" + modelNumber);
			long modelId = f.getModelDAOInstance().createPersistentCopy(model);
			
			// Create modules
			for (int moduleNumber = 0; moduleNumber < random.nextInt(numberOfModels) + 1; moduleNumber++) {
				Module module = new Module();
				module.setModelId(modelId);
				module.setName("Module " + moduleNumber);
				module.setUniqueIdentifier("module" + modelNumber + "-" + moduleNumber);
				module.setRegistrationRequired(moduleNumber % 2 == 0);
				long moduleId = f.getModuleDAOInstance().createPersistentCopy(module);
				
				// Set staff
				Collections.shuffle(staff, random);
				ArrayList<Person> moduleStaff = new ArrayList<Person>();
				for (int staffNumber = 0; staffNumber < random.nextInt(maxStaffPerModule) + 1; staffNumber++) {
					f.getModuleDAOInstance().addAdministratorAssociation(moduleId, staff.get(staffNumber).getId());
					moduleStaff.add(staff.get(staffNumber));
				}

				// Set students
				Collections.shuffle(students, random);
				ArrayList<Person> moduleStudents = new ArrayList<Person>();
				for (int studentNumber = 0; studentNumber < random.nextInt(maxStudentsPerModule) + 1; studentNumber++) {
					f.getModuleDAOInstance().addStudentAssociation(moduleId, students.get(studentNumber).getId());
					moduleStudents.add(students.get(studentNumber));
				}
				
				// Create assignments
				for (int assignmentNumber = 0; assignmentNumber < random.nextInt(maxAssignmentsPerModule) + 1; assignmentNumber++) {
					// Create resource
					Resource resource = new Resource();
					resource.setFilename("assignment" + modelNumber + "-" + moduleNumber + "-" + assignmentNumber + ".resource");
					resource.setMimeType("text/plain");
					resource.setTimestamp(new Date());
					long resourceId = f.getResourceDAOInstance().createPersistentCopy(resource);
					
					// Create assignment
					Assignment assignment = new Assignment();
					assignment.setName("Assignment " + assignmentNumber);
					assignment.setAllowDeletion(assignmentNumber % 2 == 0);
					
					Calendar deadline = Calendar.getInstance();				
					deadline.add(Calendar.WEEK_OF_YEAR, (assignmentNumber % 2 == 0 ? -random.nextInt(3) : random.nextInt(3)));
					deadline.add(Calendar.DAY_OF_WEEK, random.nextInt(7));
					deadline.add(Calendar.SECOND, random.nextInt(86400) - 43200);
					assignment.setDeadline(deadline.getTime());
					
					Calendar opening = (Calendar)deadline.clone();
					opening.add(Calendar.WEEK_OF_YEAR, -random.nextInt(3));
					opening.add(Calendar.DAY_OF_WEEK, -random.nextInt(7));
					opening.add(Calendar.SECOND, -random.nextInt(86400));
					assignment.setOpeningTime(opening.getTime());
					
					Calendar closing = (Calendar)deadline.clone();
					closing.add(Calendar.WEEK_OF_YEAR, random.nextInt(3));
					closing.add(Calendar.DAY_OF_WEEK, random.nextInt(7));
					closing.add(Calendar.SECOND, random.nextInt(86400));
					assignment.setClosingTime(closing.getTime());
					
					assignment.setModuleId(moduleId);
					assignment.setResourceId(resourceId);
					long assignmentId = f.getAssignmentDAOInstance().createPersistentCopy(assignment);
					
					// Create assignment required files;
					for (int fileNumber = 0; fileNumber < random.nextInt(3) + 1; fileNumber++) {
						f.getAssignmentDAOInstance().addRequiredFilename(assignmentId, "file" + fileNumber + ".data");
					}
					
					// Create markers
					Collections.shuffle(staff, random);
					ArrayList<Person> assignmentMarkers = new ArrayList<Person>();
					for (int markerNumber = 0; markerNumber < random.nextInt(maxMarkersPerAssignment) + 1; markerNumber++) {
						f.getAssignmentDAOInstance().addMarkerAssociation(assignmentId, staff.get(markerNumber).getId());
						assignmentMarkers.add(staff.get(markerNumber));
					}
					
					// Create deadline revisions
					for (Person student : moduleStudents) {
						if (random.nextInt(2) == 0) {
							DeadlineRevision deadlineRevision = new DeadlineRevision();
							deadlineRevision.setAssignmentId(assignmentId);
							deadlineRevision.setComment("Deadline extended");
							Calendar extended = (Calendar)deadline.clone();
							extended.add(Calendar.SECOND, random.nextInt(86400));
							deadlineRevision.setNewDeadline(extended.getTime());
							deadlineRevision.setPersonId(student.getId());
							
							long deadlineRevisionId = f.getDeadlineRevisionDAOInstance().createPersistentCopy(deadlineRevision);
						}
					}
					
					// Create marking assignments
					for (Person marker : assignmentMarkers) {
						if (random.nextInt(2) == 0) {
							MarkingAssignment markingAssignment = new MarkingAssignment();
							markingAssignment.setAssignmentId(assignmentId);
							markingAssignment.setBlind(random.nextInt(2) == 0);
							markingAssignment.setMarkerId(marker.getId());
							markingAssignment.setModerator(random.nextInt(2) == 0);
							markingAssignment.setStudentId(moduleStudents.get(random.nextInt(moduleStudents.size())).getId());
							
							long markingAssignmentId = f.getMarkingAssignmentDAOInstance().createPersistentCopy(markingAssignment);
						}
					}
				}
			}
			
		}
		
		// Done
		f.endTransaction();
	}	
}
