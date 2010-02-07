/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.warwick.dcs.boss.model.session;

/**
 * An exception thrown whenever a user was not found (or authentication failed)
 * @author davidbyard
 */
public class UserNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public UserNotFoundException(String reason, Throwable cause) {
        super(reason, cause);
    }
    
    public UserNotFoundException(String reason) {
        super(reason);
    }
    
    public UserNotFoundException(Throwable cause) {
        super(cause);
    }
    
}
