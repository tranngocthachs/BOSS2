package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

/**
 * Specialised query result for the staff frontend.
 * 
 * Contains a module, its parent model, and how many assignments and students it has.
 * @author davidbyard
 *
 */
public class StaffModulesQueryResult {

	/**
	 * The parent model
	 */
	private Model model;
	
	/**
	 * The module
	 */
	private Module module;
	
	/**
	 * How many assignments the module has.
	 */
	private Long assignmentCount;
	
	/**
	 * How many students the module has.
	 */
	private Long studentCount;

	public void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	public Module getModule() {
		return module;
	}

	public void setAssignmentCount(Long assignmentCount) {
		this.assignmentCount = assignmentCount;
	}

	public Long getAssignmentCount() {
		return assignmentCount;
	}

	public void setStudentCount(Long studentCount) {
		this.studentCount = studentCount;
	}

	public Long getStudentCount() {
		return studentCount;
	}

}
