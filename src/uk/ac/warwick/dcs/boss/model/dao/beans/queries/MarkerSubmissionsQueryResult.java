package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;
import uk.ac.warwick.dcs.boss.model.dao.beans.Submission;

/**
 * Specialised query result for the marker front end.
 * 
 * Contains a submission, the parent assignment, and that assignment's parent module.
 * @author davidbyard
 *
 */
public class MarkerSubmissionsQueryResult {

	/**
	 * The parent assignment's parent module.
	 */
	private Module parentModule;
	
	/**
	 * The submission's parent assignment.
	 */
	private Assignment assignment;
	
	/**
	 * The submission.
	 */
	private Submission submission;
	
	public void setParentModule(Module parentModule) {
		this.parentModule = parentModule;
	}
	
	public Module getParentModule() {
		return parentModule;
	}
	
	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}
	
	public Assignment getAssignment() {
		return assignment;
	}
	
	public void setSubmission(Submission submission) {
		this.submission = submission;
	}
	
	public Submission getSubmission() {
		return submission;
	}
		
}
