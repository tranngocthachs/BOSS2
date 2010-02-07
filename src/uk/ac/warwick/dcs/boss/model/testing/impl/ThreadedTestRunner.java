package uk.ac.warwick.dcs.boss.model.testing.impl;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;
import uk.ac.warwick.dcs.boss.model.testing.ITestRunner;
import uk.ac.warwick.dcs.boss.model.testing.TestResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;


// TODO: cancelling running tests and cleaning up old schedule entries
// TODO: rewrite this - PROTOTYPE CODE
public class ThreadedTestRunner implements ITestRunner {

	private ExecutorService executor;		
	private final File testingTempDir;
	
	public ThreadedTestRunner(ExecutorService executor, File testingTempDir) throws TestingException {
		if (!testingTempDir.exists()) {
			throw new TestingException("Path doesn't exist: " + testingTempDir);
		}

        if (!testingTempDir.canWrite()) {
        	throw new TestingException("Insufficient permissions: " + testingTempDir);
        }

        if (!ThreadedTestRunnerWorker.killDirectory(testingTempDir, false)) {
        	throw new TestingException("Could not clean " + testingTempDir);
        }

		this.executor = executor;
		this.testingTempDir = testingTempDir;
	}
	
	public Future<TestResult> runTest(Submission submission, Test test)
			throws TestingException {
		File testTempDir;
		try {
			testTempDir = TemporaryDirectory.createTempDir("test", testingTempDir);
		} catch (IOException e) {
			throw new TestingException("couldn't create test temp dir", e);
		}
		ThreadedTestRunnerWorker worker = new ThreadedTestRunnerWorker(
				submission, test,
				testTempDir
		);
		return this.executor.submit(worker);
	}
	
	public File createTempDir(String prefix, String suffix, File baseDir) throws IOException {
		File tempFile = File.createTempFile(prefix, suffix, baseDir);
		tempFile.delete();
		tempFile.mkdir();
		return tempFile;
	}
	
	public void cleanUp() {
		Logger logger = Logger.getLogger("testing");
		try {
			logger.log(Level.INFO, "Waiting 5 seconds for tests to be done.");
			if (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
				logger.log(Level.WARN, "Wait interrupted, killing anyway.");
			}
		} catch (InterruptedException e) {
			logger.log(Level.WARN, "Wait interrupted, killing anyway.");
		}
		
		this.executor.shutdownNow();
	}

}
