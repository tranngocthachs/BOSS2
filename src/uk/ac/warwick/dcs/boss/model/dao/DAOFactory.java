package uk.ac.warwick.dcs.boss.model.dao;

import uk.ac.warwick.dcs.boss.model.Factory;
import uk.ac.warwick.dcs.boss.model.FactoryException;

/**
 * The work-horse of the model.  The DAOFactory instantiates all known DAOs, the
 * only way to access data in the BOSS 2 model.
 * @author davidbyard
 *
 */
public abstract class DAOFactory extends Factory<IDAOSession> {
	// Extra
	/**
	 * This returns a secret used for salting submission hashes.
	 */
	abstract public String getSubmissionHashSalt() throws FactoryException;
}
