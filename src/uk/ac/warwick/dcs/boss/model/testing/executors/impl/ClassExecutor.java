package uk.ac.warwick.dcs.boss.model.testing.executors.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;
import uk.ac.warwick.dcs.boss.model.testing.executors.ITestExecutor;

public class ClassExecutor implements ITestExecutor {

	public ExecutionResult execute(File testResourcDirectory, File submissionDirectory,
			String command, int maximumExecutionTime) throws TestingException {
		// Create policy file.
		File policyFile;
		try {
			policyFile = File.createTempFile("boss", ".policy", submissionDirectory);
			policyFile.deleteOnExit();
			writePolicy(new FileWriter(policyFile), testResourcDirectory, submissionDirectory);
		} catch (IOException e) {
			throw new TestingException("Could not write policy file", e);
		}
		
		// Construct command line.
		List<String> commandList = new ArrayList<String>();
		commandList.add("java");
		commandList.add("-Djava.security.manager");
		commandList.add("-Djava.security.policy==file:" + policyFile.getAbsolutePath());
		commandList.add("-classpath");
		commandList.add(submissionDirectory.getAbsolutePath() + ":" + testResourcDirectory.getAbsolutePath());
		commandList.add(command);
		
		// Run!
		ExecutionResult result = new ExecutorProcessRunner(testResourcDirectory, submissionDirectory, commandList, maximumExecutionTime).run();
		policyFile.delete();
		return result;
	}
	
	private void writePolicy(FileWriter fw, File testResourceDirectory, File submissionDirectory) throws IOException {
		fw.write("grant codeBase \"file:" + submissionDirectory.getAbsolutePath() + "/-\" {\n");
		fw.write("  permission java.io.FilePermission \"file:" + System.getProperty("java.io.tmpdir") + "\", \"read, write, delete\";");
		fw.write("  permission java.io.FilePermission \"file:" + submissionDirectory.getAbsolutePath() + "\", \"read\";");
		fw.write("  permission java.lang.RuntimePermission \"accessDeclaredMembers\";");
		fw.write("  permission java.util.PropertyPermission \"*\", \"read\";");
	}

}
