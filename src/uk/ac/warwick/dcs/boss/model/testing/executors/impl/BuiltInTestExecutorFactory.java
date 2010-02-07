package uk.ac.warwick.dcs.boss.model.testing.executors.impl;

import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.testing.executors.ITestExecutorDirectory;
import uk.ac.warwick.dcs.boss.model.testing.executors.TestExecutorFactory;

public class BuiltInTestExecutorFactory extends TestExecutorFactory {

	private BuiltInTestExecutorDirectory directory = new BuiltInTestExecutorDirectory();
	
	@Override
	public Collection<ConfigurationOption> getConfigurationOptions() {
		return new Vector<ConfigurationOption>();
	}

	@Override
	public ITestExecutorDirectory getInstance() throws FactoryException {
		return directory;
	}

	@Override
	public void init(Properties configuration) throws FactoryException {		
	}

}
