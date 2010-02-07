package uk.ac.warwick.dcs.boss.model.dao;

/**
 * Localisation stubs.  Localisation will be added in the future, but is not part of BOSS2 1.0.
 * @author davidbyard
 */
public interface ILocalisationDAO {

	abstract public String getLocalisation(String locale, String original) throws DAOException;
	abstract public void addLocalisation(String locale, String original, String translation) throws DAOException;
	abstract public void removeLocalisation(String locale, String original) throws DAOException;
	
	
}
