package uk.ac.warwick.dcs.boss.model.testing.tests;

import java.util.Collection;

/**
 * A descriptor class for ITestMethod classes.  Used by factories.
 * @author davidbyard
 *
 */
public class TestMethodDescription {

	/**
	 * The name of the testing method.
	 */
	private String name;
	
	/**
	 * What the testing method does.
	 */
	private String description;
	
	/**
	 * The class name of the testing method.
	 */
	private String className;
	
	/**
	 * The parameters that the testing method requires.
	 */
	private Collection<TestMethodParameterDescription> parameters;
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
	public void setParameters(Collection<TestMethodParameterDescription> parameters) {
		this.parameters = parameters;
	}
	public Collection<TestMethodParameterDescription> getParameters() {
		return parameters;
	}
	
}
