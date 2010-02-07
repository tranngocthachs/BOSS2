package uk.ac.warwick.dcs.boss.model.testing.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import uk.ac.warwick.dcs.boss.model.FactoryException;
import uk.ac.warwick.dcs.boss.model.FactoryRegistrar;
import uk.ac.warwick.dcs.boss.model.dao.beans.Resource;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;
import uk.ac.warwick.dcs.boss.model.dao.beans.Test;
import uk.ac.warwick.dcs.boss.model.dao.DAOFactory;
import uk.ac.warwick.dcs.boss.model.dao.IDAOSession;
import uk.ac.warwick.dcs.boss.model.dao.IResourceDAO;
import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestResult;
import uk.ac.warwick.dcs.boss.model.testing.executors.ITestExecutor;
import uk.ac.warwick.dcs.boss.model.testing.tests.ITestMethod;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityException;

public class ThreadedTestRunnerWorker implements Callable<TestResult> {	
	private Submission submission;
	private Test test;
	private File testTempDirectory;

	public ThreadedTestRunnerWorker(Submission submission, Test test, File testTempDir) {
		super();
		this.submission = submission;
		this.test = test;
		this.testTempDirectory = testTempDir;
	}

	public TestResult call() {	
		Logger logger = Logger.getLogger("testing");
		logger.log(Level.INFO, "Thread about to run test with ID " + test.getId() + " for submission with ID " + submission.getId());

		IDAOSession daoSession = null;
		
		try {
			// Load DAOs

			try {
				DAOFactory f = (DAOFactory)FactoryRegistrar.getFactory(DAOFactory.class);
				daoSession = f.getInstance();
			} catch (FactoryException e) {
				throw new AdminUtilityException("dao init error", e);
			}

			daoSession.beginTransaction();
	
			// Get parameters and test method
			Map<String, String> parameters = daoSession.getTestDAOInstance().getTestParameters(test.getId());
			ITestMethod testMethod = (ITestMethod)Class.forName(test.getTestClassName()).newInstance();
			
			// get test executor
			ITestExecutor testExecutor = (ITestExecutor)Class.forName(test.getExecutorClassName()).newInstance();
			
			// Get resources
			IResourceDAO resourceDao = daoSession.getResourceDAOInstance();
			Resource testResource = resourceDao.retrievePersistentEntity(test.getLibraryResourceId());
			InputStream submissionInputStream = resourceDao.openInputStream(submission.getResourceId());
			InputStream testInputStream = resourceDao.openInputStream(test.getLibraryResourceId());
	
			// Create the temporary directories
			File testResourceDirectory = new File(testTempDirectory.getAbsolutePath() + File.separator + "test_resource");
			File submissionDirectory = new File(testTempDirectory.getAbsoluteFile() + File.separator + "submission");
			if (!testResourceDirectory.mkdir()) {
				throw new IOException("Could not create " + testResourceDirectory);
			}
			if (!submissionDirectory.mkdir()) {
				throw new IOException("Could not create " + submissionDirectory);
			}
			extractResource(submissionInputStream, submissionDirectory);

			// If the resource is a zip file, extract it
			if (testResource.getMimeType().equals("application/zip")) {
				logger.log(Level.DEBUG, "Extracting test resource as it is a zip file...");
				try {
					extractResource(testInputStream, testResourceDirectory);
				} catch (IOException e) {
					logger.log(Level.ERROR, "Could not extract test resource: not a zip file?");
					logger.log(Level.ERROR, e);
				}
			// Otherwise copy it to the resource directory
			} else {
				// Copy the resource over.
				logger.log(Level.DEBUG, "Copying test resource as it is not a zip file...");
				File testResourceFile = new File(testResourceDirectory.getAbsolutePath() + File.separator + "resource");
				FileOutputStream fos = new FileOutputStream(testResourceFile);
				byte buffer[] = new byte[4096];
				int nread = -1;
				while ((nread = testInputStream.read(buffer)) != -1) {
					fos.write(buffer, 0, nread);
				}
				fos.flush();
				fos.close();
			}
			submissionInputStream.close();
			testInputStream.close();
	
			// Move submission files up.  This is because they're in a subdirectory.  This has been stored for us in
			// the submission data object.
			File submissionFilesDirectory = new File(submissionDirectory.getAbsolutePath() + File.separator + submission.getResourceSubdirectory());
			for (File file : submissionFilesDirectory.listFiles()) {
				if (!file.renameTo(new File(submissionDirectory.getAbsolutePath() + File.separator + file.getName()))) {
					throw new IOException("could not move " + file + " to submission directory");
				}
			}
			submissionFilesDirectory.delete();
									
			// Right, we're done with the data.
			daoSession.endTransaction();
			
			// Run the test
			ExecutionResult executionResult = testExecutor.execute(testResourceDirectory, submissionDirectory, test.getCommand(), test.getMaximumExecutionTime());
			TestResult result = testMethod.test(parameters, executionResult);
			if (result.getOutput() == null) {
				result.setOutput("Test had no output");
			}
			if (result.getComment() == null) {
				result.setComment("Test had no comment");
			}
			
			// Clean up
			if (!killDirectory(testTempDirectory, true)) {
				logger.log(Level.WARN, "didn't delete test directory - " + testTempDirectory);
			}

			return result;
			
		} catch (Exception e) {
			if (daoSession != null) {
				daoSession.abortTransaction();
			}
			
			if (testTempDirectory != null) {
				if (!killDirectory(testTempDirectory, true)) {
					logger.log(Level.WARN, "didn't delete test directory - " + testTempDirectory);
				}
			}
			
			TestResult output = new TestResult();
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			output.setComment("Un-recoverable error");
			output.setOutput(stringWriter.getBuffer().toString());
			output.setFinishTime(new Date());
			output.setResult(0);
			output.setMaxMark(0);

			return output;
		}		
	}	

	protected static void extractResource(InputStream resource, File targetDirectory) throws IOException {
			ZipInputStream zis = new ZipInputStream(resource);
			ZipEntry entry;
			byte buffer[] = new byte[1024];
			
			while ((entry = zis.getNextEntry()) != null) {
				String destination = targetDirectory.getAbsolutePath() + File.separator + entry.getName();
				File destinationFile = new File(destination);
				
				if (!entry.isDirectory() && !destinationFile.getParentFile().exists()) {
					destinationFile.getParentFile().mkdirs();
				}
				
				if (entry.isDirectory() && !destinationFile.exists()) {
					destinationFile.getParentFile().mkdirs();
				}

				
				FileOutputStream fos = new FileOutputStream(destinationFile);
				int n;
				while ((n = zis.read(buffer, 0, 1024)) > -1) {
                    fos.write(buffer, 0, n);
				}
				
				fos.flush();
				fos.close();
			}
	}

	static public boolean killDirectory(File path, boolean removeDirectory) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					killDirectory(files[i], true);
				}
				else {
					files[i].delete();
				}
			}
		}
		
		if (removeDirectory) {
			return( path.delete() );
		} else {
			return true;
		}
	}
}