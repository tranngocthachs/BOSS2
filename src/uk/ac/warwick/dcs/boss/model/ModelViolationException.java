package uk.ac.warwick.dcs.boss.model;

/**
 * The model exception appears to be never used, but it makes the
 * directory look less empty.
 * 
 * TODO: throw when the model is violated.  For example, when you have a closing
 * date before the opening date in an assignment, or other such niceties.
 * 
 * :-)
 * 
 * @author davidbyard
 *
 */
public class ModelViolationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ModelViolationException() {
	}

	public ModelViolationException(String arg0) {
		super(arg0);
	}

	public ModelViolationException(Throwable arg0) {
		super(arg0);
	}

	public ModelViolationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
