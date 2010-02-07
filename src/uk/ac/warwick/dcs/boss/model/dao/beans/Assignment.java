package uk.ac.warwick.dcs.boss.model.dao.beans;

import java.util.Date;

/**
 * An assignment bean.  This is an assignment that a student can submit answers for.
 * @author davidbyard
 *
 */
public class Assignment extends Entity {
	/**
	 * Name of the assignment.
	 */
	private String name;
	
	/**
	 * Weak deadline for the assignment.
	 */
	private Date deadline;
	
	/**
	 * Time that people may start submitting solutions after.
	 */
	private Date openingTime;
	
	/**
	 * Hard deadline for the assignment, though deadlinerevisions may override this.
	 */
	private Date closingTime;
	
	/**
	 * Parent module.
	 */
	private Long moduleId;
	
	/**
	 * Optional resource for the module (libraries, documentation, etc)
	 */
	private Long resourceId;
	
	/**
	 * Flag saying whether students may delete submissions after providing them.
	 */
	private Boolean allowDeletion;
	

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setOpeningTime(Date openingTime) {
		this.openingTime = openingTime;
	}

	public Date getOpeningTime() {
		return openingTime;
	}

	public void setClosingTime(Date closing) {
		this.closingTime = closing;
	}

	public Date getClosingTime() {
		return closingTime;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	public Long getModuleId() {
		return moduleId;
	}

	public void setResourceId(Long resourceUri) {
		this.resourceId = resourceUri;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setAllowDeletion(Boolean allowDeletion) {
		this.allowDeletion = allowDeletion;
	}

	public Boolean getAllowDeletion() {
		return allowDeletion;
	}
}
