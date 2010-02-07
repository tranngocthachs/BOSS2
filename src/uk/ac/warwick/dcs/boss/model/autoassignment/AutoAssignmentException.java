package uk.ac.warwick.dcs.boss.model.autoassignment;

/**
 * The AutoAssignmentException is thrown by IAutoAssignmentMethod instances.
 * @author davidbyard
 *
 */
public class AutoAssignmentException extends Exception {

	private static final long serialVersionUID = 1L;

	public AutoAssignmentException() {
		super();
	}

	public AutoAssignmentException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AutoAssignmentException(String arg0) {
		super(arg0);
	}

	public AutoAssignmentException(Throwable arg0) {
		super(arg0);
	}

	
}
