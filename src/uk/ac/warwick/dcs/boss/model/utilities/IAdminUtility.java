package uk.ac.warwick.dcs.boss.model.utilities;

import java.util.Map;

/**
 * A class that provides a pluggable utility for the admin interface.
 * @author davidbyard
 *
 */
public interface IAdminUtility {

	/**
	 * Mark an execution result.
	 * @param parameters are the parameters of the utility.
	 * @return an AdminUtilityResult detailing the execution result.
	 * @throws AdminUtilityException if something bad happens.
	 */
	public AdminUtilityResult execute(Map<String, String> parameters) throws AdminUtilityException;
	
}
