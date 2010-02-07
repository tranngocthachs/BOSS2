package uk.ac.warwick.dcs.boss.model.dao.beans;

/**
 * The module is the top level part of a curriculum.  It could loosely be likened to a class.
 * @author davidbyard
 *
 */
public class Module extends Entity {
	/**
	 * Name of the module.
	 */
	private String name;
	
	/**
	 * A unique identifier that isn't shared with any other module.
	 */
	private String uniqueIdentifier;
	
	/**
	 * The parent model.
	 */
	private Long modelId;
	
	/**
	 * Whether registration as a student is required to submit solutions to this module's assignments.
	 */
	private Boolean registrationRequired;
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

	public Long getModelId() {
		return modelId;
	}

	public Boolean isRegistrationRequired() {
		return registrationRequired;
	}
	
	public void setRegistrationRequired(boolean registrationRequired) {
		this.registrationRequired = registrationRequired;
	}

}
