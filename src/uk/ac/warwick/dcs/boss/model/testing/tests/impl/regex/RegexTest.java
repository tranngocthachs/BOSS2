package uk.ac.warwick.dcs.boss.model.testing.tests.impl.regex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;
import uk.ac.warwick.dcs.boss.model.testing.tests.ITestMethod;

public class RegexTest implements ITestMethod {

	public static final int MAX_LINES = 1000; 

	private static LinkedList<String> readLines(Reader reader, int max, String prefix) throws IOException {
		LinkedList<String> lines = new LinkedList<String>();
		BufferedReader br = new BufferedReader(reader);
		int i;

		for (i = 0; i < max; i++) {
			String line = br.readLine();
			if (line == null) {
				break;
			} else {
				lines.add(prefix + line);
			}
		}

		if (i == max) {
			lines.add("<Output truncated at " + max + "lines>");
		}

		return lines;
	}

	public TestResult test(Map<String, String> parameters, ExecutionResult executionResult)
	throws TestingException {
		// Obtain parameters
		String script = parameters.get("script");
		boolean matchWholeLine = Boolean.valueOf(parameters.get("matchWholeLine"));
		boolean matchErrors = Boolean.valueOf(parameters.get("matchErrors"));
		boolean matchExitCode = Boolean.valueOf(parameters.get("matchExitCode"));

		// Get lists of strings to compare.
		LinkedList<String> outputStrings = null;
		LinkedList<String> errorStrings = null;
		LinkedList<String> scriptStrings = null;
		LinkedList<String> obtainedStrings = new LinkedList<String>();

		try { 
			scriptStrings = readLines(new StringReader(script), MAX_LINES, "");
			outputStrings = readLines(new StringReader(executionResult.getOutput()), MAX_LINES, "OUTPUT> ");
			errorStrings = readLines(new StringReader(executionResult.getErrors()), MAX_LINES,  "ERROR> ");
		} catch (IOException e) {
			throw new TestingException("IO error", e);
		}

		obtainedStrings.addAll(outputStrings);
		if (matchErrors) {
			obtainedStrings.addAll(errorStrings);
		}
		if (matchExitCode) {
			obtainedStrings.add("EXIT CODE> " + executionResult.getExitCode());
		}
		
		// Convert to arrays
		String scriptStringsArray[] = new String[scriptStrings.size()];
		String obtainedStringsArray[] = new String[obtainedStrings.size()];
		scriptStringsArray = scriptStrings.toArray(scriptStringsArray);
		obtainedStringsArray = obtainedStrings.toArray(obtainedStringsArray);

		// Get rules
		Vector<RegexRule> rules = parseRules(scriptStringsArray, matchWholeLine);
	    		
		//  Store the number of matches each rule has got
	    int[] matchCount = new int[rules.size()];

	    matchLines(matchCount, rules, obtainedStringsArray);
	    
	    // Construct output
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
	    output.append(" - Matching output lines\n");
    	output.append(" - " + (matchErrors ? "Matching" : "Ignoring") + " error lines\n");
    	output.append(" - " + (matchExitCode ? "Matching" : "Ignoring") + " exit code\n");
    	output.append(RegexRule.feedbackString(rules, matchCount));
    	
	    TestResult result = new TestResult();
	    result.setOutput(output.toString());
	    result.setResult(RegexRule.countScore(rules, matchCount));
	    result.setMaxMark(RegexRule.countMaxScore(rules, matchCount));
	    result.setComment("Checked output against " + matchCount.length + " regex(es).");
	    result.setFinishTime(new Date());
		return result;
	}

	private Vector<RegexRule> parseRules(String lines[], boolean matchWholeLine) throws TestingException {
		Vector<RegexRule> rules = new Vector<RegexRule>();
		for (String line : lines) {
			line = line.trim();
			if (line.startsWith("#") || line.startsWith("//")) {
				// Comment line
				continue;
			}
			if (line.length() == 0) {
				// Empty line
				continue;
			}

			try {
				RegexRule rule = null;

				rule = RegexRule.parseRule(line);
				rule.setMatchWholeLine(matchWholeLine);

				// Warn about things that are not implemented
				if (rule.getOperator() == RegexRule.AFTER
						|| rule.getOperator() == RegexRule.NOT_AFTER) {
					throw new TestingException("Operators + (AFTER) and - " +
							"(NOT AFTER) are not implemented in " +
					"RegexTest.java.");
				}

				rules.add(rule);
			}
			catch (RegexRuleSyntaxException rrse) {
				throw new TestingException("Syntax error in " +
						"rule: " + rrse.toString());
			}
		}

		return rules;
	}

	/**
	 * Match lines with the rules. Update matchCount array.
	 *
	 * The matchCount parameter will be updated during this method.
	 *
	 * @param matchCount matchCount.length has to be equal or greater than
	 *        rules.size(). Each match to a rule in rules will increment
	 *        the corresponding int in matchCount
	 * @param rules rules to match the lines against
	 * @param lines the lines
	 */
	private void matchLines(int matchCount[], Vector<RegexRule> rules, 
			String[] lines) {
		for (int lineNum = 0; lineNum < lines.length; lineNum++) {
			String line = lines[lineNum];

			// Each rule may match a line. A line may be matched by
			// several rules at a time.
			for (int ruleNum = 0; ruleNum < rules.size(); ruleNum++) {
				RegexRule rule = (RegexRule) rules.get(ruleNum);

				if (rule.matches(line)) {
					matchCount[ruleNum] = matchCount[ruleNum] + 1;
				}
			}
		}
	}

}
