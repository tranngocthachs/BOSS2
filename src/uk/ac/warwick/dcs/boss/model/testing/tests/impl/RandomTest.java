package uk.ac.warwick.dcs.boss.model.testing.tests.impl;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import uk.ac.warwick.dcs.boss.model.testing.ExecutionResult;
import uk.ac.warwick.dcs.boss.model.testing.TestResult;
import uk.ac.warwick.dcs.boss.model.testing.TestingException;
import uk.ac.warwick.dcs.boss.model.testing.tests.ITestMethod;

public class RandomTest implements ITestMethod {
	
	public TestResult test(Map<String, String> parameters, ExecutionResult executionResult) throws TestingException {
		TestResult testResult = new TestResult();
		
		Random random = new Random();
		int min = Integer.valueOf(parameters.get("rangemin"));
		int max = Integer.valueOf(parameters.get("rangemax"));
		int result = random.nextInt(max - min) + min;
	
		int sleepTime = random.nextInt(10 - 3) + 3;
		try { Thread.sleep(sleepTime * 1000); } catch (InterruptedException e) {}
		
		testResult.setResult(result);
		testResult.setMaxMark(max);
		testResult.setComment("Slept for " + sleepTime + " seconds and generated random number between " + min + " and " + max);
		testResult.setFinishTime(new Date());
		
		return testResult;
	}
		
}
