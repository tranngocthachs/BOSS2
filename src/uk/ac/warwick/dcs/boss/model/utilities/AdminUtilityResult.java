package uk.ac.warwick.dcs.boss.model.utilities;

import java.util.Date;

/**
 * A result obtained by executing an admin utility.
 * @author davidbyard
 *
 */
public class AdminUtilityResult {
	
	/**
	 * Output of the test.
	 */
	private String output;
	
	/**
	 * Short comment describing the result.
	 */
	private String comment;

	/**
	 * Whether the utility was successful.
	 */	
	private boolean success;
	
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
		

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean getSuccess() {
		return success;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getOutput() {
		return output;
	}	
}
