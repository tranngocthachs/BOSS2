package uk.ac.warwick.dcs.boss.model.testing.executors;

import java.io.File;

import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;

/**
 * A class that runs a command in the context of a test, producing an ExecutionResult.
 * @author davidbyard
 *
 */
public interface ITestExecutor {

	/**
	 * Run the given command, producing an Execution result.
	 * @param testResourceDirectory is the directory the test resource provided with the test is unpacked within.
	 * @param submissionDirectory is the directory that the submission is unpacked within.
	 * @param command is the command to run.
	 * @param maximumExecutionTime is the maximum time in seconds to run for.
	 * @return an ExecutionResult detailing the output of the given command.
	 * @throws TestingException if something bad happens.
	 */
	public ExecutionResult execute(File testResourceDirectory, File submissionDirectory, String command, int maximumExecutionTime) throws TestingException;
	
}
