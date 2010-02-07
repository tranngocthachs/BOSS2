package uk.ac.warwick.dcs.boss.model.testing.tests.impl;

import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.testing.tests.ITestMethodDirectory;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodFactory;

public class BuiltInTestMethodFactory extends TestMethodFactory {

	private BuiltInTestMethodDirectory directory = new BuiltInTestMethodDirectory();
	
	@Override
	public Collection<ConfigurationOption> getConfigurationOptions() {
		return new Vector<ConfigurationOption>();
	}

	@Override
	public ITestMethodDirectory getInstance() throws FactoryException {
		return directory;
	}

	@Override
	public void init(Properties configuration) throws FactoryException {
	}

}
