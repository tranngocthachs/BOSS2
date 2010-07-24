package uk.ac.warwick.dcs.boss.plugins.spi.pages;

import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public abstract class MarkerPluginPageProvider extends PluginPageProvider {
	
	protected MarkerPluginPageProvider(String pageTemplate) throws PageLoadException {
		super(pageTemplate, AccessLevel.USER);
	}
	
	public MarkerPluginPageProvider() throws PageLoadException {
		this(null);
	}
}
