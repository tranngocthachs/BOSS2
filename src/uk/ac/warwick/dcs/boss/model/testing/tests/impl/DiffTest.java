package uk.ac.warwick.dcs.boss.model.testing.tests.impl;

import gnu.diffutils.Diff;
import gnu.diffutils.DiffPrint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;
import uk.ac.warwick.dcs.boss.model.testing.tests.ITestMethod;

public class DiffTest implements ITestMethod {

	public static final int MAX_LINES = 1000; 

	private static LinkedList<String> readLines(Reader reader, int max,
			boolean ignoreAllSpace, boolean ignoreSpaceChange, boolean ignoreBlankLines,
			String prefix) throws IOException {
		LinkedList<String> lines = new LinkedList<String>();
		BufferedReader br = new BufferedReader(reader);
		int i;

		for (i = 0; i < max; i++) {
			String line = br.readLine();
			if (line == null) {
				break;
			} else {
				if (ignoreAllSpace) {
					line = line.replaceAll("[ \t]", "");
				} else if (ignoreSpaceChange) {
					line = line.replaceAll("[\t ]+", " ");
					line = line.trim();
				}

				if (ignoreBlankLines && line.length() == 0) {
					continue;
				} else {
					lines.add(prefix + line);
				}
			}
		}

		if (i == max) {
			lines.add("<Output truncated at " + max + "lines>");
		}

		return lines;
	}

	private static int countInsertions(Diff.change script) {
		int insertions = 0;

		Diff.change current = script;
		while (current != null) {
			insertions = insertions + current.inserted;
			current = current.link;
		}

		return insertions;
	}

	private static int countDeletions(Diff.change script) {
		int deletions = 0;

		Diff.change current = script;
		while (current != null) {
			deletions = deletions + current.deleted;
			current = current.link;
		}

		return deletions;
	}

	public TestResult test(Map<String, String> parameters, ExecutionResult executionResult)
	throws TestingException {
		// Obtain parameters
		String expectedOutput = parameters.get("expected");
		boolean ignoreAllSpace = Boolean.valueOf(parameters.get("ignoreAllSpace"));
		boolean ignoreSpaceChange = Boolean.valueOf(parameters.get("ignoreSpaceChange"));
		boolean ignoreBlankLines = Boolean.valueOf(parameters.get("ignoreBlankLines"));
		boolean compareErrors = Boolean.valueOf(parameters.get("compareErrors"));
		boolean compareExitCode = Boolean.valueOf(parameters.get("compareExitCode"));
		
		// Get lists of strings to compare.
		LinkedList<String> outputStrings = null;
		LinkedList<String> errorStrings = null;
		LinkedList<String> expectedStrings = null;
		LinkedList<String> obtainedStrings = new LinkedList<String>();

		try { 
			expectedStrings = readLines(new StringReader(expectedOutput), MAX_LINES, ignoreAllSpace, ignoreSpaceChange, ignoreBlankLines, "");
			outputStrings = readLines(new StringReader(executionResult.getOutput()), MAX_LINES, ignoreAllSpace, ignoreSpaceChange, ignoreBlankLines, "OUTPUT> ");
			errorStrings = readLines(new StringReader(executionResult.getErrors()), MAX_LINES, ignoreAllSpace, ignoreSpaceChange, ignoreBlankLines, "ERROR> ");
		} catch (IOException e) {
			throw new TestingException("IO error", e);
		}

		obtainedStrings.addAll(outputStrings);
		if (compareErrors) {
			obtainedStrings.addAll(errorStrings);
		}
		if (compareExitCode) {
			obtainedStrings.add("EXIT CODE> " + executionResult.getExitCode());
		}
		
		// Perform diff
		String expectedStringsArray[] = new String[expectedStrings.size()];
		String obtainedStringsArray[] = new String[obtainedStrings.size()];
		expectedStringsArray = expectedStrings.toArray(expectedStringsArray);
		obtainedStringsArray = obtainedStrings.toArray(obtainedStringsArray);
		Diff diff = new Diff(expectedStringsArray, obtainedStringsArray);
		Diff.change change = diff.diff_2(false);

		// Obtain insertions and deletions
		int insertions = countInsertions(change);
		int deletions = countDeletions(change);
		
		int averageChange = (insertions + deletions) / 2;
		
		int correctLines = Math.max(0, obtainedStringsArray.length - averageChange);
		
		// Construct a test result.
		TestResult testResult = new TestResult();
		testResult.setFinishTime(new Date());
		testResult.setMaxMark(Math.max(obtainedStringsArray.length, expectedStringsArray.length));
		testResult.setResult(correctLines);
		testResult.setComment("Output had " + insertions + " added line(s) and " + deletions + " removed line(s) compared to expected output, receiving a penalty of " + (averageChange) + ".");

		// Use DiffPrint to construct output and append it with the actual output
	    DiffPrint.Base print = new DiffPrint.UnifiedPrint(expectedStringsArray, obtainedStringsArray);
	    StringWriter output = new StringWriter();
	    output.append("Execution result\n");
	    output.append("========= =====\n");
	    for (String line : outputStrings) {
	    	output.append(line + "\n");
	    }
	    for (String line : errorStrings) {
	    	output.append(line + "\n");
	    }
	    output.append("EXIT CODE> " + executionResult.getExitCode());
	    output.append("\n\n");
	    
	    output.append("Test result\n");
	    output.append("==== ======\n");
	    output.append(" - Comparing output lines\n");
    	output.append(" - " + (compareErrors ? "Comparing" : "Ignoring") + " error lines\n");
    	output.append(" - " + (compareExitCode ? "Comparing" : "Ignoring") + " exit code\n");

	    print.setOutput(output);
	    print.print_header("Missing output", "Extra output");

	    try {
	      print.print_script(change);
	    }
	    catch (ArrayIndexOutOfBoundsException aioobe) {
	      aioobe.printStackTrace();
	      testResult.setOutput("Diff output unavailable due to a bug in BOSS.");
	    }

	    testResult.setOutput(output.getBuffer().toString());

		return testResult;
	}

}
