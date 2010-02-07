package uk.ac.warwick.dcs.boss.model.junit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.ModelViolationException;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IAssignmentDAO;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModelDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;
import junit.framework.TestCase;

public class DAOBasicsTestCase extends TestCase {

	IDAOSession f = null;
	final int numberOfStudents = 500;
	final int numberOfStaff = 500;
	final int numberOfModels = 50;
	final int maxModulesPerModel = 40;
	final int maxAssignmentsPerModule = 20;
	final int maxMarkersPerAssignment = 20;
	
	protected void setUp() throws Exception {
		super.setUp();
		DAOFactory df = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
		f = df.getInstance();
		f.beginTransaction();
		f.initialiseStorage(true);
		f.endTransaction();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		f.abortTransaction();
	}

	public void testBasics() throws DAOException, ModelViolationException {
		f.beginTransaction();
		
		/*
		 * Create sample model
		 */
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
		for (int modelNumber = 0; modelNumber < numberOfModels; modelNumber++) {
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
					deadline.add(Calendar.WEEK_OF_YEAR, random.nextInt(3));
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
				}
			}

		}

		// Done
		f.endTransaction();

	
		// Defs
		IPersonDAO personDao;
		Person personExample;
		Collection<Person> personResult;
		
		IModelDAO modelDao;
		Collection<Model> modelResult;
		
		IModuleDAO moduleDao;
		Module moduleExample;
		Collection<Module> moduleResult;
		
		IAssignmentDAO assignmentDao;
		Assignment assignmentExample;
		Collection<Assignment> assignmentResult;
		
		IResourceDAO resourceDao;
		Resource resource;

		
		/** Read sample model to verify creation **/
		f.beginTransaction();
		personDao = f.getPersonDAOInstance();
		modelDao = f.getModelDAOInstance();
		moduleDao = f.getModuleDAOInstance();
		assignmentDao = f.getAssignmentDAOInstance();
		resourceDao = f.getResourceDAOInstance();
				
		// Fetch students by wildcard
		personDao.setSortingType(IPersonDAO.SortingType.ID_ASC);
		personExample = new Person();
		personExample.setUniqueIdentifier("student%");
		personResult = personDao.findPersistentEntitiesByWildcards(personExample);

		int studentNumber = 0;
		for (Person person : personResult) {
			assertEquals(person.getUniqueIdentifier(), "student" + studentNumber);
			assertEquals(person.getPassword(), Person.passwordHash("student" + studentNumber));
			assertEquals(person.getEmailAddress(), "student" + studentNumber + "@institution.example");
			assertEquals(person.getChosenName(), "Student Number " + studentNumber);
			assertEquals((Boolean)person.isAdministrator(), (Boolean)false);
			studentNumber++;
		}

		// Fetch staff by password
		personDao.setSortingType(IPersonDAO.SortingType.NONE);
		for (int staffNumber = 0; staffNumber < numberOfStaff; staffNumber++) {
			personExample = new Person();
			personExample.setPassword(Person.passwordHash("staff" + staffNumber));
			Person person = personDao.findPersistentEntitiesByExample(personExample).iterator().next();
			
			assertEquals(person.getUniqueIdentifier(), "staff" + staffNumber);
			assertEquals(person.getPassword(), Person.passwordHash("staff" + staffNumber));
			assertEquals(person.getEmailAddress(), "staff" + staffNumber + "@institution.example");
			assertEquals(person.getChosenName(), "Staff Number " + staffNumber);
			assertEquals((Boolean)person.isAdministrator(), (Boolean)(staffNumber == 0));
		}
		
		// Delve the tree, checking various data
		// Models
		modelDao.setSortingType(IModelDAO.SortingType.ID_ASC);
		modelResult = modelDao.retrieveAllPersistentEntities();
		int modelNumber = 0;
		for (Model model : modelResult) {
			assertEquals(model.getName(), "Model " + modelNumber);
			assertEquals(model.getUniqueIdentifier(), "model" + modelNumber);
			long modelId = model.getId();			

			// Modules
			moduleExample = new Module();
			moduleExample.setModelId(modelId);
			moduleDao.setSortingType(IModuleDAO.SortingType.ID_ASC);
			moduleResult = moduleDao.findPersistentEntitiesByExample(moduleExample);
			
			int moduleNumber = 0;
			for (Module module : moduleResult) {
				assertTrue(module.getName().startsWith("Module "));
				assertEquals(module.getName(), "Module " + moduleNumber);
				assertEquals(module.getUniqueIdentifier(), "module" + modelNumber + "-" + moduleNumber);
				assertEquals((Long)module.getModelId(), (Long)modelId);
				long moduleId = module.getId();
				
				// Assignments
				assignmentExample = new Assignment();
				assignmentExample.setModuleId(moduleId);
				assignmentDao.setSortingType(IAssignmentDAO.SortingType.NONE);
				assignmentResult = assignmentDao.findPersistentEntitiesByExample(assignmentExample);
				
				for (Assignment assignment : assignmentResult) {
					assertTrue(assignment.getClosingTime().after(assignment.getDeadline()));
					assertTrue(assignment.getDeadline().after(assignment.getOpeningTime()));
					assertTrue(assignment.getName().startsWith("Assignment "));
					long assignmentId = assignment.getId();
					
					// Resource
					int assignmentNumber = Integer.valueOf(assignment.getName().substring("Assignment ".length()));
					resource = resourceDao.retrievePersistentEntity(assignment.getResourceId());
					assertEquals(resource.getFilename(), "assignment" + modelNumber + "-" + moduleNumber + "-" + assignmentNumber + ".resource");
				}
				
				moduleNumber++;
			}
			
			modelNumber++;
		}
		
		f.endTransaction();
		
		/** Update sample model by setting all person names upper case **/
		f.beginTransaction();
		personDao = f.getPersonDAOInstance();
		modelDao = f.getModelDAOInstance();
		moduleDao = f.getModuleDAOInstance();
		assignmentDao = f.getAssignmentDAOInstance();
		resourceDao = f.getResourceDAOInstance();
		
		// Fetch students by wildcard
		personDao.setSortingType(IPersonDAO.SortingType.UNIQUE_IDENTIFIER_ASCENDING);
		personExample = new Person();
		personExample.setUniqueIdentifier("student%");
		personResult = personDao.findPersistentEntitiesByWildcards(personExample);

		studentNumber = 0;
		for (Person person : personResult) {
			person.setChosenName(person.getChosenName().toUpperCase());
			personDao.updatePersistentEntity(person);
			studentNumber++;
		}

		// Fetch staff by password
		personDao.setSortingType(IPersonDAO.SortingType.NONE);
		for (int staffNumber = 0; staffNumber < numberOfStaff; staffNumber++) {
			personExample = new Person();
			personExample.setPassword(Person.passwordHash("staff" + staffNumber));
			Person person = personDao.findPersistentEntitiesByExample(personExample).iterator().next();
			person.setChosenName(person.getChosenName().toUpperCase());
			personDao.updatePersistentEntity(person);
		}

		f.endTransaction();
		
		/** Read sample model to verify update **/
		f.beginTransaction();
		personDao = f.getPersonDAOInstance();
		modelDao = f.getModelDAOInstance();
		moduleDao = f.getModuleDAOInstance();
		assignmentDao = f.getAssignmentDAOInstance();
		resourceDao = f.getResourceDAOInstance();
		
		personDao.setSortingType(IPersonDAO.SortingType.ID_ASC);
		personExample = new Person();
		personExample.setUniqueIdentifier("student%");
		personResult = personDao.findPersistentEntitiesByWildcards(personExample);

		studentNumber = 0;
		for (Person person : personResult) {
			assertEquals(person.getUniqueIdentifier(), "student" + studentNumber);
			assertEquals(person.getPassword(), Person.passwordHash("student" + studentNumber));
			assertEquals(person.getEmailAddress(), "student" + studentNumber + "@institution.example");
			assertEquals(person.getChosenName(), "STUDENT NUMBER " + studentNumber);
			assertEquals((Boolean)person.isAdministrator(), (Boolean)false);
			studentNumber++;
		}

		// Fetch staff by password
		personDao.setSortingType(IPersonDAO.SortingType.NONE);
		for (int staffNumber = 0; staffNumber < numberOfStaff; staffNumber++) {
			personExample = new Person();
			personExample.setPassword(Person.passwordHash("staff" + staffNumber));
			Person person = personDao.findPersistentEntitiesByExample(personExample).iterator().next();
			
			assertEquals(person.getUniqueIdentifier(), "staff" + staffNumber);
			assertEquals(person.getPassword(), Person.passwordHash("staff" + staffNumber));
			assertEquals(person.getEmailAddress(), "staff" + staffNumber + "@institution.example");
			assertEquals(person.getChosenName(), "STAFF NUMBER " + staffNumber);
			assertEquals((Boolean)person.isAdministrator(), (Boolean)(staffNumber == 0));
		}
		
		f.endTransaction();
		
		/** Delete test **/
		f.beginTransaction();
		personDao = f.getPersonDAOInstance();
		modelDao = f.getModelDAOInstance();
		moduleDao = f.getModuleDAOInstance();
		assignmentDao = f.getAssignmentDAOInstance();
		resourceDao = f.getResourceDAOInstance();
		
		// Delete assignments and resources
		assignmentResult = assignmentDao.retrieveAllPersistentEntities();
		for (Assignment assignmentToDelete : assignmentResult) {
			assignmentDao.deletePersistentEntity(assignmentToDelete.getId());
		}
		
		Collection<Resource> resourceResult = resourceDao.retrieveAllPersistentEntities();
		for (Resource resourceToDelete : resourceResult) {
			resourceDao.deletePersistentEntity(resourceToDelete.getId());
		}
			
		// Assert that for every single module there are no assignments
		moduleResult = moduleDao.retrieveAllPersistentEntities();
		for (Module module : moduleResult) {
			// Assignments
			assignmentExample = new Assignment();
			assignmentExample.setModuleId(module.getId());
			assignmentDao.setSortingType(IAssignmentDAO.SortingType.NONE);
			assignmentResult = assignmentDao.findPersistentEntitiesByExample(assignmentExample);
			
			for (Assignment assignment : assignmentResult) {
				// One was found
				fail();
			}
		}
		
		f.endTransaction();
	}

	
}
