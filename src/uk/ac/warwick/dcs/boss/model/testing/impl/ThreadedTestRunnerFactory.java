package uk.ac.warwick.dcs.boss.model.testing.impl;

import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executors;

import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.testing.ITestRunner;
import uk.ac.warwick.dcs.boss.model.testing.TestRunnerFactory;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;

public class ThreadedTestRunnerFactory extends TestRunnerFactory {

	ThreadedTestRunner testRunner = null;

	@Override
	public Collection<ConfigurationOption> getConfigurationOptions() {
		Vector<ConfigurationOption> options = new Vector<ConfigurationOption>();
		options.add(new ConfigurationOption("testing.temp_dir", "Directory for temporary files for the testing framework.  Must be read/write/execute for the Tomcat user.", "/var/lib/boss2/testing"));
		return options;
	}
	
	@Override
	public void init(Properties configuration) throws FactoryException {
		try {
			this.testRunner = new ThreadedTestRunner(Executors.newCachedThreadPool(), new File(configuration.getProperty("testing.temp_dir")));
		} catch (TestingException e) {
			throw new FactoryException("factory init error", e);
		}
	}
	
	@Override
	public ITestRunner getInstance() throws FactoryException {
		if (this.testRunner != null) {
			return testRunner;
		} else {
			throw new FactoryException("factory not initialised");
		}
	}
	
	@Override
	public void cleanUp() {
		if (this.testRunner != null) {
			this.testRunner.cleanUp();
			this.testRunner = null;
		}
		
		super.cleanUp();
	}

}
