package uk.ac.warwick.dcs.boss.model.session;

import uk.ac.warwick.dcs.boss.model.dao.beans.Person;

/**
 * Authenticators are responsible for login and credential update.
 * @author davidbyard
 *
 */
public interface ISessionAuthenticator {

	/**
	 * Attempt to login with the given locale, username, and password.
	 * @param preferredLocale is ignored at the present time.
	 * @param username is the username to log in with
	 * @param password is the password to log in with
	 * @return a valid user session
	 * @throws UserNotFoundException if authentication failed
	 * @throws SessionException if something bad happens
	 */
	abstract public UserSession performLogin(String preferredLocale, String username, String password) throws UserNotFoundException, SessionException;
		
	/**
	 * Set a new password
	 * @param person is the Person object
	 * @throws UserNotFoundException is thrown if the User doesn't exist
	 * @throws SessionException if something bad happens.
	 */
	abstract public void updatePassword(Person person, String newPassword) throws UserNotFoundException, SessionException;
	
}
