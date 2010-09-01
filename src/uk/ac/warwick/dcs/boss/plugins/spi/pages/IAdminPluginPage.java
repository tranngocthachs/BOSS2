package uk.ac.warwick.dcs.boss.plugins.spi.pages;

import uk.ac.warwick.dcs.boss.frontend.Page.AccessLevel;
import uk.ac.warwick.dcs.boss.plugins.IPluginPage;

public abstract class IAdminPluginPage extends IPluginPage {
	
	@Override
	public AccessLevel getAccessLevel() {
		return AccessLevel.ADMIN;
	}
}
