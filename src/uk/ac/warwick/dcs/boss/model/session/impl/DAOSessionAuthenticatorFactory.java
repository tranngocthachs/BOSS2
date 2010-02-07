package uk.ac.warwick.dcs.boss.model.session.impl;

import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.session.ISessionAuthenticator;
import uk.ac.warwick.dcs.boss.model.session.SessionAutenticatorFactory;

public class DAOSessionAuthenticatorFactory extends SessionAutenticatorFactory {

	DAOSessionAuthenticator authenticator = new DAOSessionAuthenticator();

	@Override
	public Collection<ConfigurationOption> getConfigurationOptions() {
		return new Vector<ConfigurationOption>();
	}

	@Override
	public ISessionAuthenticator getInstance() throws FactoryException {
		return authenticator;
	}

	@Override
	public void init(Properties configuration) throws FactoryException {		
	}

}
