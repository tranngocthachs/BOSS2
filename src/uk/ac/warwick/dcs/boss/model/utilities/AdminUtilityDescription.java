package uk.ac.warwick.dcs.boss.model.utilities;

import java.util.Collection;

/**
 * A descriptor class for IAdminUtility classes.  Used by factories.
 * @author davidbyard
 *
 */
public class AdminUtilityDescription {

	/**
	 * The name of the admin utility.
	 */
	private String name;
	
	/**
	 * What the admin utility does.
	 */
	private String description;
	
	/**
	 * The class name of the admin utility.
	 */
	private String className;
	
	/**
	 * The parameters that the admin utility requires.
	 */
	private Collection<AdminUtilityParameterDescription> parameters;
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
	public void setClassName(String className) {
		this.className = className;
	}
	public String getClassName() {
		return className;
	}
	public void setParameters(Collection<AdminUtilityParameterDescription> parameters) {
		this.parameters = parameters;
	}
	public Collection<AdminUtilityParameterDescription> getParameters() {
		return parameters;
	}
	
}
