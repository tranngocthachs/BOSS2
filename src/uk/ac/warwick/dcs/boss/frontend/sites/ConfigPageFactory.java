package uk.ac.warwick.dcs.boss.frontend.sites;

import uk.ac.warwick.dcs.boss.frontend.Page;
import uk.ac.warwick.dcs.boss.frontend.PageFactory;
import uk.ac.warwick.dcs.boss.frontend.PageLoadException;
import uk.ac.warwick.dcs.boss.frontend.sites.configpages.ConfigPage;
import uk.ac.warwick.dcs.boss.frontend.sites.configpages.PerformConfigPage;

public class ConfigPageFactory extends PageFactory {

	public static String SITE_NAME = SystemPageFactory.SITE_NAME;
	
	public static String HOME_PAGE = "home";
	public static String CONFIG_PAGE = "config";
	public static String PERFORM_CONFIG_PAGE = "perform_config";
	
	@Override
	protected Page getPage(String pageName) throws PageLoadException {
		if (pageName.equals(HOME_PAGE) || pageName.equals(CONFIG_PAGE)){
			return new ConfigPage();
		} else if (pageName.equals(PERFORM_CONFIG_PAGE)) {
			return new PerformConfigPage();
		} else {
			throw new PageLoadException(404, "Unknown page identifier");
		}
	}

}
