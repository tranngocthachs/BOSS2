package uk.ac.warwick.dcs.boss.model.testing.tests;

import java.util.Collection;

public interface ITestMethodDirectory {

	/**
	 * Fetch a list of descriptions of known ITestMethod classes.
	 * @return a list of TestMethodDescription objects.
	 */
	public abstract Collection<TestMethodDescription> getTestMethodDescriptions();

	
}
