package uk.ac.warwick.dcs.boss.model.dao.beans;

/**
 * The entire purpose of BOSS2, part 2.
 * 
 * A test that can be performed on a submission.  ITestMethods require parameters.  These are handled
 * by the DAO.
 * @author davidbyard
 *
 */
public class Test extends Entity {
	/**
	 * Whether a student may run this test.
	 */
	private Boolean studentTest;
	
	/**
	 * The command for the IExecutorMethod to run.
	 */
	private String command;
	
	/**
	 * The ITestMethod class name.
	 */
	private String testClassName;
	
	/**
	 * The IExecutorMethod class name.
	 */
	private String executorClassName;
	
	/**
	 * How long the IExecutorMethod is allowed to try running the given command for.
	 */
	private Integer maximumExecutionTime;
	
	/**
	 * Name of the test.
	 */
	private String name;
	
	/**
	 * The assignment that this test is for.
	 */
	private Long assignmentId;
	
	/**
	 * Resource file passed to the IExecutorMethod.
	 */
	private Long resourceId;
	
	public void setStudentTest(Boolean studentTest) {
		this.studentTest = studentTest;
	}

	public Boolean getStudentTest() {
		return studentTest;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public void setTestClassName(String className) {
		this.testClassName = className;
	}

	public String getTestClassName() {
		return testClassName;
	}

	public void setExecutorClassName(String executorClassName) {
		this.executorClassName = executorClassName;
	}

	public String getExecutorClassName() {
		return executorClassName;
	}
	
	public void setMaximumExecutionTime(Integer maximumExecutionTime) {
		this.maximumExecutionTime = maximumExecutionTime;
	}

	public Integer getMaximumExecutionTime() {
		return maximumExecutionTime;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAssignmentId(Long markingCategoryId) {
		this.assignmentId = markingCategoryId;
	}

	public Long getAssignmentId() {
		return assignmentId;
	}

	public void setLibraryResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public Long getLibraryResourceId() {
		return resourceId;
	}
}
