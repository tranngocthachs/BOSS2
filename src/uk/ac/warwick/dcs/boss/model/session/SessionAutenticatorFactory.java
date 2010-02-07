package uk.ac.warwick.dcs.boss.model.session;

import uk.ac.warwick.dcs.boss.model.Factory;

/**
 * The session factory is responsible for authenticators.
 * 
 * It also handles password recovery.
 * @author davidbyard
 *
 */
abstract public class SessionAutenticatorFactory extends Factory<ISessionAuthenticator> {

}
