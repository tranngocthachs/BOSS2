package uk.ac.warwick.dcs.boss.model;

/**
 * Thrown when factories can't initialise. 
 * @author davidbyard
 *
 */
public class FactoryException extends Exception {

	private static final long serialVersionUID = 1L;

	public FactoryException() {
	}

	public FactoryException(String arg0) {
		super(arg0);
	}

	public FactoryException(Throwable arg0) {
		super(arg0);
	}

	public FactoryException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
