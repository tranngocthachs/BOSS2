package uk.ac.warwick.dcs.boss.model.testing.executors;

import java.util.Collection;

public interface ITestExecutorDirectory {

	/**
	 * Fetch a list of descriptions of known ITestExecutor classes.
	 * @return a list of TestExecutorDescription objects.
	 */
	public abstract Collection<TestExecutorDescription> getTestExecutorDescriptions();
	
}
