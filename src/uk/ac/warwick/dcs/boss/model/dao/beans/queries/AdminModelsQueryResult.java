package uk.ac.warwick.dcs.boss.model.dao.beans.queries;

import uk.ac.warwick.dcs.boss.model.dao.beans.Model;

/**
 * Specialised query result for the admin front-end.  A model and how many modules it has.
 * @author davidbyard
 *
 */
public class AdminModelsQueryResult {
	/**
	 * The model.
	 */
	private Model model;
	
	/**
	 * How many modules the model has.
	 */
	private Long moduleCount;

	public void setModel(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}

	public void setModuleCount(Long moduleCount) {
		this.moduleCount = moduleCount;
	}

	public Long getModuleCount() {
		return moduleCount;
	}

}
