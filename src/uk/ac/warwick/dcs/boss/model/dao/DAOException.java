package uk.ac.warwick.dcs.boss.model.dao;

/**
 * The DAOException is thrown whenever a DAO encounters a problem, such as an SQL
 * exception or a file being missing.
 * @author davidbyard
 */
public class DAOException extends Exception {
	private static final long serialVersionUID = 1L;

	public DAOException() {
	}

	public DAOException(String arg0) {
		super(arg0);
	}

	public DAOException(Throwable arg0) {
		super(arg0);
	}

	public DAOException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
