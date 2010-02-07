package uk.ac.warwick.dcs.boss.model.testing.executors.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;
import uk.ac.warwick.dcs.boss.model.testing.executors.ITestExecutor;

//Executes using system SH
public class ShellExecutor implements ITestExecutor {

	public ExecutionResult execute(File testResource, File submissionDirectory,
			String command, int maximumExecutionTime) throws TestingException {
		
		List<String> commandList = new ArrayList<String>();
		
	    // Choose a shell depending on the operating system
	    String osName = System.getProperty("os.name");

	    if (osName.indexOf("windows 9") == 0) {
	      // Windows' command.com
	      commandList.add("command.com");
	      commandList.add("/c");
	    }
	    else if (osName.indexOf("windows") == 0) {
	      // Windows' cmd.exe
	      commandList.add("cmd.exe");
	      commandList.add("/c");
	    }
	    else {
	      // Assume sh
	      commandList.add("/bin/sh");
	      commandList.add("-c");
	    }

		commandList.add(command);
		return new ExecutorProcessRunner(testResource, submissionDirectory, commandList, maximumExecutionTime).run();
	}

}
