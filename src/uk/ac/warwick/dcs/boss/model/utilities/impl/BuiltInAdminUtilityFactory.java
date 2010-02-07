package uk.ac.warwick.dcs.boss.model.utilities.impl;

import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityFactory;
import uk.ac.warwick.dcs.boss.model.utilities.IAdminUtilityDirectory;

public class BuiltInAdminUtilityFactory extends AdminUtilityFactory {

	private BuiltInAdminUtilityDirectory directory = new BuiltInAdminUtilityDirectory();
	
	@Override
	public Collection<ConfigurationOption> getConfigurationOptions() {
		return new Vector<ConfigurationOption>();
	}

	@Override
	public IAdminUtilityDirectory getInstance() throws FactoryException {
		return directory;
	}

	@Override
	public void init(Properties configuration) throws FactoryException {
	}

}
