package uk.ac.warwick.dcs.boss.model.utilities;

import java.util.Collection;

public interface IAdminUtilityDirectory {

	/**
	 * Fetch a list of descriptions of known IAdminUtility classes.
	 * @return a list of AdminUtilityDescription objects.
	 */
	public abstract Collection<AdminUtilityDescription> getAdminUtilityDescriptions();

	
}
