package uk.ac.warwick.dcs.boss.model.testing.executors.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;
import uk.ac.warwick.dcs.boss.model.testing.executors.ITestExecutor;

public class FileReaderExecutor implements ITestExecutor {

	// Just returns null
	public ExecutionResult execute(File testDirectory, File submissionDirectory,
			String command, int maximumExecutionTime) throws TestingException {
		
		ExecutionResult result = new ExecutionResult();
		result.setErrors("");
		result.setOutput("");
		result.setExitCode(0);
		result.setInterruptedByException(false);
		
		try {
			File inputFile = new File(testDirectory.getAbsolutePath() + File.separator + command);
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			String line = null;
			StringBuffer output = new StringBuffer();
			
			while ((line = br.readLine()) != null) {
				output.append(line);
			}
			
			result.setOutput(output.toString());
		} catch (IOException e) {
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			result.setErrors(stringWriter.toString());
		}
		
		return result;
	}

}
