package uk.ac.warwick.dcs.boss.model.utilities.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IModelDAO;
import uk.ac.warwick.dcs.boss.model.dao.IModuleDAO;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityException;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityResult;
import uk.ac.warwick.dcs.boss.model.utilities.IAdminUtility;

public class ADSImport implements IAdminUtility {

	private static final String ADS_SERVER_ADDRESS = "romulus.warwick.ac.uk:1521";
	private static final String ADS_DATABASE_NAME = "romulus";	
	private static final String ADS_USERNAME = "ADS_COMPUTER_ACCESS";
	private static final String ADS_PASSWORD = "AdsComputer!12!";
	
	private static final String MODULE_DETAILS_TABLE = "ADSADMIN.ADS_DEPARTMENT_MOD_DETAILS";
	private static final String MODULE_CODE_FIELD = "MODULE_CODE";
	private static final String MODULE_NAME_FIELD = "NAME";
	private static final String MODULE_YEAR_FIELD = "ACADEMIC_YEAR_CODE";
	private static final String MODULE_CODE_FILTER = "CS%";

	private static final String STUDENT_DETAILS_TABLE = "ADSADMIN.ADS_DEPARTMENT_STU_DETAILS";
	private static final String STUDENT_SPR_CODE_FIELD = "SPR_CODE";
	private static final String STUDENT_ID_FIELD = "UNIVERSITY_ID";
	private static final String STUDENT_FIRST_NAMES_FIELD = "FORENAMES";
	private static final String STUDENT_LAST_NAME_FIELD = "FAMILY_NAME";
	private static final String STUDENT_EMAIL_ADDRESS_FIELD = "P_EMAIL";

	private static final String STAFF_DETAILS_TABLE = "ADSADMIN.ADS_DEPARTMENT_MEMBERS";
	private static final String STAFF_ID_FIELD = "UNIVERSITY_ID";
	private static final String STAFF_FIRST_NAMES_FIELD = "FORENAMES";
	private static final String STAFF_LAST_NAME_FIELD = "FAMILY_NAME";
	private static final String STAFF_EMAIL_ADDRESS_FIELD = "PREFERRED_EMAIL_ADDRESS";

	public AdminUtilityResult execute(Map<String, String> parameters)
			throws AdminUtilityException {
		AdminUtilityResult result = new AdminUtilityResult();
		StringBuffer output = new StringBuffer();
		
		int foundCounter = 0;  int addedCounter = 0;  int updatedCounter = 0;
		
		IModelDAO modelDao;
		IModuleDAO moduleDao;
		IPersonDAO personDao;

		PreparedStatement statement;
		ResultSet rs;

		HashMap<String, Long> modelUniqueToIdMap = new HashMap<String, Long>();
		HashMap<String, Long> moduleUniqueToIdMap = new HashMap<String, Long>();
		HashMap<String, Long> personUniqueToIdMap = new HashMap<String, Long>();

		// Load DAOs
		IDAOSession daoSession;
		try {
			DAOFactory f = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			daoSession = f.getInstance();
		} catch (FactoryException e) {
			throw new AdminUtilityException("dao init error", e);
		}
		
		// Generic try/catch
		try {
			daoSession.beginTransaction();
			modelDao = daoSession.getModelDAOInstance();
			moduleDao = daoSession.getModuleDAOInstance();
			personDao = daoSession.getPersonDAOInstance();
	
			// Load SQL driver
			Class.forName("oracle.jdbc.driver.OracleDriver");
	
			// Obtain connection to ADS.
			Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@" + ADS_SERVER_ADDRESS + ":" + ADS_DATABASE_NAME, ADS_USERNAME, ADS_PASSWORD);
	
			/* Execute the query for models. */
			statement = connection.prepareStatement(
					"SELECT DISTINCT " + MODULE_YEAR_FIELD + " FROM " + MODULE_DETAILS_TABLE + " WHERE " + MODULE_CODE_FIELD + " LIKE ?"
			);
			statement.setString(1, MODULE_CODE_FILTER);
			rs = statement.executeQuery();
	
			// Construct models.
			foundCounter = updatedCounter = addedCounter = 0;
			while (rs.next()) {
				Model model = new Model();
				model.setUniqueIdentifier(rs.getString(MODULE_YEAR_FIELD));			
	
				// Check existence.
				Collection<Model> searchResults = modelDao.findPersistentEntitiesByExample(model);
				if (searchResults.isEmpty()) {
					model.setName("Computer Science " + model.getUniqueIdentifier());
					modelUniqueToIdMap.put(model.getUniqueIdentifier(), modelDao.createPersistentCopy(model));
					addedCounter++;
				} else {
					model = searchResults.iterator().next();
					model.setName("Computer Science " + model.getUniqueIdentifier());
					modelUniqueToIdMap.put(model.getUniqueIdentifier(), model.getId());
					modelDao.updatePersistentEntity(model);
					updatedCounter++;
				}
				
				foundCounter++;
			}
			output.append("Imported " + foundCounter + " model(s): " + updatedCounter + " updated, " + addedCounter + " added\n");
	
			/* Execute the query for modules. */
			statement = connection.prepareStatement(
					"SELECT DISTINCT " + MODULE_CODE_FIELD + "," + MODULE_NAME_FIELD + "," + MODULE_YEAR_FIELD
					+ " FROM " + MODULE_DETAILS_TABLE + " WHERE " + MODULE_CODE_FIELD + " LIKE ?"
			);
			statement.setString(1, MODULE_CODE_FILTER);
			rs = statement.executeQuery();
	
			// Construct a module from each result.
			foundCounter = updatedCounter = addedCounter = 0;
			while (rs.next()) {
				Module module = new Module();
				module.setUniqueIdentifier(rs.getString(MODULE_CODE_FIELD));
	
				// Check existence.
				Collection<Module> searchResults = moduleDao.findPersistentEntitiesByExample(module);			
				if (searchResults.isEmpty()) {
					module.setName(capitaliseSentence(rs.getString(MODULE_NAME_FIELD)));
					module.setModelId(modelUniqueToIdMap.get(rs.getString(MODULE_YEAR_FIELD)));
					module.setRegistrationRequired(false);
					moduleUniqueToIdMap.put(rs.getString(MODULE_CODE_FIELD), moduleDao.createPersistentCopy(module));
					addedCounter++;
				} else {
					module = searchResults.iterator().next();
					module.setName(capitaliseSentence(rs.getString(MODULE_NAME_FIELD)));
					module.setModelId(modelUniqueToIdMap.get(rs.getString(MODULE_YEAR_FIELD)));
					module.setRegistrationRequired(false);
					moduleUniqueToIdMap.put(module.getUniqueIdentifier(), module.getId());
					moduleDao.updatePersistentEntity(module);
					updatedCounter++;
				}
				
				foundCounter++;
			}
			output.append("Imported " + foundCounter + " modules(s): " + updatedCounter + " updated, " + addedCounter + " added\n\n");
	
			/* Execute the query for students. */
			statement = connection.prepareStatement(
					"SELECT DISTINCT " + STUDENT_ID_FIELD + "," + STUDENT_FIRST_NAMES_FIELD + "," + STUDENT_LAST_NAME_FIELD + "," + STUDENT_EMAIL_ADDRESS_FIELD
					+ " FROM " + STUDENT_DETAILS_TABLE
			);
			Logger.getLogger(ADSImport.class).log(Level.DEBUG, "SELECT DISTINCT " + STUDENT_ID_FIELD + "," + STUDENT_FIRST_NAMES_FIELD + "," + STUDENT_LAST_NAME_FIELD + "," + STUDENT_EMAIL_ADDRESS_FIELD
					+ " FROM " + STUDENT_DETAILS_TABLE);
			rs = statement.executeQuery();
	
			// Construct a person from each result.
			foundCounter = addedCounter = updatedCounter = 0;
			while (rs.next()) {
				Person person = new Person();
				person.setUniqueIdentifier(rs.getString(STUDENT_ID_FIELD));
	
				// Check existence.
				Collection<Person> searchResults = personDao.findPersistentEntitiesByExample(person);			
				if (searchResults.isEmpty()) {
					String chosenName = rs.getString(STUDENT_FIRST_NAMES_FIELD) + " " + rs.getString(STUDENT_LAST_NAME_FIELD);
					person.setChosenName(capitaliseSentence(chosenName));
					
					String emailAddress = rs.getString(STUDENT_EMAIL_ADDRESS_FIELD);
					if (emailAddress == null || !emailAddress.endsWith("@warwick.ac.uk")) {
						output.append("Rejected non-Warwick email address for student " + person.getUniqueIdentifier() + ": " + emailAddress + "\n");
						emailAddress = person.getUniqueIdentifier() + "@invalid";
					}
					person.setEmailAddress(emailAddress.toLowerCase());
					
					person.setAdministrator(false);
					person.setPassword("mangled");
					personUniqueToIdMap.put(rs.getString(STUDENT_ID_FIELD), personDao.createPersistentCopy(person));
					addedCounter++;
				} else {
					person = searchResults.iterator().next();
					String chosenName = rs.getString(STUDENT_FIRST_NAMES_FIELD) + " " + rs.getString(STUDENT_LAST_NAME_FIELD);
					person.setChosenName(capitaliseSentence(chosenName));
					
					String emailAddress = rs.getString(STUDENT_EMAIL_ADDRESS_FIELD);
					if (emailAddress == null || !emailAddress.endsWith("@warwick.ac.uk")) {
						output.append("Rejected non-Warwick email address for student " + person.getUniqueIdentifier() + ": " + emailAddress + "\n");
						emailAddress = person.getUniqueIdentifier() + "@invalid";
					}
					person.setEmailAddress(emailAddress.toLowerCase());
					
					personUniqueToIdMap.put(person.getUniqueIdentifier(), person.getId());
					personDao.updatePersistentEntity(person);
					updatedCounter++;
				}
				
				foundCounter++;
			}
			output.append("Imported " + foundCounter + " students(s): " + updatedCounter + " updated, " + addedCounter + " added\n\n");
			
			/* Execute the query for staff */
			statement = connection.prepareStatement(
					"SELECT DISTINCT " + STAFF_ID_FIELD + "," + STAFF_FIRST_NAMES_FIELD + "," + STAFF_LAST_NAME_FIELD + "," + STAFF_EMAIL_ADDRESS_FIELD
					+ " FROM " + STAFF_DETAILS_TABLE
			);
			Logger.getLogger(ADSImport.class).log(Level.DEBUG, "SELECT DISTINCT " + STAFF_ID_FIELD + "," + STAFF_FIRST_NAMES_FIELD + "," + STAFF_LAST_NAME_FIELD + "," + STAFF_EMAIL_ADDRESS_FIELD
					+ " FROM " + STAFF_DETAILS_TABLE);
			rs = statement.executeQuery();
	
			// Construct a person from each result.
			foundCounter = addedCounter = updatedCounter = 0;
			while (rs.next()) {
				Person person = new Person();
				person.setUniqueIdentifier(rs.getString(STAFF_ID_FIELD));
	
				// Check existence.
				Collection<Person> searchResults = personDao.findPersistentEntitiesByExample(person);			
				if (searchResults.isEmpty()) {
					String chosenName = rs.getString(STAFF_FIRST_NAMES_FIELD) + " " + rs.getString(STAFF_LAST_NAME_FIELD);
					person.setChosenName(capitaliseSentence(chosenName));
					
					String emailAddress = rs.getString(STAFF_EMAIL_ADDRESS_FIELD);
					if (emailAddress == null || !emailAddress.endsWith("@warwick.ac.uk")) {
						output.append("Rejected non-Warwick email address for student " + person.getUniqueIdentifier() + ": " + emailAddress + "\n");
						emailAddress = person.getUniqueIdentifier() + "@invalid";
					}
					person.setEmailAddress(emailAddress.toLowerCase());
					
					person.setAdministrator(false);
					person.setPassword("mangled");
					personUniqueToIdMap.put(rs.getString(STAFF_ID_FIELD), personDao.createPersistentCopy(person));
					addedCounter++;
				} else {
					person = searchResults.iterator().next();
					String chosenName = rs.getString(STAFF_FIRST_NAMES_FIELD) + " " + rs.getString(STAFF_LAST_NAME_FIELD);
					person.setChosenName(capitaliseSentence(chosenName));
					
					String emailAddress = rs.getString(STAFF_EMAIL_ADDRESS_FIELD);
					if (emailAddress == null || !emailAddress.endsWith("@warwick.ac.uk")) {
						output.append("Rejected non-Warwick email address for student " + person.getUniqueIdentifier() + ": " + emailAddress + "\n");
						emailAddress = person.getUniqueIdentifier() + "@invalid";
					}
					person.setEmailAddress(emailAddress.toLowerCase());
					
					personUniqueToIdMap.put(person.getUniqueIdentifier(), person.getId());
					personDao.updatePersistentEntity(person);
					updatedCounter++;
				}
				
				foundCounter++;
			}
			output.append("Imported " + foundCounter + " staff: " + updatedCounter + " updated, " + addedCounter + " added\n\n");
			
			/* Execute the query for course registration */
			/* SELECT DISTINCT MODULE_DETAILS_TABLE.MODULE_CODE_FIELD, STUDENT_DETAILS_TABLE.STUDENT_ID_FIELD
			   FROM MODULE_DETAILS_TABLE, STUDENT_DETAILS_TABLE
			   WHERE MODULE_DETAILS_TABLE.SPR_CODE_FIELD = STUDENT_DETAILS_TABLE.SPR_CODE_FIELD
			   AND MODULE_DETAILS_TABLE.MODULE_CODE_FIELD LIKE ? */
			statement = connection.prepareStatement(
					"SELECT DISTINCT " + MODULE_DETAILS_TABLE + "." + MODULE_CODE_FIELD + ", " + STUDENT_DETAILS_TABLE + "." + STUDENT_ID_FIELD
					+ " FROM " + MODULE_DETAILS_TABLE + ", " + STUDENT_DETAILS_TABLE
					+ " WHERE " + MODULE_DETAILS_TABLE + "." + STUDENT_SPR_CODE_FIELD +  "=" + STUDENT_DETAILS_TABLE + "." + STUDENT_SPR_CODE_FIELD
					+ " AND " + MODULE_DETAILS_TABLE + "." + MODULE_CODE_FIELD + " LIKE ?"
			);
			Logger.getLogger(ADSImport.class).log(Level.DEBUG, 
					"SELECT DISTINCT " + MODULE_DETAILS_TABLE + "." + MODULE_CODE_FIELD + ", " + STUDENT_DETAILS_TABLE + "." + STUDENT_ID_FIELD
					+ " FROM " + MODULE_DETAILS_TABLE + ", " + STUDENT_DETAILS_TABLE
					+ " WHERE " + MODULE_DETAILS_TABLE + "." + STUDENT_SPR_CODE_FIELD +  "=" + STUDENT_DETAILS_TABLE + "." + STUDENT_SPR_CODE_FIELD
					+ " AND " + MODULE_DETAILS_TABLE + "." + MODULE_CODE_FIELD + " LIKE ?"
			);
			statement.setString(1, MODULE_CODE_FILTER);
			rs = statement.executeQuery();
	
			// Register each of these students.
			foundCounter = 0;
			while (rs.next()) {				
				if (!moduleUniqueToIdMap.containsKey(rs.getString(MODULE_CODE_FIELD))) {
					throw new RuntimeException("When parsing registrations, module " + rs.getString(MODULE_CODE_FIELD) + " was unknown"); 
				}
	
				if (!personUniqueToIdMap.containsKey(rs.getString(STUDENT_ID_FIELD))) {
					throw new RuntimeException("When parsing registrations, student " + rs.getString(STUDENT_ID_FIELD) + " was unknown"); 
				}
	
				
				moduleDao.removeStudentAssociation(
						moduleUniqueToIdMap.get(rs.getString(MODULE_CODE_FIELD)),
						personUniqueToIdMap.get(rs.getString(STUDENT_ID_FIELD))
				);
				moduleDao.addStudentAssociation(
						moduleUniqueToIdMap.get(rs.getString(MODULE_CODE_FIELD)),
						personUniqueToIdMap.get(rs.getString(STUDENT_ID_FIELD))
				);
				
				foundCounter++;
			}
			output.append("Imported " + foundCounter + " student registrations\n");
	
			// Done
			daoSession.endTransaction();
			
			// Construct result.
			result.setComment("Import completed successfully.");
			result.setOutput(output.toString());
			result.setSuccess(true);
		} catch (ClassNotFoundException e) {
			daoSession.abortTransaction();
			result.setComment("Failure due to oracle.jdbc.driver.OracleDriver being missing");
			result.setOutput("");
			result.setSuccess(false);
		} catch (SQLException e) {
			daoSession.abortTransaction();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			pw.close();
						
			result.setComment("Failure due to communication error with Oracle");
			result.setOutput(sw.toString());
			result.setSuccess(false);
		} catch (DAOException e) {
			daoSession.abortTransaction();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			pw.close();
			
			result.setComment("Failure due to DAO error");
			result.setOutput(sw.toString());
			result.setSuccess(false);
		}
		
		// Exit
		result.setFinishTime(new Date());
		return result;
	}

	private static String capitaliseWord(String word) {
		if (word.length() == 0) {
			return "";
		} else if (word.length() == 1) {
			return word.toUpperCase();
		} else {
			return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
		}
	}
	
	private static String capitaliseSentence(String sentence) {
		String words[] = sentence.trim().replaceAll("[ \t\n]+", " ").split(" ");
		StringBuffer result = new StringBuffer();
		for (String word : words) {
			result.append(capitaliseWord(word));
			result.append(' ');
		}
		return result.toString().substring(0, result.length() - 1);
	}
}
