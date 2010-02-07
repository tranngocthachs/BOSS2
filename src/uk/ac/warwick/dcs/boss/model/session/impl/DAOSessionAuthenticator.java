package uk.ac.warwick.dcs.boss.model.session.impl;

import java.util.Collection;

import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IPersonDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Person;
import uk.ac.warwick.dcs.boss.model.session.ISessionAuthenticator;
import uk.ac.warwick.dcs.boss.model.session.SessionException;
import uk.ac.warwick.dcs.boss.model.session.UserNotFoundException;
import uk.ac.warwick.dcs.boss.model.session.UserSession;

public class DAOSessionAuthenticator implements ISessionAuthenticator {

	public UserSession performLogin(String preferredLocale, String username, String password) throws UserNotFoundException, SessionException {
		IDAOSession daoSession;
		try {
			DAOFactory f = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			daoSession = f.getInstance();
		} catch (FactoryException e) {
			throw new SessionException("dao init error", e);
		}
		
		String passwordHash = null;

		// Hash password
		passwordHash = Person.passwordHash(password);

		// Search for user
		Person searchCriteria = new Person();
		searchCriteria.setUniqueIdentifier(username);
		searchCriteria.setPassword(passwordHash);

		Collection<Person> searchResults = null;
		try {
			daoSession.beginTransaction();
			searchResults = daoSession.getPersonDAOInstance().findPersistentEntitiesByExample(searchCriteria);
			daoSession.endTransaction();
		} catch (DAOException e) {
			daoSession.abortTransaction();
			throw new SessionException("dao exception", e);
		}

		if (searchResults.isEmpty()) {
			throw new UserNotFoundException("user not found");
		}

		return new UserSession(preferredLocale, searchResults.iterator().next());
	}

	public void updatePassword(Person person, String newPassword)
	throws UserNotFoundException, SessionException {
		IDAOSession daoSession;
		try {
			DAOFactory f = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
			daoSession = f.getInstance();
		} catch (FactoryException e) {
			throw new SessionException("dao init error", e);
		}
		
		try {
			daoSession.beginTransaction();
			IPersonDAO dao = daoSession.getPersonDAOInstance();
			String passwordHash = Person.passwordHash(newPassword);
			person.setPassword(passwordHash);
			dao.updatePersistentEntity(person);
			daoSession.endTransaction();
		} catch (DAOException e) {
			daoSession.abortTransaction();
			throw new SessionException("dao error", e);
		}
	}


}
