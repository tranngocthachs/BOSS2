package uk.ac.warwick.dcs.boss.model;

import java.util.Collection;
import java.util.Properties;

public abstract class Factory<T> {
	
	/**
	 * Initialise the factory.
	 */
	public abstract void init(Properties configuration) throws FactoryException;

	/**
	 * Obtain what the factory produces.
	 */
	public abstract T getInstance() throws FactoryException;
	
	/**
	 * Clean up the factory.  This means that it was unregistered and should cleanly unload.
	 */
	protected void cleanUp() {
		System.gc();
	}
	
	/**
	 * Obtain configuration options this factory requires.
	 */
	abstract public Collection<ConfigurationOption> getConfigurationOptions();

	
}
