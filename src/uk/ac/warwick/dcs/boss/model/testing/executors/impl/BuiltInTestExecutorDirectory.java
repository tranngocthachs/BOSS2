package uk.ac.warwick.dcs.boss.model.testing.executors.impl;

import java.util.Collection;
import java.util.LinkedList;

import uk.ac.warwick.dcs.boss.model.testing.executors.ITestExecutorDirectory;
import uk.ac.warwick.dcs.boss.model.testing.executors.TestExecutorDescription;

public class BuiltInTestExecutorDirectory implements ITestExecutorDirectory {

	public Collection<TestExecutorDescription> getTestExecutorDescriptions() {
		TestExecutorDescription readerExecutorDescription = new TestExecutorDescription();
		readerExecutorDescription.setClassName(FileReaderExecutor.class.getCanonicalName());
		readerExecutorDescription.setName("Read a file");
		readerExecutorDescription.setDescription("The command to run is a submission file that is read and treated as output");

		TestExecutorDescription shellExecutorDescription = new TestExecutorDescription();
		shellExecutorDescription.setClassName(ShellExecutor.class.getCanonicalName());
		shellExecutorDescription.setName("Execute Shell Command");
		shellExecutorDescription.setDescription("Executes the given command under a shell: command.com on Windows 9x, cmd.exe on Windows NT, and /bin/sh on anything else.  The environment variables TEST_RESOURCE_DIRECTORY and SUBMISSION_DIRECTORY are set.  The CWD defaults to SUBMISSION_DIRECTORY.  This test has NO security restrictions so be careful!");

		TestExecutorDescription classExecutorDescription = new TestExecutorDescription();
		classExecutorDescription.setClassName(ClassExecutor.class.getCanonicalName());
		classExecutorDescription.setName("Execute Java Class");
		classExecutorDescription.setDescription("Executes the given Java class as a program.  The program is run in a safe, restricted environment, with the classpath set to the submission directory and the test resource (i.e., you may upload a JAR as the test resource for extra libraries).");


		LinkedList<TestExecutorDescription> result = new LinkedList<TestExecutorDescription>();
		result.add(readerExecutorDescription);
		result.add(shellExecutorDescription);
		result.add(classExecutorDescription);
		return result;

	}
}
