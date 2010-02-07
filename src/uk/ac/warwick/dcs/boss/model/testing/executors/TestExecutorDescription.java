package uk.ac.warwick.dcs.boss.model.testing.executors;

/**
 * A descriptor class for ITestExecutor classes.  Used by factories.
 * @author davidbyard
 *
 */
public class TestExecutorDescription {

	/**
	 * NAme of the ITestExecutor class.
	 */
	private String name;
	
	/**
	 * What the ITestExecutor does.
	 */
	private String description;
	
	/**
	 * Class name of the ITestExecutor.
	 */
	private String className;

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
	
}
