package uk.ac.warwick.dcs.boss.model.testing;

/**
 * The result of executing a command with an ITestExecutor.
 * 
 * Contains output, errors, and information on the finish state.
 * @author davidbyard
 *
 */
public class ExecutionResult {

	/**
	 * stdout of the run command.
	 */
	private String output;
	
	/**
	 * stderr of the run command.
	 */
	private String errors;
	
	/**
	 * Exit code of the run command.
	 */
	private int exitCode;
	
	/**
	 * Did the command fail due to an internal error?
	 */
	private boolean interruptedByException;
	
	/**
	 * Did the command complete (if not, it may have been killed by an exception or running out of time.)
	 */
	private boolean finished;
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	public String getOutput() {
		return output;
	}
	
	public void setErrors(String errors) {
		this.errors = errors;
	}
	
	public String getErrors() {
		return errors;
	}
	
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	
	public int getExitCode() {
		return exitCode;
	}
	
	public void setInterruptedByException(boolean interruptedByException) {
		this.interruptedByException = interruptedByException;
	}
	
	public boolean isInterruptedByException() {
		return interruptedByException;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isFinished() {
		return finished;
	}
}
