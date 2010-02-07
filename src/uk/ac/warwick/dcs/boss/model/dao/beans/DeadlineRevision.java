package uk.ac.warwick.dcs.boss.model.dao.beans;

import java.util.Date;

/**
 * A deadline revision provides relief for students that need to submit after an assignment's closing time.
 * @author davidbyard
 *
 */
public class DeadlineRevision extends Entity {
	/**
	 * Why the deadline extension was provided.
	 */
	private String comment;
	
	/**
	 * The new hard deadline.
	 */
	private Date newDeadline;
	
	/**
	 * The assignment to extend the deadline for.
	 */
	private Long assignmentId;
	
	/**
	 * The person granted the extension.
	 */
	private Long personId;

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public void setNewDeadline(Date deadline) {
		this.newDeadline = deadline;
	}

	public Date getNewDeadline() {
		return newDeadline;
	}

	public void setAssignmentId(
			Long parentAssignmentId) {
		this.assignmentId = parentAssignmentId;
	}

	public Long getAssignmentId() {
		return assignmentId;
	}

	public void setPersonId(Long parentPersonId) {
		this.personId = parentPersonId;
	}

	public Long getPersonId() {
		return personId;
	}

}
