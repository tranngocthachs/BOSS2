package uk.ac.warwick.dcs.boss.model.dao.impl;

import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.dao.DAOException;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;

public class MySQLDAOFactory extends DAOFactory {

	private String submissionHashSalt = null;
	private MySQLDAOProducer daoProducer = null;
	
	@Override
	public String getSubmissionHashSalt() throws FactoryException {
		if (submissionHashSalt != null) {
			return submissionHashSalt;
		} else {
			throw new FactoryException("factory not initialised");
		}
	}

	@Override
	public Collection<ConfigurationOption> getConfigurationOptions() {
		Vector<ConfigurationOption> options = new Vector<ConfigurationOption>();
		
		options.add(new ConfigurationOption("db.host", "Change the following to set the MySQL database server that BOSS2 should connect to.", "localhost"));
		options.add(new ConfigurationOption("db.port", "Change the following to set the MySQL database server port that BOSS2 should connect to.", "3306"));
		options.add(new ConfigurationOption("db.db", "Change the following to set the MySQL database that BOSS2 should connect use.", "boss2"));
		options.add(new ConfigurationOption("db.username", "Change the following to set the username that BOSS2 should connect to the MySQL database with.", "boss2"));
		options.add(new ConfigurationOption("db.password", "Change the following to set the password that BOSS2 should connect to the MySQL database with.", ""));
		options.add(new ConfigurationOption("db.resource_dir", "Change the following to set the location that data resources should be stored.  This must not be changed,\nmoved, or in any way lose sync with the MySQL database or BOSS will crash.\n\nFor your first configuration, set this to a directory that is writable by tomcat, as well as backed up\nsimilarly to your MySQL database.", "/var/lib/boss2/resource"));
			
		options.add(new ConfigurationOption("submission.secret_salt", "Change the following to set a secret phrase used when generating security codes.  This should not be guessable\n or distributed.", "floozyflarf"));			

		return options;
	}

	@Override
	public IDAOSession getInstance() throws FactoryException {
		if (daoProducer != null) {
			return daoProducer;
		} else {
			throw new FactoryException("Not initialised");
		}
	}

	@Override
	public void init(Properties configuration) throws FactoryException {
		this.submissionHashSalt = configuration.getProperty("submission.secret_salt");
		try {
			this.daoProducer = new MySQLDAOProducer(configuration);
		} catch (DAOException e) {
			this.submissionHashSalt = null;
			throw new FactoryException("error initialising", e);
		}
	}
	
	@Override
	protected void cleanUp() {
		if (this.daoProducer != null) {
			this.daoProducer.cleanUp();
		}
		super.cleanUp();
	}

}
