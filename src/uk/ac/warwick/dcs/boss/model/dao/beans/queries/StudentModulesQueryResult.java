package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Model;
import uk.ac.warwick.dcs.boss.model.dao.beans.Module;

/**
 * Specialised query result for the student frontend.
 * 
 * Contains a module and the parent model.
 * @author davidbyard
 *
 */
public class StudentModulesQueryResult {

	/**
	 * The parent model.
	 */
	private Model model;
	
	/**
	 * The module.
	 */
	private Module module;

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
}