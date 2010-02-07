package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

/**
 * Specialised query result for the admin front end.
 * 
 * Returns a module, along with its parent model and some further details.
 * @author davidbyard
 *
 */
public class AdminModulesQueryResult {

	/**
	 * The parent model.
	 */
	private Model model;
	
	/**
	 * The module.
	 */
	private Module module;
	
	/**
	 * How many assignments the module has.
	 */
	private Long assignmentCount;
	
	/**
	 * How many administrators the module has.
	 */
	private Long administratorCount;

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

	public void setAdministratorCount(Long administratorCount) {
		this.administratorCount = administratorCount;
	}

	public Long getAdministratorCount() {
		return administratorCount;
	}

}
