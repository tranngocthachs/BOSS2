package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Assignment;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

/**
 * A specialised query result for the marker interface.
 * 
 * Contains an assignment, its parent module, and how many students the assignment has to mark.
 * @author davidbyard
 *
 */
public class MarkerAssignmentsQueryResult {
	/**
	 * The assignment.
	 */
	private Assignment assignment;
	
	/**
	 * The assignment's parent module.
	 */
	private Module parentModule;
	
	/**
	 * How many students the assignment has to mark.
	 */
	private long studentCount;
	
	public void setAssignment(Assignment assignment) {
		this.assignment = assignment;
	}
	
	public Assignment getAssignment() {
		return assignment;
	}

	public void setParentModule(Module parentModule) {
		this.parentModule = parentModule;
	}

	public Module getParentModule() {
		return parentModule;
	}

	public void setStudentCount(long studentCount) {
		this.studentCount = studentCount;
	}

	public long getStudentCount() {
		return studentCount;
	}
	
}
