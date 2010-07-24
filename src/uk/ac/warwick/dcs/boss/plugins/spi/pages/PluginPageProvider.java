package uk.ac.warwick.dcs.boss.plugins.spi.pages;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;

abstract class PluginPageProvider extends Page {

	public PluginPageProvider(String pageTemplate, AccessLevel accessLevel)
			throws PageLoadException {
		super(pageTemplate, accessLevel);
	}

	public abstract String getName();
}
