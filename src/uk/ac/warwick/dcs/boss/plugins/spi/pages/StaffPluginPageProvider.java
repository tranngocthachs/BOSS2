package uk.ac.warwick.dcs.boss.plugins.spi.pages;

import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public abstract class StaffPluginPageProvider extends PluginPageProvider {
	
	protected StaffPluginPageProvider(String pageTemplate) throws PageLoadException {
		super(pageTemplate, AccessLevel.USER);
	}
	
	public StaffPluginPageProvider() throws PageLoadException {
		this(null);
	}
}
