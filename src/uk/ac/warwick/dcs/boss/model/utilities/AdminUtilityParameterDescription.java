package uk.ac.warwick.dcs.boss.model.utilities;

/**
 * A descriptor for an admin utility parameter.  Used by factories.
 * @author davidbyard
 *
 */
public class AdminUtilityParameterDescription {

	/**
	 * Whether the parameter is optional.
	 */
	private boolean optional;
	
	/**
	 * The name of the parameter.
	 */
	private String name;
	
	/**
	 * What the parameter should contain.
	 */
	private String description;
		
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	
	public boolean isOptional() {
		return optional;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
}
