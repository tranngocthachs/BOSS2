package uk.ac.warwick.dcs.boss.model.dao.beans;

import java.util.Date;

/**
 * The entire purpose of BOSS2, part one.  A submission from a student!
 * @author davidbyard
 *
 */
public class Submission extends Entity {
	/**
	 * The person that made this submission.
	 */
	private Long personId;
	
	/**
	 * The assignment the submission is for.
	 */
	private Long assignmentId;
	
	/**
	 * The time the submission was made.
	 */
	private Date submissionTime;
	
	/**
	 * The zip resource storing the submission.
	 */
	private Long resourceId;
	
	/**
	 * The subdirectory in the resource that the submission's files are contained.
	 * 
	 * This is a bit of a kludge, but it means that the submissions can have uniquiely identifying subdirectories
	 * so that all of them could be extracted in one fell swoop by a marker.
	 */
	private String resourceSubdirectory;
	
	/**
	 * The security code of the submission.
	 */
	private String securityCode;
	
	/**
	 * Whether the submitter wishes this submission to be marked.
	 */
	private Boolean active;
	
	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	public Long getPersonId() {
		return personId;
	}

	public void setAssignmentId(Long assignmentId) {
		this.assignmentId = assignmentId;
	}

	public Long getAssignmentId() {
		return assignmentId;
	}

	public void setSubmissionTime(Date lastSubmissionId) {
		this.submissionTime = lastSubmissionId;
	}

	public Date getSubmissionTime() {
		return submissionTime;
	}
	
	public void setResourceId(Long resourceUri) {
		this.resourceId = resourceUri;
	}

	public Long getResourceId() {
		return resourceId;
	}
	
	public void setResourceSubdirectory(String resourceSubdirectory) {
		this.resourceSubdirectory = resourceSubdirectory;
	}

	public String getResourceSubdirectory() {
		return resourceSubdirectory;
	}

	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}

	public String getSecurityCode() {
		return securityCode;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getActive() {
		return active;
	}
}
