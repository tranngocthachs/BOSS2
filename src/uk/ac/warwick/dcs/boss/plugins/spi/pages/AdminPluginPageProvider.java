package uk.ac.warwick.dcs.boss.plugins.spi.pages;

import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public abstract class AdminPluginPageProvider extends PluginPageProvider {
	
	protected AdminPluginPageProvider(String pageTemplate) throws PageLoadException {
		super(pageTemplate, AccessLevel.ADMIN);
	}
	
	public AdminPluginPageProvider() throws PageLoadException {
		this(null);
	}
}
