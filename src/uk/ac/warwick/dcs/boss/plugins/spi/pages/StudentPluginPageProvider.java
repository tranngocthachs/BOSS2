package uk.ac.warwick.dcs.boss.plugins.spi.pages;

import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

public abstract class StudentPluginPageProvider extends PluginPageProvider {
	
	protected StudentPluginPageProvider(String pageTemplate) throws PageLoadException {
		super(pageTemplate, AccessLevel.USER);
	}
	
	public StudentPluginPageProvider() throws PageLoadException {
		this(null);
	}
}
