package uk.ac.warwick.dcs.boss.model.testing.tests.impl;

import java.util.Collection;
import java.util.LinkedList;

import uk.ac.warwick.dcs.boss.model.testing.tests.ITestMethodDirectory;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodDescription;
import uk.ac.warwick.dcs.boss.model.testing.tests.TestMethodParameterDescription;
import uk.ac.warwick.dcs.boss.model.testing.tests.impl.regex.RegexTest;

public class BuiltInTestMethodDirectory implements ITestMethodDirectory {

	public Collection<TestMethodDescription> getTestMethodDescriptions() {
		// Random test
		TestMethodDescription randomTestDescription = new TestMethodDescription();
		randomTestDescription.setClassName(RandomTest.class.getCanonicalName());
		randomTestDescription.setName("Random Test");
		randomTestDescription.setDescription("Generate a random number between two values");
		
		LinkedList<TestMethodParameterDescription> randomTestParameters = new LinkedList<TestMethodParameterDescription>();
		TestMethodParameterDescription rangemin = new TestMethodParameterDescription();
		rangemin.setDescription("Lower bound of the random result");
		rangemin.setName("rangemin");
		rangemin.setOptional(false);
		randomTestParameters.add(rangemin);
		
		TestMethodParameterDescription rangemax = new TestMethodParameterDescription();
		rangemax.setDescription("Upper bound of the random result");
		rangemax.setName("rangemax");
		rangemax.setOptional(false);
		randomTestParameters.add(rangemax);
		
		randomTestDescription.setParameters(randomTestParameters);
	
		// Diff test
		TestMethodDescription diffTestDescription = new TestMethodDescription();
		diffTestDescription.setClassName(DiffTest.class.getCanonicalName());
		diffTestDescription.setName("Diff Test");
		diffTestDescription.setDescription("Compare output of command to expected result.");
		
		LinkedList<TestMethodParameterDescription> diffTestParameters = new LinkedList<TestMethodParameterDescription>();
		TestMethodParameterDescription expected = new TestMethodParameterDescription();
		expected.setDescription("Expected output.  Output lines will be prepended with 'OUTPUT&gt; ', error lines with 'ERROR&gt; ' and the exit code 'EXIT CODE&gt; '.");
		expected.setName("expected");
		expected.setOptional(false);
		diffTestParameters.add(expected);
		
		TestMethodParameterDescription ignoreAllSpace = new TestMethodParameterDescription();
		ignoreAllSpace.setDescription("Ignore all spacing in output (true/false)");
		ignoreAllSpace.setName("ignoreAllSpace");
		ignoreAllSpace.setOptional(false);
		diffTestParameters.add(ignoreAllSpace);
		
		TestMethodParameterDescription ignoreSpaceChange = new TestMethodParameterDescription();
		ignoreSpaceChange.setDescription("Ignore indentation in output (true/false)");
		ignoreSpaceChange.setName("ignoreSpaceChange");
		ignoreSpaceChange.setOptional(false);
		diffTestParameters.add(ignoreSpaceChange);

		TestMethodParameterDescription ignoreBlankLines = new TestMethodParameterDescription();
		ignoreBlankLines.setDescription("Ignore blank lines in output (true/false)");
		ignoreBlankLines.setName("ignoreBlankLines");
		ignoreBlankLines.setOptional(false);
		diffTestParameters.add(ignoreBlankLines);
		
		TestMethodParameterDescription compareErrors = new TestMethodParameterDescription();
		compareErrors.setDescription("Include error output in the test (true/false)");
		compareErrors.setName("compareErrors");
		compareErrors.setOptional(false);
		diffTestParameters.add(compareErrors);
		
		TestMethodParameterDescription compareExitCode = new TestMethodParameterDescription();
		compareExitCode.setDescription("Include exit code in the test (true/false)");
		compareExitCode.setName("compareExitCode");
		compareExitCode.setOptional(false);
		diffTestParameters.add(compareExitCode);
		
		diffTestDescription.setParameters(diffTestParameters);
		
		// Regex test
		TestMethodDescription regexTestDescription = new TestMethodDescription();
		regexTestDescription.setClassName(RegexTest.class.getCanonicalName());
		regexTestDescription.setName("Regex Test");
		regexTestDescription.setDescription("Run a regex testing script against result");
		
		LinkedList<TestMethodParameterDescription> regexTestParameters = new LinkedList<TestMethodParameterDescription>();
		TestMethodParameterDescription script = new TestMethodParameterDescription();
		script.setDescription("Testing script.  Implements Oracle standard, found at http://www.cs.nott.ac.uk/~ceilidh/papers/Oracle.html.  Note: BEFORE and AFTER statements are not supported.  Comments start with a hash (#).  Output lines will be prepended with 'OUTPUT$gt; ', error lines with 'ERROR&gt; ' and the exit code 'EXIT CODE&gt; '.");
		script.setName("script");
		script.setOptional(false);
		regexTestParameters.add(script);
		
		TestMethodParameterDescription matchWholeLine = new TestMethodParameterDescription();
		matchWholeLine.setDescription("Match whole line instead of part of line (true/false)");
		matchWholeLine.setName("matchWholeLine");
		matchWholeLine.setOptional(false);
		regexTestParameters.add(matchWholeLine);
		
		TestMethodParameterDescription matchErrors = new TestMethodParameterDescription();
		matchErrors.setDescription("Include error output in the test (true/false)");
		matchErrors.setName("matchErrors");
		matchErrors.setOptional(false);
		regexTestParameters.add(matchErrors);
		
		TestMethodParameterDescription matchExitCode = new TestMethodParameterDescription();
		matchExitCode.setDescription("Include exit code in the test (true/false)");
		matchExitCode.setName("matchExitCode");
		matchExitCode.setOptional(false);
		regexTestParameters.add(matchExitCode);
		
		
		regexTestDescription.setParameters(regexTestParameters);
		
		// Done
		LinkedList<TestMethodDescription> result = new LinkedList<TestMethodDescription>();
		result.add(randomTestDescription);
		result.add(diffTestDescription);
		result.add(regexTestDescription);
		return result;

	}

}
