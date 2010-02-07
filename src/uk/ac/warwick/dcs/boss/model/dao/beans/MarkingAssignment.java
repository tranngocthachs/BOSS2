package uk.ac.warwick.dcs.boss.model.dao.beans;

/**
 * Much like an Assignment is for students, the MarkingAssignment is for markers, stating
 * who and how they should mark.
 * @author davidbyard
 *
 */
public class MarkingAssignment extends Entity {
	/**
	 * The assignment to mark.
	 */
	private Long assignmentId;
	
	/**
	 * The student to mark.
	 */
	private Long studentId;
	
	/**
	 * The marker doing the marking.
	 */
	private Long markerId;
	
	/**
	 * Whether the marker may see the student's name or not.
	 */
	private Boolean blind;
	
	/**
	 * Whether the marker is a moderator - that is, may see the marks that other markers have created.
	 */
	private Boolean moderator;
	
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
	
	public void setMarkerId(Long markerId) {
		this.markerId = markerId;
	}
	
	public Long getMarkerId() {
		return markerId;
	}
	
	public void setBlind(Boolean blind) {
		this.blind = blind;
	}
	
	public Boolean getBlind() {
		return blind;
	}

	public void setModerator(Boolean moderator) {
		this.moderator = moderator;
	}

	public Boolean getModerator() {
		return moderator;
	}

}
