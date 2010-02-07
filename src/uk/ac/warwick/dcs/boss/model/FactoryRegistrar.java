package uk.ac.warwick.dcs.boss.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

abstract public class FactoryRegistrar {

	static private Map<Class, Factory> factories = new HashMap<Class, Factory>();
	
	static public void registerFactory(Class factoryBase, Factory factory) throws FactoryException {
		if (!factoryBase.isAssignableFrom(factory.getClass())) {
			throw new FactoryException("factory is not child instance of factoryBase");
		}
		
		if (factoryBase.equals(factory.getClass())) {
			throw new FactoryException("cannot register a factory under itself");
		}
		
		if (factories.containsKey(factory.getClass())) {
			Factory oldFactory = factories.get(factory.getClass());
			if (oldFactory != null) {
				oldFactory.cleanUp();
			}
		}
		
		factories.put(factoryBase, factory);
	}
		
	static public Factory getFactory(Class clazz) throws FactoryException {
		if (!factories.containsKey(clazz)) {
			throw new FactoryException("factory not found");
		}
		return factories.get(clazz);
	}
	
	static public Collection<ConfigurationOption> knownConfigurationOptions() {
		Vector<ConfigurationOption> options = new Vector<ConfigurationOption>();
		for (Factory factory : factories.values()) {
			options.addAll(factory.getConfigurationOptions());
		}
		return options;
	}
	
	static public void initialiseFactories(Properties configuration) throws FactoryException {
		for (Factory factory : factories.values()) {
			try {
				factory.init(configuration);
			} catch (Exception e) {
				for (Factory deadFactory : factories.values()) {
					deadFactory.cleanUp();
				}
				
				throw new FactoryException("init error", e);
			}
		}

	}
	
}
