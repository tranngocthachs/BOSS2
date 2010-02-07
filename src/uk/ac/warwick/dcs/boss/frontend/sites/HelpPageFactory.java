package uk.ac.warwick.dcs.boss.frontend.sites;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageFactory;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.helppages.HelpPage;

public class HelpPageFactory extends PageFactory {

	public static String SITE_NAME = "help";
		
	@Override
	protected Page getPage(String pageName) throws PageLoadException {
		return new HelpPage(pageName);
	}

}
