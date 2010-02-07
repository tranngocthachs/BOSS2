package uk.ac.warwick.dcs.boss.model.autoassignment.impl;

import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.ConfigurationOption;
import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.autoassignment.AutoAssignmentMethodDirectoryFactory;
import uk.ac.warwick.dcs.boss.model.autoassignment.IAutoAssignmentMethodDirectory;

public class BuiltInAutoAssignmentMethodFactory extends
		AutoAssignmentMethodDirectoryFactory {

	BuiltInAutoAssignmentMethodDirectory directory = new BuiltInAutoAssignmentMethodDirectory();
	
	@Override
	public Collection<ConfigurationOption> getConfigurationOptions() {
		return new Vector<ConfigurationOption>();
	}
	
	@Override
	public void init(Properties configuration) {
	}	
	
	@Override
	public IAutoAssignmentMethodDirectory getInstance()
			throws FactoryException {
		return directory;
	}

}
