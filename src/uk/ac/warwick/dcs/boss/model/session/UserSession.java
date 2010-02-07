package uk.ac.warwick.dcs.boss.model.session;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.testing.TestResult;

/**
 * A user session within BOSS2.
 * 
 * Holds running tests and the bound Person object representing the person.  Also allows
 * storing of arbitrary information.
 * @author davidbyard
 */
public class UserSession {

	private String preferredLocale = null;
	private Person personBinding = null;
	private LinkedList<Future<TestResult>> testResults = null;
	private HashMap<String, Object> sessionMemory;
	
	public UserSession(String locale, Person credentials)
			throws SessionException {
		// 
		this.personBinding = credentials;
		this.preferredLocale = locale;
		this.testResults = new LinkedList<Future<TestResult>>();
		this.sessionMemory = new HashMap<String, Object>();
	}

	/**
	 * REmember arbitrary information
	 * @param memoryName is the key
	 * @param what is the value
	 */
	public void remember(String memoryName, Object what) {
		if (what == null) {
			forget(memoryName);
			return;
		}
		sessionMemory.put(memoryName, what);
	}
	
	/**
	 * Forget arbitrary information
	 * @param memoryName is the key
	 */
	public void forget(String memoryName) {
		sessionMemory.remove(memoryName);
	}
	
	/**
	 * Recall remembered information
	 * @param memoryName is the key
	 * @return the value
	 */
	public Object recall(String memoryName) {
		if (sessionMemory.containsKey(memoryName)) { 
			return sessionMemory.get(memoryName);
		} else {
			return null;
		}
	}
	
	/**
	 * Get the Person object bound to this session.
	 * @return a valid person object (null for an anonymous session)
	 */
	public Person getPersonBinding() {
		return personBinding;
	}

	/**
	 * Unused so far
	 * @return
	 */
	public String getPreferredLocale() {
		return preferredLocale;
	}
	
	/**
	 * Get all pending test results.
	 * @return
	 */
	public List<Future<TestResult>> getTestResults() {
		return testResults;
	}
	
	/**
	 * Log activity.
	 * 
	 * TODO: not sure if this is redundant or not?
	 * @param when date something occured
	 * @param what occured
	 */
	public void logActivity(Date when, String what) {
		Logger.getLogger("user.activity").log(Level.INFO, when + " User[" + personBinding.getUniqueIdentifier() + "] " + what);
	}

	/**
	 * Called when someone logs in.
	 */
	public void login() {
		logActivity(new Date(), "logged in");
	}

	/**
	 * Called when someone logs out.
	 * 
	 * Thus far cancels all pending test results.
	 */
	public void logout() {
		for (Future<TestResult> testResult : testResults) {
			testResult.cancel(true);
		}
		
		logActivity(new Date(), "logged out");
	}
}
