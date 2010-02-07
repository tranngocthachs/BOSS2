package uk.ac.warwick.dcs.boss.frontend;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import uk.ac.warwick.dcs.boss.model.session.UserSession;

/**
 * A simple servlet session containing a BOSS2 session.
 * Has a listener for automatically logging out user sessions when required.
 * @author davidbyard
 *
 */
public class PageSession implements
		HttpSessionBindingListener {

	private UserSession userSession;
	
	public PageSession(UserSession session) {
		this.userSession = session;
	}
	
	public UserSession getUserSession() {
		return userSession;
	}
		
	public void valueBound(HttpSessionBindingEvent event) {
		userSession.login();
	}

	public void valueUnbound(HttpSessionBindingEvent event) {
		userSession.logout();
	}

}
