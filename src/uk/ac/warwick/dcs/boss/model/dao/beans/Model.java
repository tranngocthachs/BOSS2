package uk.ac.warwick.dcs.boss.model.dao.beans;

/**
 * The model is a top level entity containing an entire curriculum.
 * 
 * For example, Computer Science 2007/2008, or Singing Competition.
 * @author davidbyard
 *
 */
public class Model extends Entity {

	/**
	 * A unique identifier that cannot be shared with any other model.
	 */
	private String uniqueIdentifier;
	
	/**
	 * The name of this model.
	 */
	private String name;

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
