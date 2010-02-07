package uk.ac.warwick.dcs.boss.model.utilities.impl;

import java.util.Collection;
import java.util.LinkedList;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityDescription;
import uk.ac.warwick.dcs.boss.model.utilities.AdminUtilityParameterDescription;
import uk.ac.warwick.dcs.boss.model.utilities.IAdminUtilityDirectory;

public class BuiltInAdminUtilityDirectory implements IAdminUtilityDirectory {
		
	public Collection<AdminUtilityDescription> getAdminUtilityDescriptions() {
		LinkedList<AdminUtilityDescription> utilities = new LinkedList<AdminUtilityDescription>();
		
		AdminUtilityDescription adsImportDescription = new AdminUtilityDescription();
		adsImportDescription.setClassName(ADSImport.class.getCanonicalName());
		adsImportDescription.setName("ADS Import");
		adsImportDescription.setDescription(
				"Automatically import users, modules and\n" +
				"registrations from the ADS database.\n\n" +
				"WILL NOT WORK OUTSITE DCS."
		);
		adsImportDescription.setParameters(new LinkedList<AdminUtilityParameterDescription>());
		
		utilities.add(adsImportDescription);
		return utilities;
	}

}
