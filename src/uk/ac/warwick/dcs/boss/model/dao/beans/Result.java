package uk.ac.warwick.dcs.boss.model.dao.beans;

import java.util.Date;

/**
 * A result represents a final result from all marking.  They are created
 * when an assignment's results are published.
 * @author davidbyard
 */
public class Result extends Entity {

	/**
	 * The assignment this result is for.
	 */
	private Long assignmentId;
	
	/**
	 * The student this result is for.
	 */
	private Long studentId;
	
	/**
	 * The percentage mark of the result.
	 */
	private Double result;
	
	/**
	 * The timestamp of the mark.
	 */
	private Date timestamp;
	
	/**
	 * Flag - did the result have incomplete marking?
	 */
	private Boolean hadIncompleteMarking;
	
	public void setAssignmentId(Long assignmentId) {
		this.assignmentId = assignmentId;
	}
	
	public Long getAssignmentId() {
		return assignmentId;
	}
	
	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}
	
	public Long getStudentId() {
		return studentId;
	}
	
	public void setResult(Double result) {
		this.result = result;
	}
	
	public Double getResult() {
		return result;
	}
		
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setHadIncompleteMarking(Boolean hadIncompleteMarking) {
		this.hadIncompleteMarking = hadIncompleteMarking;
	}
	
	public Boolean getHadIncompleteMarking() {
		return hadIncompleteMarking;
	}
}
