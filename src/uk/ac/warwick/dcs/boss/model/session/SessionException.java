/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.warwick.dcs.boss.model.session;

/**
 * An exception thrown whenever there's a problem with a session.
 * @author davidbyard
 */
public class SessionException extends Exception {

	private static final long serialVersionUID = 1L;

	public SessionException(String reason, Throwable cause) {
        super(reason, cause);
    }
    
    public SessionException(String reason) {
        super(reason);
    }
    
    public SessionException(Throwable cause) {
        super(cause);
    }
    
}
