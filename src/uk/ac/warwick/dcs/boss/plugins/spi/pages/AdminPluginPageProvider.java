package uk.ac.warwick.dcs.boss.plugins.spi.pages;

import uk.ac.warwick.dcs.boss.frontend.Page.AccessLevel;
import uk.ac.warwick.dcs.boss.plugins.PluginPageProvider;

public abstract class AdminPluginPageProvider extends PluginPageProvider {
	
	@Override
	public AccessLevel getAccessLevel() {
		return AccessLevel.ADMIN;
	}
}
