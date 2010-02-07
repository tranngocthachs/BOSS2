package uk.ac.warwick.dcs.boss.model.utilities;

/**
 * Execption thrown when there's a problem with running an admin utility.
 * @author davidbyard
 *
 */
public class AdminUtilityException extends Exception {

	private static final long serialVersionUID = 1L;

	public AdminUtilityException() {
		super();
	}

	public AdminUtilityException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AdminUtilityException(String arg0) {
		super(arg0);
	}

	public AdminUtilityException(Throwable arg0) {
		super(arg0);
	}

	
}
