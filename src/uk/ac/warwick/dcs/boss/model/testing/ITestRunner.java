package uk.ac.warwick.dcs.boss.model.testing;

import java.util.concurrent.Future;

import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;

/**
 * A class that can schedule tests to run at some point in the future.
 * @author davidbyard
 *
 */
public interface ITestRunner {	
	/**
	 * Schedule a test to run at some point in the future.
	 * @param submission is the submission to test.
	 * @param test is the test to run on the submission.
	 * @return a TestResult that may or may not be available at some future point.
	 * @throws TestingException if something bad happens.
	 */
	public Future<TestResult> runTest(Submission submission, Test test) throws TestingException;
}
