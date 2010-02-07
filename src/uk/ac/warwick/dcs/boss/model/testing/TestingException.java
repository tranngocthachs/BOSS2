package uk.ac.warwick.dcs.boss.model.testing;

/**
 * Execption thrown when there's a problem with testing.
 * @author davidbyard
 *
 */
public class TestingException extends Exception {

	private static final long serialVersionUID = 1L;

	public TestingException() {
		super();
	}

	public TestingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public TestingException(String arg0) {
		super(arg0);
	}

	public TestingException(Throwable arg0) {
		super(arg0);
	}

	
}
