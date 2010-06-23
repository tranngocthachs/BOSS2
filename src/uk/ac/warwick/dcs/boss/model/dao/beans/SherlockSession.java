package uk.ac.warwick.dcs.boss.model.dao.beans;

public class SherlockSession extends Entity {

	private Long assignmentId;
	private Long resourceId;
	/**
	 * @return the assignmentId
	 */
	public Long getAssignmentId() {
		return assignmentId;
	}
	/**
	 * @param assignmentId the assignmentId to set
	 */
	public void setAssignmentId(Long assignmentId) {
		this.assignmentId = assignmentId;
	}
	/**
	 * @return the resourceId
	 */
	public Long getResourceId() {
		return resourceId;
	}
	/**
	 * @param resourceId the resourceId to set
	 */
	public void setResourceId(Long submissionId) {
		this.resourceId = submissionId;
	}
}
