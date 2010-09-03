package boss.plugins.spi.pages;

import boss.plugins.IPluginPage;
import uk.ac.warwick.dcs.boss.frontend.Page.AccessLevel;

/**
 * Plugin should extend this class to provide a page for admin.
 * 
 * @author tranngocthachs
 * 
 */
public abstract class IAdminPluginPage extends IPluginPage {

	@Override
	public AccessLevel getAccessLevel() {
		return AccessLevel.ADMIN;
	}
}
