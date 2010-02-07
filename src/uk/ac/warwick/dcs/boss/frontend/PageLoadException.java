package uk.ac.warwick.dcs.boss.frontend;

/**
 * An execption thrown when pages couldn't be loaded.  Contains an HTTP error code.
 * @author davidbyard
 *
 */
public class PageLoadException extends Exception {
	private static final long serialVersionUID = 1L;

	private int httpErrorEquivalent;
	
	public PageLoadException(int httpErrorEquivalent) {
		this.httpErrorEquivalent = httpErrorEquivalent;
	}

	public PageLoadException(int httpErrorEquivalent, String reason) {
		super(reason);
		this.httpErrorEquivalent = httpErrorEquivalent;
	}

	public PageLoadException(int httpErrorEquivalent, Throwable cause) {
		super(cause);
		this.httpErrorEquivalent = httpErrorEquivalent;
	}

	public PageLoadException(int httpErrorEquivalent, String reason, Throwable cause) {
		super(reason, cause);
		this.httpErrorEquivalent = httpErrorEquivalent;
	}

	public int getHttpErrorEquivalent() {
		return httpErrorEquivalent;
	}

}
