package uk.ac.warwick.dcs.boss.model.testing;

import java.util.Date;

/**
 * A result obtained by testing a ExecutionResult with an ITestMethod.
 * @author davidbyard
 *
 */
public class TestResult {
	
	/**
	 * Output of the test.
	 */
	private String output;
	
	/**
	 * Short comment describing the result.
	 */
	private String comment;
	
	/**
	 * The result obtained.
	 */
	private int result;
	
	/**
	 * The maximum result possible.
	 */
	private int maxMark;
	
	/**
	 * When the testing completed.
	 */
	private Date finishTime;
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}
		
	public void setResult(int result) {
		this.result = result;
	}
	
	public int getResult() {
		return result;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setMaxMark(int maxMark) {
		this.maxMark = maxMark;
	}

	public int getMaxMark() {
		return maxMark;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getOutput() {
		return output;
	}	
}
