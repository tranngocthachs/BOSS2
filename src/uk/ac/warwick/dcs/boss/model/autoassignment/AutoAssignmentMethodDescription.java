package uk.ac.warwick.dcs.boss.model.autoassignment;

/**
 * The AutoAssignmentMethodDescription provides information about an IAutoAssignmentMethodClass.
 * 
 * It is returned when querying an AutoAssignmentMethodFactory.
 * @author davidbyard
 *
 */
public class AutoAssignmentMethodDescription {
	private String name;
	private String className;
	private String description;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
