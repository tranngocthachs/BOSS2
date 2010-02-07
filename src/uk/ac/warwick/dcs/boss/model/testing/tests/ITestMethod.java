package uk.ac.warwick.dcs.boss.model.testing.tests;

import java.util.Map;

import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;

/**
 * A class that provide a mark for given ExecutionResult objects.
 * @author davidbyard
 *
 */
public interface ITestMethod {

	/**
	 * Mark an execution result.
	 * @param parameters are the parameters of the test.
	 * @param executionResult is the command output to mark.
	 * @return a TestResult detailing the marks obtained.
	 * @throws TestingException if something bad happens.
	 */
	public TestResult test(Map<String, String> parameters, ExecutionResult executionResult) throws TestingException;
	
}
